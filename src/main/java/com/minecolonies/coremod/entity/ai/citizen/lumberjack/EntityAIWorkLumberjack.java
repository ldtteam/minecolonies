package com.minecolonies.coremod.entity.ai.citizen.lumberjack;

import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingLumberjack;
import com.minecolonies.coremod.colony.jobs.JobLumberjack;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.coremod.entity.ai.statemachine.AITarget;
import com.minecolonies.coremod.entity.ai.statemachine.states.IAIState;
import com.minecolonies.coremod.entity.pathfinding.PathJobFindTree;
import com.minecolonies.coremod.util.WorkerUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.SoundType;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.minecolonies.coremod.entity.ai.statemachine.states.AIWorkerState.*;

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
    public static final int SEARCH_RANGE = 50;

    /**
     * If no trees are found, increment the range.
     */
    private static final int    SEARCH_INCREMENT       = 5;
    /**
     * If this limit is reached, no trees are found.
     */
    private static final int    SEARCH_LIMIT           = 150;

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
     * Number of ticks until he gives up destroying leaves
     * and walks a bit back to try a new path.
     */
    private static final int    WALKING_BACK_WAIT_TIME = 60;
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
    private static final int    MIN_WORKING_RANGE      = 1;
    /**
     * Time in ticks to wait before placing a sapling.
     * Is used to collect falling saplings from the ground.
     */
    private static final int    WAIT_BEFORE_SAPLING    = 50;
    /**
     * Time in ticks to wait before placing a sapling.
     * Is used to collect falling saplings from the ground.
     */
    private static final int    MAX_WAITING_TIME       = 50;

    /**
     * Number of ticks to wait for tree.
     */
    private static final int TIMEOUT_DELAY      = 10;
    private static final int LEAVES_RADIUS      = 3;
    /**
     * Time in ticks to wait before rechecking
     * if there are trees in the
     * range of the lumberjack.
     */
    private static final int WAIT_BEFORE_SEARCH = 100;

    /**
     * Time in ticks before incrementing the search radius.
     */
    private static final int WAIT_BEFORE_INCREMENT = 20;

    /**
     * How often should strength factor into the lumberjacks skill modifier.
     */
    private static final int STRENGTH_MULTIPLIER = 2;

    /**
     * How often should charisma factor into the lumberjacks skill modifier.
     */
    private static final int CHARISMA_MULTIPLIER = 1;

    /**
     * Return to chest after half a stack.
     */
    private static final int MAX_BLOCKS_MINED = 32;

    /**
     * Position where the Builders constructs from.
     */
    private BlockPos workFrom;
    /**
     * The time in ticks the lumberjack has waited already.
     * Directly connected with the MAX_WAITING_TIME.
     */
    private int      timeWaited = 0;

    /**
     * Number of ticks the lumberjack is standing still.
     */
    private int stillTicks = 0;

    /**
     * Used to store the walk distance
     * to check if the lumberjack is still walking.
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
    private PathJobFindTree.TreePathResult pathResult;
    /**
     * A counter by how much the tree search radius
     * has been increased by now.
     */
    private int                            searchIncrement = 0;

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
        worker.getCitizenExperienceHandler().setSkillModifier(STRENGTH_MULTIPLIER * worker.getCitizenData().getStrength()
                                                                + CHARISMA_MULTIPLIER * worker.getCitizenData().getCharisma());
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class getExpectedBuildingClass()
    {
        return BuildingLumberjack.class;
    }

    /**
     * Checks if a stack is a type of log.
     *
     * @param stack the stack to check.
     * @return true if it is a log type.
     */
    private boolean isStackLog(@Nullable final ItemStack stack)
    {
        return !ItemStackUtils.isEmpty(stack) && stack.getItem() instanceof ItemBlock && ((ItemBlock) stack.getItem()).getBlock().isWood(world, new BlockPos(0, 0, 0));
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
    private IAIState waitBeforeCheckingAgain()
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
     * @return next IAIState
     */
    private IAIState findTrees()
    {
        if (job.tree == null)
        {
            worker.getCitizenStatusHandler().setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.searchingtree"));

            return findTree();
        }
        return LUMBERJACK_CHOP_TREE;
    }

    /**
     * Search for a tree.
     *
     * @return LUMBERJACK_GATHERING if job was canceled.
     */
    private IAIState findTree()
    {
        final AbstractBuilding building = getOwnBuilding();

        // Waiting for current path to finish
        if (pathResult != null && pathResult.isInProgress())
        {
            return getState();
        }


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
                pathResult = worker.getNavigator().moveToTree(SEARCH_RANGE + searchIncrement, 1.0D, copy.getOrDefault(SAPLINGS_LIST, Collections.emptyList()), worker.getCitizenColonyHandler().getColony());
            }

            // Delay between area searches
            setDelay(100);
            return getState();
        }
        if (pathResult.isPathReachingDestination())
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
            job.tree = new Tree(world, pathResult.treeLocation);

            // Check if tree creation was successful
            if (job.tree.isTree())
            {
                job.tree.findLogs(world);
            }
            else
            {
                job.tree = null;
            }
        }
        pathResult = null;

        return getState();
    }

    /**
     * Again checks if all preconditions are given to execute chopping.
     * If yes go chopping, else return to previous AIStates.
     *
     * @return next IAIState
     */
    private IAIState chopWood()
    {
        if (checkForToolOrWeapon(ToolType.AXE))
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
     * then place a sapling if shouldReplant is true
     *
     * @return LUMBERJACK_GATHERING if tree is done
     */
    private IAIState chopTree()
    {
        worker.getCitizenStatusHandler().setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.chopping"));

        if (job.tree.hasLogs() || checkedInHut)
        {
            final BlockPos location = job.tree.getLocation();
            if (!walkToTree(job.tree.getStumpLocations().get(0)))
            {
                checkIfStuckOnLeaves(location);
                return getState();
            }
        }

        if (!job.tree.hasLogs() && (!job.tree.isSlimeTree() || !job.tree.hasLeaves()))
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
                job.tree = null;
                checkedInHut = false;
            }
            incrementActionsDoneAndDecSaturation();
            building.getColony().getStatsManager().incrementStatistic("trees");
            workFrom = null;
            return LUMBERJACK_GATHERING;
        }

        if (isOnSapling())
        {
            @Nullable final BlockPos spawnPoint =
              Utils.scanForBlockNearPoint
                      (world, workFrom, 1, 1, 1, 3,
                        Blocks.AIR,
                        Blocks.SNOW_LAYER,
                        Blocks.TALLGRASS,
                        Blocks.RED_FLOWER,
                        Blocks.YELLOW_FLOWER);
            WorkerUtil.setSpawnPoint(spawnPoint, worker);
        }

        if (job.tree.hasLogs())
        {
            //take first log from queue
            final BlockPos log = job.tree.peekNextLog();

            if (job.tree.isDynamicTree())
            {
                // Dynamic Trees handles drops/tool dmg upon tree break, so those are set to false here
                if (!mineBlock(log, workFrom, false, false, Compatibility.getDynamicTreeBreakAction(
                  world,
                  log,
                  worker.getHeldItem(EnumHand.MAIN_HAND),
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
            job.tree.pollNextLog();
            worker.decreaseSaturationForContinuousAction();
        }
        else if (job.tree.hasLeaves() && job.tree.isSlimeTree())
        {
            //take first leaf from queue
            final BlockPos leaf = job.tree.peekNextLeaf();
            if (!mineBlock(leaf, workFrom))
            {
                return getState();
            }
            job.tree.pollNextLeaf();
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
        if (workFrom == null || world.getBlockState(workFrom.up()).getBlock() == Blocks.SAPLING || world.getBlockState(workFrom).getBlock() == Blocks.SAPLING)
        {
            workFrom = getWorkingPosition(workAt);
        }

        return worker.isWorkerAtSiteWithMove(workFrom, STANDARD_WORKING_RANGE) || MathUtils.twoDimDistance(worker.getPosition(), workFrom) <= MIN_WORKING_RANGE;
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
        worker.getCitizenStatusHandler().setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.stuckinleaves"));

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
        return world.getBlockState(worker.getPosition()).getBlock() == Blocks.SAPLING
                 || world.getBlockState(worker.getPosition().up()).getBlock() == Blocks.SAPLING
                 || world.getBlockState(worker.getPosition().down()).getBlock() == Blocks.SAPLING;
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
        if (!mineBlock(nextLeaves, workFrom))
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
        if (worldBlock != Blocks.AIR && !(worldBlock instanceof BlockSapling) && worldBlock != Blocks.SNOW_LAYER)
        {
            return true;
        }

        worker.getCitizenStatusHandler().setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.planting"));

        final int saplingSlot = findSaplingSlot();
        final BlockPos dirtLocation = new BlockPos(location.getX(), location.getY() - 1, location.getZ());
        final Block dirt = world.getBlockState(dirtLocation).getBlock();

        if (saplingSlot != -1 && ((job.tree.isSlimeTree() && Compatibility.isSlimeDirtOrGrass(dirt))
                                    || (!job.tree.isSlimeTree() && !Compatibility.isSlimeDirtOrGrass(dirt))))
        {
            final ItemStack stack = getInventory().getStackInSlot(saplingSlot);
            worker.getCitizenItemHandler().setHeldItem(EnumHand.MAIN_HAND, saplingSlot);

            if (job.tree.isDynamicTree() && Compatibility.isDynamicTreeSapling(stack))
            {
                Compatibility.plantDynamicSapling(world, location, stack);
                new InvWrapper(getInventory()).extractItem(saplingSlot, 1, false);
                worker.swingArm(worker.getActiveHand());
                timeWaited = 0;
                incrementActionsDoneAndDecSaturation();
                setDelay(TIMEOUT_DELAY);
                return true;
            }
            else
            {
                final Block block = ((ItemBlock) stack.getItem()).getBlock();
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
            this.getOwnBuilding().getColony().getStatsManager().incrementStatistic("saplings");
        }

        if (timeWaited >= MAX_WAITING_TIME / 2 && !checkedInHut && !walkToBuilding())
        {
            isInHut(job.tree.getSapling());
            checkedInHut = true;
        }

        if (job.tree.getStumpLocations().isEmpty() || timeWaited >= MAX_WAITING_TIME)
        {
            timeWaited = 0;
            incrementActionsDoneAndDecSaturation();
            setDelay(TIMEOUT_DELAY);
            return true;
        }
        timeWaited += 10;
        return false;
    }

    /**
     * Fill the list of the item positions to gather.
     */
    @Override
    public void fillItemsList()
    {
        if (job.tree != null)
        {
            searchForItems(new AxisAlignedBB(job.tree.getLocation())
                             .expand(RANGE_HORIZONTAL_PICKUP, RANGE_VERTICAL_PICKUP, RANGE_HORIZONTAL_PICKUP)
                             .expand(-RANGE_HORIZONTAL_PICKUP, -RANGE_VERTICAL_PICKUP, -RANGE_HORIZONTAL_PICKUP));
        }
        else
        {
            searchForItems(worker.getEntityBoundingBox()
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
        for (int slot = 0; slot < new InvWrapper(getInventory()).getSlots(); slot++)
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
        while (!job.tree.getStumpLocations().isEmpty())
        {
            final BlockPos pos = job.tree.getStumpLocations().get(0);
            if ((BlockPosUtil.setBlock(world, pos, block.getStateFromMeta(stack.getMetadata()), 0x02) && getInventory().getStackInSlot(saplingSlot) != null)
                  || Objects.equals(world.getBlockState(pos), block.getStateFromMeta(stack.getMetadata())))
            {

                new InvWrapper(getInventory()).extractItem(saplingSlot, 1, false);
                job.tree.removeStump(pos);
            }
            else
            {
                return;
            }
        }
    }

    /**
     * Checks if this is the correct Sapling.
     * @param stack incoming stack.
     * @return true if so.
     */
    private boolean isCorrectSapling(final ItemStack stack)
    {
        if (!ItemStackUtils.isStackSapling(stack))
        {
            return false;
        }

        if (ItemStackUtils.isEmpty(job.tree.getSapling()))
        {
            return true;
        }
        else
        {
            return job.tree.getSapling().isItemEqual(stack);
        }
    }

    /**
     * Checks if the lumberjack found items on the ground,
     * if yes collect them, if not search for them.
     *
     * @return LUMBERJACK_GATHERING as long as gathering takes.
     */
    private IAIState gathering()
    {
        worker.getCitizenStatusHandler().setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.gathering"));

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
     * Calculates the working position.
     * <p>
     * Takes a min distance from width and length.
     * <p>
     * Then finds the floor level at that distance and then check if it does contain two air levels.
     *
     * @param targetPosition the position to work at.
     * @return BlockPos position to work from.
     */
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
        return InventoryUtils.hasItemInItemHandler(new InvWrapper(getInventory()), this::isStackLog);
    }
}
