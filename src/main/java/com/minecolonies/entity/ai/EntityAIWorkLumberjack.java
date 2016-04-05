package com.minecolonies.entity.ai;

import com.minecolonies.MineColonies;
import com.minecolonies.colony.jobs.JobLumberjack;
import com.minecolonies.entity.pathfinding.PathJobFindTree;
import com.minecolonies.util.ChunkCoordUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSapling;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.minecolonies.entity.ai.AIState.*;

public class EntityAIWorkLumberjack extends AbstractEntityAIWork<JobLumberjack>
{
    private static final String TOOL_TYPE_AXE          = "axe";
    private static final String RENDER_META_LOGS       = "Logs";
    private static final int    MAX_LOG_BREAK_TIME     = 30;
    private static final int    SEARCH_RANGE           = 50;
    /**
     * Number of ticks to wait before coming
     * to the conclusion of being stuck
     */
    private static final int    STUCK_WAIT_TIME        = 10;
    /**
     * Number of ticks until he gives up destroying leaves
     * and walks a bit back to try a new path
     */
    private static final int    WALKING_BACK_WAIT_TIME = 60;
    /**
     * How much he backs away when really not finding any path
     */
    private static final double WALK_BACK_RANGE        = 3.0;
    /**
     * The speed in which he backs away
     */
    private static final double WALK_BACK_SPEED        = 1.0;
    /**
     * Time in ticks to wait before placing a sapling.
     * Is used to collect falling saplings from the ground.
     */
    private static final int    WAIT_BEFORE_SAPLING    = 100;
    private              int    chopTicks              = 0;
    /**
     * Number of ticks the lumberjack is standing still
     */
    private              int    stillTicks             = 0;
    private              int    previousDistance       = 0;
    private              int    previousIndex          = 0;
    private              int    logBreakTime           = Integer.MAX_VALUE;
    private List<ChunkCoordinates>         items;
    private PathJobFindTree.TreePathResult pathResult;
    private int woodCuttingSkill = worker.getStrength() * worker.getSpeed() * (worker.getExperienceLevel() + 1);

    public EntityAIWorkLumberjack(JobLumberjack job)
    {
        super(job);
        super.registerTargets(
                new AITarget(IDLE, () -> START_WORKING),
                new AITarget(START_WORKING, this::startWorkingAtOwnBuilding),
                new AITarget(PREPARING, this::prepareForWoodcutting),
                new AITarget(LUMBERJACK_SEARCHING_TREE, this::findTrees),
                new AITarget(LUMBERJACK_CHOPP_TREES, this::choppWood),
                new AITarget(LUMBERJACK_GATHERING, this::gathering)
                             );
    }

    /**
     * Walk to own building to check for tools.
     *
     * @return PREPARING once at the building
     */
    private AIState startWorkingAtOwnBuilding()
    {
        if (walkToBuilding())
        {
            return state;
        }
        return PREPARING;
    }

    /**
     * Checks if lumberjack has all necessary tools
     *
     * @return next AIState
     */
    private AIState prepareForWoodcutting()
    {
        if (checkForAxe())
        {
            return state;
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
        return LUMBERJACK_CHOPP_TREES;
    }

    private AIState findTree()
    {
        if (pathResult == null)
        {
            pathResult = worker.getNavigator().moveToTree(SEARCH_RANGE, 1.0D);
            return state;
        }
        if (pathResult.getPathReachesDestination())
        {
            if (pathResult.treeLocation != null)
            {
                job.tree = new Tree(world, pathResult.treeLocation);
                job.tree.findLogs(world);
            }
            pathResult = null;
            return state;
        }
        if (pathResult.isCancelled())
        {
            pathResult = null;
            return LUMBERJACK_GATHERING;
        }
        return state;
    }

    /**
     * Again checks if all preconditions are given to execute chopping.
     * If yes go chopping, else return to previous AIStates.
     *
     * @return next AIState
     */
    private AIState choppWood()
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
        ChunkCoordinates location = job.tree.getLocation();
        MineColonies.logger.info(location.toString());
        if (walkToBlock(location))
        {
            checkIfStuckOnLeaves(location);
            return state;
        }

        if (!job.tree.hasLogs())
        {
            if (hasNotDelayed(WAIT_BEFORE_SAPLING))
            {
                return state;
            }
            plantSapling();
            return LUMBERJACK_GATHERING;
        }

        //take first log from queue
        ChunkCoordinates log = job.tree.peekNextLog();
        MineColonies.logger.info(location.toString() + " | " + log.toString());
        if (!mineBlock(log))
        {
            return state;
        }
        job.tree.pollNextLog();
        return state;
    }

    /**
     * Place a sappling for the current tree.
     */
    private void plantSapling()
    {
        //TODO place correct sapling
        plantSapling(job.tree.getLocation());
        job.tree = null;
    }

