/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft;

import buildcraft.factory.*;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.stats.Achievement;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;

import buildcraft.api.blueprints.BuilderAPI;
import buildcraft.core.BCRegistry;
import buildcraft.core.CompatHooks;
import buildcraft.core.DefaultProps;
import buildcraft.core.InterModComms;
import buildcraft.core.Version;
import buildcraft.core.builders.schematics.SchematicFree;
import buildcraft.core.config.ConfigManager;
import buildcraft.core.lib.network.ChannelHandler;
import buildcraft.core.lib.network.PacketHandler;
import buildcraft.factory.schematics.SchematicTileIgnoreState;

@Mod(name = "BuildCraft Factory", version = Version.VERSION, useMetadata = false, modid = "BuildCraft|Factory", dependencies = DefaultProps.DEPENDENCY_CORE)
public class BuildCraftFactory extends BuildCraftMod {

	@Mod.Instance("BuildCraft|Factory")
	public static BuildCraftFactory instance;
	public static BlockFloodGate floodGateBlock;
	public static BlockPlainPipe plainPipeBlock;

	public static Achievement aLotOfCraftingAchievement;

	public static int miningDepth = 256;
	public static boolean pumpsNeedRealPower = false;
	public static PumpDimensionList pumpDimensionList;

	@Mod.EventHandler
	public void load(FMLInitializationEvent evt) {
		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new FactoryGuiHandler());
		BCRegistry.INSTANCE.registerTileEntity(TileFloodGate.class, "net.minecraft.src.buildcraft.factory.TileFloodGate");
		FactoryProxy.proxy.initializeTileEntities();

		BuilderAPI.schematicRegistry.registerSchematicBlock(plainPipeBlock, SchematicFree.class);
		BuilderAPI.schematicRegistry.registerSchematicBlock(floodGateBlock, SchematicTileIgnoreState.class);

	}

	@Mod.EventHandler
	public void initialize(FMLPreInitializationEvent evt) {
		channels = NetworkRegistry.INSTANCE.newChannel
				(DefaultProps.NET_CHANNEL_NAME + "-FACTORY", new ChannelHandler(), new PacketHandler());

		String plc = "Allows admins to whitelist or blacklist pumping of specific fluids in specific dimensions.\n"
				+ "Eg. \"-/-1/Lava\" will disable lava in the nether. \"-/*/Lava\" will disable lava in any dimension. \"+/0/*\" will enable any fluid in the overworld.\n"
				+ "Entries are comma seperated, banned fluids have precedence over allowed ones."
				+ "Default is \"+/*/*,+/-1/Lava\" - the second redundant entry (\"+/-1/lava\") is there to show the format.";

		BuildCraftCore.mainConfigManager.register("general.miningDepth", 256, "Should the mining well only be usable once after placing?", ConfigManager.RestartRequirement.NONE);

		BuildCraftCore.mainConfigManager.get("general.miningDepth").setMinValue(2).setMaxValue(256);
		BuildCraftCore.mainConfigManager.register("general.pumpDimensionControl", DefaultProps.PUMP_DIMENSION_LIST, plc, ConfigManager.RestartRequirement.NONE);
		BuildCraftCore.mainConfigManager.register("general.pumpsNeedRealPower", false, "Do pumps need real (non-redstone) power?", ConfigManager.RestartRequirement.WORLD);

		floodGateBlock = (BlockFloodGate) CompatHooks.INSTANCE.getBlock(BlockFloodGate.class);
		BCRegistry.INSTANCE.registerBlock(floodGateBlock.setBlockName("floodGateBlock"), false);

		reloadConfig(ConfigManager.RestartRequirement.GAME);

		FactoryProxy.proxy.initializeEntityRenders();

		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
	}

	public void reloadConfig(ConfigManager.RestartRequirement restartType) {
		if (restartType == ConfigManager.RestartRequirement.GAME) {
			reloadConfig(ConfigManager.RestartRequirement.WORLD);
		} else if (restartType == ConfigManager.RestartRequirement.WORLD) {
			reloadConfig(ConfigManager.RestartRequirement.NONE);
		} else {
			miningDepth = BuildCraftCore.mainConfigManager.get("general.miningDepth").getInt();
			pumpsNeedRealPower = BuildCraftCore.mainConfigManager.get("general.pumpsNeedRealPower").getBoolean();
			pumpDimensionList = new PumpDimensionList(BuildCraftCore.mainConfigManager.get("general.pumpDimensionControl").getString());

			if (BuildCraftCore.mainConfiguration.hasChanged()) {
				BuildCraftCore.mainConfiguration.save();
			}
		}
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		if ("BuildCraft|Core".equals(event.modID)) {
			reloadConfig(event.isWorldRunning ? ConfigManager.RestartRequirement.NONE : ConfigManager.RestartRequirement.WORLD);
		}
	}

	@Mod.EventHandler
	public void processIMCRequests(FMLInterModComms.IMCEvent event) {
		InterModComms.processIMC(event);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void loadTextures(TextureStitchEvent.Pre evt) {
		if (evt.map.getTextureType() == 0) {
			TextureMap terrainTextures = evt.map;
			FactoryProxyClient.pumpTexture = terrainTextures.registerIcon("buildcraftfactory:pumpBlock/tube");
		}
	}

	@Mod.EventHandler
	public void whiteListAppliedEnergetics(FMLInitializationEvent event) {
		FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial", TileFloodGate.class.getCanonicalName());
		FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial", TileTank.class.getCanonicalName());
		FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial", TileRefinery.class.getCanonicalName());
		FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial", TileHopper.class.getCanonicalName());

	}

	@Mod.EventHandler
	public void remap(FMLMissingMappingsEvent event) {
		for (FMLMissingMappingsEvent.MissingMapping mapping : event.get()) {
			if (mapping.name.equals("BuildCraft|Factory:machineBlock")) {
				if (Loader.isModLoaded("BuildCraft|Builders")) {
					if (mapping.type == GameRegistry.Type.BLOCK) {
						mapping.remap(Block.getBlockFromName("BuildCraft|Builders:machineBlock"));
					} else if (mapping.type == GameRegistry.Type.ITEM) {
						mapping.remap(Item.getItemFromBlock(Block.getBlockFromName("BuildCraft|Builders:machineBlock")));
					}
				} else {
					mapping.warn();
				}
			} else if (mapping.name.equals("BuildCraft|Factory:frameBlock")) {
				if (Loader.isModLoaded("BuildCraft|Builders")) {
					if (mapping.type == GameRegistry.Type.BLOCK) {
						mapping.remap(Block.getBlockFromName("BuildCraft|Builders:frameBlock"));
					} else if (mapping.type == GameRegistry.Type.ITEM) {
						mapping.remap(Item.getItemFromBlock(Block.getBlockFromName("BuildCraft|Builders:frameBlock")));
					}
				} else {
					mapping.ignore();
				}
			}
		}
	}
}
