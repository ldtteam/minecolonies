package com.minecolonies.entity.ai;

import com.minecolonies.colony.buildings.BuildingFisherman;
import com.minecolonies.colony.jobs.JobFisherman;
import com.minecolonies.inventory.InventoryCitizen;
import com.minecolonies.items.MineColoniesEntityFishHook;
import com.minecolonies.util.InventoryUtils;
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

    private static final String TOOL_TYPE_ROD= "rod";
    private static final String RENDER_META_FISH = "fish";
    public boolean returnedRod=true;
    public int fishesCaught=0;

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

    //TODO Create list with all previous water locations, let him find all water locations connected to land, then let him randomly work from the list
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
        for (int y = posY; y>posY-10; y--)
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


    public void doFishing()
    {
        //TODO Skills can't be 0!
        double chance = (Math.random()*500)/(worker.intelligence+1);
        boolean foundCloseWater = false;
        //We really do have our Rod in our inventory?
        if(!worker.hasitemInInventory(Items.fishing_rod))
        {
            job.setStage(Stage.PREPARING);
            return;
        }

        //Check if there is water really close to me!
        for(int x = (int)worker.posX-2;x<(int)worker.posX+2;x++)
        {
            for(int z = (int)worker.posX-2;z<(int)worker.posX+2;z++)
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

            if(++fishesCaught > 10)
            {
                fishesCaught=0;
                job.setStage(Stage.INVENTORY_FULL);
            }
            return;
        }

        if(worker.fishEntity==null)
        {
            //Only sometimes the fisherman gets to throw its Rod (depends on intelligence)
            if (!(chance < 2))
            {
                return;
            }

            if (!world.isRemote)
            {
                world.playSoundAtEntity(worker, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
                MineColoniesEntityFishHook hook = new MineColoniesEntityFishHook(world,worker);
                worker.fishEntity = hook;
                world.spawnEntityInWorld(hook);
            }

            worker.swingItem();
            returnedRod=false;
        }
        else
        {
            //Check if hook landed on ground or in water, in some cases the hook bugs -> remove it after 2 minutes
            if (!worker.fishEntity.isInWater() && (worker.fishEntity.onGround || (System.nanoTime() - worker.fishEntity.creationTime)/1000000000 > worker.fishEntity.getTtl()))
            {
                worker.swingItem();

                int i = worker.fishEntity.func_146034_e();
                worker.getInventory().getHeldItem().damageItem(i, worker);

                if(worker.getInventory().getHeldItem().stackSize < 1)//if tool breaks
                {
                    worker.setCurrentItemOrArmor(0, null);
                    getInventory().setInventorySlotContents(getInventory().getHeldItemSlot(), null);
                }

                worker.fishEntity.setDead();
                worker.fishEntity = null;
                //Reposition to water!
                job.setStage(Stage.WATER_FOUND);
            }
        }
    }

    private void dumpInventory()
    {
        if(worker.isWorkerAtSiteWithMove(worker.getWorkBuilding().getLocation(), 4))
        {
            int saplingStacks = 0;
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
                requestTool(Items.fishing_rod);
            }
            return;
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
            worker.setAngles(90f,worker.rotationPitch);

            worker.getLookHelper();

            //TODO Walk up and down the water a bit and choose a spot
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