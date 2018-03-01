package com.minecolonies.coremod.tileentities;

import com.google.common.primitives.Ints;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.coremod.util.SoundUtils.*;
import static net.minecraft.util.EnumFacing.*;

/**
 * This Class is about the MultiBlock TileEntity which takes care of pushing others around (In a non mean way).
 */
public class TileEntityMultiBlock extends TileEntity implements ITickable
{
    /**
     * Max block range.
     */
    private static final int MAX_RANGE = 10;

    /**
     * Max block speed.
     */
    private static final int MAX_SPEED = 3;

    /**
     * Min block speed.
     */
    private static final int MIN_SPEED = 1;

    /**
     * Default gate and bridge range.
     */
    public static final int DEFAULT_RANGE         = 3;

    /**
     * Default gate and bridge range.
     */
    public static final int DEFAULT_SPEED        = 2;

    /**
     * The last redstone state which got in.
     */
    private boolean on = false;

    /**
     * The direction it should push or pull rom.
     */
    private EnumFacing direction = UP;

    /**
     * The output direction.
     */
    private EnumFacing output = DOWN;

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
     * Speed of the multiblock, max 3, min 1.
     */
    private int speed = 2;

    /**
     * Handle redstone input.
     *
     * @param signal true if positive.
     */
    public void handleRedstone(final boolean signal)
    {
        if(speed == 0)
        {
            speed = DEFAULT_SPEED;
        }

        if (signal != on && progress == range)
        {
            on = signal;
            if (signal)
            {
                currentDirection = output;
            }
            else
            {
                currentDirection = direction;
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
            if (ticksPassed % ( TICKS_SECOND / speed) == 0)
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
        final EnumFacing currentOutPutDirection = currentDirection == direction ? output : direction;

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

                final BlockPos posToGo = blockToGoTo > 0 ? pos.offset(currentDirection, blockToGoTo) : pos.offset(currentOutPutDirection, Math.abs(blockToGoTo));
                final BlockPos posToGoFrom = blockToGoFrom > 0 ? pos.offset(currentDirection, blockToGoFrom) : pos.offset(currentOutPutDirection, Math.abs(blockToGoFrom));
                if (world.isAirBlock(posToGo))
                {
                    final IBlockState tempState = world.getBlockState(posToGoFrom);
                    if (blockToMove.getBlock() == tempState.getBlock() && world.isBlockLoaded(posToGoFrom) && world.isBlockLoaded(posToGo))
                    {
                        pushEntitiesIfNecessary(posToGo, pos);
                        world.setBlockState(posToGo, tempState);
                        world.setBlockToAir(posToGoFrom);
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

    private void pushEntitiesIfNecessary(final BlockPos posToGo, final BlockPos pos)
    {
        final List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(posToGo));
        final BlockPos vector = posToGo.subtract(pos);
        final BlockPos posTo = posToGo.offset(getFacingFromVector(vector.getX(), vector.getY(), vector.getZ()));
        for(final Entity entity : entities)
        {
            entity.setPositionAndUpdate(posTo.getX() + HALF_BLOCK, posTo.getY() + HALF_BLOCK, posTo.getZ() + HALF_BLOCK);
        }
    }

    @Override
    public void rotate(final Rotation rotationIn)
    {
        if(output != UP && output != DOWN)
        {
            output = rotationIn.rotate(output);
        }

        if(direction != UP && direction != DOWN)
        {
            direction = rotationIn.rotate(direction);
        }
        super.rotate(rotationIn);
    }

    @Override
    public void mirror(final Mirror mirrorIn)
    {
        if(output != UP && output != DOWN)
        {
            output = mirrorIn.mirror(output);
        }

        if(direction != UP && direction != DOWN)
        {
            direction = mirrorIn.mirror(direction);
        }

        super.mirror(mirrorIn);
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
     * Get the output direction the block is facing.
     *
     * @return the EnumFacing.
     */
    public EnumFacing getOutput()
    {
        return output;
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
     * Set the direction it should output to.
     *
     * @param output the direction.
     */
    public void setOutput(final EnumFacing output)
    {
        this.output = output;
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

    /**
     * Get the speed of the block.
     * @return the speed (min 1 max 3).
     */
    public int getSpeed()
    {
        return speed;
    }

    /**
     * Setter for speed.
     * @param speed the speed to set.
     */
    public void setSpeed(final int speed)
    {
        this.speed = Ints.constrainToRange(speed, MIN_SPEED, MAX_SPEED);
    }

    @Override
    public void readFromNBT(final NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        range = compound.getInteger(TAG_RANGE);
        this.progress = compound.getInteger(TAG_PROGRESS);
        direction = values()[compound.getInteger(TAG_DIRECTION)];
        on = compound.getBoolean(TAG_INPUT);
        if(compound.hasKey(TAG_OUTPUT_DIRECTION))
        {
            output = values()[compound.getInteger(TAG_OUTPUT_DIRECTION)];
        }
        else
        {
            output = direction.getOpposite();
        }
        speed = compound.getInteger(TAG_SPEED);
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setInteger(TAG_RANGE, range);
        compound.setInteger(TAG_PROGRESS, progress);
        compound.setInteger(TAG_DIRECTION, direction.ordinal());
        compound.setBoolean(TAG_INPUT, on);
        if(output != null)
        {
            compound.setInteger(TAG_OUTPUT_DIRECTION, output.ordinal());
        }
        compound.setInteger(TAG_SPEED, speed);
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
