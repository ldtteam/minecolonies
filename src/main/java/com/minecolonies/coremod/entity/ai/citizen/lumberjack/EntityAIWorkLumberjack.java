package com.minecolonies.coremod.entity.ai.citizen.lumberjack;

import com.minecolonies.coremod.colony.jobs.JobLumberjack;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import com.minecolonies.coremod.entity.pathfinding.PathJobFindTree;
import com.minecolonies.coremod.util.BlockPosUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.SoundType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.minecolonies.coremod.entity.ai.util.AIState.*;

/**
 * The lumberjack AI class.
 */
public class EntityAIWorkLumberjack extends AbstractEntityAIInteract<JobLumberjack>
{
    /**
     * The render name to render logs.
     */
    private static final String RENDER_META_LOGS = "Logs";

    /**
     * The range in which the lumberjack searches for trees.
     */
    private static final int SEARCH_RANGE = 50;

    /**
     * If no trees are found, increment the range.
     */
    private static final int SEARCH_INCREMENT = 5;

    /**
     * If this limit is reached, no trees are found.
     */
    private static final int SEARCH_LIMIT = 150;

    /**
     * Number of ticks to wait before coming to the conclusion of being stuck.
     */
    private static final int STUCK_WAIT_TIME = 10;

    /**
     * Number of ticks until he gives up destroying leaves
     * and walks a bit back to try a new path.
     */
    private static final int WALKING_BACK_WAIT_TIME = 60;

    /**
     * How much he backs away when really not finding any path.
     */
    private static final double WALK_BACK_RANGE = 3.0;

    /**
     * The speed in which he backs away.
     */
    private static final double WALK_BACK_SPEED = 1.0;

    /**
     * Time in ticks to wait before placing a sapling.
     * Is used to collect falling saplings from the ground.
     */
    private static final int WAIT_BEFORE_SAPLING = 50;

    /**
     * Time in ticks to wait before placing a sapling.
     * Is used to collect falling saplings from the ground.
     */
    private static final int MAX_WAITING_TIME = 100;

    /**
     * Number of ticks to wait for tree.
     */
    private static final int   TIMEOUT_DELAY           = 10;
    private static final int   LEAVES_RADIUS           = 3;
    private static final int   ITEM_PICKUP_RANGE       = 3;
    private static final int   STUCK_WAIT_TICKS        = 20;
    /**
     * Time in ticks to wait before rechecking
     * if there are trees in the
     * range of the lumberjack.
     */
    private static final int   WAIT_BEFORE_SEARCH      = 100;
    /**
     * Time in ticks before incrementing the search radius.
     */
    private static final int   WAIT_BEFORE_INCREMENT   = 20;
    /**
     * The amount of time to wait while walking to items.
     */
    private static final int   WAIT_WHILE_WALKING      = 5;
    /**
     * Horizontal range in which the lumberjack picks up items.
     */
    private static final float RANGE_HORIZONTAL_PICKUP = 45.0F;
    /**
     * Vertical range in which the lumberjack picks up items.
     */
    private static final float RANGE_VERTICAL_PICKUP   = 3.0F;
    /**
     * How often should strength factor into the lumberjacks skill modifier.
     */
    private static final int   STRENGTH_MULTIPLIER     = 2;
    /**
     * How often should charisma factor into the lumberjacks skill modifier.
     */
    private static final int   CHARISMA_MULTIPLIER     = 1;
    /**
     * Return to chest after half a stack.
     */
    private static final int   MAX_BLOCKS_MINED        = 32;
    /**
     * The time in ticks the lumberjack has waited already.
     * Directly connected with the MAX_WAITING_TIME.
     */
    private              int   timeWaited              = 0;
    /**
     * Number of ticks the lumberjack is standing still.
     */
    private              int   stillTicks              = 0;
    /**
     * Used to store the walk distance
     * to check if the lumberjack is still walking.
     */
    private              int   previousDistance        = 0;
    /**
     * Used to store the path index
     * to check if the lumberjack is still walking.
     */
    private              int   previousIndex           = 0;

    /**
     * Positions of all items that have to be collected.
     */
    @Nullable
    private List<BlockPos> items;

    /**
     * The active pathfinding job used to walk to trees.
     */
    @Nullable
    private PathJobFindTree.TreePathResult pathResult;
    /**
     * A counter by how much the tree search radius
     * has been increased by now.
     */
    private int searchIncrement = 0;

