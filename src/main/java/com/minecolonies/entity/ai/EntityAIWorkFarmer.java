package com.minecolonies.entity.ai;

import com.minecolonies.colony.buildings.BuildingFarmer;
import com.minecolonies.colony.buildings.BuildingMiner;
import com.minecolonies.colony.jobs.Job;
import com.minecolonies.colony.jobs.JobFarmer;
import com.minecolonies.colony.jobs.JobMiner;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.util.ChunkCoordUtils;
import com.minecolonies.util.InventoryUtils;
import com.minecolonies.util.LanguageHandler;
import com.minecolonies.util.Utils;
import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.ForgeHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Miner AI class
 * Created: December 20, 2014
 *
 * @author Raycoms
 */

public class EntityAIWorkFarmer extends EntityAIWork<JobFarmer>
{
    public enum Stage
    {
        WORKING,
        MAKING_LAND,
        PLANTING,
        NEED_SEEDS,
        HARVESTING,
        SEARCHING_LAND

    }
    private static Logger logger = LogManager.getLogger("Farmer");

    private String NEED_ITEM;
    private double baseSpeed;
    private int delay=0;
    int i = 0;
    private boolean made_land = false;
    public List<ChunkCoordinates> farmLand = new ArrayList<ChunkCoordinates>();
    public List<ChunkCoordinates> crops = new ArrayList<ChunkCoordinates>();


