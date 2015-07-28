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

    private static final String TOOL_TYPE_AXE = "axe";
    private static final String RENDER_META_LOGS = "Logs";

    private static final int MAX_LOG_BREAK_TIME = 30;
    private static final int SEARCH_RANGE = 50;

    private int chopTicks = 0;
    private int stillTicks = 0;
    private int previousDistance = 0;
    private int previousIndex = 0;

    private int delay = 0;

    private int logBreakTime = Integer.MAX_VALUE;

    private List<ChunkCoordinates> items;

    private PathJobFindTree.TreePathResult pathResult;

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
        if(delay > 0)
        {
            delay--;
            return;
        }

        worker.setRenderMetadata(hasLogs() ? RENDER_META_LOGS : "");

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
            if(job.tree == null)
            {
                findTree();
            }
            else
            {
                job.setStage(Stage.CHOPPING);
            }
            break;
        case CHOPPING:
            if (!hasAxeWithEquip())
            {
                job.setStage(Stage.IDLE);
            }
            else if (job.tree == null)
            {
                job.setStage(Stage.SEARCHING);
            }
            else
            {
                if(logBreakTime == Integer.MAX_VALUE)
                {
                    ItemStack axe = worker.getHeldItem();
                    logBreakTime = MAX_LOG_BREAK_TIME - (int) axe.getItem().getDigSpeed(axe,
                            ChunkCoordUtils.getBlock(world, job.tree.getLocation()),
                            ChunkCoordUtils.getBlockMetadata(world, job.tree.getLocation()));
                }

                chopTree();
            }
            break;
        //Entities now pick up nearby items
        case GATHERING:
            if(items == null)
            {
                searchForItems();
            }
            else if(!items.isEmpty())
            {
                gatherItems();
            }
            else
            {
                items = null;
                job.setStage(Stage.SEARCHING);
            }
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
        ChunkCoordUtils.isWorkerAtSiteWithMove(worker, worker.getWorkBuilding().getLocation());//Go Home
    }

    private void findTree()
    {
        if(pathResult == null)
        {
            pathResult = worker.getNavigator().moveToTree(SEARCH_RANGE, 1.0D);
        }
        else if(pathResult.getPathReachesDestination())
        {
            if(pathResult.treeLocation != null)
            {
                job.tree = new Tree(world, pathResult.treeLocation);
                job.tree.findLogs(world);
            }
            pathResult = null;
        }
        else if(pathResult.isCancelled())
        {
            job.setStage(Stage.GATHERING);
            pathResult = null;
        }
    }

    private void chopTree()
    {
        if (InventoryUtils.getOpenSlot(getInventory()) == -1)//inventory has an open slot - this doesn't account for slots with non full stacks
        {                                                   //also we still may have problems if the block drops multiple items
            job.setStage(Stage.INVENTORY_FULL);
            return;
        }

        ChunkCoordinates location = job.tree.getLocation();
        if (!ChunkCoordUtils.isWorkerAtSiteWithMove(worker, location))
        {
            int distance = (int) ChunkCoordUtils.distanceSqrd(location, worker.getPosition());
            if(previousDistance == distance)//Stuck, probably on leaves
            {
                stillTicks++;
                if(stillTicks >= 10)
                {
                    Vec3 treeDirection = Vec3Utils.vec3Floor(worker.getPosition()).subtract(Vec3.createVectorHelper(location.posX, location.posY + 2, location.posZ)).normalize();

                    int x = MathHelper.floor_double(worker.posX);
                    int y = MathHelper.floor_double(worker.posY)+1;
                    int z = MathHelper.floor_double(worker.posZ);
                    if(treeDirection.xCoord > 0.5F)
                    {
                        x++;
                    }
                    else if(treeDirection.xCoord < -0.5F)
                    {
                        x--;
                    }
                    else if(treeDirection.zCoord > 0.5F)
                    {
                        z++;
                    }
                    else if(treeDirection.zCoord < -0.5F)
                    {
                        z--;
                    }
                    //These need some work
                    if(treeDirection.yCoord > 0.75F)
                    {
                        y++;
                    }
                    else if(treeDirection.yCoord < -0.75F)
                    {
                        y--;
                    }

                    Block block = world.getBlock(x, y, z);
                    if(worker.getOffsetTicks() % 20 == 0)//Less spam
                    {
                        System.out.println(String.format("Block: %s  x:%d y:%d z:%d", block.getUnlocalizedName(), x, y, z));
                    }
                    if(block.isLeaves(world, x, y, z))//Parameters not used
                    {
                        //drops
                        List<ItemStack> items = block.getDrops(world, x, y, z, world.getBlockMetadata(x, y, z), 0 /*worker.getHeldItem().getEnchantmentTagList()*/);//TODO fortune
                        for (ItemStack item : items)
                        {
                            InventoryUtils.setStack(getInventory(), item);
                        }
                        //break leaves
                        world.setBlockToAir(x, y, z);
                        world.playSoundEffect(
                                (float) x + 0.5F,
                                (float) y + 0.5F,
                                (float) z + 0.5F,
                                block.stepSound.getBreakSound(), block.stepSound.getVolume(), block.stepSound.getPitch());

                        //TODO particles
                        worker.swingItem();

                        stillTicks = 0;
                    }
                    else if(stillTicks > 60)//If the worker gets too stuck he moves around a bit
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

        if(chopTicks == logBreakTime)//log break
        {
            ChunkCoordinates log = job.tree.pollNextLog();//remove log from queue
            Block block = ChunkCoordUtils.getBlock(world, log);
            if(block.isWood(null, 0,0,0))
            {
                //handle drops (usually one log)
                List<ItemStack> items = ChunkCoordUtils.getBlockDrops(world, log, 0);//0 is fortune level, it doesn't matter
                for (ItemStack item : items)
                {
                    InventoryUtils.setStack(getInventory(), item);
                }
                //break block
                world.playSoundEffect(
                        (float) log.posX + 0.5F,
                        (float) log.posY + 0.5F,
                        (float) log.posZ + 0.5F,
                        block.stepSound.getBreakSound(), block.stepSound.getVolume(), block.stepSound.getPitch());
                //TODO particles
                //Minecraft.getMinecraft().effectRenderer.addBlockDestroyEffects(log.posX, log.posY, log.posZ, block, ChunkCoordUtils.getBlockMetadata(world, log));
                ChunkCoordUtils.setBlock(world, log, Blocks.air);
                //Damage axe
                ItemStack axe = worker.getInventory().getHeldItem();
                axe.getItem().onBlockDestroyed(axe, world, block, log.posX, log.posY, log.posZ, worker);
                if (axe.stackSize < 1)//if axe breaks
                {
                    worker.setCurrentItemOrArmor(0, null);
                    getInventory().setInventorySlotContents(getInventory().getHeldItemSlot(), null);
                    //TODO particles
                }
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
        else if(chopTicks % 5 == 0)//time to swing and play sounds
        {
            ChunkCoordinates log = job.tree.peekNextLog();
            Block block = ChunkCoordUtils.getBlock(world, log);
            if(chopTicks == 0)
            {
                if(!block.isWood(null, 0,0,0))
                {
                    chopTicks = logBreakTime;
                    return;
                }
                worker.getLookHelper().setLookPosition(log.posX, log.posY, log.posZ, 10f, worker.getVerticalFaceSpeed());//TODO doesn't work right
            }
            worker.swingItem();
            world.playSoundEffect((float)log.posX + 0.5F, (float)log.posY + 0.5F, (float)log.posZ + 0.5F, block.stepSound.getStepResourcePath(), (block.stepSound.getVolume() + 1.0F) / 8.0F, block.stepSound.getPitch() * 0.5F);
            //TODO particles
        }
        chopTicks++;
    }

    private void searchForItems()
    {
        items = new ArrayList<ChunkCoordinates>();

        @SuppressWarnings("unchecked")
        List<EntityItem> list = world.getEntitiesWithinAABB(EntityItem.class, worker.boundingBox.expand(15.0F, 3.0F, 15.0F));

        for(EntityItem item : list)
        {
            if(item != null && !item.isDead)//TODO check if sapling or apple (currently picks up all items, which may be okay)
            {
                items.add(ChunkCoordUtils.fromEntity(item));
            }
        }
    }

    private void gatherItems()
    {
        if(worker.getNavigator().noPath())
        {
            ChunkCoordinates pos = getAndRemoveClosestItem();
            ChunkCoordUtils.isWorkerAtSiteWithMove(worker, pos);
        }
        else if(worker.getNavigator().getPath() != null)
        {
            int currentIndex = worker.getNavigator().getPath().getCurrentPathIndex();
            if(currentIndex == previousIndex)
            {
                stillTicks++;
                if(stillTicks > 20)//Stuck
                {
                    worker.getNavigator().clearPathEntity();//Skip this item
                    System.out.println("Lumberjack skipped item (couldn't reach)");
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
        int index = 0;
        float distance = Float.MAX_VALUE;

        for(int i = 0; i < items.size(); i++)
        {
            float tempDistance = ChunkCoordUtils.distanceSqrd(items.get(i), worker.getPosition());
            if(tempDistance < distance)
            {
                index = i;
                distance = tempDistance;
            }
        }

        return items.remove(index);
    }

    private void dumpInventory()
    {
        if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, worker.getWorkBuilding().getLocation()))
        {
            int saplingStacks = 0;
            for (int i = 0; i < getInventory().getSizeInventory(); i++)
            {
                ItemStack stack = getInventory().getStackInSlot(i);
                if (stack != null && !isStackAxe(stack))
                {
                    if(isStackSapling(stack) && saplingStacks < 5)
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

    private boolean isStackAxe(ItemStack stack)
    {
        return stack != null && stack.getItem().getToolClasses(null /* not used */).contains(TOOL_TYPE_AXE);
    }

    private boolean isStackSapling(ItemStack stack)
    {
        return stack != null && stack.getItem() instanceof ItemBlock && ((ItemBlock) stack.getItem()).field_150939_a instanceof BlockSapling;
    }

    private boolean plantSapling(ChunkCoordinates location)
    {
        if(ChunkCoordUtils.getBlock(world, location) != Blocks.air)
        {
            return false;
        }

        for (int slot = 0; slot < getInventory().getSizeInventory(); slot++)
        {
            ItemStack stack = getInventory().getStackInSlot(slot);
            if (stack != null && stack.getItem() instanceof ItemBlock)
            {
                Block block = ((ItemBlock) stack.getItem()).field_150939_a;
                if(block instanceof BlockSapling)
                {
                    worker.setHeldItem(slot);
                    if (ChunkCoordUtils.setBlock(world, location, block, stack.getItemDamage(), 0x02))
                    {
                        worker.swingItem();
                        world.playSoundEffect(
                                (float) location.posX + 0.5F,
                                (float) location.posY + 0.5F,
                                (float) location.posZ + 0.5F,
                                block.stepSound.getBreakSound(), block.stepSound.getVolume(), block.stepSound.getPitch());
                        getInventory().decrStackSize(slot, 1);
                        delay = 10;
                        return true;
                    }
                    break;
                }
            }
        }
        return false;
    }

    private boolean hasLogs()
    {
        for(int i = 0; i < getInventory().getSizeInventory(); i++)
        {
            if(isStackLog(getInventory().getStackInSlot(i)))
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
}