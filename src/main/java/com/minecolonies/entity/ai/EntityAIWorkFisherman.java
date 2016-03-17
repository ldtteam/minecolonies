package com.minecolonies.entity.ai;

import com.minecolonies.colony.buildings.BuildingFisherman;
import com.minecolonies.colony.buildings.BuildingMiner;
import com.minecolonies.colony.jobs.JobFisherman;
import com.minecolonies.colony.jobs.JobMiner;
import com.minecolonies.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Objects;


/**
 * Miner AI class
 * Created: March 17, 2016
 *
 * @author Raycoms
 */

public class EntityAIWorkFisherman extends AbstractEntityAIWork<JobFisherman>
{

    private static final String RENDER_META_FISH = "fish";

    private static Logger logger = LogManager.getLogger("Fisherman");

    public EntityAIWorkFisherman(JobFisherman job)
    {
        super(job);
    }

    @Override
    protected void updateRenderMetaData()
    {
        String renderMetaData = getRenderMetaTorch();
        //TODO: Have pickaxe etc. displayed?
        worker.setRenderMetadata(renderMetaData);
    }

    //TODO Render data Rod/Fish
    private String getRenderMetaTorch()
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

    //TODO walk to Water
    private boolean walkToWater()
    {
        return walkToBlock(getOwnBuilding().waterLocation);
    }

    @Override
    protected boolean neededForWorker(ItemStack stack)
    {
        return Utils.isFishingTool(stack);
    }

    //TODO Look for water
    private void lookForLadder()
    {
        BuildingFisherman buildingFisherman = getOwnBuilding();

        //Check for already found ladder
        if (buildingFisherman.foundWater && buildingFisherman.waterLocation != null)
        {
            if (world.getBlock(buildingFisherman.waterLocation.posX,
                    buildingFisherman.waterLocation.posY,
                    buildingFisherman.waterLocation.posZ) == Blocks.ladder)
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
        int posY = buildingFisherman.getLocation().posY + 2;
        int posZ = buildingFisherman.getLocation().posZ;
        for (int y = posY - 10; y < posY; y++)
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
            //TODO Eventually search for bigger water pond
            buildingfisherman.waterLocation = new ChunkCoordinates(x, y, z);
        }
    }

    //TODO Fishing Tool
    private void requestTool(Block curblock)
    {
        if (Objects.equals(curblock.getHarvestTool(0), Rod))
        {
            job.setStage(Stage.PREPARING);
            needsRod = true;
        }
    }

    //TODO Missing Rod
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
        //TODO Actually fish!
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
            if (!getOwnBuilding().foundWater)
            {
                job.setStage(Stage.SEARCHING_WATER);
                return;
            }
           job.setStage(Stage.CHECK_WATER);
        }

        //Looking for the ladder to walk to
        if (job.getStage() == Stage.SEARCHING_WATER)
        {
            lookForLadder();
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