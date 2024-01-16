package com.minecolonies.core.tileentities;

import com.ldtteam.structurize.util.BlockUtils;
import com.minecolonies.api.tileentities.ITickable;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.minecolonies.api.util.WorldUtil;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.Constants.UPDATE_FLAG;

/**
 * The composted dirty tileEntity to grow all kinds of flowers.
 */
public class TileEntityCompostedDirt extends BlockEntity implements ITickable
{
    /**
     * If currently composted.
     */
    private boolean composted = false;

    /**
     * The current tick timer.
     */
    private int ticker = 0;

    /**
     * Chance to grow something (per second).
     */
    private double percentage = 1.0D;

    /**
     * Max tick limit.
     */
    private final static int TICKER_LIMIT = 300;

    /**
     * Random tick.
     */
    private final Random random = new Random();

    /**
     * The flower to grow.
     */
    private ItemStack flower;

    /**
     * Constructor to create an instance of this tileEntity.
     */
    public TileEntityCompostedDirt(final BlockPos pos, final BlockState state)
    {
        super(MinecoloniesTileEntities.COMPOSTED_DIRT.get(), pos, state);
    }

    @Override
    public void tick()
    {
        final Level world = this.getLevel();
        if (!world.isClientSide && this.composted && ticker % TICKS_SECOND == 0)
        {
            this.updateTick(world);
        }
        ticker++;
    }

    /**
     * Update tick running on the server world.
     *
     * @param worldIn the server world.
     */
    private void updateTick(@NotNull final Level worldIn)
    {
        if (flower == null || flower.isEmpty())
        {
            this.composted = false;
            return;
        }

        if (this.composted)
        {
            ((ServerLevel) worldIn).sendParticles(
              ParticleTypes.HAPPY_VILLAGER, this.getBlockPos().getX() + 0.5,
              this.getBlockPos().getY() + 1, this.getBlockPos().getZ() + 0.5,
              1, 0.2, 0, 0.2, 0);
        }

        if (random.nextDouble() * 100 <= this.percentage)
        {
            final BlockPos position = worldPosition.above();
            if (worldIn.getBlockState(position).getBlock() instanceof AirBlock)
            {
                if (flower.getItem() instanceof BlockItem)
                {
                    if (((BlockItem) flower.getItem()).getBlock() instanceof DoublePlantBlock)
                    {
                        ((DoublePlantBlock) ((BlockItem) flower.getItem()).getBlock()).placeAt(worldIn, ((BlockItem) flower.getItem()).getBlock().defaultBlockState(), position, UPDATE_FLAG);
                    }
                    else
                    {
                        worldIn.setBlockAndUpdate(position, ((BlockItem) flower.getItem()).getBlock().defaultBlockState());
                    }
                }
                else
                {
                    worldIn.setBlockAndUpdate(position, BlockUtils.getBlockStateFromStack(flower));
                }
            }
        }

        if (this.ticker >= TICKER_LIMIT * TICKS_SECOND)
        {
            this.ticker = 0;
            this.composted = false;
        }
    }

    @Override
    public void setChanged()
    {
        if (level != null)
        {
            WorldUtil.markChunkDirty(level, worldPosition);
        }
    }

    /**
     * Method for the composter to call to start producing flowers.
     *
     * @param percentage the chance for this block to appear per second.
     * @param flower     the flower to grow.
     */
    public void compost(final double percentage, @NotNull final ItemStack flower)
    {
        if (percentage >= 0 && percentage <= 100)
        {
            this.percentage = percentage;
            try
            {
                this.flower = flower;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        this.composted = true;
    }

    /**
     * Check if the current compost tile entity is running.
     *
     * @return true if so.
     */
    public boolean isComposted()
    {
        return this.composted;
    }
}