    /**
     * Create a new LumberjackAI.
     *
     * @param job the lumberJackJob
     */
    public EntityAIWorkLumberjack(@NotNull final JobLumberjack job)
    {

        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING),
          new AITarget(START_WORKING, this::startWorkingAtOwnBuilding),
          new AITarget(PREPARING, this::prepareForWoodcutting),
          new AITarget(LUMBERJACK_SEARCHING_TREE, this::findTrees),
          new AITarget(LUMBERJACK_CHOP_TREE, this::chopWood),
          new AITarget(LUMBERJACK_GATHERING, this::gathering),
          new AITarget(LUMBERJACK_NO_TREES_FOUND, this::waitBeforeCheckingAgain)
        );
        worker.setSkillModifier(STRENGTH_MULTIPLIER * worker.getCitizenData().getStrength()
                                  + CHARISMA_MULTIPLIER * worker.getCitizenData().getCharisma());
    }

    /**
     * Walk to own building to check for tools.
     *
     * @return PREPARING once at the building.
     */
    private AIState startWorkingAtOwnBuilding()
    {
        if (walkToBuilding())
        {
            return getState();
        }
        return PREPARING;
    }

    /**
     * Checks if lumberjack has all necessary tools.
     *
     * @return next AIState.
     */
    private AIState prepareForWoodcutting()
    {
        if (checkForAxe())
        {
            return getState();
        }
        return LUMBERJACK_SEARCHING_TREE;
    }

    /**
     * If the search radius was exceeded,
     * we have to wait dome time before
     * searching again.
     *
     * @return LUMBERJACK_SEARCHING_TREE once waited enough.
     */
    private AIState waitBeforeCheckingAgain()
    {
        if (hasNotDelayed(WAIT_BEFORE_SEARCH))
        {
            return getState();
        }
        return LUMBERJACK_SEARCHING_TREE;
    }

    /**
     * Checks if lumberjack has already found some trees. If not search trees.
     *
     * @return next AIState
     */
    private AIState findTrees()
    {
        if (job.tree == null)
        {
            return findTree();
        }
        return LUMBERJACK_CHOP_TREE;
    }

    /**
     * Search for a tree.
     *
     * @return LUMBERJACK_GATHERING if job was canceled.
     */
    private AIState findTree()
    {
        if (pathResult == null || pathResult.treeLocation == null)
        {
            pathResult = worker.getNavigator().moveToTree(SEARCH_RANGE + searchIncrement, 1.0D);
            return getState();
        }
        if (pathResult.getPathReachesDestination())
        {
            return setNewTree();
        }
        if (pathResult.isCancelled())
        {
            pathResult = null;
            return LUMBERJACK_GATHERING;
        }
        return getState();
    }

    private AIState setNewTree()
    {
        if (pathResult.treeLocation == null)
        {
            setDelay(WAIT_BEFORE_INCREMENT);
            if (searchIncrement + SEARCH_RANGE > SEARCH_LIMIT)
            {
                return LUMBERJACK_NO_TREES_FOUND;
            }
            searchIncrement += SEARCH_INCREMENT;
        }
        else
        {
            job.tree = new Tree(world, pathResult.treeLocation);
            job.tree.findLogs(world);
        }
        pathResult = null;

        return getState();
    }

    /**
     * Again checks if all preconditions are given to execute chopping.
     * If yes go chopping, else return to previous AIStates.
     *
     * @return next AIState
     */
    private AIState chopWood()
    {
        if (checkForAxe())
        {
            return IDLE;
        }
        if (job.tree == null)
        {
            return LUMBERJACK_SEARCHING_TREE;
        }
        return chopTree();
    }

    /**
     * Work on the tree.
     * First find your way to the tree trunk.
     * Then chop away
     * and wait for saplings to drop
     * then place a sapling
     *
     * @return LUMBERJACK_GATHERING if tree is done
     */
    private AIState chopTree()
    {
        final BlockPos location = job.tree.getLocation();
        if (walkToBlock(location))
        {
            checkIfStuckOnLeaves(location);
            return getState();
        }

        if (!job.tree.hasLogs())
        {
            if (hasNotDelayed(WAIT_BEFORE_SAPLING))
            {
                return getState();
            }
            plantSapling();
            this.getOwnBuilding().getColony().incrementStatistic("trees");
            return LUMBERJACK_GATHERING;
        }

        //take first log from queue
        final BlockPos log = job.tree.peekNextLog();
        if (!mineBlock(log))
        {
            return getState();
        }
        job.tree.pollNextLog();
        return getState();
    }

    /**
     * Check if distance to block changed and
     * if we are not moving for too long, try to get unstuck.
     *
     * @param location the block we want to go to.
     */
    private void checkIfStuckOnLeaves(@NotNull final BlockPos location)
    {
        final int distance = (int) location.distanceSq(worker.getPosition());
        if (previousDistance != distance)
        {
            //something is moving, reset counters
            stillTicks = 0;
            previousDistance = distance;
            return;
        }
        //Stuck, probably on leaves
        stillTicks++;
        if (stillTicks < STUCK_WAIT_TIME)
        {
            //Wait for some time before jumping to conclusions
            return;
        }
        //now we seem to be stuck!
        tryGettingUnstuckFromLeaves();
    }

    /**
     * Place a sappling for the current tree.
     */
    private void plantSapling()
    {
        if (plantSapling(job.tree.getLocation()))
        {
            job.tree = null;
        }
    }

    /**
     * We are stuck, remove some leaves and try to get unstuck.
     * <p>
     * if this takes too long, try backing up a bit.
     */
    private void tryGettingUnstuckFromLeaves()
    {
        @Nullable final BlockPos nextLeaves = findNearLeaves();
        //If the worker gets too stuck he moves around a bit
        if (nextLeaves == null || stillTicks > WALKING_BACK_WAIT_TIME)
        {
            worker.getNavigator().moveAwayFromXYZ(worker.getPosition(), WALK_BACK_RANGE, WALK_BACK_SPEED);
            stillTicks = 0;
            return;
        }
        if (!mineBlock(nextLeaves))
        {
            return;
        }
        stillTicks = 0;
    }

    /**
     * Plant a sapling at said location.
     *
     * @param location the location to plant the sapling at
     * @return true if a sapling was planted
     */
    private boolean plantSapling(@NotNull final BlockPos location)
    {
        final Block worldBlock = world.getBlockState(location).getBlock();
        if (worldBlock != Blocks.AIR && !(worldBlock instanceof BlockSapling))
        {
            return false;
        }

        final int saplingSlot = findSaplingSlot();

        if (saplingSlot != -1)
        {
            final ItemStack stack = getInventory().getStackInSlot(saplingSlot);
            final Block block = ((ItemBlock) stack.getItem()).getBlock();
            worker.setHeldItem(saplingSlot);

            placeSaplings(saplingSlot, stack, block);
            final SoundType soundType = block.getSoundType(world.getBlockState(location), world, location, worker);
            world.playSound(null,
              this.worker.getPosition(),
              soundType.getPlaceSound(),
              SoundCategory.BLOCKS,
              soundType.getVolume(),
              soundType.getPitch());
            worker.swingArm(worker.getActiveHand());
            this.getOwnBuilding().getColony().incrementStatistic("saplings");
        }

        if (job.tree.getStumpLocations().isEmpty() || timeWaited >= MAX_WAITING_TIME)
        {
            timeWaited = 0;
            setDelay(TIMEOUT_DELAY);
            return true;
        }
        timeWaited++;
        return false;
    }

    /**
     * Utility method to check for leaves around the citizen.
     * <p>
     * Will report the location of the first leaves block it finds.
     *
     * @return a leaves block or null if none found
     */
    private BlockPos findNearLeaves()
    {
        final int playerX = worker.getPosition().getX();
        final int playerY = worker.getPosition().getY() + 1;
        final int playerZ = worker.getPosition().getZ();
        final int radius = LEAVES_RADIUS;
        for (int x = playerX - radius; x < playerX + radius; x++)
        {
            for (int y = playerY - radius; y < playerY + radius; y++)
            {
                for (int z = playerZ - radius; z < playerZ + radius; z++)
                {
                    @NotNull final BlockPos pos = new BlockPos(x, y, z);
                    if (world.getBlockState(pos).getBlock().isLeaves(world.getBlockState(pos), world, pos))
                    {
                        return pos;
                    }
                }
            }
        }
        return null;
    }

    private int findSaplingSlot()
    {
        for (int slot = 0; slot < getInventory().getSizeInventory(); slot++)
        {
            final ItemStack stack = getInventory().getStackInSlot(slot);
            if (isCorrectSapling(stack))
            {
                return slot;
            }
        }
        return -1;
    }

    //todo: we need to use a different way to get Metadata
    @SuppressWarnings("deprecation")
    private void placeSaplings(final int saplingSlot, @NotNull final ItemStack stack, @NotNull final Block block)
    {
        while (!job.tree.getStumpLocations().isEmpty())
        {
            final BlockPos pos = job.tree.getStumpLocations().get(0);

            if ((BlockPosUtil.setBlock(world, pos, block.getStateFromMeta(stack.getMetadata()), 0x02) && getInventory().getStackInSlot(saplingSlot) != null)
                  || Objects.equals(world.getBlockState(pos), block.getStateFromMeta(stack.getMetadata())))
            {

                getInventory().decrStackSize(saplingSlot, 1);
                job.tree.removeStump(pos);
            }
            else
            {
                return;
            }
        }
    }

    //todo: we need to use a different way to get Metadata
    @SuppressWarnings("deprecation")
    /**
     * Checks if this is the correct Sapling.
     * @param stack incoming stack.
     * @return true if so.
     */
    private boolean isCorrectSapling(final ItemStack stack)
    {
        return isStackSapling(stack) && job.tree.getVariant() == ((ItemBlock) stack.getItem()).getBlock().getStateFromMeta(stack.getMetadata()).getValue(BlockSapling.TYPE);
    }

    /**
     * Checks if a stack is a type of sapling.
     *
     * @param stack the stack to check.
     * @return true if sapling.
     */
    private static boolean isStackSapling(@Nullable final ItemStack stack)
    {
        return stack != null && stack.getItem() instanceof ItemBlock && ((ItemBlock) stack.getItem()).getBlock() instanceof BlockSapling;
    }

    /**
     * Checks if the lumberjack found items on the ground,
     * if yes collect them, if not search for them.
     *
     * @return LUMBERJACK_GATHERING as long as gathering takes.
     */
    private AIState gathering()
    {
        if (items == null)
        {
            searchForItems();
        }
        if (!items.isEmpty())
        {
            gatherItems();
            return getState();
        }
        items = null;
        return LUMBERJACK_SEARCHING_TREE;
    }

    /**
     * Search for all items around the Lumberjack.
     * and store them in the items list.
     */
    private void searchForItems()
    {
        items = new ArrayList<>();

        //TODO check if sapling or apple (currently picks up all items, which may be okay)
        items = world.getEntitiesWithinAABB(EntityItem.class, worker.getEntityBoundingBox().expand(RANGE_HORIZONTAL_PICKUP, RANGE_VERTICAL_PICKUP, RANGE_HORIZONTAL_PICKUP))
                  .stream()
                  .filter(item -> item != null && !item.isDead)
                  .map(BlockPosUtil::fromEntity)
                  .collect(Collectors.toList());
    }

    /**
     * Collect one item by walking to it.
     */
    private void gatherItems()
    {
        worker.setCanPickUpLoot(true);
        if (worker.getNavigator().noPath())
        {
            final BlockPos pos = getAndRemoveClosestItem();
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
        }
    }

    /**
     * Find the closest item and remove it from the list.
     *
     * @return the closest item
     */
    private BlockPos getAndRemoveClosestItem()
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

    /**
     * Calculates after how many actions the ai should dump it's inventory.
     * <p>
     * Override this to change the value.
     *
     * @return the number of actions done before item dump.
     */
    @Override
    protected int getActionsDoneUntilDumping()
    {
        return MAX_BLOCKS_MINED;
    }

    /**
     * Can be overridden in implementations.
     * <p>
     * Here the AI can check if the chestBelt has to be re rendered and do it.
     */
    @Override
    protected void updateRenderMetaData()
    {
        worker.setRenderMetadata(hasLogs() ? RENDER_META_LOGS : "");
    }

    /**
     * Checks if the lumberjack has logs in it's inventory.
     *
     * @return true if he has logs.
     */
    private boolean hasLogs()
    {
        for (int i = 0; i < getInventory().getSizeInventory(); i++)
        {
            if (isStackLog(getInventory().getStackInSlot(i)))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a stack is a type of log.
     *
     * @param stack the stack to check.
     * @return true if it is a log type.
     */
    private static boolean isStackLog(@Nullable final ItemStack stack)
    {
        return stack != null && stack.getItem() instanceof ItemBlock && ((ItemBlock) stack.getItem()).getBlock().isWood(null, new BlockPos(0, 0, 0));
    }
}
