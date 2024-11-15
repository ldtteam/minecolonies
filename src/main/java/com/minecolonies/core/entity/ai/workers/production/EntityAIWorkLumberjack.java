package com.minecolonies.core.entity.ai.workers.production;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.ColonyConstants;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.buildings.modules.ItemListModule;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingLumberjack;
import com.minecolonies.core.colony.jobs.JobLumberjack;
import com.minecolonies.core.entity.ai.workers.crafting.AbstractEntityAICrafting;
import com.minecolonies.core.entity.ai.workers.util.Tree;
import com.minecolonies.core.entity.pathfinding.PathfindingUtils;
import com.minecolonies.core.entity.pathfinding.navigation.MinecoloniesAdvancedPathNavigate;
import com.minecolonies.core.entity.pathfinding.pathjobs.PathJobMoveToWithPassable;
import com.minecolonies.core.entity.pathfinding.pathresults.PathResult;
import com.minecolonies.core.entity.pathfinding.pathresults.TreePathResult;
import com.minecolonies.core.util.WorkerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.items.ModTags.fungi;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.StatisticsConstants.ITEM_OBTAINED;
import static com.minecolonies.api.util.constant.StatisticsConstants.TREE_CUT;
import static com.minecolonies.core.colony.buildings.modules.BuildingModules.STATS_MODULE;

/**
 * The lumberjack AI class.
 */
public class EntityAIWorkLumberjack extends AbstractEntityAICrafting<JobLumberjack, BuildingLumberjack>
{
    /**
     * The render name to render logs.
     */
    public static final String RENDER_META_LOGS = "logs";

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
    public static final String SAPLINGS_LIST = "saplings";

    /**
     * Vertical range in which the worker picks up items.
     */
    public static final float RANGE_VERTICAL_PICKUP = 2.0F;

    /**
     * Horizontal range in which the worker picks up items.
     */
    public static final float RANGE_HORIZONTAL_PICKUP = 5.0F;

    /**
     * The minimum range the lumberjack has to reach in order to construct or clear.
     */
    private static final int MIN_WORKING_RANGE   = 2;
    /**
     * Time in ticks to wait before placing a sapling. Is used to collect falling saplings from the ground.
     */
    private static final int WAIT_BEFORE_SAPLING = 50;
    /**
     * Time in ticks to wait before placing a sapling. Is used to collect falling saplings from the ground.
     */
    private static final int MAX_WAITING_TIME    = 50;

    /**
     * Number of ticks to wait for tree.
     */
    private static final int TIMEOUT_DELAY      = 10;
    /**
     * Time in ticks to wait before rechecking if there are trees in the range of the lumberjack.
     */
    private static final int WAIT_BEFORE_SEARCH = 400;

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
    private static final int GATHERING_DELAY = 3;

    /**
     * Searching icon
     */
    private final static VisibleCitizenStatus SEARCH =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/work/lumberjack_search.png"), "com.minecolonies.gui.visiblestatus.lumberjack_search");

    /**
     * Xp bonus per finished tree
     */
    private static final double XP_PER_TREE = 1;

    /**
     * Position where the Builders constructs from.
     */
    private BlockPos workFrom;

