package com.minecolonies.entity.ai;

import com.minecolonies.colony.jobs.JobLumberjack;
import com.minecolonies.inventory.InventoryCitizen;
import com.minecolonies.util.ChunkCoordUtils;
import com.minecolonies.util.InventoryUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockSapling;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class EntityAIWorkLumberjack extends EntityAIWork<JobLumberjack>
{
    public enum Stage
    {
        IDLE,//No resources
        SEARCHING,
        CHOPPING,
        GATHERING,
        INVENTORY_FULL
    }

    private static final int SEARCH_RANGE = 20;
    private static final int WORKER_INTERVAL = 10;

    private Queue<Tree> trees = new LinkedList<Tree>();

    public EntityAIWorkLumberjack(JobLumberjack job)
    {
        super(job);
    }

    @Override
    public boolean shouldExecute()
    {
        return super.shouldExecute();
    }

    @Override
    public void startExecuting()
    {
        if (!hasAxeWithEquip())
        {
            requestAxe();
        }
    }

    @Override
    public void updateTask()
    {
        if (worker.getOffsetTicks() % WORKER_INTERVAL != 0)
        {
            return;
        }

        switch (job.getStage())
        {
        case IDLE:
            if (!hasAxeWithEquip())
            {
                requestAxe();
            }
            else
            {
                job.setStage(Stage.SEARCHING);
            }
            break;
        case SEARCHING:
            //TODO only search if needed
            findTrees();//Find tree location

            break;
        case CHOPPING:
            if (!hasAxeWithEquip())
            {
                job.setStage(Stage.IDLE);
            }
            else if (job.tree == null)
            {
                if (trees.size() > 0)
                {
                    job.tree = trees.poll();//TODO find trees more efficiently
                }
                else
                {
                    job.setStage(Stage.SEARCHING);
                }
            }
            else
            {
                chopTree();//Go through Queue
            }
            break;
        case GATHERING://TODO never happens
            pickupSaplings();
            break;
        case INVENTORY_FULL:
            dumpInventory();
            break;
        default:
            System.out.println("Invalid stage in EntityAIWorkLumberjack");
        }
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
        //TODO go home or plant trees
    }

    private void findTrees()
    {
        //Search
        int posX = (int) worker.posX;
        int y = (int) worker.posY + 2;
        int posZ = (int) worker.posZ;

        for (int x = posX - SEARCH_RANGE; x < posX + SEARCH_RANGE; x++)
        {
            for (int z = posZ - SEARCH_RANGE; z < posZ + SEARCH_RANGE; z++)
            {
                Block block = world.getBlock(x, y, z);
                if (block instanceof BlockLog)
                {
                    System.out.println("BlockLog found");
                    Tree t = new Tree(world, new ChunkCoordinates(x, y, z));
                    if (t.isTree())
                    {
                        System.out.println("Tree found");
                        if (!trees.contains(t))
                        {
                            System.out.println("Tree added");
                            trees.add(t);
                        }
                    }
                }
            }
        }
        //TODO sort trees - IDEA(by distance(square distance) from TopMiddle of search square)
        System.out.println("Trees: " + trees.size());
        job.setStage(Stage.CHOPPING);
    }

    private void chopTree()
    {
        if (InventoryUtils.getOpenSlot(getInventory()) == -1)//inventory has an open slot - this doesn't account for slots with non full stacks
        {                                                   //also we still may have problems if the block drops multiple items
            job.setStage(Stage.INVENTORY_FULL);
            return;
        }
        if (!ChunkCoordUtils.isWorkerAtSiteWithMove(worker, job.tree.getLocation()))
        {
            System.out.println("Worker not at site: " + job.tree.getLocation().toString());
            System.out.println("\tDistance: " + ChunkCoordUtils.distanceSqrd(job.tree.getLocation(), worker.getPosition()));
            //TODO break leaves in way of path to tree
            return;
        }

        ChunkCoordinates log = job.tree.getNextLog();
        List<ItemStack> items = ChunkCoordUtils.getBlockDrops(world, log, 0);//0 is fortune level, it doesn't matter
        for (ItemStack item : items)
        {
            InventoryUtils.setStack(getInventory(), item);
        }
        ChunkCoordUtils.setBlock(world, log, Blocks.air);
        worker.getHeldItem().damageItem(1, worker);
        worker.swingItem();

        //tree is gone
        if (!job.tree.hasLogs())
        {
            boolean success = plantSapling(job.tree.getLocation());
            System.out.println("Tree planting success: " + success);
            job.tree = null;
        }
    }

    private void pickupSaplings()
    {
        //TODO
    }

    private void dumpInventory()
    {
        if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, worker.getWorkBuilding().getLocation()))
        {
            for (int i = 0; i < getInventory().getSizeInventory(); i++)
            {
                ItemStack stack = getInventory().getStackInSlot(i);
                if (stack != null && !(stack.getItem() instanceof ItemAxe || isStackSapling(stack)))
                {
                    ItemStack returnStack = InventoryUtils.setStack(worker.getWorkBuilding().getTileEntity(), stack);
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
            job.setStage(Stage.IDLE);
        }
    }

    private InventoryCitizen getInventory()
    {
        return worker.getInventory();
    }

    private boolean hasAxe()
    {
        return getAxeSlot() != -1;
    }

    private boolean hasAxeWithEquip()
    {
        if (hasAxe())
        {
            if (!(worker.getHeldItem() != null && worker.getHeldItem().getItem() instanceof ItemAxe))
            {
                equipAxe();
            }
            return true;
        }
        return false;
    }

    private int getAxeSlot()
    {
        return InventoryUtils.getFirstSlotContainingTool(getInventory(), ItemAxe.class);
    }

    private void equipAxe()
    {
        worker.setCurrentItemOrArmor(0, getInventory().getStackInSlot(getAxeSlot()));
    }

    private boolean isStackSapling(ItemStack stack)
    {
        return stack != null && stack.getItem() instanceof ItemBlock && ((ItemBlock) stack.getItem()).field_150939_a instanceof BlockSapling;
    }

    private int getSaplingSlot()
    {
        for (int i = 0; i < getInventory().getSizeInventory(); i++)
        {
            if (isStackSapling(getInventory().getStackInSlot(i)))
            {
                return i;
            }
        }
        return -1;
    }

    private boolean plantSapling(ChunkCoordinates location)
    {
        int slot = getSaplingSlot();
        if (slot != -1)
        {
            ItemStack stack = getInventory().getStackInSlot(slot);
            if (stack.getItem() instanceof ItemBlock)
            {
                Block block = ((ItemBlock) stack.getItem()).field_150939_a;
                if (ChunkCoordUtils.setBlock(world, location, block, stack.getItemDamage(), 0x02))
                {
                    getInventory().decrStackSize(slot, 1);
                    return true;
                }
            }
        }
        return false;
    }
}