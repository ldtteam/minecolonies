package com.minecolonies.entity.ai;

import com.minecolonies.colony.buildings.BuildingFisherman;
import com.minecolonies.colony.jobs.JobFisherman;
import com.minecolonies.items.MineColoniesEntityFishHook;
import com.minecolonies.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Miner AI class
 * Created: March 17, 2016
 *
 * @author Raycoms
 */

public class EntityAIWorkFisherman extends AbstractEntityAIWork<JobFisherman>
{

    private static final String RENDER_META_FISH = "fish";
    public boolean returnedRod=true;
    public int fishingLevel=0;
    public int fishingXP=0;

    private static Logger logger = LogManager.getLogger("Fisherman");

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

        /*if (worker.hasitemInInventory(Blocks.torch))
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
        return walkToBlock(getOwnBuilding().waterLocation);
    }

    @Override
    protected boolean neededForWorker(ItemStack stack)
    {
        return Utils.isFishingTool(stack);
    }

    private void lookForWater()
    {
        BuildingFisherman buildingFisherman = getOwnBuilding();

        //Check for already found ladder
        if (buildingFisherman.foundWater && buildingFisherman.waterLocation != null)
        {
            if (world.getBlock(buildingFisherman.waterLocation.posX,
                    buildingFisherman.waterLocation.posY,
                    buildingFisherman.waterLocation.posZ) == Blocks.water)
            {
                job.setStage(Stage.WATER_FOUND);
                return;
            }
            else
            {
                buildingFisherman.foundWater = false;
                buildingFisherman.waterLocation = null;
            }
        }
        int posX = buildingFisherman.getLocation().posX;
        int posY = buildingFisherman.getLocation().posY;
        int posZ = buildingFisherman.getLocation().posZ;
        for (int y = posY; posY-10>y; y--)
        {
            for (int x = posX - 10; x < posX + 10; x++)
            {
                for (int z = posZ - 10; z < posZ + 10; z++)
                {
                    tryFindWaterAt(x, y, z);
                }
            }
        }
    }

    private void tryFindWaterAt(int x, int y, int z)
    {
        BuildingFisherman buildingfisherman = getOwnBuilding();
        if (buildingfisherman.foundWater)
        {
            return;
        }
        if (world.getBlock(x, y, z).equals(Blocks.water))
        {
            //Check if there is a block covering our water
            if(!world.getBlock(x,y+1,z).equals(Blocks.air) && !world.getBlock(x,y+2,z).equals(Blocks.air) && !world.getBlock(x,y+3,z).equals(Blocks.air))
            {
                return;
            }
            //TODO Eventually search for bigger water pond
            buildingfisherman.foundWater=true;
            buildingfisherman.waterLocation = new ChunkCoordinates(x, y, z);
        }
    }

    private void requestTool(Item item)
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
        for (ItemStack stack : items)
        {
            itemsNeeded.add(stack);
        }
        job.setStage(Stage.PREPARING);
        return true;
    }

    public void doFishing()
    {
        double x = Math.random()*500;


        //We really do have our Rod in our inventory?
        if(!worker.hasitemInInventory(Items.fishing_rod))
        {
            job.setStage(Stage.PREPARING);
            return;
        }

        //I am at the water?
        //Check if there is water really close to me!
        if(!worker.getInventory().getHeldItem().getItem().equals(Items.fishing_rod))
        {
            //TODO worker.getInventory().setHeldItem(worker.getInventory().getSlotFromItem(Items.fishing_rod));
        }


        for (EntityXPOrb orb : worker.getXPOrbsOnGrid())
        {
            worker.addExperience(orb.getXpValue());
            orb.setDead();
        }

        if(worker.caughtFish)
        {
            worker.setCanPickUpLoot(true);
            worker.captureDrops = true;

            int i = worker.fishEntity.func_146034_e();
            worker.getInventory().getHeldItem().damageItem(i, worker);
            worker.swingItem();
            if (worker.fishEntity != null)
            {
                worker.fishEntity.setDead();
                worker.fishEntity = null;
            }

            System.out.println("Caught a fish, yeah!");
            worker.caughtFish = false;
            return;


        }

        if(worker.fishEntity==null)
        {
            if (!(x < 2))
            {
                return;
            }


            world.playSoundAtEntity(worker, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

            if (!world.isRemote && this.worker!=null)
            {
                MineColoniesEntityFishHook hook = new MineColoniesEntityFishHook(world,worker);
                worker.fishEntity = hook;
                world.spawnEntityInWorld(hook);
            }

            //Check if hook landed in water

            worker.swingItem();
            returnedRod=false;
        }
        else
        {
            if (worker.fishEntity.isInWater() && worker.fishEntity !=null)
            {
                int i = worker.fishEntity.func_146034_e();
                worker.getInventory().getHeldItem().damageItem(i, worker);
                worker.swingItem();
                //Reposition to water!
                job.setStage(Stage.CHECK_WATER);
            }
        }

    }

    //Called by MineColoniesEntityFishHook when the Fisherman has caught a fish
    public void caughtFish()
    {
        fishingXP = fishingXP + worker.intelligence*worker.speed;

        if((int)(fishingXP/100) > fishingLevel)
        {
            fishingLevel = fishingXP/100;
        }
    }

    //TODO!!!
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
            if(worker.hasitemInInventory(Items.fishing_rod))
            {
                if (!getOwnBuilding().foundWater)
                {
                    job.setStage(Stage.SEARCHING_WATER);
                    return;
                }
                job.setStage(Stage.CHECK_WATER);
            }
            else
            {
                missesItemsInInventory(new ItemStack(Items.fishing_rod,1));
            }

        }

        //Looking for the ladder to walk to
        if (job.getStage() == Stage.SEARCHING_WATER)
        {
            lookForWater();
            return;
        }

        //Walking to the ladder to check out the mine
        if (job.getStage() == Stage.WATER_FOUND)
        {
            if (walkToWater())
            {
                return;
            }
            job.setStage(Stage.CHECK_WATER);
        }

        //Standing on top of the ladder, checking out mine
        if (job.getStage() == Stage.CHECK_WATER)
        {
            //TODO Walk up and down the water a bit and choose a spot
            job.setStage(Stage.START_FISHING);
            return;
        }

        if (job.getStage() == Stage.START_FISHING)
        {
            doFishing();
            return;
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

    private int getBlockMetadata(ChunkCoordinates loc)
    {
        return world.getBlockMetadata(loc.posX, loc.posY, loc.posZ);
    }

    private Block getBlock(ChunkCoordinates loc)
    {
        return world.getBlock(loc.posX, loc.posY, loc.posZ);
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
        LADDER_WATER,
    }
}