    /**
     * Plant a sapling at said location.
     * <p>
     * todo: make sure to get the right sapling
     *
     * @param location the location to plant the sapling at
     * @return true if a sapling was planted
     */
    private boolean plantSapling(ChunkCoordinates location)
    {
        if (ChunkCoordUtils.getBlock(world, location) != Blocks.air)
        {
            return false;
        }

        for (int slot = 0; slot < getInventory().getSizeInventory(); slot++)
        {
            ItemStack stack = getInventory().getStackInSlot(slot);
            if (isStackSapling(stack))
            {
                Block block = ((ItemBlock) stack.getItem()).field_150939_a;
                worker.setHeldItem(slot);
                if (ChunkCoordUtils.setBlock(world, location, block, stack.getItemDamage(), 0x02))
                {
                    worker.swingItem();
                    world.playSoundEffect((float) location.posX + 0.5F,
                                          (float) location.posY + 0.5F,
                                          (float) location.posZ + 0.5F,
                                          block.stepSound.getBreakSound(),
                                          block.stepSound.getVolume(),
                                          block.stepSound.getPitch());
                    getInventory().decrStackSize(slot, 1);
                    setDelay(10);
                    return true;
                }
                break;
            }
        }
        return false;
    }

    private boolean isStackSapling(ItemStack stack)
    {
        return stack != null && stack.getItem() instanceof ItemBlock && ((ItemBlock) stack.getItem()).field_150939_a instanceof BlockSapling;
    }

    /**
     * Check if distance to block changed and
     * if we are not moving for too long, try to get unstuck
     *
     * @param location the block we want to go to
     */
    private void checkIfStuckOnLeaves(final ChunkCoordinates location)
    {
        int distance = (int) ChunkCoordUtils.distanceSqrd(location, worker.getPosition());
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
     * We are stuck, remove some leaves and try to get unstuck
     * <p>
     * if this takes too long, try backing up a bit
     */
    private void tryGettingUnstuckFromLeaves()
    {
        ChunkCoordinates nextLeaves = findNearLeaves();
        //If the worker gets too stuck he moves around a bit
        if (nextLeaves == null || stillTicks > WALKING_BACK_WAIT_TIME)
        {
            worker.getNavigator().moveAwayFromXYZ(worker.posX, worker.posY, worker.posZ, WALK_BACK_RANGE, WALK_BACK_SPEED);
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
     * Utility method to check for leaves around the citizen.
     * <p>
     * Will report the location of the first leaves block it finds.
     *
     * @return a leaves block or null if none found
     */
    private ChunkCoordinates findNearLeaves()
    {
        int playerX = (int) worker.posX;
        int playerY = (int) (worker.posY + 1);
        int playerZ = (int) worker.posZ;
        int radius  = 3;
        for (int x = -radius; x < playerX + radius; x++)
        {
            for (int y = -radius; y < playerY + radius; y++)
            {
                for (int z = -radius; z < playerZ + radius; z++)
                {
                    if (world.getBlock(x, y, z).isLeaves(world, x, y, z))
                    {
                        return new ChunkCoordinates(x, y, z);
                    }
                }
            }
        }
        return null;
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
            return state;
        }
        if (!items.isEmpty())
        {
            gatherItems();
            return state;
        }
        items = null;
        return LUMBERJACK_SEARCHING_TREE;
    }

    private void searchForItems()
    {
        items = new ArrayList<>();

        @SuppressWarnings("unchecked") List<EntityItem> list = world.getEntitiesWithinAABB(EntityItem.class, worker.boundingBox.expand(15.0F, 3.0F, 15.0F));

        //TODO check if sapling or apple (currently picks up all items, which may be okay)
        items.addAll(list.stream().filter(item -> item != null && !item.isDead).map(ChunkCoordUtils::fromEntity).collect(Collectors.toList()));
    }

    private void gatherItems()
    {
        if (worker.getNavigator().noPath())
        {
            ChunkCoordinates pos = getAndRemoveClosestItem();
            worker.isWorkerAtSiteWithMove(pos, 3);
        }
        else if (worker.getNavigator().getPath() != null)
        {
            int currentIndex = worker.getNavigator().getPath().getCurrentPathIndex();
            if (currentIndex == previousIndex)
            {
                stillTicks++;
                if (stillTicks > 20)//Stuck
                {
                    worker.getNavigator().clearPathEntity();//Skip this item
                    //System.out.println("Lumberjack skipped item (couldn't reach)");
                }
            }
            else
            {
                stillTicks = 0;
                previousIndex = currentIndex;
            }
        }
    }

    private ChunkCoordinates getAndRemoveClosestItem()
    {
        int   index    = 0;
        float distance = Float.MAX_VALUE;

        for (int i = 0; i < items.size(); i++)
        {
            float tempDistance = ChunkCoordUtils.distanceSqrd(items.get(i), worker.getPosition());
            if (tempDistance < distance)
            {
                index = i;
                distance = tempDistance;
            }
        }

        return items.remove(index);
    }

    @Override
    protected boolean neededForWorker(ItemStack stack)
    {
        return isStackAxe(stack) || isStackSapling(stack);
    }

    private boolean isStackAxe(ItemStack stack)
    {
        return stack != null && stack.getItem().getToolClasses(stack).contains(TOOL_TYPE_AXE);
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

    private boolean isStackLog(ItemStack stack)
    {
        return stack != null && stack.getItem() instanceof ItemBlock && ((ItemBlock) stack.getItem()).field_150939_a.isWood(null, 0, 0, 0);
    }

    /**
     * This method will be overridden by AI implementations.
     * It will serve as a tick function.
     */
    @Override
    protected void workOnTask()
    {
        //Migration to new system complete
    }

    @Override
    public boolean continueExecuting()
    {
        return super.continueExecuting();
    }

    @Override
    public void resetTask()
    {
        job.setStage(Stage.IDLE);
    }

    public enum Stage
    {
        IDLE,
        //No resources
        SEARCHING,
        CHOPPING,
        GATHERING,
        INVENTORY_FULL
    }
}