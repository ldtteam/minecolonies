package com.minecolonies.entity.ai;

import com.minecolonies.colony.jobs.JobLumberjack;
import com.minecolonies.entity.pathfinding.PathJobFindTree;
import com.minecolonies.inventory.InventoryCitizen;
import com.minecolonies.util.ChunkCoordUtils;
import com.minecolonies.util.InventoryUtils;
import com.minecolonies.util.Vec3Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSapling;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.minecolonies.entity.ai.AIState.*;

public class EntityAIWorkLumberjack extends AbstractEntityAIWork<JobLumberjack>
{
    private static final String TOOL_TYPE_AXE      = "axe";
    private static final String RENDER_META_LOGS   = "Logs";
    private static final int    MAX_LOG_BREAK_TIME = 30;
    private static final int    SEARCH_RANGE       = 50;
    private              int    chopTicks          = 0;
    private              int    stillTicks         = 0;
    private              int    previousDistance   = 0;
    private              int    previousIndex      = 0;
    private              int    logBreakTime       = Integer.MAX_VALUE;
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
        if (logBreakTime == Integer.MAX_VALUE)
        {
            //todo: use api diggingspeed and use setDelay()
            ItemStack axe = worker.getHeldItem();
            logBreakTime = MAX_LOG_BREAK_TIME - (int) axe.getItem().getDigSpeed(
                    axe, ChunkCoordUtils.getBlock(world, job.tree.getLocation()),
                    ChunkCoordUtils.getBlockMetadata(world, job.tree.getLocation()));
        }
        chopTree();
        return state;
    }

    private void chopTree()
    {
        if (InventoryUtils.getOpenSlot(getInventory()) == -1)//inventory has an open slot - this doesn't account for slots with non full stacks
        {                                                   //also we still may have problems if the block drops multiple items
            job.setStage(Stage.INVENTORY_FULL);
            return;
        }

        ChunkCoordinates location = job.tree.getLocation();
        if (!worker.isWorkerAtSiteWithMove(location, 3))
        {
            int distance = (int) ChunkCoordUtils.distanceSqrd(location, worker.getPosition());
            if (previousDistance == distance)//Stuck, probably on leaves
            {
                stillTicks++;
                if (stillTicks >= 10)
                {
                    Vec3 treeDirection = Vec3Utils
                            .vec3Floor(worker.getPosition())
                            .subtract(Vec3.createVectorHelper(location.posX, location.posY + 2, location.posZ))
                            .normalize();

                    int x = MathHelper.floor_double(worker.posX);
                    int y = MathHelper.floor_double(worker.posY) + 1;
                    int z = MathHelper.floor_double(worker.posZ);
                    if (treeDirection.xCoord > 0.5F)
                    {
                        x++;
                    }
                    else if (treeDirection.xCoord < -0.5F)
                    {
                        x--;
                    }
                    else if (treeDirection.zCoord > 0.5F)
                    {
                        z++;
                    }
                    else if (treeDirection.zCoord < -0.5F)
                    {
                        z--;
                    }
                    //These need some work
                    if (treeDirection.yCoord > 0.75F)
                    {
                        y++;
                    }
                    else if (treeDirection.yCoord < -0.75F)
                    {
                        y--;
                    }

                    Block block = world.getBlock(x, y, z);
                    if (worker.getOffsetTicks() % 20 == 0)//Less spam
                    {
                        //System.out.println(String.format("Block: %s  x:%d y:%d z:%d", block.getUnlocalizedName(), x, y, z));
                    }
                    if (block.isLeaves(world, x, y, z))//Parameters not used
                    {
                        //drops
                        List<ItemStack> items = block.getDrops(world, x, y, z, world.getBlockMetadata(x, y, z), 0 /*worker.getHeldItem().getEnchantmentTagList()*/);//TODO fortune
                        for (ItemStack item : items)
                        {
                            InventoryUtils.setStack(getInventory(), item);
                        }
                        //break leaves
                        world.setBlockToAir(x, y, z);
                        worker.hitBlockWithToolInHand(x, y, z);//TODO should this damage tool? if so change to breakBlockWithToolInHand

                        stillTicks = 0;
                    }
                    else if (stillTicks > 60)//If the worker gets too stuck he moves around a bit
                    {
                        worker.getNavigator().moveAwayFromXYZ(worker.posX, worker.posY, worker.posZ, 3.0, 1.0);
                    }
                }
            }
            else
            {
                stillTicks = 0;
                previousDistance = distance;
            }
            return;
        }

        if (chopTicks == logBreakTime)//log break
        {
            ChunkCoordinates log   = job.tree.pollNextLog();//remove log from queue
            Block            block = ChunkCoordUtils.getBlock(world, log);
            if (block.isWood(null, 0, 0, 0))
            {
                //handle drops (usually one log)
                List<ItemStack> items = ChunkCoordUtils.getBlockDrops(world, log, 0);//0 is fortune level, it doesn't matter
                for (ItemStack item : items)
                {
                    InventoryUtils.setStack(getInventory(), item);
                }
                //break block
                worker.breakBlockWithToolInHand(log);
            }

            //tree is gone
            if (!job.tree.hasLogs())
            {
                //TODO place correct sapling
                plantSapling(location);
                job.tree = null;
                logBreakTime = Integer.MAX_VALUE;
            }
            chopTicks = -1;//will be increased to 0 at the end of the method
        }
        else if (chopTicks % 5 == 0)//time to swing and play sounds
        {
            ChunkCoordinates log   = job.tree.peekNextLog();
            Block            block = ChunkCoordUtils.getBlock(world, log);
            if (chopTicks == 0)
            {
                if (!block.isWood(null, 0, 0, 0))
                {
                    chopTicks = logBreakTime;
                    return;
                }
                worker.getLookHelper().setLookPosition(log.posX, log.posY, log.posZ, 10f, worker.getVerticalFaceSpeed());//TODO doesn't work right
            }
            worker.hitBlockWithToolInHand(log);
        }
        chopTicks++;
    }

    private InventoryCitizen getInventory()
    {
        return worker.getInventory();
    }

    private boolean plantSapling(ChunkCoordinates location)
    {
        if (ChunkCoordUtils.getBlock(world, location) != Blocks.air)
        {
            return false;
        }

        for (int slot = 0; slot < getInventory().getSizeInventory(); slot++)
        {
            ItemStack stack = getInventory().getStackInSlot(slot);
            if (stack != null && stack.getItem() instanceof ItemBlock)
            {
                Block block = ((ItemBlock) stack.getItem()).field_150939_a;
                if (block instanceof BlockSapling)
                {
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
        }
        return false;
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

    private boolean isStackSapling(ItemStack stack)
    {
        return stack != null && stack.getItem() instanceof ItemBlock && ((ItemBlock) stack.getItem()).field_150939_a instanceof BlockSapling;
    }

    @Override
    public void updateTask()
    {
        //TODO always has to be executed!
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

    private void requestAxe()
    {
        //TODO request by tool type
        //job.addItemNeeded();
        worker.isWorkerAtSiteWithMove(worker.getWorkBuilding().getLocation(), 4);//Go Home
    }

    private void dumpInventory()
    {
        if (worker.isWorkerAtSiteWithMove(worker.getWorkBuilding().getLocation(), 4))
        {
            int saplingStacks = 0;
            for (int i = 0; i < getInventory().getSizeInventory(); i++)
            {
                ItemStack stack = getInventory().getStackInSlot(i);
                if (stack != null && !isStackAxe(stack))
                {
                    if (isStackSapling(stack) && saplingStacks < 5)
                    {
                        saplingStacks++;
                    }
                    else
                    {
                        ItemStack returnStack = InventoryUtils.setStack(worker.getWorkBuilding().getTileEntity(), stack);//TODO tile entity null
                        if (returnStack == null)
                        {
                            getInventory().decrStackSize(i, stack.stackSize);
                        }
                        else
                        {
                            getInventory().decrStackSize(i, stack.stackSize - returnStack.stackSize);
                        }
                    }
                }
            }
            job.setStage(Stage.IDLE);
        }
    }

    private boolean hasAxe()
    {
        return getAxeSlot() != -1;
    }

    private boolean hasAxeWithEquip()
    {
        if (hasAxe())
        {
            if (!isStackAxe(worker.getHeldItem()))
            {
                equipAxe();
            }
            return true;
        }
        return false;
    }

    private int getAxeSlot()
    {
        return InventoryUtils.getFirstSlotContainingTool(getInventory(), TOOL_TYPE_AXE);
    }

    private void equipAxe()
    {
        worker.setHeldItem(getAxeSlot());
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