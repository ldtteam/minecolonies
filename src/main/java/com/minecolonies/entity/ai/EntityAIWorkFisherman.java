package com.minecolonies.entity.ai;

import com.minecolonies.colony.buildings.BuildingFisherman;
import com.minecolonies.colony.jobs.JobFisherman;
import com.minecolonies.entity.pathfinding.PathJobFindWater;
import com.minecolonies.inventory.InventoryCitizen;
import com.minecolonies.entity.EntityFishHook;
import com.minecolonies.util.InventoryUtils;
import com.minecolonies.util.Utils;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Collections;
import java.util.Random;

/**
 * Fisherman AI class
 * Created: March 17, 2016
 *
 * @author Raycoms
 */

public class EntityAIWorkFisherman extends AbstractEntityAIWork<JobFisherman>
{
    private static final int MAX_PONDS = 20;
    private static final String TOOL_TYPE_ROD= "rod";
    //private static final String RENDER_META_FISH = "fish"; TODO Add
    private int fishesCaught=0;
    private PathJobFindWater.WaterPathResult pathResult;
    private static final int SEARCH_RANGE       = 50;
    private int FishingSkill = worker.getIntelligence()*worker.getSpeed()*worker.getExperienceLevel();
    private static Logger logger = LogManager.getLogger("Fisherman");

    //Assign job to fisherman
    public EntityAIWorkFisherman(JobFisherman job)
    {
        super(job);
    }

    @Override
    protected void updateRenderMetaData()
    {
        String renderMetaData = getRenderMetaFish();
        //TODO: Have rod displayed as well?
        //worker.setRenderMetadata(renderMetaData);
    }

    //TODO Render model ROD/Fish
    private String getRenderMetaFish()
    {

        /*if (worker.hasItemInInventory(Blocks.torch))
        {
            return RENDER_META_FISH;
        }*/
        return "";
    }

    @Override
    protected BuildingFisherman getOwnBuilding()
    {
        return (BuildingFisherman) worker.getWorkBuilding();
    }

    private boolean walkToWater()
    {
        if(job.water == null || job.water.getLocation() == null)
        {
            return false;
        }
        return walkToBlock(job.water.getLocation());
    }

    @Override
    protected boolean neededForWorker(ItemStack stack)
    {
        return Utils.isFishingTool(stack);
    }

    private void findWater()
    {
        //If 20 ponds are already stored, take a random stored location
        if(job.ponds.size()>=MAX_PONDS)
        {
            job.water = new Water(world,job.ponds.get(new Random().nextInt(20)));
            return;
        }

        if(pathResult == null || (!pathResult.isComputing() && !pathResult.getPathReachesDestination()))
        {
            pathResult = worker.getNavigator().moveToWater(SEARCH_RANGE, 1.0D,job.ponds);
        }
        else if(pathResult.getPathReachesDestination())
        {
            if(pathResult.ponds != null)
            {
                job.water = new Water(world, pathResult.ponds);
                job.ponds.add(pathResult.ponds);
            }
            pathResult = null;
            job.setStage(Stage.CHECK_WATER);
        }
        else if(pathResult.isCancelled())
        {
            job.setStage(Stage.PREPARING);
            pathResult = null;
        }

    }

    private void requestTool()
    {
            job.setStage(Stage.PREPARING);
            needsRod = true;
    }

    private boolean missesItemsInInventory(ItemStack... items)
    {
        boolean allClear = true;
        for (ItemStack stack : items)
        {
            int countOfItem = worker.getItemCountInInventory(stack.getItem());
            if (countOfItem < stack.stackSize)
            {
                int itemsLeft = stack.stackSize - countOfItem;
                ItemStack requiredStack = new ItemStack(stack.getItem(), itemsLeft);
                itemsCurrentlyNeeded.add(requiredStack);
                allClear = false;
            }
        }
        if (allClear)
        {
            return false;
        }
        itemsNeeded.clear();
        Collections.addAll(itemsNeeded, items);
        job.setStage(Stage.PREPARING);
        return true;
    }

    private void equipRod()
    {
        worker.setHeldItem(getRodSlot());
    }

    private int getRodSlot()
    {
        return InventoryUtils.getFirstSlotContainingTool(getInventory(), TOOL_TYPE_ROD);
    }

    private InventoryCitizen getInventory()
    {
        return worker.getInventory();
    }