    //TODO Check for duplicates
    public EntityAIWorkFarmer(JobFarmer job)
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
        worker.setStatus(EntityCitizen.Status.WORKING);
        updateTask();
    }

    @Override
    public void updateTask()
    {
        BuildingFarmer b = (BuildingFarmer)(worker.getWorkBuilding());
        if(b == null){return;}


        if (delay > 0)
        {
            delay--;
        }
        else if(job.hasItemsNeeded())
        {
            if(ChunkCoordUtils.isWorkerAtSiteWithMove(worker, worker.getWorkBuilding().getLocation()))
            {
                List<ItemStack> l = new CopyOnWriteArrayList<ItemStack>();
                l.addAll(job.getItemsNeeded());


                for (ItemStack e : l)
                {

                    if (isInHut(e.getItem()) || inventoryContains(e.getItem())!=-1)
                    {
                            job.removeItemNeeded(e);
                            return;
                    }
                    LanguageHandler.sendPlayersLocalizedMessage(Utils.getPlayersFromUUID(world, worker.getColony().getPermissions().getMessagePlayers()), "entity.miner.messageNeedBlockAndItem", e.getDisplayName());

                }
                delay = 50;
            }

        }
        else if (hasAllTheTools())
        {
            switch (job.getStage())
            {
                case SEARCHING_LAND:
                    searchFarmableLand();
                case MAKING_LAND:
                    make_land();
                    break;
                case NEED_SEEDS:
                    delay = 50;
                    logger.info("Need Seeds");

                    if(hasSeed())
                    {
                        job.setStage(Stage.WORKING);
                    }

                    break;
                case WORKING:
                    if(!hasSeed() && crops!=null)
                    {
                        job.setStage(Stage.NEED_SEEDS);
                    }
                    if(farmLand==null)
                    {
                        job.setStage(Stage.MAKING_LAND);
                    }
                    else if(made_land)
                    {
                        job.setStage(Stage.MAKING_LAND);
                    }
                    else if(hasSeed())
                    {
                        job.setStage(Stage.PLANTING);
                    }
                    else if(crops!=null)
                    {
                        job.setStage(Stage.HARVESTING);
                    }
                    break;
                case PLANTING:
                    planting();
                    break;
                case HARVESTING:
                    harvesting();
                    break;
            }
        }
    }

    public void make_land()
    {


        if(world.getBlock(farmLand.get(i).posX,farmLand.get(i).posY-1,farmLand.get(i).posZ)== Blocks.farmland)
        {
            world.setBlock(farmLand.get(i).posX,farmLand.get(i).posY-1,farmLand.get(i).posZ,Blocks.farmland);
            delay = 50;
        }
        else if(i > farmLand.size())
        {
            made_land = true;
        }


        i++;
    }

    private void planting()
    {
        //Only able to plant wheat, pumpkin and melon
        int slot = getFirstSeed();
        ItemStack seed = worker.getInventory().getStackInSlot(slot);

        for(ChunkCoordinates e: farmLand)
        {
            if(world.isAirBlock(e.posX,e.posY,e.posZ))
            {
                if (seed.getItem() == Items.wheat_seeds)
                {
                    world.setBlock(e.posX, e.posY, e.posZ, Blocks.wheat, 0, 0x3);
                    crops.add(e);
                }
                else if (seed.getItem() == Items.pumpkin_seeds)
                {
                    world.setBlock(e.posX, e.posY, e.posZ, Blocks.pumpkin_stem);
                    crops.add(e);
                }
                else if (seed.getItem() == Items.melon_seeds)
                {
                    world.setBlock(e.posX, e.posY, e.posZ, Blocks.melon_stem);
                    crops.add(e);
                }
            }
        }
    }

    private int getFirstSeed()
    {
        for (int slot = 0; slot < worker.getInventory().getSizeInventory(); slot++)
        {
            ItemStack stack = worker.getInventory().getStackInSlot(slot);

            if (stack != null && stack.getItem() != null)
            {
                Item content = stack.getItem();
                if(isSeed(content))
                {
                    return slot;
                }
            }
        }

        return -1;
    }

    private void harvesting()
    {
        for(ChunkCoordinates e: crops)
        {
           Block block =  world.getBlock(e.posX,e.posY,e.posZ);

            if(block==Blocks.melon_block || block == Blocks.pumpkin || world.getBlockMetadata(e.posX,e.posY,e.posZ) == 0x7)
            {
                List<ItemStack> items = ChunkCoordUtils.getBlockDrops(world,e , 0);

                for (ItemStack item : items)
                {
                    InventoryUtils.setStack(worker.getInventory(), item);
                }

                try
                {
                    //Crashes when called before Minecraft Client fully initialized
                    FMLClientHandler.instance().getClient().effectRenderer.addBlockDestroyEffects(e.posX, e.posY, e.posZ, block, world.getBlockMetadata(e.posX, e.posY, e.posZ));
                }
                catch(Exception exp)
                {
                    logger.info("Couldn't add effect");
                }
            }
        }
    }

    private void searchFarmableLand()
    {
        BuildingFarmer b = (BuildingFarmer)(worker.getWorkBuilding());
        if(b == null){return;}

        int buildingX =  worker.getWorkBuilding().getLocation().posX;
        int buildingY =  worker.getWorkBuilding().getLocation().posY;
        int buildingZ =  worker.getWorkBuilding().getLocation().posZ;

        for(int x=buildingX-b.getFarmRadius()-1;x<buildingX+b.getFarmRadius()+1;x++)
        {
            for(int z=buildingZ-b.getFarmRadius()-1;z<buildingZ+b.getFarmRadius()+1;z++)
            {
                Block block = world.getBlock(x,buildingY,z);
                if(block == Blocks.dirt || block == Blocks.grass || block == Blocks.farmland)
                {
                    farmLand.add(new ChunkCoordinates(x,buildingY+1,z));
                }
            }
        }

    }

    private boolean isSeed(Item item)
    {
            return item.toString().contains("seed") || item.toString().contains("Seed");
    }

    private boolean hasSeed()
    {
            for (int slot = 0; slot < worker.getInventory().getSizeInventory(); slot++)
            {
                ItemStack stack = worker.getInventory().getStackInSlot(slot);

                if (stack != null && stack.getItem() != null)
                {
                    Item content = stack.getItem();
                    if(isSeed(content))
                    {
                        return true;
                    }
                }
            }

            return false;
    }


    private boolean isStackTool(ItemStack stack)
    {
        return stack != null && (stack.getItem().getToolClasses(null /* not used */).contains("pickaxe") || stack.getItem().getToolClasses(null /* not used */).contains("shovel"));
    }

    private boolean isInHut(Block block)
    {
        if(worker.getWorkBuilding().getTileEntity()==null)
        {
            return false;
        }

        int size = worker.getWorkBuilding().getTileEntity().getSizeInventory();

        for(int i = 0; i < size; i++)
        {
            ItemStack stack = worker.getWorkBuilding().getTileEntity().getStackInSlot(i);
            if(stack != null && stack.getItem() instanceof ItemBlock)
            {
                    Block content = ((ItemBlock) stack.getItem()).field_150939_a;
                    if(content.equals(block))
                    {
                        ItemStack returnStack = InventoryUtils.setStack(worker.getInventory(), stack);

                        if (returnStack == null)
                        {
                            worker.getWorkBuilding().getTileEntity().decrStackSize(i, stack.stackSize);
                        }
                        else
                        {
                            worker.getWorkBuilding().getTileEntity().decrStackSize(i, stack.stackSize - returnStack.stackSize);
                        }

                        return true;
                    }
            }
        }
        return false;
    }

    private boolean isInHut(Item item)
    {

        if(worker.getWorkBuilding().getTileEntity()==null)
        {
            return false;
        }

        int size = worker.getWorkBuilding().getTileEntity().getSizeInventory();

        for(int i = 0; i < size; i++)
        {
            ItemStack stack = worker.getWorkBuilding().getTileEntity().getStackInSlot(i);
            if(stack != null)
            {
                Item content = stack.getItem();
                if(content.equals(item) || content.getToolClasses(null /* not used */).contains(NEED_ITEM))
                {
                    ItemStack returnStack = InventoryUtils.setStack(worker.getInventory(), stack);

                    if (returnStack == null)
                    {
                        worker.getWorkBuilding().getTileEntity().decrStackSize(i, stack.stackSize);
                    }
                    else
                    {
                        worker.getWorkBuilding().getTileEntity().decrStackSize(i, stack.stackSize - returnStack.stackSize);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private void dumpInventory()
    {
        if(ChunkCoordUtils.isWorkerAtSiteWithMove(worker, worker.getWorkBuilding().getLocation()))
           {
                for (int i = 0; i < worker.getInventory().getSizeInventory(); i++)
                {
                    ItemStack stack = worker.getInventory().getStackInSlot(i);
                    if (stack != null && !isStackTool(stack))
                    {
                        if (worker.getWorkBuilding().getTileEntity() != null)
                        {
                            ItemStack returnStack = InventoryUtils.setStack(worker.getWorkBuilding().getTileEntity(), stack);
                            if (returnStack == null)
                            {
                                worker.getInventory().decrStackSize(i, stack.stackSize);
                            }
                            else
                            {
                                worker.getInventory().decrStackSize(i, stack.stackSize - returnStack.stackSize);
                            }
                        }
                    }
                }
               job.setStage(Stage.WORKING);
            }
    }


    private boolean hasAllTheTools()
    {
        boolean hasHoeInHand;
        boolean hasSpadeInHand;

        if (worker.getHeldItem() == null)
        {
            hasHoeInHand = false;
            hasSpadeInHand = false;
        }
        else
        {
            hasHoeInHand = worker.getHeldItem().getItem().getToolClasses(null /* not used */).contains("hoe");
            hasSpadeInHand = worker.getHeldItem().getItem().getToolClasses(null /* not used */).contains("shovel");
        }

        int hasSpade = InventoryUtils.getFirstSlotContainingTool(worker.getInventory(), "shovel");
        int hasPickAxe = InventoryUtils.getFirstSlotContainingTool(worker.getInventory(), "hoe");


        boolean Spade = hasSpade > -1 || hasSpadeInHand;
        boolean Hoe = hasHoeInHand || hasPickAxe > -1;


            if (!Spade)
            {
                job.addItemNeededIfNotAlready(new ItemStack(Items.iron_shovel));
                NEED_ITEM = "shovel";
            }
            else if (!Hoe)
            {
                job.addItemNeededIfNotAlready(new ItemStack(Items.iron_pickaxe));
                NEED_ITEM = "hoe";
            }

        return !Hoe || !Spade;
    }

    void holdShovel()
    {
        worker.setHeldItem(InventoryUtils.getFirstSlotContainingTool(worker.getInventory(), "shovel"));
    }

    void holdHoe()
    {
        worker.setHeldItem(InventoryUtils.getFirstSlotContainingTool(worker.getInventory(), "hoe"));
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


    private int getDelay(Block block,int x, int y, int z)
    {
        return (int)(baseSpeed * worker.getHeldItem().getItem().getDigSpeed(worker.getHeldItem(), block, 0) * block.getBlockHardness(world,x,y,z));
    }

    private void setBlockFromInventory(int x, int y, int z, Block block)//TODO Plant seed
    {
        world.setBlock(x, y, z, block);
        int slot = inventoryContains(block);

        if(slot == -1)
        {
            job.addItemNeeded(new ItemStack(block));
            return;

        }
        worker.getInventory().decrStackSize(slot,1);
    }

    private int inventoryContains(Block block)//???
    {
        for (int slot = 0; slot < worker.getInventory().getSizeInventory(); slot++)
        {
            ItemStack stack = worker.getInventory().getStackInSlot(slot);

            if (stack != null && stack.getItem() instanceof ItemBlock)
            {
                Block content = ((ItemBlock) stack.getItem()).field_150939_a;
                if(content.equals(block))
                {
                    return slot;
                }
            }
        }

        return -1;

    }

    private int inventoryContains(Item item)//???
    {
        if(item == null)
        {
            return -1;
        }

        for (int slot = 0; slot < worker.getInventory().getSizeInventory(); slot++)
        {
            ItemStack stack = worker.getInventory().getStackInSlot(slot);

            if (stack != null && stack.getItem() != null)
            {
                Item content = stack.getItem();
                if(content.equals(item))
                {
                    return slot;
                }
            }
        }

        return -1;
    }

    private int inventoryContainsMany(Block block)
    {
        int count = 0;

        for (int slot = 0; slot < worker.getInventory().getSizeInventory(); slot++)
        {
            ItemStack stack = worker.getInventory().getStackInSlot(slot);

            if (stack != null && stack.getItem() instanceof ItemBlock)
            {
                Block content = ((ItemBlock) stack.getItem()).field_150939_a;
                if(content.equals(block))
                {
                    count += stack.stackSize;
                }
            }
        }
        return count;
    }

    private int inventoryContainsMany(Item item)
    {
        int count = 0;

        for (int slot = 0; slot < worker.getInventory().getSizeInventory(); slot++)
        {
            ItemStack stack = worker.getInventory().getStackInSlot(slot);

            if (stack != null && stack.getItem() instanceof ItemBlock)
            {
                Item content =  stack.getItem();
                if(content.equals(item))
                {
                    count += stack.stackSize;
                }
            }
        }
        return count;
    }


    public double getBaseSpeed()
    {
        return baseSpeed;
    }

    public void setBaseSpeed(double baseSpeed)
    {
        this.baseSpeed = baseSpeed;
    }

}