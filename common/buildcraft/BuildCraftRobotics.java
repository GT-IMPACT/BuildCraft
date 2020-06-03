/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft;

import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.lists.ListRegistry;
import buildcraft.api.robots.RobotManager;
import buildcraft.api.transport.PipeManager;
import buildcraft.core.BCRegistry;
import buildcraft.core.DefaultProps;
import buildcraft.core.InterModComms;
import buildcraft.core.Version;
import buildcraft.core.config.ConfigManager;
import buildcraft.core.network.EntityIds;
import buildcraft.robotics.*;
import buildcraft.robotics.ai.AIRobotMain;
import buildcraft.robotics.boards.BoardRobotBomber;
import buildcraft.robotics.boards.BoardRobotBuilder;
import buildcraft.robotics.boards.BoardRobotEmpty;
import buildcraft.robotics.boards.BoardRobotPicker;
import buildcraft.robotics.map.MapManager;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;

@Mod(name = "BuildCraft Robotics", version = Version.VERSION, useMetadata = false, modid = "BuildCraft|Robotics", dependencies = DefaultProps.DEPENDENCY_CORE)
public class BuildCraftRobotics extends BuildCraftMod {
    @Mod.Instance("BuildCraft|Robotics")
    public static BuildCraftRobotics instance;

    public static MapManager manager;
    public static ItemRedstoneBoard redstoneBoard;
    private static Thread managerThread;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent evt) {
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void textureHook(TextureStitchEvent.Pre event) {
        if (event.map.getTextureType() == 1) {
            RedstoneBoardRegistry.instance.registerIcons(event.map);
        }
    }

    private void stopMapManager() {
        if (manager != null) {
            manager.stop();
            MinecraftForge.EVENT_BUS.unregister(manager);
            FMLCommonHandler.instance().bus().unregister(manager);
        }

        if (managerThread != null) {
            managerThread.interrupt();
        }

        managerThread = null;
        manager = null;
    }

    @Mod.EventHandler
    public void serverUnload(FMLServerStoppingEvent event) {
        stopMapManager();
    }

    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        File f = new File(DimensionManager.getCurrentSaveRootDirectory(), "buildcraft/zonemap");

        try {
            f.mkdirs();
        } catch (Exception e) {
            e.printStackTrace();
        }

        stopMapManager();

        manager = new MapManager(f);
        managerThread = new Thread(manager);
        managerThread.start();

        BoardRobotPicker.onServerStart();

        MinecraftForge.EVENT_BUS.register(manager);
        FMLCommonHandler.instance().bus().register(manager);
    }

    @Mod.EventHandler
    public void serverLoadFinish(FMLServerStartedEvent event) {
        manager.initialize();
    }

    @Mod.EventHandler
    public void processRequests(FMLInterModComms.IMCEvent event) {
        InterModComms.processIMC(event);
    }

    public void reloadConfig(ConfigManager.RestartRequirement restartType) {
        if (restartType == ConfigManager.RestartRequirement.GAME) {

            reloadConfig(ConfigManager.RestartRequirement.WORLD);
        } else if (restartType == ConfigManager.RestartRequirement.WORLD) {
            reloadConfig(ConfigManager.RestartRequirement.NONE);
        } else {
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
}
