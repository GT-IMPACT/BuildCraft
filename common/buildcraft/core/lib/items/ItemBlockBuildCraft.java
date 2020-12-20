/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.items;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import static buildcraft.BuildCraftBuilders.fillerBlock;


import java.util.List;

public class ItemBlockBuildCraft extends ItemBlock {

	Block mBlock;

	public ItemBlockBuildCraft(Block b) {
		super(b);
		mBlock = b;
	}

	@Override
	public int getMetadata(int i) {
		return i;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addInformation(ItemStack par1, EntityPlayer player, List aList, boolean b) {
		super.addInformation(par1, player, aList, b);
		if (mBlock==fillerBlock) {
			aList.add("Max range 128 Blocks");
		}
	}
}
