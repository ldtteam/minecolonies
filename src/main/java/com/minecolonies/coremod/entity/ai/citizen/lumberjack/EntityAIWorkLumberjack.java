package com.minecolonies.coremod.entity.ai.citizen.lumberjack;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.pathfinding.PathResult;
import com.minecolonies.api.entity.pathfinding.TreePathResult;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.MathUtils;
import com.minecolonies.api.util.Utils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingLumberjack;
import com.minecolonies.coremod.colony.jobs.JobLumberjack;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAICrafting;
import com.minecolonies.coremod.entity.pathfinding.AbstractPathJob;
import com.minecolonies.coremod.entity.pathfinding.MinecoloniesAdvancedPathNavigate;
import com.minecolonies.coremod.entity.pathfinding.PathJobMoveToWithPassable;
import com.minecolonies.coremod.util.WorkerUtil;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;

/**
 * The lumberjack AI class.
 */
public class EntityAIWorkLumberjack extends AbstractEntityAICrafting<JobLumberjack>
{
    /**
     * The render name to render logs.
     */
    private static final String RENDER_META_LOGS = "logs";

    /**
     * The range in which the lumberjack searches for trees.
     */
    public static final int SEARCH_RANGE = 50;

    /**
     * If no trees are found, increment the range.
     */
    private static final int SEARCH_INCREMENT = 5;
    /**
     * If this limit is reached, no trees are found.
     */
    private static final int SEARCH_LIMIT     = 150;

    /**
     * List of saplings.
     */
    private static final String SAPLINGS_LIST = "saplings";

    /**
     * Vertical range in which the worker picks up items.
     */
    public static final float RANGE_VERTICAL_PICKUP = 2.0F;

    /**
     * Horizontal range in which the worker picks up items.
     */
    public static final float RANGE_HORIZONTAL_PICKUP = 5.0F;

    /**
     * Number of ticks to wait before coming to the conclusion of being stuck.
     */
    private static final int    STUCK_WAIT_TIME        = 10;
    /**
     * Number of ticks until he gives up destroying leaves and walks a bit back to try a new path.
     */
    private static final int    WALKING_BACK_WAIT_TIME = 120;
    /**
     * How much he backs away when really not finding any path.
     */
    private static final double WALK_BACK_RANGE        = 3.0;
    /**
     * The speed in which he backs away.
     */
    private static final double WALK_BACK_SPEED        = 1.0;
    /**
     * The standard range the lumberjack should reach until his target.
     */
    private static final int    STANDARD_WORKING_RANGE = 1;
    /**
     * The minimum range the lumberjack has to reach in order to construct or clear.
     */
    private static final int    MIN_WORKING_RANGE      = 2;
    /**
     * Time in ticks to wait before placing a sapling. Is used to collect falling saplings from the ground.
     */
    private static final int    WAIT_BEFORE_SAPLING    = 50;
    /**
     * Time in ticks to wait before placing a sapling. Is used to collect falling saplings from the ground.
     */
    private static final int    MAX_WAITING_TIME       = 50;

    /**
     * Number of ticks to wait for tree.
     */
    private static final int TIMEOUT_DELAY      = 10;
    private static final int LEAVES_RADIUS      = 1;
    /**
     * Time in ticks to wait before rechecking if there are trees in the range of the lumberjack.
     */
    private static final int WAIT_BEFORE_SEARCH = 100;

    /**
     * Time in ticks before incrementing the search radius.
     */
    private static final int WAIT_BEFORE_INCREMENT = 20;

    /**
     * Return to chest after half a stack.
     */
    private static final int MAX_BLOCKS_MINED = 32;

    /**
     * Delay before going to gather after cutting a tree.
     */
    private static final int GATHERING_DELAY       = 3;
    private static final int MAX_LEAVES_BREAK_DIST = 8 * 8;
    private static final int TIME_TO_LEAVEBREAK    = 5;

    /**
     * Position where the Builders constructs from.
     */
    private BlockPos workFrom;

    /**
     * The time in ticks the lumberjack has waited already. Directly connected with the MAX_WAITING_TIME.
     */
    private int timeWaited = 0;

    /**
     * Number of ticks the lumberjack is standing still.
     */
    private int stillTicks = 0;

    /**
     * Used to store the walk distance to check if the lumberjack is still walking.
     */
    private int previousDistance = 0;

    /**
     * Variable describing if the lj looked in his hut for a certain sapling.
     */
    private boolean checkedInHut = false;

    /**
     * The active pathfinding job used to walk to trees.
     */
    @Nullable
    private TreePathResult pathResult;

