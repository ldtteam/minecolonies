package com.minecolonies.coremod.entity.ai.basic;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.*;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    public static final double XP_PER_BLOCK         = 0.05D;

    /**
     * The percentage of time needed if we are one level higher.
     */
    private static final double LEVEL_MODIFIER       = 0.85D;

    /**
     * The minimum range the builder has to reach in order to construct or clear.
     */
    private static final int    MIN_WORKING_RANGE    = 12;

    /**
     * Range around the worker to pickup items.
     */
    private static final int   ITEM_PICKUP_RANGE       = 3;

    /**
     * Ticks to wait until discovering that is stuck.
     */
    private static final int   STUCK_WAIT_TICKS        = 20;

    /**
     * The amount of time to wait while walking to items.
     */
    private static final int   WAIT_WHILE_WALKING      = 5;

    /**
     * Horizontal range in which the worker picks up items.
     */
    private static final float RANGE_HORIZONTAL_PICKUP = 45.0F;

    /**
     * Vertical range in which the worker picks up items.
     */
    private static final float RANGE_VERTICAL_PICKUP   = 3.0F;

    /**
     * Number of ticks the worker is standing still.
     */
    private              int   stillTicks              = 0;

    /**
     * Used to store the path index
     * to check if the worker is still walking.
     */
    private              int   previousIndex           = 0;

    /**
     * Positions of all items that have to be collected.
     */
    @Nullable
    private List<BlockPos> items;

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
        final int fortune = ItemStackUtils.getFortuneOf(tool);

        //get all item drops
        final List<ItemStack> localItems = BlockPosUtil.getBlockDrops(world, blockToMine, fortune);

        //if block in statistic then increment that statistic.
        triggerMinedBlock(blockToMine);

        //Break the block
        worker.breakBlockWithToolInHand(blockToMine);

        //add the drops to the citizen
        for (final ItemStack item : localItems)
        {
            InventoryUtils.addItemStackToItemHandler(new InvWrapper(worker.getInventoryCitizen()), item);
        }

        if (tool != null)
        {
            tool.getItem().onUpdate(tool, world, worker, worker.findFirstSlotInInventoryWith(tool.getItem(), tool.getItemDamage()), true);
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

    private void triggerMinedBlock(@NotNull final BlockPos blockToMine)
    {
        if (world.getBlockState(blockToMine).getBlock() == (Blocks.COAL_ORE)
                || world.getBlockState(blockToMine).getBlock() == (Blocks.IRON_ORE)
                || world.getBlockState(blockToMine).getBlock() == (Blocks.LAPIS_ORE)
                || world.getBlockState(blockToMine).getBlock() == (Blocks.GOLD_ORE)
                || world.getBlockState(blockToMine).getBlock() == (Blocks.REDSTONE_ORE)
                || world.getBlockState(blockToMine).getBlock() == (Blocks.EMERALD_ORE))
        {
            this.getOwnBuilding().getColony().incrementStatistic("ores");
        }
        if (world.getBlockState(blockToMine).getBlock().equals(Blocks.DIAMOND_ORE))
        {
            this.getOwnBuilding().getColony().incrementStatistic("diamonds");
        }
        if (world.getBlockState(blockToMine).getBlock().equals(Blocks.CARROTS))
        {
            this.getOwnBuilding().getColony().incrementStatistic("carrots");
        }
        if (world.getBlockState(blockToMine).getBlock().equals(Blocks.POTATOES))
        {
            this.getOwnBuilding().getColony().incrementStatistic("potatoes");
        }
        if (world.getBlockState(blockToMine).getBlock().equals(Blocks.WHEAT))
        {
            this.getOwnBuilding().getColony().incrementStatistic("wheat");
        }
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

        return (int) ((Configurations.gameplay.blockMiningDelayModifier
                         * Math.pow(LEVEL_MODIFIER, worker.getLevel()))
                        * (double) world.getBlockState(pos).getBlockHardness(world, pos)
                        / (double) (worker.getHeldItemMainhand().getItem()
                                      .getStrVsBlock(worker.getHeldItemMainhand(),
                                        block.getDefaultState())));
    }

    /**
     * Fill the list of the item positions to gather.
     */
    public void fillItemsList()
    {
        searchForItems(worker.getEntityBoundingBox().expand(RANGE_HORIZONTAL_PICKUP, RANGE_VERTICAL_PICKUP, RANGE_HORIZONTAL_PICKUP));
    }

    /**
     * Search for all items around the worker.
     * and store them in the items list.
     * @param boundingBox the area to search.
     */
    public void searchForItems(final AxisAlignedBB boundingBox)
    {
        items = world.getEntitiesWithinAABB(EntityItem.class, boundingBox)
                .stream()
                .filter(item -> item != null && !item.isDead)
                .map(BlockPosUtil::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Collect one item by walking to it.
     */
    public void gatherItems()
    {
        worker.setCanPickUpLoot(true);
        if (worker.getNavigator().noPath())
        {
            final BlockPos pos = getAndRemoveClosestItemPosition();
            worker.isWorkerAtSiteWithMove(pos, ITEM_PICKUP_RANGE);
            return;
        }
        if (worker.getNavigator().getPath() == null)
        {
            setDelay(WAIT_WHILE_WALKING);
            return;
        }

        final int currentIndex = worker.getNavigator().getPath().getCurrentPathIndex();
        //We moved a bit, not stuck
        if (currentIndex != previousIndex)
        {
            stillTicks = 0;
            previousIndex = currentIndex;
            return;
        }

        stillTicks++;
        //Stuck for too long
        if (stillTicks > STUCK_WAIT_TICKS)
        {
            //Skip this item
            worker.getNavigator().clearPathEntity();
            if(items != null && !items.isEmpty())
            {
                items.remove(0);
            }
        }
    }

    /**
     * Reset the gathering items to null.
     */
    public void resetGatheringItems()
    {
        items = null;
    }

    /**
     * Get the items to gather list.
     * @return a copy of it.
     */
    @Nullable
    public List<BlockPos> getItemsForPickUp()
    {
        return items == null ? null : new ArrayList<>(items);
    }

    /**
     * Find the closest item and remove it from the list.
     *
     * @return the closest item
     */
    private BlockPos getAndRemoveClosestItemPosition()
    {
        int index = 0;
        double distance = Double.MAX_VALUE;

        for (int i = 0; i < items.size(); i++)
        {
            final double tempDistance = items.get(i).distanceSq(worker.getPosition());
            if (tempDistance < distance)
            {
                index = i;
                distance = tempDistance;
            }
        }

        return items.remove(index);
    }
}
