/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics;

import cpw.mods.fml.common.Loader;

public class RoboticsProxyClient extends RoboticsProxy {
	public void registerRenderers() {

		// TODO: Move robot station textures locally
		if (Loader.isModLoaded("BuildCraft|Transport")) {
			loadBCTransport();
		}
	}

	private void loadBCTransport() {
	}
}
