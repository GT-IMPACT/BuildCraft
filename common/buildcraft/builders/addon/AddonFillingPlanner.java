/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.addon;

import java.io.IOException;
import java.util.Arrays;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.core.IBox;
import buildcraft.api.filler.FilledTemplate;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.containers.IFillerStatementContainer;

import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.statement.FullStatement;

import buildcraft.builders.BCBuildersGuis;
import buildcraft.builders.BCBuildersSprites;
import buildcraft.builders.filler.FillerType;
import buildcraft.builders.snapshot.Template;
import buildcraft.core.marker.volume.Addon;
import buildcraft.core.marker.volume.AddonDefaultRenderer;
import buildcraft.core.marker.volume.IFastAddonRenderer;
import buildcraft.core.marker.volume.ISingleAddon;

public class AddonFillingPlanner extends Addon implements ISingleAddon, IFillerStatementContainer {
    public final FullStatement<IFillerPattern> pattern = new FullStatement<>(FillerType.INSTANCE, 4, null);
    public boolean inverted;
    public Template.BuildingInfo buildingInfo;
    private World world;

    public void updateBuildingInfo() {
        IStatementParameter[] params = new IStatementParameter[pattern.maxParams];
        for (int i = 0; i < params.length; i++) {
            params[i] = pattern.get(i);
        }
        FilledTemplate patternTemplate = pattern.get().createTemplate(this, params);
        if (patternTemplate == null) {
            buildingInfo = null;
        } else {
            Template blueprintTemplate = new Template();
            blueprintTemplate.size = patternTemplate.size;
            blueprintTemplate.offset = BlockPos.ORIGIN;
            int sx = patternTemplate.sizeX;
            int sy = patternTemplate.sizeY;
            int sz = patternTemplate.sizeZ;
            blueprintTemplate.data = new boolean[sx][sy][sz];
            for (int x = 0; x < sx; x++) {
                for (int y = 0; y < sy; y++) {
                    for (int z = 0; z < sz; z++) {
                        blueprintTemplate.data[x][y][z] = patternTemplate.get(x, y, z) ^ inverted;
                    }
                }
            }
            buildingInfo = blueprintTemplate.new BuildingInfo(box.box.min(), Rotation.NONE);
        }
    }

    @Override
    public void onBoxSizeChange() {
        updateBuildingInfo();
    }

    @Override
    public IFastAddonRenderer<AddonFillingPlanner> getRenderer() {
        return new AddonDefaultRenderer<AddonFillingPlanner>(BCBuildersSprites.FILLING_PLANNER.getSprite())
                .then(new AddonRendererFillingPlanner());
    }

    @Override
    public void onAdded(World world) {
        this.world = world;
        updateBuildingInfo();
    }

    @Override
    public void onRemoved() {
        this.world = null;
    }

    @Override
    public void onPlayerRightClick(EntityPlayer player) {
        BCBuildersGuis.FILLING_PLANNER.openGUI(player);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setTag("pattern", pattern.writeToNbt());
        nbt.setBoolean("inverted", inverted);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        pattern.readFromNbt(nbt.getCompoundTag("pattern"));
        inverted = nbt.getBoolean("inverted");
        updateBuildingInfo();
    }

    @Override
    public void toBytes(PacketBufferBC buf) {
        pattern.writeToBuffer(buf);
        buf.writeBoolean(inverted);
    }

    @Override
    public void fromBytes(PacketBufferBC buf) throws IOException {
        pattern.readFromBuffer(buf);
        inverted = buf.readBoolean();
        updateBuildingInfo();
    }

    // IFillerStatementContainer

    @Override
    public TileEntity getNeighbourTile(EnumFacing side) {
        return null;
    }

    @Override
    public TileEntity getTile() {
        return null;
    }

    @Override
    public World getFillerWorld() {
        return world;
    }

    @Override
    public boolean hasBox() {
        return true;
    }

    @Override
    public IBox getBox() {
        return box.box;
    }

    @Override
    public void setPattern(IFillerPattern pattern, IStatementParameter[] params) {
        this.pattern.set(pattern);
        params = Arrays.copyOf(params, this.pattern.maxParams);
        for (int i = 0; i < this.pattern.maxParams; i++) {
            this.pattern.set(i, params[i]);
        }
        updateBuildingInfo();
    }
}
