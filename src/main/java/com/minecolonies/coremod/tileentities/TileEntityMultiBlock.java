package com.minecolonies.coremod.tileentities;

import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.BlockPistonStructureHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.coremod.util.SoundUtils.PITCH;
import static com.minecolonies.coremod.util.SoundUtils.VOLUME;

public class TileEntityMultiBlock extends TileEntity implements ITickable
{
    /**
     * Max block range.
     */
    public static final int MAX_RANGE = 10;

    /**
     * Default gate and bridge range.
     */
    public static final int DEFAULT_RANGE = 3;

    /**
     * The last redstone state which got in.
     */
    private boolean on = false;

    /**
     * The direction it should push or pull rom.
     */
    private EnumFacing direction = EnumFacing.UP;

    /**
     * The range it should pull to.
     */
    private int range = DEFAULT_RANGE;

    /**
     * The direction it is going to.
     */
    private EnumFacing currentDirection;

    /**
     * The progress it has made.
     */
    private int progress = 0;

    /**
     * Amount of ticks passed.
     */
    private int ticksPassed = 0;

    /**
     * Public constructor to create the tileEntity.
     */
    public TileEntityMultiBlock()
    {
        super();
    }

    /**
     * Handle redstone input.
     *
     * @param signal true if positive.
     */
    public void handleRedstone(final boolean signal)
    {
        if (signal != on && progress == range)
        {
            on = signal;
            if (signal)
            {
                currentDirection = direction;
            }
            else
            {
                currentDirection = direction.getOpposite();
            }
            progress = 0;
        }
    }


    @Override
    public void update()
    {
        if(world == null || world.isRemote)
        {
            return;
        }
        if (currentDirection == null && progress < range)
        {
            progress = range;
        }

        if(progress < range)
        {
            if (ticksPassed % TICKS_SECOND == 0)
            {
                handleTick();
                ticksPassed = 1;
            }
            ticksPassed++;
        }
    }

    /**
     * Handle the tick, to finish the sliding.
     */
    public void handleTick()
    {
        if(progress < range)
        {
            final IBlockState blockToMove = world.getBlockState(pos.offset(currentDirection, 1));
            if (blockToMove.getBlock() == Blocks.AIR
                    || blockToMove.getMobilityFlag() == EnumPushReaction.IGNORE
                    || blockToMove.getMobilityFlag() == EnumPushReaction.DESTROY
                    || blockToMove.getMobilityFlag() == EnumPushReaction.BLOCK
                    || blockToMove.getBlock().hasTileEntity(blockToMove)
                    || blockToMove.getBlock() == Blocks.BEDROCK)
            {
                progress++;
                return;
            }

            for (int i = 0; i < Math.min(range, MAX_RANGE); i++)
            {
                final int blockToGoTo = i - 1 - progress + (i - 1 - progress >= 0 ? 1 : 0);
                final int blockToGoFrom = i + 1 - progress - (i + 1 - progress <= 0 ? 1 : 0);

                if (world.isAirBlock(pos.offset(currentDirection, blockToGoTo)))
                {
                    final IBlockState tempState = world.getBlockState(pos.offset(currentDirection, blockToGoFrom));
                    if (blockToMove.getBlock() == tempState.getBlock())
                    {
                        world.setBlockState(pos.offset(currentDirection, blockToGoTo), tempState);
                        world.setBlockToAir(pos.offset(currentDirection, blockToGoFrom));
                    }
                }
            }
            world.playSound((EntityPlayer) null,
                    pos,
                    SoundEvents.BLOCK_PISTON_EXTEND,
                    SoundCategory.BLOCKS,
                    (float) VOLUME,
                    (float) PITCH);
            progress++;
        }
    }

    /**
     * Check if the redstone is on.
     *
     * @return true if so.
     */
    public boolean isOn()
    {
        return on;
    }

    /**
     * Get the direction the block is facing.
     *
     * @return the EnumFacing.
     */
    public EnumFacing getDirection()
    {
        return direction;
    }

    /**
     * Set the direction it should be facing.
     *
     * @param direction the direction.
     */
    public void setDirection(final EnumFacing direction)
    {
        this.direction = direction;
    }

    /**
     * Get the range of blocks it should push.
     *
     * @return the range.
     */
    public int getRange()
    {
        return range;
    }

    /**
     * Set the range it should push.
     *
     * @param range the range.
     */
    public void setRange(final int range)
    {
        this.range = Math.min(range, MAX_RANGE);
        this.progress = range;
    }

    @Override
    public void readFromNBT(final NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        range = compound.getInteger(TAG_RANGE);
        this.progress = compound.getInteger(TAG_PROGRESS);
        direction = EnumFacing.values()[compound.getInteger(TAG_DIRECTION)];
        on = compound.getBoolean(TAG_INPUT);
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setInteger(TAG_RANGE, range);
        compound.setInteger(TAG_PROGRESS, progress);
        compound.setInteger(TAG_DIRECTION, direction.ordinal());
        compound.setBoolean(TAG_INPUT, on);
        return compound;
    }

    @Override
    protected void setWorldCreate(final World world)
    {
        this.setWorld(world);
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        final NBTTagCompound compound = new NBTTagCompound();
        this.writeToNBT(compound);
        return new SPacketUpdateTileEntity(this.pos, 0, compound);
    }

    @NotNull
    @Override
    public NBTTagCompound getUpdateTag()
    {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(final NetworkManager net, final SPacketUpdateTileEntity packet)
    {
        final NBTTagCompound compound = packet.getNbtCompound();
        this.readFromNBT(compound);
    }
}