    private void doFishing()
    {
        //+1 since the level may be 0
        double chance = (Math.random()*500)/(FishingSkill+1);
        boolean foundCloseWater = false;
        //We really do have our Rod in our inventory?
        if(!worker.hasItemInInventory(Items.fishing_rod))
        {
            job.setStage(Stage.PREPARING);
            return;
        }

        //Check if there is water really close to me!
        for(int x = (int)worker.posX-3;x<(int)worker.posX+3;x++)
        {
            for(int z = (int)worker.posZ-3;z<(int)worker.posZ+3;z++)
            {
                if(world.getBlock(x,(int)worker.posY-1,z).equals(Blocks.water))
                {
                    foundCloseWater=true;
                }
            }
        }

        if(!foundCloseWater)
        {
            job.setStage(Stage.WATER_FOUND);
            return;
        }

        //Check if Rod is held item if not put it as held item
        if(!worker.getInventory().getHeldItem().getItem().equals(Items.fishing_rod))
        {
            equipRod();
        }

        for (EntityXPOrb orb : worker.getXPOrbsOnGrid())
        {
            worker.addExperience(orb.getXpValue());
            orb.setDead();
        }

        if(worker.isCaughtFish())
        {
            if(worker.getFishEntity()==null)
            {
                return;
            }

            worker.setCanPickUpLoot(true);
            worker.captureDrops = true;

            int i = worker.getFishEntity().func_146034_e();
            worker.getInventory().getHeldItem().damageItem(i, worker);
            worker.swingItem();
            if (worker.getFishEntity() != null)
            {
                worker.getFishEntity().setDead();
                worker.setFishEntity(null);
            }

            worker.setCaughtFish(false);
            fishesCaught++;
            if(fishesCaught > 10)
            {
                fishesCaught=0;
                job.setStage(Stage.INVENTORY_FULL);
            }
            return;
        }

        if(worker.getFishEntity()==null)
        {
            //Only sometimes the fisherman gets to throw its Rod (depends on intelligence)
            if (chance >= 2)
            {
                return;
            }

            if (!world.isRemote)
            {
                world.playSoundAtEntity(worker, "random.bow", 0.5f,
                                        (float) (0.4D / (itemRand.nextDouble() * 0.4D + 0.8D)));
                EntityFishHook hook = new EntityFishHook(world, worker);
                worker.setFishEntity(hook);
                world.spawnEntityInWorld(hook);
            }

            worker.swingItem();
        }
        else
        {
            //Check if hook landed on ground or in water, in some cases the hook bugs -> remove it after 2 minutes
            if (!worker.getFishEntity().isInWater() && (worker.getFishEntity().onGround || (System.nanoTime() - worker.getFishEntity().getCreationTime())/1000000000 > worker.getFishEntity().getTtl()))
            {
                worker.swingItem();
                int i = worker.getFishEntity().func_146034_e();
                worker.damageItemInHand(i);

                //May be null if the itemInHand has been destroyed
                if(worker.getFishEntity()!=null)
                {
                    worker.getFishEntity().setDead();
                    worker.setFishEntity(null);
                }

                job.setStage(Stage.WATER_FOUND);
            }
        }
    }

    private void dumpInventory()
    {
        if(worker.isWorkerAtSiteWithMove(worker.getWorkBuilding().getLocation(), 4))
        {
            for(int i = 0; i < getInventory().getSizeInventory(); i++)
            {
                ItemStack stack = getInventory().getStackInSlot(i);
                if(stack != null && !isStackRod(stack)) {
                    ItemStack returnStack = InventoryUtils.setStack(worker.getWorkBuilding().getTileEntity(), stack);//TODO tile entity null
                    if (returnStack == null) {
                        getInventory().decrStackSize(i, stack.stackSize);
                    } else {
                        getInventory().decrStackSize(i, stack.stackSize - returnStack.stackSize);
                    }
                }
            }
            job.setStage(Stage.PREPARING);
        }
    }

    private boolean isStackRod(ItemStack stack)
    {
        return stack != null && stack.getItem().equals(Items.fishing_rod);
    }

    @Override
    public void workOnTask()
    {
        //Fisherman wants to work but is not at building
        if (job.getStage() == Stage.START_WORKING)
        {
            if (walkToBuilding())
            {
                return;
            }
            //Fisherman is at building
            job.setStage(Stage.PREPARING);
            return;
        }

        //Fisherman is at building and prepares for work
        if (job.getStage() == Stage.PREPARING)
        {
            if(worker.hasItemInInventory(Items.fishing_rod))
            {
                if (job.water==null)
                {
                    job.setStage(Stage.SEARCHING_WATER);
                    return;
                }
                job.setStage(Stage.WATER_FOUND);
            }
            else
            {
                requestTool();
            }
            return;
        }

        //Looking for the water to walk to
        if (job.getStage() == Stage.SEARCHING_WATER)
        {
            findWater();
            return;
        }

        //Walking to the water to check out the mine
        if (job.getStage() == Stage.WATER_FOUND)
        {
            if(job.water==null)
            {
                job.setStage(Stage.SEARCHING_WATER);
                return;
            }
            if (walkToWater())
            {
                return;
            }
            job.setStage(Stage.CHECK_WATER);
        }

        //Standing at the water, checking out pond
        if (job.getStage() == Stage.CHECK_WATER)
        {
            //TODO After the executed a full rotation choose a new fishing spot!
            worker.setAngles(90f,worker.rotationPitch);
            //TODO Different angle to throw the hook not that far
            worker.getLookHelper();

            job.setStage(Stage.START_FISHING);
            return;
        }

        if (job.getStage() == Stage.START_FISHING)
        {
            doFishing();
            return;
        }

        if(job.getStage() == Stage.INVENTORY_FULL)
        {
            dumpInventory();
        }
        setDelay(100);
    }



    @Override
    public boolean continueExecuting()
    {
        return super.continueExecuting();
    }

    @Override
    public void resetTask()
    {
        super.resetTask();
    }

    public enum Stage
    {
        IDLE,
        START_WORKING,
        CHECK_WATER,
        INVENTORY_FULL,
        WATER_FOUND,
        SEARCHING_WATER,
        START_FISHING,
        PREPARING,
    }
}