package com.minecolonies.core.entity.ai.workers;

import com.minecolonies.api.entity.ai.workers.util.IBuilderUndestroyable;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.MathUtils;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.jobs.AbstractJob;
import com.minecolonies.core.entity.pathfinding.pathresults.PathResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.minecolonies.api.research.util.ResearchConstants.BLOCK_BREAK_SPEED;

/**
 * This is the base class of all worker AIs. Every AI implements this class with it's job type. There are some utilities within the class: - The AI will clear a full inventory at
 * the building chest. - The AI will animate mining a block (with delay) - The AI will request items and tools automatically (and collect them from the building chest)
 *
 * @param <J> the job type this AI has to do.
 */
public abstract class AbstractEntityAIInteract<J extends AbstractJob<?, J>, B extends AbstractBuilding> extends AbstractEntityAISkill<J, B>
{
    /**
     * Working render meta.
     */
    public static final String RENDER_META_WORKING = "working";

    /**
     * The amount of xp the entity gains per block mined.
     */
    public static final double XP_PER_BLOCK = 0.05D;

    /**
     * The percentage of time needed if we are one level higher.
     */
    private static final double LEVEL_MODIFIER = 0.85D;

    /**
     * The minimum range the builder has to reach in order to construct or clear.
     */
    private static final int MIN_WORKING_RANGE = 12;

    /**
     * Range around the worker to pickup items.
     */
    private static final int ITEM_PICKUP_RANGE = 3;

    /**
     * Ticks to wait until discovering that is stuck.
     */
    private static final int STUCK_WAIT_TICKS = 20;

    /**
     * Horizontal range in which the worker picks up items.
     */
    public static final float RANGE_HORIZONTAL_PICKUP = 45.0F;

    /**
     * Vertical range in which the worker picks up items.
     */
    public static final float RANGE_VERTICAL_PICKUP = 3.0F;

    /**
     * Number of ticks the worker is standing still.
     */
    private int stillTicks = 0;

    /**
     * Used to store the path index to check if the worker is still walking.
     */
    private int previousIndex = 0;

    /**
     * Positions of all items that have to be collected.
     */
    @Nullable
    private List<BlockPos> items;

    /**
     * The current path to the random position
     */
    private PathResult pathResult;

    /**
     * The backup factor of the path.
     */
    protected int pathBackupFactor = 1;

    /**
     * Block mining delay base
     */
    public static final int BLOCK_MINING_DELAY = 500;

    /**
     * Creates the abstract part of the AI. Always use this constructor!
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
     * Will simulate mining a block with particles ItemDrop etc. Attention: Because it simulates delay, it has to be called 2 times. So make sure the code path up to this function
     * is reachable a second time. And make sure to immediately exit the update function when this returns false.
     *
     * @param blockToMine the block that should be mined
     * @return true once we're done
     */
    protected final boolean mineBlock(@NotNull final BlockPos blockToMine)
    {
        return mineBlock(blockToMine, worker.blockPosition());
    }

    /**
     * Will simulate mining a block with particles ItemDrop etc. Attention: Because it simulates delay, it has to be called 2 times. So make sure the code path up to this function
     * is reachable a second time. And make sure to immediately exit the update function when this returns false.
     *
     * @param blockToMine the block that should be mined
     * @param safeStand   the block we want to stand on to do that
     * @return true once we're done
     */
    protected boolean mineBlock(@NotNull final BlockPos blockToMine, @NotNull final BlockPos safeStand)
    {
        return mineBlock(blockToMine, safeStand, true, true, null);
    }

