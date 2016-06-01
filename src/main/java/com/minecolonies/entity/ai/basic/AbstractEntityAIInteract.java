package com.minecolonies.entity.ai.basic;

import com.minecolonies.colony.jobs.Job;
import com.minecolonies.util.*;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraftforge.common.ForgeHooks;

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
public abstract class AbstractEntityAIInteract<J extends Job> extends AbstractEntityAICrafting<J>
{
    private static final int    DELAY_MODIFIER = 50;
    /**
     * The amount of xp the entity gains per block mined.
     */
    private static final double XP_PER_BLOCK   = 0.05;
    private              int    blocksMined    = 0;


    /**
     * Creates the abstract part of the AI.
     * Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public AbstractEntityAIInteract(J job)
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
    protected final boolean mineBlock(BlockPos blockToMine)
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
    protected final boolean mineBlock(BlockPos blockToMine, BlockPos safeStand)
    {
        //todo partially replace with methods in EntityCitizen
        Block curBlock = world.getBlockState(blockToMine).getBlock();
        if (curBlock == null || curBlock.equals(Blocks.air))
        {
            //no need to mine block...
            return true;
        }

        if (BlockUtils.shouldNeverBeMessedWith(curBlock))
        {
            Log.logger.warn("Trying to mine block " + curBlock + " which is not allowed!");
            //This will endlessly loop... If this warning comes up, check your blocks first...
            return false;
        }

        if (checkMiningLocation(blockToMine, safeStand))
        {
            //we have to wait for delay
            return false;
        }

        ItemStack tool = worker.getHeldItem();

        //calculate fortune enchantment
        int fortune = Utils.getFortuneOf(tool);

        if (tool != null)
        {
            //Reduce durability if not using hand
            //todo: maybe merge with worker methods, have to find a clean way for this later
            worker.damageItemInHand(1);
        }

        //if Tool breaks
        if (tool != null && tool.stackSize < 1)
        {
            worker.setCurrentItemOrArmor(0, null);
            worker.getInventoryCitizen().setInventorySlotContents(worker.getInventoryCitizen().getHeldItemSlot(), null);
        }

        Utils.blockBreakSoundAndEffect(world, blockToMine, curBlock,
                                       curBlock.getMetaFromState(world.getBlockState(blockToMine)));

        List<ItemStack> items = BlockPosUtil.getBlockDrops(world, blockToMine, fortune);
        for (ItemStack item : items)
        {
            InventoryUtils.setStack(worker.getInventoryCitizen(), item);
        }


        world.setBlockToAir(blockToMine);
        worker.addExperience(XP_PER_BLOCK);
        blocksMined++;
        return true;
    }

    /**
     * Checks for the right tools and waits for an appropriate delay.
     *
     * @param blockToMine the block to mine eventually
     * @param safeStand   a safe stand to mine from (AIR Block!)
     */
    private boolean checkMiningLocation(BlockPos blockToMine, BlockPos safeStand)
    {
        Block curBlock = world.getBlockState(blockToMine).getBlock();

        if (!holdEfficientTool(curBlock))
        {
            //We are missing a tool to harvest this block...
            return true;
        }

        ItemStack tool = worker.getHeldItem();

        if (curBlock.getHarvestLevel(curBlock.getDefaultState()) < Utils.getMiningLevel(tool, curBlock.getHarvestTool(curBlock.getDefaultState())))
        {
            //We have to high of a tool...
            //TODO: request lower tier tools
        }

        if (tool != null && !ForgeHooks.canToolHarvestBlock(world, blockToMine, tool) && curBlock != Blocks.bedrock)
        {
            Log.logger.info("ForgeHook not in sync with EfficientTool for " + curBlock + " and " + tool + "\n"
                            + "Please report to MineColonies with this text to add support!");
        }

        if (walkToBlock(safeStand))
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
    private int getBlockMiningDelay(Block block, BlockPos pos)
    {
        if (worker.getHeldItem() == null)
        {
            return (int) block.getBlockHardness(world, pos);
        }
        return (int) ((DELAY_MODIFIER - worker.getLevel())
                      * (double) block.getBlockHardness(world, pos)
                      / (double) (worker.getHeldItem().getItem()
                                        .getDigSpeed(worker.getHeldItem(),
                                                     block.getDefaultState())));
    }

    /**
     * Will return the number of blocks mined.
     * Counting from the last time dumping inventory.
     * Useful for calculating when to return to chest.
     *
     * @return the number of blocks mined
     */
    protected final int getBlocksMined()
    {
        return blocksMined;
    }

    /**
     * Clear the amount of blocks mined.
     * Call this when dumping into the chest.
     */
    protected final void clearBlocksMined()
    {
        this.blocksMined = 0;
    }

}
