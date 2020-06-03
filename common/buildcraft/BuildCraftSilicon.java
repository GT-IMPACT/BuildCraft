/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft;

import net.minecraft.stats.Achievement;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;

import buildcraft.core.BCRegistry;
import buildcraft.core.DefaultProps;
import buildcraft.core.InterModComms;
import buildcraft.core.Version;
import buildcraft.core.config.ConfigManager;
import buildcraft.core.lib.network.ChannelHandler;
import buildcraft.core.network.EntityIds;
import buildcraft.silicon.EntityPackage;
import buildcraft.silicon.SiliconGuiHandler;
import buildcraft.silicon.SiliconProxy;
import buildcraft.silicon.TileAdvancedCraftingTable;
import buildcraft.silicon.TileAssemblyTable;
import buildcraft.silicon.TileChargingTable;
import buildcraft.silicon.TileIntegrationTable;
import buildcraft.silicon.TileLaser;
import buildcraft.silicon.TileProgrammingTable;
import buildcraft.silicon.TileStampingTable;
import buildcraft.silicon.network.PacketHandlerSilicon;

@Mod(name = "BuildCraft Silicon", version = Version.VERSION, useMetadata = false, modid = "BuildCraft|Silicon", dependencies = DefaultProps.DEPENDENCY_CORE)
public class BuildCraftSilicon extends BuildCraftMod {
	@Mod.Instance("BuildCraft|Silicon")
	public static BuildCraftSilicon instance;

	public static Achievement timeForSomeLogicAchievement;
	public static Achievement tinglyLaserAchievement;

	public static float chipsetCostMultiplier = 1.0F;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		BuildCraftCore.mainConfigManager.register("power.chipsetCostMultiplier", 1.0D, "The cost multiplier for Chipsets", ConfigManager.RestartRequirement.GAME);
		BuildCraftCore.mainConfiguration.save();
		chipsetCostMultiplier = (float) BuildCraftCore.mainConfigManager.get("power.chipsetCostMultiplier").getDouble();

		if (BuildCraftCore.mainConfiguration.hasChanged()) {
			BuildCraftCore.mainConfiguration.save();
		}

		EntityRegistry.registerModEntity(EntityPackage.class, "bcPackageThrowable", EntityIds.PACKAGE_THROWABLE, instance, 48, 10, true);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent evt) {
		channels = NetworkRegistry.INSTANCE
				.newChannel
						(DefaultProps.NET_CHANNEL_NAME + "-SILICON", new ChannelHandler(), new PacketHandlerSilicon());

		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new SiliconGuiHandler());



		SiliconProxy.proxy.registerRenderers();
	}


	@Mod.EventHandler
	public void processIMCRequests(FMLInterModComms.IMCEvent event) {
		InterModComms.processIMC(event);
	}

	@Mod.EventHandler
	public void whiteListAppliedEnergetics(FMLInitializationEvent event) {
		FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial",
				TileLaser.class.getCanonicalName());
		FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial",
				TileAssemblyTable.class.getCanonicalName());
		FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial",
				TileAdvancedCraftingTable.class.getCanonicalName());
		FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial",
				TileIntegrationTable.class.getCanonicalName());
	}

	@Mod.EventHandler
	public void remap(FMLMissingMappingsEvent event) {
	}
}
