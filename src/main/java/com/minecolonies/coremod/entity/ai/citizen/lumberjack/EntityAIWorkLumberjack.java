package com.minecolonies.coremod.entity.ai.citizen.lumberjack;

import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.entity.pathfinding.PathResult;
import com.minecolonies.api.entity.pathfinding.TreePathResult;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.modules.ItemListModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingLumberjack;
import com.minecolonies.coremod.colony.jobs.JobLumberjack;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAICrafting;
import com.minecolonies.coremod.entity.pathfinding.MinecoloniesAdvancedPathNavigate;
import com.minecolonies.coremod.entity.pathfinding.pathjobs.AbstractPathJob;
import com.minecolonies.coremod.entity.pathfinding.pathjobs.PathJobMoveToWithPassable;
import com.minecolonies.coremod.util.WorkerUtil;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.items.ModTags.fungi;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;

/**
 * The lumberjack AI class.
 */
public class EntityAIWorkLumberjack extends AbstractEntityAICrafting<JobLumberjack, BuildingLumberjack>
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
          new AITarget(LUMBERJACK_NO_TREES_FOUND, this::waitBeforeCheckingAgain, TICKS_SECOND)
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
        if (checkForToolOrWeapon(ToolType.AXE) || checkForToolOrWeapon(ToolType.HOE))
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

        // Reset everything, maybe there are new crafting requests
        return START_WORKING;
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
        final BuildingLumberjack building = getOwnBuilding();
        worker.getCitizenData().setVisibleStatus(SEARCH);

        if (pathResult != null && pathResult.isComputing())
        {
            return getState();
        }
        if (pathResult == null)
        {
            final List<ItemStorage> copy = building.getModuleMatching(ItemListModule.class, m -> m.getId().equals(SAPLINGS_LIST)).getList();
            if (building.shouldRestrict())
            {
                final BlockPos startPos = building.getStartRestriction();
                final BlockPos endPos = building.getEndRestriction();

                pathResult = worker.getNavigator()
                    .moveToTree(startPos,
                        endPos,
                        1.0D,
                        copy,
                        worker.getCitizenColonyHandler().getColony());
            }
            else
            {
                pathResult = worker.getNavigator()
                    .moveToTree(SEARCH_RANGE + searchIncrement,
                        1.0D,
                        copy,
                        worker.getCitizenColonyHandler().getColony());
            }
            return getState();
        }
        if (pathResult.isPathReachingDestination())
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

        if (job.getTree().hasLogs() || (job.getTree().hasLeaves() && job.getTree().isNetherTree()) || checkedInHut)
        {
            if (!walkToTree(job.getTree().getStumpLocations().get(0)))
            {
                if (checkIfStuck())
                {
                    tryUnstuck();
                }
                return getState();
            }
        }

        if (!job.getTree().hasLogs() && (!job.getTree().isNetherTree() || !(job.getTree().hasLeaves())))
        {
            if (hasNotDelayed(WAIT_BEFORE_SAPLING))
            {
                return getState();
            }

            final BuildingLumberjack building = getOwnBuilding();
            if (building.shouldReplant())
            {
                plantSapling();
            }
            else
            {
                job.setTree(null);
                checkedInHut = false;
            }

            worker.getCitizenExperienceHandler().addExperience(XP_PER_TREE);
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
        else if (job.getTree().hasLeaves() && job.getTree().isNetherTree())
        {
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
                if (stack.getItem() == Items.NETHER_WART_BLOCK) {
                    newDrops.add(new ItemStack(Items.CRIMSON_FUNGUS, 1));
                } else if (stack.getItem() == Items.WARPED_WART_BLOCK) {
                    newDrops.add(new ItemStack(Items.WARPED_FUNGUS, 1));
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
    private boolean checkIfStuck()
    {
        if (!worker.getNavigator().noPath())
        {
            final Path path = worker.getNavigator().getPath();
            if (path != null)
            {
                if (path.getCurrentPathLength() > path.getCurrentPathIndex())
                {
                    return true;
                }
                return path.getCurrentPathLength() == 0;
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
        if (!worker.getNavigator().noPath())
        {
            Path path = worker.getNavigator().getPath();
            if (path != null)
            {
                // Unstuck with path
                final List<BlockPos> checkPositions = new ArrayList<>();
                PathPoint next = path.getPathPointFromIndex(Math.min(path.getCurrentPathIndex() + 1, path.getCurrentPathLength() - 1));

                // Blocks in front of the worker
                for (int i = 0; i < 2; i++)
                {
                    checkPositions.add(new BlockPos(next.x, next.y + i, next.z));
                }
                if (next.previous != null)
                {
                    next = next.previous;
                    for (int i = 0; i < 2; i++)
                    {
                        checkPositions.add(new BlockPos(next.x, next.y + i, next.z));
                    }
                    if (next.previous != null)
                    {
                        next = next.previous;
                        for (int i = 0; i < 2; i++)
                        {
                            checkPositions.add(new BlockPos(next.x, next.y + i, next.z));
                        }
                    }
                }

                mineIfEqualsBlockTag(checkPositions, BlockTags.LEAVES);
                return;
            }
        }

        // General unstuck
        ArrayList<BlockPos> checkPositions = new ArrayList<>();

        for (Direction direction : Direction.Plane.HORIZONTAL)
        {
            checkPositions.add(new BlockPos(worker.getPosition().getX(), worker.getPosition().getY(), worker.getPosition().getZ()).offset(direction));
            checkPositions.add(new BlockPos(worker.getPosition().getX(), worker.getPosition().getY() + 1, worker.getPosition().getZ()).offset(direction));
        }

        mineIfEqualsBlockTag(checkPositions, BlockTags.LEAVES);
    }

    /**
     * Checks blocks for tag and mines the first it fines if its the same
     *
     * @param blockPositions block positions
     * @param tag            tag to check
     */
    private boolean mineIfEqualsBlockTag(List<BlockPos> blockPositions, ITag<Block> tag)
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
                    world.getBlockState(currentPos).getBlock().isIn(tag)));
            }
            if (world.getBlockState(currentPos).getBlock().isIn(tag))
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

        if (saplingSlot != -1)
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
    private void placeSaplings(final int saplingSlot, @NotNull final ItemStack stack, @NotNull final Block block)
    {
        while (!job.getTree().getStumpLocations().isEmpty())
        {
            final BlockPos pos = job.getTree().getStumpLocations().get(0);
            final Item sapling = getInventory().getStackInSlot(saplingSlot).getItem();
            final Block new_block;
            if (sapling.isIn(fungi))
            {
                if (sapling == Items.WARPED_FUNGUS)
                {
                    new_block = Blocks.WARPED_NYLIUM;
                }
                else
                {
                    new_block = Blocks.CRIMSON_NYLIUM;
                }
                world.setBlockState(pos.down(), new_block.getDefaultState());
                getOwnBuilding().addNetherTree(pos);
            }
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
            return false;
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
