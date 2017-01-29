package com.minecolonies.coremod.entity.ai.basic;

import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.configuration.Configurations;
import com.minecolonies.coremod.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * This is the base class of all worker AIs.
 * Every AI implements this class with it's job type.
 * There are some utilities within the class:
 * - The AI will clear a full inventory at the building chest.
 * - The AI will animate mining a block (with delay)
 * - The AI will request items and tools automatically
 * (and collect them from the building chest)
 *
 * @param <J> the job type this AI has to do.
 */
public abstract class AbstractEntityAIInteract<J extends AbstractJob> extends AbstractEntityAICrafting<J>
{
    /**
     * The amount of xp the entity gains per block mined.
     */
    private static final double XP_PER_BLOCK         = 0.05D;
    /**
     * The percentage of time needed if we are one level higher.
     */
    private static final double LEVEL_MODIFIER       = 0.85D;
    /**
     * The Multiplier to add to hand mining speed.
     */
    private static final int    HAND_MINING_MODIFIER = 10;
    /**
     * The minimum range the builder has to reach in order to construct or clear.
     */
    private static final int    MIN_WORKING_RANGE             = 12;

    /**
     * Creates the abstract part of the AI.
     * Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public AbstractEntityAIInteract(@NotNull final J job)
    {
        super(job);
        super.registerTargets(
          //no new targets for now
        );
    }

    /**
     * Will simulate mining a block with particles ItemDrop etc.
     * Attention:
     * Because it simulates delay, it has to be called 2 times.
     * So make sure the code path up to this function is reachable a second time.
     * And make sure to immediately exit the update function when this returns false.
     *
     * @param blockToMine the block that should be mined
     * @return true once we're done
     */
    protected final boolean mineBlock(@NotNull final BlockPos blockToMine)
    {
        return mineBlock(blockToMine, new BlockPos((int) worker.posX, (int) worker.posY, (int) worker.posZ));
    }

    /**
     * Will simulate mining a block with particles ItemDrop etc.
     * Attention:
     * Because it simulates delay, it has to be called 2 times.
     * So make sure the code path up to this function is reachable a second time.
     * And make sure to immediately exit the update function when this returns false.
     *
     * @param blockToMine the block that should be mined
     * @param safeStand   the block we want to stand on to do that
     * @return true once we're done
     */
    protected final boolean mineBlock(@NotNull final BlockPos blockToMine, @NotNull final BlockPos safeStand)
    {
        final IBlockState curBlockState = world.getBlockState(blockToMine);
        @Nullable final Block curBlock = curBlockState.getBlock();
        if (curBlock == null
              || curBlock.equals(Blocks.AIR)
              || BlockUtils.shouldNeverBeMessedWith(curBlock))
        {
            if (curBlock != null
                  && curBlockState.getMaterial().isLiquid())
            {
                world.setBlockToAir(blockToMine);
            }
            //no need to mine block...
            return true;
        }

        if (checkMiningLocation(blockToMine, safeStand))
        {
            //we have to wait for delay
            return false;
        }

        final ItemStack tool = worker.getHeldItemMainhand();

        //calculate fortune enchantment
        final int fortune = Utils.getFortuneOf(tool);

        //get all item drops
        final List<ItemStack> items = BlockPosUtil.getBlockDrops(world, blockToMine, fortune);

        //Break the block
        worker.breakBlockWithToolInHand(blockToMine);

        //add the drops to the citizen
        for (final ItemStack item : items)
        {
            InventoryUtils.setStack(worker.getInventoryCitizen(), item);
        }

        worker.addExperience(XP_PER_BLOCK);
        this.incrementActionsDone();
        return true;
    }

    /**
     * Checks for the right tools and waits for an appropriate delay.
     *
     * @param blockToMine the block to mine eventually
     * @param safeStand   a safe stand to mine from (AIR Block!)
     */
    private boolean checkMiningLocation(@NotNull final BlockPos blockToMine, @NotNull final BlockPos safeStand)
    {
        final Block curBlock = world.getBlockState(blockToMine).getBlock();

        if (!holdEfficientTool(curBlock))
        {
            //We are missing a tool to harvest this block...
            return true;
        }

        final ItemStack tool = worker.getHeldItemMainhand();

        if (tool != null && !ForgeHooks.canToolHarvestBlock(world, blockToMine, tool) && curBlock != Blocks.BEDROCK)
        {
            Log.getLogger().info(String.format(
              "ForgeHook not in sync with EfficientTool for %s and %s\n"
                + "Please report to MineColonies with this text to add support!",
              curBlock, tool
            ));
        }

        if (walkToBlock(safeStand) && MathUtils.twoDimDistance(worker.getPosition(), safeStand) > MIN_WORKING_RANGE)
        {
            return true;
        }
        currentWorkingLocation = blockToMine;
        currentStandingLocation = safeStand;


        return hasNotDelayed(getBlockMiningDelay(curBlock, blockToMine));
    }

    /**
     * Calculate how long it takes to mine this block.
     *
     * @param block the block type
     * @param pos   coordinate
     * @return the delay in ticks
     */
    private int getBlockMiningDelay(@NotNull final Block block, @NotNull final BlockPos pos)
    {
        if (worker.getHeldItemMainhand() == null)
        {
            return (int) world.getBlockState(pos).getBlockHardness(world, pos);
        }

        return (int) ((Configurations.blockMiningDelayModifier
                         * Math.pow(LEVEL_MODIFIER, worker.getLevel()))
                        * (double) world.getBlockState(pos).getBlockHardness(world, pos)
                        / (double) (worker.getHeldItemMainhand().getItem()
                                      .getStrVsBlock(worker.getHeldItemMainhand(),
                                        block.getDefaultState())));
    }
}