    /**
     * Will simulate mining a block with particles ItemDrop etc. Attention: Because it simulates delay, it has to be called 2 times. So make sure the code path up to this function
     * is reachable a second time. And make sure to immediately exit the update function when this returns false.
     *
     * @param blockToMine      the block that should be mined
     * @param safeStand        the block we want to stand on to do that
     * @param damageTool       boolean wether we want to damage the tool used
     * @param getDrops         boolean wether we want to get Drops
     * @param blockBreakAction Runnable that is used instead of the default block break action, can be null
     * @return true once we're done
     */
    protected final boolean mineBlock(
      @NotNull final BlockPos blockToMine,
      @NotNull final BlockPos safeStand,
      final boolean damageTool,
      final boolean getDrops,
      final Runnable blockBreakAction)
    {
        final BlockState curBlockState = world.getBlockState(blockToMine);
        @Nullable final Block curBlock = curBlockState.getBlock();
        if (curBlock instanceof AirBlock
              || curBlock instanceof IBuilderUndestroyable
              || curBlock == Blocks.BEDROCK)
        {
            if (!curBlockState.getFluidState().isEmpty())
            {
                world.removeBlock(blockToMine, false);
            }
            //no need to mine block...
            return true;
        }

        if (checkMiningLocation(blockToMine, safeStand))
        {
            //we have to wait for delay
            return false;
        }

        final ItemStack tool = worker.getMainHandItem();

        if (getDrops)
        {
            //calculate fortune enchantment
            final int fortune = ItemStackUtils.getFortuneOf(tool);

            //create list for all item drops to be stored in
            List<ItemStack> localItems = new ArrayList<>();

            //Checks to see if the equipped tool has Silk Touch AND if the blocktoMine has a viable Item SilkTouch can get.
            if (!tool.isEmpty() && shouldSilkTouchBlock(curBlockState))
            {
                final ItemStack fakeTool = tool.copy();
                fakeTool.enchant(Enchantments.SILK_TOUCH, 1);
                localItems.addAll(BlockPosUtil.getBlockDrops(world, blockToMine, fortune, fakeTool, worker));
            }
            //If Silk Touch doesn't work, get blocks with Fortune value as normal.
            else
            {
                localItems.addAll(BlockPosUtil.getBlockDrops(world, blockToMine, fortune, tool, worker));
            }

            localItems = increaseBlockDrops(localItems);

            //add the drops to the citizen
            for (final ItemStack item : localItems)
            {
                InventoryUtils.transferItemStackIntoNextBestSlotInItemHandler(item, worker.getInventoryCitizen());
            }
            onBlockDropReception(localItems);
        }

        triggerMinedBlock(curBlockState);

        if (blockBreakAction == null)
        {
            //Break the block
            worker.getCitizenItemHandler().breakBlockWithToolInHand(blockToMine);
        }
        else
        {
            blockBreakAction.run();
        }

        if (tool != ItemStack.EMPTY && damageTool)
        {
            tool.getItem().inventoryTick(tool, world, worker, worker.getCitizenInventoryHandler().findFirstSlotInInventoryWith(tool.getItem()), true);
        }
        worker.getCitizenExperienceHandler().addExperience(XP_PER_BLOCK);
        this.incrementActionsDone();
        return true;
    }

    /**
     * Event triggered after receiving a list of block drops.
     *
     * @param blockDrops the received items from the block.
     */
    public void onBlockDropReception(final List<ItemStack> blockDrops)
    {
        //Override if needed
    }

    /**
     * Check if this specific block should be picked up via silk touch.
     *
     * @param curBlockState the state to check.
     * @return true if so.
     */
    public boolean shouldSilkTouchBlock(final BlockState curBlockState)
    {
        return false;
    }

    /**
     * Potentially increase the blockdrops. To be overriden by the worker.
     *
     * @param drops the drops.
     * @return the list of additional drops.
     */
    protected List<ItemStack> increaseBlockDrops(final List<ItemStack> drops)
    {
        return drops;
    }

    /**
     * Trigger for miners if they want to do something specific per mined block.
     *
     * @param blockToMine the mined block.
     */
    protected void triggerMinedBlock(@NotNull final BlockState blockToMine)
    {

    }

    /**
     * Checks for the right tools and waits for an appropriate delay.
     *
     * @param blockToMine the block to mine eventually
     * @param safeStand   a safe stand to mine from (empty Block!)
     * @return true if you should wait
     */
    private boolean checkMiningLocation(@NotNull final BlockPos blockToMine, @NotNull final BlockPos safeStand)
    {
        final BlockState curBlock = world.getBlockState(blockToMine);

        if (!holdEfficientTool(curBlock, blockToMine))
        {
            //We are missing a tool to harvest this block...
            return true;
        }

        if (walkToBlock(safeStand) && MathUtils.twoDimDistance(worker.blockPosition(), safeStand) > MIN_WORKING_RANGE)
        {
            return true;
        }
        currentWorkingLocation = blockToMine;

        return hasNotDelayed(getBlockMiningTime(curBlock, blockToMine));
    }

    /**
     * Calculate how long it takes to mine this block.
     *
     * @param state the blockstate
     * @param pos   coordinate
     * @return the delay in ticks
     */
    public int getBlockMiningTime(@NotNull final BlockState state, @NotNull final BlockPos pos)
    {
        if (worker.getMainHandItem() == null)
        {
            return (int) state.getDestroySpeed(world, pos);
        }

        return MineColonies.getConfig().getServer().pvp_mode.get()
                 ? BLOCK_MINING_DELAY / 2
                 : calculateWorkerMiningDelay(state, pos);
    }

    /**
     * Calculate the worker mining delay for a block at a pos.
     *
     * @param state the blockstate.
     * @param pos   the pos.
     * @return the mining delay of the worker.
     */
    private int calculateWorkerMiningDelay(@NotNull final BlockState state, @NotNull final BlockPos pos)
    {
        final double reduction = 1 - worker.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(BLOCK_BREAK_SPEED);

        return (int) (((BLOCK_MINING_DELAY * Math.pow(LEVEL_MODIFIER, getBreakSpeedLevel() / 2.0))
                         * (double) world.getBlockState(pos).getDestroySpeed(world, pos) / (double) (worker.getMainHandItem()
                                                                                                       .getItem()
                                                                                                       .getDestroySpeed(worker.getMainHandItem(), state)))
                        * reduction);
    }