    /**
     * A counter by how much the tree search radius has been increased by now.
     */
    private int searchIncrement = 0;

    /**
     * The current path to the tree
     */
    private PathResult pathToTree;

    @Override
    protected int getActionRewardForCraftingSuccess()
    {
        return getActionsDoneUntilDumping();
    }

    /**
     * Create a new LumberjackAI.
     *
     * @param job the lumberJackJob
     */
    public EntityAIWorkLumberjack(@NotNull final JobLumberjack job)
    {

        // Override state machine, otherwise the lumberjack will never check for wood to cut.
        super(job);
        super.registerTargets(
          new AITarget(LUMBERJACK_START_WORKING, this::startWorkingAtOwnBuilding, TICKS_SECOND),
          new AITarget(PREPARING, this::prepareForWoodcutting, TICKS_SECOND),
          new AITarget(LUMBERJACK_SEARCHING_TREE, this::findTrees, TICKS_SECOND),
          new AITarget(LUMBERJACK_CHOP_TREE, this::chopWood, TICKS_SECOND),
          new AITarget(LUMBERJACK_GATHERING, this::gathering, TICKS_SECOND),
          new AITarget(LUMBERJACK_NO_TREES_FOUND, this::waitBeforeCheckingAgain, TICKS_SECOND)
        );
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class getExpectedBuildingClass()
    {
        return BuildingLumberjack.class;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The lumberjack is a special worker.
     * In their decision state, they will try to add lumberjack cycles
     * If there's nothing left to craft, they will proceed with woodworking
     * </p>
     */
    @Override
    protected IAIState decide()
    {
        if (walkToBuilding())
        {
            return START_WORKING;
        }

        if (job.getActionsDone() >= getActionsDoneUntilDumping())
        {
            // Wait to dump before continuing.
            return getState();
        }

        // This got moved downwards compared to the AICrafting-implementation,
        // because in this case waiting for dumping is more important
        // than restarting chopping
        if (job.getTaskQueue().isEmpty())
        {
            return LUMBERJACK_START_WORKING;
        }

        if (job.getCurrentTask() == null)
        {
            return LUMBERJACK_START_WORKING;
        }


        if (currentRequest != null && currentRecipeStorage != null)
        {
            return QUERY_ITEMS;
        }

        return GET_RECIPE;
    }

    /**
     * Checks if a stack is a type of log.
     *
     * @param stack the stack to check.
     * @return true if it is a log type.
     */
    private boolean isStackLog(@Nullable final ItemStack stack)
    {
        return !ItemStackUtils.isEmpty(stack) && stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock().isIn(BlockTags.LOGS);
    }

    /**
     * Walk to own building to check for tools.
     *
     * @return PREPARING once at the building.
     */
    private IAIState startWorkingAtOwnBuilding()
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
     * @return next IAIState.
     */
    private IAIState prepareForWoodcutting()
    {
        if (checkForToolOrWeapon(ToolType.AXE))
        {
            return START_WORKING; // Reset everything, maybe there are new crafting requests
        }
        return LUMBERJACK_SEARCHING_TREE;
    }

    /**
     * If the search radius was exceeded, we have to wait dome time before searching again.
     *
     * @return LUMBERJACK_SEARCHING_TREE once waited enough.
     */
    private IAIState waitBeforeCheckingAgain()
    {
        if (hasNotDelayed(WAIT_BEFORE_SEARCH))
        {
            return getState();
        }
        return START_WORKING; // Reset everything, maybe there are new crafting requests
    }

    /**
     * Checks if lumberjack has already found some trees. If not search trees.
     *
     * @return next IAIState
     */
    private IAIState findTrees()
    {
        if (job.getTree() == null)
        {
            worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.searchingtree"));

            return findTree();
        }

        stillTicks = 0;
        return LUMBERJACK_CHOP_TREE;
    }

    /**
     * Search for a tree.
     *
     * @return LUMBERJACK_GATHERING if job was canceled.
     */
    private IAIState findTree()
    {
        final IBuilding building = getOwnBuilding();

        if (pathResult == null || pathResult.treeLocation == null)
        {

            final BuildingLumberjack buildingLumberjack = (BuildingLumberjack) building;
            final Map<String, List<ItemStorage>> copy = buildingLumberjack.getCopyOfAllowedItems();
            if (buildingLumberjack.shouldRestrict())
            {
                final BlockPos startPos = buildingLumberjack.getStartRestriction();
                final BlockPos endPos = buildingLumberjack.getEndRestriction();

                pathResult = worker.getNavigator().moveToTree(
                  startPos, endPos,
                  1.0D,
                  copy.getOrDefault(SAPLINGS_LIST, Collections.emptyList()),
                  worker.getCitizenColonyHandler().getColony()
                );
            }
            else
            {
                pathResult = worker.getNavigator()
                               .moveToTree(SEARCH_RANGE + searchIncrement,
                                 1.0D,
                                 copy.getOrDefault(SAPLINGS_LIST, Collections.emptyList()),
                                 worker.getCitizenColonyHandler().getColony());
            }
        }
        if (pathResult.isPathReachingDestination())
        {
            return setNewTree();
        }
        if (pathResult.isCancelled())
        {
            pathResult = null;
            setDelay(TICKS_SECOND * GATHERING_DELAY);
            return LUMBERJACK_GATHERING;
        }

        // None of the above yielded a result, report no trees found.
        return LUMBERJACK_NO_TREES_FOUND;
    }

    private IAIState setNewTree()
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
            job.setTree(new Tree(world, pathResult.treeLocation));

            // Check if tree creation was successful
            if (job.getTree().isTree())
            {
                job.getTree().findLogs(world);
                return LUMBERJACK_CHOP_TREE;
            }
            else
            {
                job.setTree(null);
            }
        }
        pathResult = null;

        return getState();
    }