    /**
     * The time in ticks the lumberjack has waited already. Directly connected with the MAX_WAITING_TIME.
     */
    private int timeWaited = 0;

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
    private PathResult<?> pathToTree;

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
          new AITarget(LUMBERJACK_NO_TREES_FOUND, this::waitBeforeCheckingAgain, TICKS_SECOND),
          new AITarget(LUMBERJACK_GATHERING_2, this::gathering2, TICKS_SECOND)
        );
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class<BuildingLumberjack> getExpectedBuildingClass()
    {
        return BuildingLumberjack.class;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The lumberjack is a special worker. In their decision state, they will try to add lumberjack cycles If there's nothing left to craft, they will proceed with woodworking
     * </p>
     */
    @Override
    protected IAIState decide()
    {
        if (checkIfStuck())
        {
            tryUnstuck();
            return getState();
        }

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

        return getNextCraftingState();
    }

    /**
     * Checks if a stack is a type of log.
     *
     * @param stack the stack to check.
     * @return true if it is a log type.
     */
    private boolean isStackLog(@Nullable final ItemStack stack)
    {
        return !ItemStackUtils.isEmpty(stack) && stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock().defaultBlockState().is(BlockTags.LOGS);
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
        if (checkForToolOrWeapon(ModEquipmentTypes.axe.get()) || checkForToolOrWeapon(building.getSetting(AbstractBuilding.USE_SHEARS).getValue() ? ModEquipmentTypes.shears.get() : ModEquipmentTypes.hoe.get()))
        {
            // Reset everything, maybe there are new crafting requests
            return START_WORKING;
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
        pathResult = null;

        if (hasNotDelayed(WAIT_BEFORE_SEARCH))
        {
            return getState();
        }

        // Check again for saplings
        resetGatheringItems();
        return LUMBERJACK_GATHERING_2;
    }

    /**
     * After we ran out of trees, and waited a bit, double-check if there are any saplings to gather
     * anywhere within our restriction zone.
     */
    private IAIState gathering2()
    {
        if (building.shouldRestrict())
        {
            if (getItemsForPickUp() == null)
            {
                // search for interesting items in our restriction zone, if we ran out of trees
                searchForItems(new AABB(building.getStartRestriction(), building.getEndRestriction())
                                 .inflate(RANGE_HORIZONTAL_PICKUP, RANGE_VERTICAL_PICKUP, RANGE_HORIZONTAL_PICKUP));
            }

            if (getItemsForPickUp() != null && !getItemsForPickUp().isEmpty())
            {
                gatherItems();
                return getState();
            }
        }

        // Reset everything, maybe there are new crafting requests
        resetGatheringItems();
        if (ColonyConstants.rand.nextInt(100) <= 10)
        {
            return START_WORKING;
        }
        else
        {
            return LUMBERJACK_SEARCHING_TREE;
        }
    }

    @Override
    protected boolean isItemWorthPickingUp(final ItemStack stack)
    {
        if (getState() == LUMBERJACK_GATHERING_2)
        {
            // we're only interested in saplings at this point
            return stack.is(ItemTags.SAPLINGS) || stack.is(Tags.Items.MUSHROOMS) || stack.is(fungi);
        }

        return super.isItemWorthPickingUp(stack);
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

            return findTree();
        }

        worker.getCitizenData().setVisibleStatus(VisibleCitizenStatus.WORKING);
        return LUMBERJACK_CHOP_TREE;
    }

    /**
     * Search for a tree.
     *
     * @return LUMBERJACK_GATHERING if job was canceled.
     */
    private IAIState findTree()
    {
        worker.getCitizenData().setVisibleStatus(SEARCH);

        if (pathResult != null && pathResult.isComputing())
        {
            return getState();
        }
        if (pathResult == null)
        {
            if (building.shouldRestrict())
            {
                final BlockPos startPos = building.getStartRestriction();
                final BlockPos endPos = building.getEndRestriction();

                pathResult = worker.getNavigation()
                               .moveToTree(startPos,
                                 endPos,
                                 1.0D,
                                 building.getModuleMatching(ItemListModule.class, m -> m.getId().equals(SAPLINGS_LIST)).getList(),
                                 building.getSetting(BuildingLumberjack.DYNAMIC_TREES_SIZE).getValue(),
                                 worker.getCitizenColonyHandler().getColonyOrRegister());
            }
            else
            {
                pathResult = worker.getNavigation()
                               .moveToTree(SEARCH_RANGE + searchIncrement,
                                 1.0D,
                                 building.getModuleMatching(ItemListModule.class, m -> m.getId().equals(SAPLINGS_LIST)).getList(),
                                 building.getSetting(BuildingLumberjack.DYNAMIC_TREES_SIZE).getValue(),
                                 worker.getCitizenColonyHandler().getColonyOrRegister());
            }
            return getState();
        }
        if (pathResult.isDone())
        {
            return setNewTree(building);
        }

        // None of the above yielded a result, report no trees found.
        return LUMBERJACK_NO_TREES_FOUND;
    }

    private IAIState setNewTree(final BuildingLumberjack building)
    {
        if (pathResult.treeLocation == null)
        {
            if (!building.shouldRestrict() && searchIncrement + SEARCH_RANGE <= SEARCH_LIMIT)
            {
                searchIncrement += SEARCH_INCREMENT;
                setDelay(WAIT_BEFORE_INCREMENT);
            }
            else
            {
                return LUMBERJACK_NO_TREES_FOUND;
            }
        }
        else
        {
            job.setTree(new Tree(world, pathResult.treeLocation, building.shouldRestrict() ? null : building.getColony()));

            // Check if tree creation was successful
            if (job.getTree().isTree())
            {
                job.getTree().findLogs(world, building.shouldRestrict() ? null : building.getColony());
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
        if (checkForToolOrWeapon(ModEquipmentTypes.axe.get()))
        {
            return IDLE;
        }

        if (job.getTree() == null)
        {
            return LUMBERJACK_SEARCHING_TREE;
        }
        return chopTree();
    }

    //TODO: On walking to the zone(no tree found) leaves are not getting broken
    /**
     * Work on the tree. First find your way to the tree trunk. Then chop away and wait for saplings to drop then place a sapling if shouldReplant is true
     *
     * @return LUMBERJACK_GATHERING if tree is done
     */
    private IAIState chopTree()
    {
        final boolean shouldBreakLeaves = building.shouldDefoliate() || job.getTree().isNetherTree();

        if (building.shouldRestrict() && !BlockPosUtil.isInArea(building.getStartRestriction(), building.getEndRestriction(), job.getTree().getLocation()))
        {
            job.setTree(null);
            pathResult = null;
            return START_WORKING;
        }

        if (job.getTree().hasLogs() || (shouldBreakLeaves && job.getTree().hasLeaves()) || checkedInHut)
        {
            if (!walkToTree(job.getTree().getStumpLocations().isEmpty() ? job.getTree().getLocation() : job.getTree().getStumpLocations().get(0)))
            {
                if (checkIfStuck())
                {
                    tryUnstuck();
                }
                return getState();
            }
        }

        if (!job.getTree().hasLogs() && (!shouldBreakLeaves || !job.getTree().hasLeaves()))
        {
            if (hasNotDelayed(WAIT_BEFORE_SAPLING))
            {
                return getState();
            }

            if (building.shouldReplant())
            {
                plantSapling();
            }
            else
            {
                job.setTree(null);
                pathResult = null;
                checkedInHut = false;
            }

            building.getColony().getStatisticsManager().increment(TREE_CUT, building.getColony().getDay());
            worker.getCitizenExperienceHandler().addExperience(XP_PER_TREE);
            incrementActionsDoneAndDecSaturation();
            workFrom = null;
            setDelay(TICKS_SECOND * GATHERING_DELAY);
            return LUMBERJACK_GATHERING;
        }

        if (isOnSapling())
        {
            @Nullable final BlockPos spawnPoint = EntityUtils.getSpawnPoint(world, workFrom);
            if (spawnPoint != null)
            {
                WorkerUtil.setSpawnPoint(spawnPoint, worker);
            }
        }

        if (job.getTree().hasLeaves() && shouldBreakLeaves)
        {
            final BlockPos leaf = job.getTree().peekNextLeaf();
            if (!mineBlock(leaf, workFrom))
            {
                return getState();
            }
            job.getTree().pollNextLeaf();
        }
        else if (job.getTree().hasLogs())
        {
            //take first log from queue
            final BlockPos log = job.getTree().peekNextLog();

            if (job.getTree().isDynamicTree())
            {
                // Dynamic Trees handles drops/tool dmg upon tree break, so those are set to false here
                if (!mineBlock(log, workFrom, false, false, Compatibility.getDynamicTreeBreakAction(
                  world,
                  log,
                  worker.getItemInHand(InteractionHand.MAIN_HAND),
                  worker.blockPosition())))
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
        return getState();
    }

    @Override
    public void onBlockDropReception(final List<ItemStack> blockDrops)
    {
        super.onBlockDropReception(blockDrops);
        for (final ItemStack stack : blockDrops)
        {
            building.getModule(STATS_MODULE).incrementBy(ITEM_OBTAINED + ";" + stack.getItem().getDescriptionId(), stack.getCount());
        }
    }

    /**
     * Drop fungus instead of wart block
     *
     * @param drops the drops.
     * @return the list of additional drops.
     */
    @Override
    protected List<ItemStack> increaseBlockDrops(final List<ItemStack> drops)
    {
        final List<ItemStack> newDrops = new ArrayList<>();
        for (final ItemStack stack : drops)
        {
            if (world.getRandom().nextInt(100) > 95)
            {
                if (stack.is(ItemTags.WART_BLOCKS))
                {
                    final Block wartBlock = ((BlockItem) stack.getItem()).getBlock();
                    final ItemStack fungus = IColonyManager.getInstance().getCompatibilityManager().getSaplingForLeaf(wartBlock);
                    if (fungus != null)
                    {
                        newDrops.add(fungus);
                    }
                }
            }
        }
        if (newDrops.isEmpty())
        {
            return drops;
        }
        return newDrops;
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
        if (workFrom == null || world.getBlockState(workFrom.above()).is(BlockTags.SAPLINGS) || world.getBlockState(workFrom).is(BlockTags.SAPLINGS))
        {
            workFrom = getWorkingPosition(workAt);
        }

        if (MathUtils.twoDimDistance(worker.blockPosition(), workFrom) <= MIN_WORKING_RANGE)
        {
            return true;
        }

        if (pathToTree == null || !pathToTree.isInProgress())
        {
            pathToTree = ((MinecoloniesAdvancedPathNavigate) worker.getNavigation()).setPathJob(new PathJobMoveToWithPassable(world,
              PathfindingUtils.prepareStart(worker),
              workFrom,
              SEARCH_RANGE,
              worker,
              this::isPassable), workFrom, 1.0d, true);
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
        return blockState.is(BlockTags.LEAVES) || blockState.is(ModTags.hugeMushroomBlocks);
    }

    /**
     * Check if distance to block changed and if we are not moving for too long, try to get unstuck.
     *
     * @return false
     */
    private boolean checkIfStuck()
    {
        if (!worker.getNavigation().isDone())
        {
            final Path path = worker.getNavigation().getPath();
            if (path != null)
            {
                if (path.getNodeCount() > path.getNextNodeIndex())
                {
                    return true;
                }
                return path.getNodeCount() == 0;
            }
            else
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Mines leaves on path and above.
     */
    private void tryUnstuck()
    {
        if (!worker.getNavigation().isDone())
        {
            Path path = worker.getNavigation().getPath();
            if (path != null)
            {
                // Unstuck with path
                final List<BlockPos> checkPositions = new ArrayList<>();
                Node next = path.getNode(Math.min(path.getNextNodeIndex() + 1, path.getNodeCount() - 1));

                // Blocks in front of the worker
                for (int i = 0; i < 2; i++)
                {
                    checkPositions.add(new BlockPos(next.x, next.y + i, next.z));
                }
                if (next.cameFrom != null)
                {
                    next = next.cameFrom;
                    for (int i = 0; i < 2; i++)
                    {
                        checkPositions.add(new BlockPos(next.x, next.y + i, next.z));
                    }
                    if (next.cameFrom != null)
                    {
                        next = next.cameFrom;
                        for (int i = 0; i < 2; i++)
                        {
                            checkPositions.add(new BlockPos(next.x, next.y + i, next.z));
                        }
                    }
                }

                mineIfEqualsBlockTag(checkPositions, BlockTags.LEAVES);
                mineIfEqualsBlockTag(checkPositions, ModTags.hugeMushroomBlocks);
                return;
            }
        }

        // General unstuck
        ArrayList<BlockPos> checkPositions = new ArrayList<>();

        for (Direction direction : Direction.Plane.HORIZONTAL)
        {
            checkPositions.add(new BlockPos(worker.blockPosition().getX(), worker.blockPosition().getY(), worker.blockPosition().getZ()).relative(direction));
            checkPositions.add(new BlockPos(worker.blockPosition().getX(), worker.blockPosition().getY() + 1, worker.blockPosition().getZ()).relative(direction));
        }

        mineIfEqualsBlockTag(checkPositions, BlockTags.LEAVES);
        mineIfEqualsBlockTag(checkPositions, ModTags.hugeMushroomBlocks);
    }

    /**
     * Checks blocks for tag and mines the first it fines if its the same
     *
     * @param blockPositions block positions
     * @param tag            tag to check
     */
    private boolean mineIfEqualsBlockTag(List<BlockPos> blockPositions, TagKey<Block> tag)
    {
        for (BlockPos currentPos : blockPositions)
        {
            if (MineColonies.getConfig().getServer().pathfindingDebugVerbosity.get() > 0)
            {
                Log.getLogger()
                  .info(String.format("Check Leaves Pos(%d, %d, %d) is %s: %s",
                    currentPos.getX(),
                    currentPos.getY(),
                    currentPos.getZ(),
                    tag.toString(),
                    world.getBlockState(currentPos).is(tag)));
            }
            if (world.getBlockState(currentPos).is(tag))
            {
                mineBlock(currentPos);
                return true;
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
            pathResult = null;
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
        return world.getBlockState(worker.blockPosition()).is(BlockTags.SAPLINGS)
                 || world.getBlockState(worker.blockPosition().above()).is(BlockTags.SAPLINGS)
                 || world.getBlockState(worker.blockPosition().below()).is(BlockTags.SAPLINGS);
    }

    /**
     * Plant a sapling at said location.
     *
     * @param location the location to plant the sapling at
     * @return true if a sapling was planted
     */
    private boolean plantSapling(@NotNull final BlockPos location)
    {
        final BlockState worldState = world.getBlockState(location);
        final Block worldBlock = worldState.getBlock();
        if (!(worldBlock instanceof AirBlock) && !(worldState.is(BlockTags.SAPLINGS)) && worldBlock != Blocks.SNOW)
        {
            return true;
        }

        final int saplingSlot = findSaplingSlot();

        if (saplingSlot != -1)
        {
            final ItemStack stack = getInventory().getStackInSlot(saplingSlot);
            worker.getCitizenItemHandler().setHeldItem(InteractionHand.MAIN_HAND, saplingSlot);

            if (job.getTree().isDynamicTree() && Compatibility.isDynamicTreeSapling(stack))
            {
                Compatibility.plantDynamicSapling(world, location, stack);
                getInventory().extractItem(saplingSlot, 1, false);
                worker.swing(worker.getUsedItemHand());
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
                  this.worker.blockPosition(),
                  soundType.getPlaceSound(),
                  SoundSource.BLOCKS, 
                  (soundType.getVolume() + 1.0F) * 0.5F,
                  soundType.getPitch() * 0.8F);
            }

            worker.swing(worker.getUsedItemHand());
        }

        if (timeWaited >= MAX_WAITING_TIME / 2 && !checkedInHut && !walkToBuilding())
        {
            checkAndTransferFromHut(job.getTree().getSapling());
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
            searchForItems(new AABB(job.getTree().getLocation())
                             .expandTowards(RANGE_HORIZONTAL_PICKUP, RANGE_VERTICAL_PICKUP, RANGE_HORIZONTAL_PICKUP)
                             .expandTowards(-RANGE_HORIZONTAL_PICKUP, -RANGE_VERTICAL_PICKUP, -RANGE_HORIZONTAL_PICKUP));
        }
        else
        {
            searchForItems(worker.getBoundingBox()
                             .expandTowards(RANGE_HORIZONTAL_PICKUP, RANGE_VERTICAL_PICKUP, RANGE_HORIZONTAL_PICKUP)
                             .expandTowards(-RANGE_HORIZONTAL_PICKUP, -RANGE_VERTICAL_PICKUP, -RANGE_HORIZONTAL_PICKUP));
        }
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

    /**
     * Place saplings to the stump locations
     *
     * @param saplingSlot sapling item slot
     * @param stack
     * @param block       sapling block
     */
    private void placeSaplings(final int saplingSlot, @NotNull final ItemStack stack, @NotNull final Block block)
    {
        while (!job.getTree().getStumpLocations().isEmpty())
        {
            final BlockPos pos = job.getTree().getStumpLocations().get(0);
            final ItemStack sapling = getInventory().getStackInSlot(saplingSlot);
            if (sapling.is(Tags.Items.MUSHROOMS))
            {
                building.addNetherTree(pos);
            }
            else if (sapling.is(fungi))
            {
                if (world.getBlockState(pos.below()).getBlock() instanceof NetherrackBlock)
                {
                    final Block new_block = sapling.is(Items.WARPED_FUNGUS) ? Blocks.WARPED_NYLIUM : Blocks.CRIMSON_NYLIUM;
                    world.setBlockAndUpdate(pos.below(), new_block.defaultBlockState());
                    building.addNetherTree(pos);
                }
            }

            if (!(block instanceof IPlantable && block.canSustainPlant(world.getBlockState(pos.below()), world, pos.below(), Direction.UP, (IPlantable) block))
                  || Objects.equals(world.getBlockState(pos), block.defaultBlockState()))
            {
                job.getTree().removeStump(pos);
                continue;
            }

            if (world.setBlockAndUpdate(pos, block.defaultBlockState()) && !ItemStackUtils.isEmpty(getInventory().getStackInSlot(saplingSlot)))
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
            return false;
        }
        else
        {
            return ItemStack.isSameItem(job.getTree().getSapling(), stack);
        }
    }

    /**
     * Checks if the lumberjack found items on the ground, if yes collect them, if not search for them.
     *
     * @return LUMBERJACK_GATHERING as long as gathering takes.
     */
    private IAIState gathering()
    {

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
