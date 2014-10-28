/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.statements;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.core.NetworkData;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.utils.StringUtils;

public class StatementParameterDirection implements IStatementParameter {

    	@NetworkData
	public ForgeDirection direction = ForgeDirection.UNKNOWN;

    private IIcon[] icons;
    
	public StatementParameterDirection() {
		
	}

	@Override
	public ItemStack getItemStack() {
		return null;
	}

	@Override
	public IIcon getIcon() {
	    if (direction == ForgeDirection.UNKNOWN) {
	    	return null;
	    } else {
	    	return icons[direction.ordinal()];
	    }
	}

	@Override
	public void onClick(Object source, IStatement stmt, ItemStack stack, int mouseButton) {
		if (source instanceof IPipe) {
			do {
				direction = ForgeDirection.getOrientation((direction.ordinal() + (mouseButton > 0 ? -1 : 1)) % 6);
			} while (!((IPipe) source).getTile().isPipeConnected(direction));
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
	    nbt.setByte("direction", (byte) direction.ordinal());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
	    if (nbt.hasKey("direction")) {
	    	direction = ForgeDirection.getOrientation(nbt.getByte("direction"));
	    } else {
	    	direction = ForgeDirection.UNKNOWN;
	    }
	}

	@Override
	public boolean equals(Object object) {
	    if (object instanceof StatementParameterDirection) {
	    	StatementParameterDirection param = (StatementParameterDirection) object;
	    	return param.direction == this.direction;
	    }
	    return false;
	}

	@Override
	public String getDescription() {
		if (direction == ForgeDirection.UNKNOWN) {
			return "";
		} else {
			return StringUtils.localize("direction." + direction.name().toLowerCase());
		}
	}

	@Override
	public String getUniqueTag() {
		return "buildcraft:pipeActionDirection";
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {
		icons = new IIcon[] {
				iconRegister.registerIcon("buildcraft:triggers/trigger_dir_down"),
				iconRegister.registerIcon("buildcraft:triggers/trigger_dir_up"),
				iconRegister.registerIcon("buildcraft:triggers/trigger_dir_north"),
				iconRegister.registerIcon("buildcraft:triggers/trigger_dir_south"),
				iconRegister.registerIcon("buildcraft:triggers/trigger_dir_west"),
				iconRegister.registerIcon("buildcraft:triggers/trigger_dir_east")
		};
	}

	@Override
	public IStatementParameter rotateLeft() {
		StatementParameterDirection d = new StatementParameterDirection();
		d.direction = direction.getRotation(ForgeDirection.UP);
		return d;
	}
}