    /**
     * Again checks if all preconditions are given to execute chopping. If yes go chopping, else return to previous AIStates.
     *
     * @return next IAIState
     */
    private IAIState chopWood()
    {
        if (checkForToolOrWeapon(ToolType.AXE))
        {
            return IDLE;
        }

        if (job.getTree() == null)
        {
            return LUMBERJACK_SEARCHING_TREE;
        }
        return chopTree();
    }

    /**
     * Work on the tree. First find your way to the tree trunk. Then chop away and wait for saplings to drop then place a sapling if shouldReplant is true
     *
     * @return LUMBERJACK_GATHERING if tree is done
     */
    private IAIState chopTree()
    {
        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.chopping"));

        if (job.getTree().hasLogs() || checkedInHut)
        {
            if (!walkToTree(job.getTree().getStumpLocations().get(0)))
            {
                checkIfStuckOnLeaves();
                return getState();
            }
        }

        if (!job.getTree().hasLogs() && (!job.getTree().isSlimeTree() || !job.getTree().hasLeaves()))
        {
            if (hasNotDelayed(WAIT_BEFORE_SAPLING))
            {
                return getState();
            }

            final BuildingLumberjack building = getOwnBuilding(BuildingLumberjack.class);
            if (building.shouldReplant())
            {
                plantSapling();
            }
            else
            {
                job.setTree(null);
                checkedInHut = false;
            }
            incrementActionsDoneAndDecSaturation();
            workFrom = null;
            setDelay(TICKS_SECOND * GATHERING_DELAY);
            return LUMBERJACK_GATHERING;
        }

        if (isOnSapling())
        {
            @Nullable final BlockPos spawnPoint =
              Utils.scanForBlockNearPoint
                      (world, workFrom, 1, 1, 1, 3,
                        Blocks.AIR,
                        Blocks.CAVE_AIR,
                        Blocks.SNOW,
                        Blocks.TALL_GRASS);
            WorkerUtil.setSpawnPoint(spawnPoint, worker);
        }

        if (job.getTree().hasLogs())
        {
            //take first log from queue
            final BlockPos log = job.getTree().peekNextLog();

            if (job.getTree().isDynamicTree())
            {
                // Dynamic Trees handles drops/tool dmg upon tree break, so those are set to false here
                if (!mineBlock(log, workFrom, false, false, Compatibility.getDynamicTreeBreakAction(
                  world,
                  log,
                  worker.getHeldItem(Hand.MAIN_HAND),
                  worker.getPosition())))
                {
                    return getState();
                }
                // Successfully mined Dynamic tree, count as 6 actions done(1+5)
                for (int i = 0; i < 6; i++)
                {
                    this.incrementActionsDone();
                }
                // Wait 5 sec for falling trees(dyn tree feature)/drops
                setDelay(100);
            }
            else
            {
                if (!mineBlock(log, workFrom))
                {
                    return getState();
                }
            }
            job.getTree().pollNextLog();
            worker.decreaseSaturationForContinuousAction();
        }
        else if (job.getTree().hasLeaves() && job.getTree().isSlimeTree())
        {
            //take first leaf from queue
            final BlockPos leaf = job.getTree().peekNextLeaf();
            if (!mineBlock(leaf, workFrom))
            {
                return getState();
            }
            job.getTree().pollNextLeaf();
        }
        return getState();
    }