    /**
     * Get the level that affects the break speed.
     *
     * @return the level.
     */
    public int getBreakSpeedLevel()
    {
        return getPrimarySkillLevel();
    }

    /**
     * Fill the list of the item positions to gather.
     */
    public void fillItemsList()
    {
        searchForItems(worker.getBoundingBox()
                         .expandTowards(RANGE_HORIZONTAL_PICKUP, RANGE_VERTICAL_PICKUP, RANGE_HORIZONTAL_PICKUP)
                         .expandTowards(-RANGE_HORIZONTAL_PICKUP, -RANGE_VERTICAL_PICKUP, -RANGE_HORIZONTAL_PICKUP));
    }

    /**
     * Search for all items around the worker. and store them in the items list.
     *
     * @param boundingBox the area to search.
     */
    public void searchForItems(final AABB boundingBox)
    {
        items = world.getEntitiesOfClass(ItemEntity.class, boundingBox)
                  .stream()
                  .filter(item -> item != null && item.isAlive() &&
                                    (!item.getPersistentData().contains("PreventRemoteMovement") || !item.getPersistentData().getBoolean("PreventRemoteMovement")) &&
                                    isItemWorthPickingUp(item.getItem()))
                  .map(BlockPosUtil::fromEntity)
                  .collect(Collectors.toList());
    }

    /**
     * Check if an item is sufficiently interesting to want to go pick it up.  (This won't stop a
     * worker picking up something else as they pass by, but it makes them not want to go over to it.)
     *
     * @param stack the stack to check.
     * @return true if the worker wants to go over to it.
     */
    protected boolean isItemWorthPickingUp(final ItemStack stack)
    {
        return true;
    }

    /**
     * Collect one item by walking to it.
     */
    public void gatherItems()
    {
        worker.setCanPickUpLoot(true);
        if (worker.getNavigation().isDone() || worker.getNavigation().getPath() == null)
        {
            final BlockPos pos = getAndRemoveClosestItemPosition();
            worker.isWorkerAtSiteWithMove(pos, ITEM_PICKUP_RANGE);
            return;
        }

        final int currentIndex = worker.getNavigation().getPath().getNextNodeIndex();
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
            worker.getNavigation().stop();
            if (items != null && !items.isEmpty())
            {
                items.remove(0);
            }
        }
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
            final double tempDistance = items.get(i).distSqr(worker.blockPosition());
            if (tempDistance < distance)
            {
                index = i;
                distance = tempDistance;
            }
        }

        return items.remove(index);
    }

    /**
     * Search for a random position to go to, anchored around the citizen.
     *
     * @param range the max range
     * @return null until position was found.
     */
    protected BlockPos findRandomPositionToWalkTo(final int range)
    {
        return findRandomPositionToWalkTo(range, worker.blockPosition());
    }

    /**
     * Search for a random position to go to.
     *
     * @param range the max range
     * @param pos   position we want to find a random position around in the given range
     * @return null until position was found.
     */
    protected BlockPos findRandomPositionToWalkTo(final int range, final BlockPos pos)
    {
        if (pathResult == null)
        {
            pathBackupFactor = 1;
            pathResult = getRandomNavigationPath(range, pos);
        }
        else if (pathResult.failedToReachDestination())
        {
            pathBackupFactor++;
            pathResult = getRandomNavigationPath(range * pathBackupFactor, pos);
        }

        if (pathResult == null)
        {
            return null;
        }

        if (pathResult.isPathReachingDestination())
        {
            final BlockPos resultPos = pathResult.getPath().getEndNode().asBlockPos();
            pathResult = null;
            return resultPos;
        }

        if (pathResult.isCancelled())
        {
            pathResult = null;
            return null;
        }

        if (pathBackupFactor > 10)
        {
            pathResult = null;
            return null;
        }

        return null;
    }

    /**
     * Get a navigator to find a certain position.
     *
     * @param range the max range.
     * @param pos   the position to
     * @return the navigator.
     */
    protected PathResult getRandomNavigationPath(final int range, final BlockPos pos)
    {
        if (pos == null || pos == worker.blockPosition())
        {
            return worker.getNavigation().moveToRandomPos(range, 1.0D);
        }
        else
        {
            return worker.getNavigation().moveToRandomPosAroundX(range, 1.0D, pos);
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
     *
     * @return a copy of it.
     */
    @Nullable
    public List<BlockPos> getItemsForPickUp()
    {
        return items == null ? null : new ArrayList<>(items);
    }
}
