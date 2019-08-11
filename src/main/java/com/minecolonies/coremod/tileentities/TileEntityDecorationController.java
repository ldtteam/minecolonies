package com.minecolonies.coremod.tileentities;

import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
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
    private Direction basicFacing = Direction.NORTH;

    public TileEntityDecorationController()
    {
        super(MinecoloniesTileEntities.DECO_CONTROLLER);
    }

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
    public void setBasicFacing(final Direction basicFacing)
    {
        this.basicFacing = basicFacing;
    }

    /**
     * Get the basic facing of the block.
     * @return the basic facing.
     */
    public Direction getBasicFacing()
    {
        return basicFacing;
    }

    /**
     * Trigger update action.
     */
    private void update()
    {
        this.markDirty();
        final BlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state, 0x03);
    }

    @Override
    public void read(final CompoundNBT compound)
    {
        super.read(compound);
        this.schematicName = compound.getString(TAG_NAME);
        this.level = compound.getInt(TAG_LEVEL);
        this.basicFacing = Direction.byHorizontalIndex(compound.getInt(TAG_FACING));
    }

    @NotNull
    @Override
    public CompoundNBT write(final CompoundNBT compound)
    {
        super.write(compound);
        compound.putString(TAG_NAME, schematicName);
        compound.putInt(TAG_LEVEL, level);
        compound.putInt(TAG_FACING, basicFacing.getHorizontalIndex());
        return compound;
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        return new SUpdateTileEntityPacket(this.pos, 0x9, this.getUpdateTag());
    }

    @NotNull
    @Override
    public CompoundNBT getUpdateTag()
    {
        return this.write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(final NetworkManager net, final SUpdateTileEntityPacket packet)
    {
        final CompoundNBT compound = packet.getNbtCompound();
        this.read(compound);
    }
}