    /**
     * Walk to the current construction site.
     * <p>
     * Calculates and caches the position where to walk to.
     *
     * @param workAt block to work at.
     * @return true while walking to the site.
     */
    public boolean walkToTree(final BlockPos workAt)
    {
        if (workFrom == null || world.getBlockState(workFrom.up()).getBlock().isIn(BlockTags.SAPLINGS) || world.getBlockState(workFrom).getBlock().isIn(BlockTags.SAPLINGS))
        {
            workFrom = getWorkingPosition(workAt);
        }

        if (MathUtils.twoDimDistance(worker.getPosition(), workFrom) <= MIN_WORKING_RANGE)
        {
            return true;
        }

        if (pathToTree == null || !pathToTree.isInProgress())
        {
            pathToTree = ((MinecoloniesAdvancedPathNavigate) worker.getNavigator()).setPathJob(new PathJobMoveToWithPassable(world,
              AbstractPathJob.prepareStart(worker),
              workAt,
              SEARCH_RANGE,
              worker,
              this::isPassable), workAt, 1.0d);
        }

        return false;
    }

    /**
     * Checks whether the given blockstate is passable
     *
     * @param blockState to check
     * @return true if passable
     */
    private Boolean isPassable(final BlockState blockState)
    {
        return blockState.getMaterial() == Material.LEAVES;
    }

    /**
     * Check if distance to block changed and if we are not moving for too long, try to get unstuck.
     *
     * @return false
     */
    private boolean checkIfStuckOnLeaves()
    {
        if (!worker.getNavigator().noPath())
        {
            Path pathToTree = worker.getNavigator().getPath();
            if (pathToTree != null && pathToTree.getCurrentPathLength() > pathToTree.getCurrentPathIndex())
            {
                PathPoint next = pathToTree.getPathPointFromIndex(pathToTree.getCurrentPathIndex());
                BlockPos nextPos = new BlockPos(next.x, next.y, next.z);
                BlockPos nextPosUp = new BlockPos(next.x, next.y + 1, next.z);

                if (world.getBlockState(nextPos).getBlock().isIn(BlockTags.LEAVES))
                {
                    mineBlock(nextPos);
                }
                else if (world.getBlockState(nextPosUp).getBlock().isIn(BlockTags.LEAVES))
                {
                    mineBlock(nextPosUp);
                }
            }
        }
        return false;
    }

    /**
     * Place a sappling for the current tree.
     */
    private void plantSapling()
    {
        if (plantSapling(job.getTree().getLocation()))
        {
            job.setTree(null);
            checkedInHut = false;
        }
    }

    /**
     * Check if the worker is standing on a sapling.
     *
     * @return true if so.
     */
    private boolean isOnSapling()
    {
        return world.getBlockState(worker.getPosition()).getBlock().isIn(BlockTags.SAPLINGS)
                 || world.getBlockState(worker.getPosition().up()).getBlock().isIn(BlockTags.SAPLINGS)
                 || world.getBlockState(worker.getPosition().down()).getBlock().isIn(BlockTags.SAPLINGS);
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
        if (!(worldBlock instanceof AirBlock) && !(worldBlock.isIn(BlockTags.SAPLINGS)) && worldBlock != Blocks.SNOW)
        {
            return true;
        }

        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.planting"));

        final int saplingSlot = findSaplingSlot();
        final BlockPos dirtLocation = new BlockPos(location.getX(), location.getY() - 1, location.getZ());
        final Block dirt = world.getBlockState(dirtLocation).getBlock();

        if (saplingSlot != -1 && ((job.getTree().isSlimeTree() && Compatibility.isSlimeDirtOrGrass(dirt))
                                    || (!job.getTree().isSlimeTree() && !Compatibility.isSlimeDirtOrGrass(dirt))))
        {
            final ItemStack stack = getInventory().getStackInSlot(saplingSlot);
            worker.getCitizenItemHandler().setHeldItem(Hand.MAIN_HAND, saplingSlot);

            if (job.getTree().isDynamicTree() && Compatibility.isDynamicTreeSapling(stack))
            {
                Compatibility.plantDynamicSapling(world, location, stack);
                getInventory().extractItem(saplingSlot, 1, false);
                worker.swingArm(worker.getActiveHand());
                timeWaited = 0;
                incrementActionsDoneAndDecSaturation();
                setDelay(TIMEOUT_DELAY);
                return true;
            }
            else
            {
                final Block block = ((BlockItem) stack.getItem()).getBlock();
                placeSaplings(saplingSlot, stack, block);
                final SoundType soundType = block.getSoundType(world.getBlockState(location), world, location, worker);
                world.playSound(null,
                  this.worker.getPosition(),
                  soundType.getPlaceSound(),
                  SoundCategory.BLOCKS,
                  soundType.getVolume(),
                  soundType.getPitch());
            }

            worker.swingArm(worker.getActiveHand());
        }

        if (timeWaited >= MAX_WAITING_TIME / 2 && !checkedInHut && !walkToBuilding())
        {
            isInHut(job.getTree().getSapling());
            checkedInHut = true;
        }

        if (job.getTree().getStumpLocations().isEmpty() || timeWaited >= MAX_WAITING_TIME)
        {
            timeWaited = 0;
            incrementActionsDoneAndDecSaturation();
            setDelay(TIMEOUT_DELAY);
            return true;
        }
        timeWaited += 10;
        return false;
    }

