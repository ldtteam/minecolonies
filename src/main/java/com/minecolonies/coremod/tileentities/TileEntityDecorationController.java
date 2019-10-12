package com.minecolonies.coremod.tileentities;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

public class TileEntityDecorationController extends TileEntity
{
    /**
     * Tag to store the basic facing to NBT
     */
    private static final String TAG_FACING = "facing";

    /**
     * The schematic name of the placerholder block.
     */
    private String schematicName        = "";

    /**
     * The current level.
     */
    private int level = 0;

    /**
     * The basic direction this block is facing.
     */
    private EnumFacing basicFacing = EnumFacing.NORTH;

    /**
     * Geter for the name stored in this.
     * @return String name.
     */
    public String getSchematicName()
    {
        return schematicName;
    }

    /**
     * Setter for the schematic name connected to this.
     * @param schematicName the name to set.
     */
    public void setSchematicName(final String schematicName)
    {
        this.schematicName = schematicName;
        this.update();
    }

    /**
     * Getter for the deco level associated.
     * @return the level.
     */
    public int getLevel()
    {
        return level;
    }

    /**
     * Set the deco level.
     * @param level the max.
     */
    public void setLevel(final int level)
    {
        this.level = level;
        this.update();
    }

    /**
     * Set the basic facing of this block.
     * @param basicFacing the basic facing.
     */
    public void setBasicFacing(final EnumFacing basicFacing)
    {
        this.basicFacing = basicFacing;
    }

    /**
     * Get the basic facing of the block.
     * @return the basic facing.
     */
    public EnumFacing getBasicFacing()
    {
        return basicFacing;
    }

    /**
     * Trigger update action.
     */
    private void update()
    {
        this.markDirty();
        final IBlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state, 0x03);
    }

    @Override
    public void readFromNBT(final NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        this.schematicName = compound.getString(TAG_NAME);
        this.level = compound.getInteger(TAG_LEVEL);
        this.basicFacing = EnumFacing.byHorizontalIndex(compound.getInteger(TAG_FACING));
    }

    @NotNull
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setString(TAG_NAME, schematicName);
        compound.setInteger(TAG_LEVEL, level);
        compound.setInteger(TAG_FACING, basicFacing.getHorizontalIndex());
        return compound;
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        return new SPacketUpdateTileEntity(this.pos, 0x9, this.getUpdateTag());
    }

    @NotNull
    @Override
    public NBTTagCompound getUpdateTag()
    {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(final NetworkManager net, final SPacketUpdateTileEntity packet)
    {
        final NBTTagCompound compound = packet.getNbtCompound();
        this.readFromNBT(compound);
    }
}