    @Override
    public void fillItemsList()
    {
        if (job.getTree() != null)
        {
            searchForItems(new AxisAlignedBB(job.getTree().getLocation())
                             .expand(RANGE_HORIZONTAL_PICKUP, RANGE_VERTICAL_PICKUP, RANGE_HORIZONTAL_PICKUP)
                             .expand(-RANGE_HORIZONTAL_PICKUP, -RANGE_VERTICAL_PICKUP, -RANGE_HORIZONTAL_PICKUP));
        }
        else
        {
            searchForItems(worker.getBoundingBox()
                             .expand(RANGE_HORIZONTAL_PICKUP, RANGE_VERTICAL_PICKUP, RANGE_HORIZONTAL_PICKUP)
                             .expand(-RANGE_HORIZONTAL_PICKUP, -RANGE_VERTICAL_PICKUP, -RANGE_HORIZONTAL_PICKUP));
        }
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
                    if (world.getBlockState(pos).getBlock().isIn(BlockTags.LEAVES))
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
        for (int slot = 0; slot < getInventory().getSlots(); slot++)
        {
            final ItemStack stack = getInventory().getStackInSlot(slot);
            if (isCorrectSapling(stack))
            {
                return slot;
            }
        }
        return -1;
    }

    //todo: we need to use a different way to get Metadata, check other Mods like BOP for compatibility then
    @SuppressWarnings("deprecation")
    private void placeSaplings(final int saplingSlot, @NotNull final ItemStack stack, @NotNull final Block block)
    {
        while (!job.getTree().getStumpLocations().isEmpty())
        {
            final BlockPos pos = job.getTree().getStumpLocations().get(0);
            if ((world.setBlockState(pos, block.getDefaultState()) && getInventory().getStackInSlot(saplingSlot) != null)
                  || Objects.equals(world.getBlockState(pos), block.getDefaultState()))
            {

                getInventory().extractItem(saplingSlot, 1, false);
                job.getTree().removeStump(pos);
            }
            else
            {
                return;
            }
        }
    }

    /**
     * Checks if this is the correct Sapling.
     *
     * @param stack incoming stack.
     * @return true if so.
     */
    private boolean isCorrectSapling(final ItemStack stack)
    {
        if (!ItemStackUtils.isStackSapling(stack))
        {
            return false;
        }

        if (ItemStackUtils.isEmpty(job.getTree().getSapling()))
        {
            return true;
        }
        else
        {
            return job.getTree().getSapling().isItemEqual(stack);
        }
    }

    /**
     * Checks if the lumberjack found items on the ground, if yes collect them, if not search for them.
     *
     * @return LUMBERJACK_GATHERING as long as gathering takes.
     */
    private IAIState gathering()
    {
        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.gathering"));

        if (getItemsForPickUp() == null)
        {
            fillItemsList();
        }

        if (getItemsForPickUp() != null && !getItemsForPickUp().isEmpty())
        {
            gatherItems();
            return getState();
        }
        resetGatheringItems();
        return LUMBERJACK_SEARCHING_TREE;
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return MAX_BLOCKS_MINED;
    }

    @Override
    protected void updateRenderMetaData()
    {
        worker.setRenderMetadata(hasLogs() ? RENDER_META_LOGS : "");
    }

    @Override
    public BlockPos getWorkingPosition(final BlockPos targetPosition)
    {
        return getWorkingPosition(2, targetPosition, 0);
    }

    /**
     * Checks if the lumberjack has logs in it's inventory.
     *
     * @return true if he has logs.
     */
    private boolean hasLogs()
    {
        return InventoryUtils.hasItemInItemHandler(getInventory(), this::isStackLog);
    }
}
