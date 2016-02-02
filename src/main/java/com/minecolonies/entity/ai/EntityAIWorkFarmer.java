package com.minecolonies.entity.ai;

import com.minecolonies.colony.buildings.BuildingFarmer;
import com.minecolonies.colony.jobs.JobFarmer;
import com.minecolonies.util.ChunkCoordUtils;
import com.minecolonies.util.InventoryUtils;
import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Farmer AI class
 * Created: December 20, 2014
 *
 * @author Raycoms, Kostronor
 */

public class EntityAIWorkFarmer extends EntityAIWork<JobFarmer>
{
    private static final String TOOL_TYPE_HOE = "hoe";
    private static final String TOOL_TYPE_SHOVEL = "shovel";
    private static final String RENDER_META_SEEDS = "Seeds";
    private static Logger logger = LogManager.getLogger("Farmer");
    private List<ChunkCoordinates> farmAbleLand = new ArrayList<>();
    private List<ChunkCoordinates> plowedLand = new ArrayList<>();
    private List<ChunkCoordinates> crops = new ArrayList<>();
    private List<ChunkCoordinates> crops2 = new ArrayList<>();
    private ChunkCoordinates currentFarmLand;
    private int harvestCounter = 0;
    private String needItem = "";
    private double baseSpeed;
    private int delay = 0;

    //TODO Check for duplicates
    public EntityAIWorkFarmer(JobFarmer job)
    {
        super(job);
    }
    //TODO Planting randomly depending on option in Hut, each level, one more crop type
    //TODO Adding Language Strings in files

    @Override
    protected BuildingFarmer getOwnBuilding()
    {
        return (BuildingFarmer) (worker.getWorkBuilding());
    }

    private void checkForCrops()
    {
        if (crops.isEmpty() && !crops2.isEmpty())
        {
            crops.addAll(crops2);
            crops2.clear();
            job.setStage(Stage.NEED_SEEDS);
        }
    }

    private String getRenderMetaSeeds()
    {
        if (hasSeed())
        {
            return RENDER_META_SEEDS;
        }
        return "";
    }

    @Override
    protected void updateRenderMetaData()
    {
        String renderMetaData = getRenderMetaSeeds();
        //TODO: Merge this into worker
        worker.setRenderMetadata(renderMetaData);
    }

    private boolean waitingForSomething()
    {
        if (delay > 0)
        {
            if (job.getStage() == Stage.MAKING_LAND)
            {
                worker.hitBlockWithToolInHand(currentFarmLand);
            }
            delay--;
            return true;
        }
        return false;
    }

    private boolean itemsAreMissing()
    {
        if (job.isMissingNeededItem())
        {
            if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, worker.getWorkBuilding().getLocation()))
            {
                List<ItemStack> l = new CopyOnWriteArrayList<>();
                l.addAll(job.getItemsNeeded());

                for (ItemStack e : l)
                {
                    if (isInHut(e.getItem()) || hasAllTheTools() || inventoryContains(e.getItem()) != -1)
                    {
                        job.removeItemNeeded(e);
                        return true;
                    }
                    worker.sendLocalizedChat("entity.miner.messageNeedBlockAndItem", e.getDisplayName());
                }
                delay = 50;
            }
            return true;
        }
        return false;
    }

    private void startWorking()
    {
        switch (job.getStage())
        {
            case FULL_INVENTORY:
                dumpInventory();
                break;
            case SEARCHING_LAND:
                searchFarmableLand();
                break;
            case MAKING_LAND:
                make_land();
                break;
            case NEED_SEEDS:
                if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, worker.getWorkBuilding().getLocation()))
                {
                    delay = 200;
                    logger.info("Need Seeds");

                    if (hasSeed() || hasSeedInHut())
                    {
                        job.setStage(Stage.PLANTING);
                    }
                }
                break;
            case WORKING:
                if (farmAbleLand.isEmpty() && plowedLand.isEmpty() && crops.isEmpty())
                {
                    job.setStage(Stage.SEARCHING_LAND);
                }
                else if (!hasSeed() && crops.size() + crops2.size() < 10)
                {
                    job.setStage(Stage.NEED_SEEDS);
                }
                else if (!farmAbleLand.isEmpty())
                {
                    job.setStage(Stage.MAKING_LAND);
                }
                else if (hasSeed() && !plowedLand.isEmpty())
                {
                    job.setStage(Stage.PLANTING);
                }
                else if (!crops.isEmpty())
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

    @Override
    public void updateTask()
    {
        BuildingFarmer buildingFarmer = getOwnBuilding();
        if (buildingFarmer == null)
        {
            return;
        }

        updateRenderMetaData();

        //TODO: check what this does, add comments
        //Seems to transition crop locations somewhere???
        checkForCrops();

        if (waitingForSomething())
        {
            return;
        }



        //Old Code since here
        if (itemsAreMissing())
        {
            return;
        }

        if (!hasAllTheTools())
        {
            return;
        }
        startWorking();
    }

    /**
     * This method will be overridden by AI implementations
     */
    @Override
    protected void workOnTask()
    {
        //TODO: rework the farmer to use workOnTask eventually
    }

    private void searchFarmableLand()
    {
        BuildingFarmer b = (BuildingFarmer) (worker.getWorkBuilding());
        if (b == null){return;}

        int buildingX = worker.getWorkBuilding().getLocation().posX;
        int buildingY = worker.getWorkBuilding().getLocation().posY;
        int buildingZ = worker.getWorkBuilding().getLocation().posZ;

        for (int x = buildingX - b.getFarmRadius() - 1; x <= buildingX + b.getFarmRadius() + 1; x++)
        {
            for (int z = buildingZ - b.getFarmRadius() - 1; z <= buildingZ + b.getFarmRadius() + 1; z++)
            {
                Block block = world.getBlock(x, buildingY - 1, z);
                if (block == Blocks.dirt || block == Blocks.grass)
                {

                    if (world.isAirBlock(x, buildingY + 1, z))
                    {
                        if (farmAbleLand.size() == 0 || !farmAbleLand.contains(new ChunkCoordinates(x, buildingY, z)))
                        {
                            farmAbleLand.add(new ChunkCoordinates(x, buildingY, z));
                        }
                    }

                }
                else if (block == Blocks.farmland)
                {
                    Block blockAbove = world.getBlock(x, buildingY, z);

                    if (blockAbove == Blocks.wheat
                        || blockAbove == Blocks.potatoes
                        || blockAbove == Blocks.carrots
                        || blockAbove == Blocks.melon_stem
                        || blockAbove == Blocks.melon_block
                        || blockAbove == Blocks.pumpkin
                        || blockAbove == Blocks.pumpkin_stem)
                    {
                        if (crops.size() == 0 || !crops.contains(new ChunkCoordinates(x, buildingY, z)))
                        {
                            crops.add(new ChunkCoordinates(x, buildingY, z));
                        }
                    }
                    else if (plowedLand.size() == 0 || !plowedLand.contains(new ChunkCoordinates(x, buildingY, z)))
                    {
                        plowedLand.add(new ChunkCoordinates(x, buildingY, z));
                    }
                }
            }
        }
        job.setStage(Stage.WORKING);
    }

    public void make_land()
    {
        if (!farmAbleLand.isEmpty())
        {
            if (world.getBlock(farmAbleLand.get(0).posX, farmAbleLand.get(0).posY - 1, farmAbleLand.get(0).posZ)
                != Blocks.farmland)
            {
                delay = 20;
                world.setBlock(farmAbleLand.get(0).posX,
                               farmAbleLand.get(0).posY - 1,
                               farmAbleLand.get(0).posZ,
                               Blocks.farmland);
                currentFarmLand = new ChunkCoordinates(farmAbleLand.get(0).posX,
                                                       farmAbleLand.get(0).posY - 1,
                                                       farmAbleLand.get(0).posZ);
            }

            if (plowedLand.isEmpty() || !plowedLand.contains(new ChunkCoordinates(farmAbleLand.get(0).posX,
                                                                                    farmAbleLand.get(0).posY,
                                                                                    farmAbleLand.get(0).posZ)))
            {
                plowedLand.add(new ChunkCoordinates(farmAbleLand.get(0).posX,
                                                    farmAbleLand.get(0).posY,
                                                    farmAbleLand.get(0).posZ));
                farmAbleLand.remove(0);
            }
        }
        else
        {
            if (plowedLand.size() == 0 && crops.size() == 0)
            {
                job.setStage(Stage.SEARCHING_LAND);
            }
            else
            {
                job.setStage(Stage.WORKING);
            }
        }
    }

    private void planting()
    {
        //Only able to plant wheat, pumpkin and melon, Potatoe and carrot
        if (!plowedLand.isEmpty())
        {
            if (world.getBlock(plowedLand.get(0).posX, plowedLand.get(0).posY - 1, plowedLand.get(0).posZ)
                == Blocks.farmland)
            {
                if (world.isAirBlock(plowedLand.get(0).posX, plowedLand.get(0).posY, plowedLand.get(0).posZ))
                {
                    world.setBlock(plowedLand.get(0).posX,
                                   plowedLand.get(0).posY - 1,
                                   plowedLand.get(0).posZ,
                                   Blocks.farmland);
                    delay = 20;
                    currentFarmLand = new ChunkCoordinates(plowedLand.get(0).posX,
                                                           plowedLand.get(0).posY - 1,
                                                           plowedLand.get(0).posZ);

                    int slot = getFirstSeed();
                    ItemStack seed = worker.getInventory().getStackInSlot(slot);
                    if (seed == null)
                    {
                        job.setStage(Stage.WORKING);
                        return;
                    }

                    if (seed.getItem() == Items.wheat_seeds)
                    {
                        ChunkCoordUtils.setBlock(world, plowedLand.get(0), Blocks.wheat);
                    }
                    else if (seed.getItem() == Items.pumpkin_seeds)
                    {
                        ChunkCoordUtils.setBlock(world, plowedLand.get(0), Blocks.pumpkin_stem);
                    }
                    else if (seed.getItem() == Items.melon_seeds)
                    {
                        ChunkCoordUtils.setBlock(world, plowedLand.get(0), Blocks.melon_stem);
                    }
                    else if (seed.getItem() == Items.potato)
                    {
                        ChunkCoordUtils.setBlock(world, plowedLand.get(0), Blocks.potatoes);
                    }
                    else if (seed.getItem() == Items.carrot)
                    {
                        ChunkCoordUtils.setBlock(world, plowedLand.get(0), Blocks.carrots);
                    }
                    worker.getInventory().decrStackSize(slot, 1);
                    delay = 10;
                }

                if (crops.size() == 0 || !crops.contains(new ChunkCoordinates(plowedLand.get(0).posX,
                                                                              plowedLand.get(0).posY,
                                                                              plowedLand.get(0).posZ)))
                {
                    crops.add(new ChunkCoordinates(plowedLand.get(0).posX,
                                                   plowedLand.get(0).posY,
                                                   plowedLand.get(0).posZ));
                    plowedLand.remove(0);
                }
            }
            else
            {
                if (farmAbleLand.isEmpty() || !farmAbleLand.contains(new ChunkCoordinates(plowedLand.get(0).posX,
                                                                                            plowedLand.get(0).posY,
                                                                                            plowedLand.get(0).posZ)))
                {
                    farmAbleLand.add(new ChunkCoordinates(plowedLand.get(0).posX,
                                                          plowedLand.get(0).posY,
                                                          plowedLand.get(0).posZ));
                    plowedLand.remove(0);
                }
            }
        }
        else
        {
            if (plowedLand.isEmpty() && crops.isEmpty())
            {
                job.setStage(Stage.SEARCHING_LAND);
            }
            else
            {
                job.setStage(Stage.WORKING);
            }
        }
    }

    private void harvesting()
    {
        if (!crops.isEmpty())
        {
            delay = 10;

            Block block = ChunkCoordUtils.getBlock(world, crops.get(0));

            if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, crops.get(0)))
            {
                if (block == Blocks.melon_block || block == Blocks.pumpkin || world.getBlockMetadata(crops.get(0).posX,
                                                                                                     crops.get(0).posY,
                                                                                                     crops.get(0).posZ)
                                                                              == 0x7)
                {
                    List<ItemStack> items = ChunkCoordUtils.getBlockDrops(world, crops.get(0), 0);

                    for (ItemStack item : items)
                    {
                        InventoryUtils.setStack(worker.getInventory(), item);
                    }
                    try
                    {
                        //Crashes when called before Minecraft Client fully initialized
                        FMLClientHandler.instance().getClient().effectRenderer.addBlockDestroyEffects(crops.get(0).posX,
                                                                                                      crops.get(0).posY,
                                                                                                      crops.get(0).posZ,
                                                                                                      block,
                                                                                                      world.getBlockMetadata(
                                                                                                              crops.get(
                                                                                                                      0).posX,
                                                                                                              crops.get(
                                                                                                                      0).posY,
                                                                                                              crops.get(
                                                                                                                      0).posZ));
                    }
                    catch (Exception exp)
                    {
                        logger.info("Couldn't add effect", exp);
                    }
                    world.setBlockToAir(crops.get(0).posX, crops.get(0).posY, crops.get(0).posZ);
                    harvestCounter++;

                    if (!plowedLand.contains(new ChunkCoordinates(crops.get(0).posX,
                                                                  crops.get(0).posY,
                                                                  crops.get(0).posZ)))
                    {
                        plowedLand.add(new ChunkCoordinates(crops.get(0).posX, crops.get(0).posY, crops.get(0).posZ));
                        crops.remove(0);
                    }
                }
                else
                {
                    crops2.add(crops.get(0));
                    crops.remove(0);
                }
            }
        }
        else
        {
            if (plowedLand.isEmpty() && crops.isEmpty())
            {
                job.setStage(Stage.SEARCHING_LAND);
            }
            else
            {
                job.setStage(Stage.WORKING);
            }
        }

        if (harvestCounter == 15)
        {
            job.setStage(Stage.FULL_INVENTORY);
            harvestCounter = 0;
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
                if (isSeed(content))
                {
                    return slot;
                }
            }
        }
        return -1;
    }

    private boolean isSeed(Item item)
    {
        return item.toString().contains("Seed") || item.toString().contains("potatoe") || item.toString().contains(
                "carrot");
    }

    private boolean hasSeed()
    {
        for (int slot = 0; slot < worker.getInventory().getSizeInventory(); slot++)
        {
            ItemStack stack = worker.getInventory().getStackInSlot(slot);

            if (stack != null && stack.getItem() != null)
            {
                Item content = stack.getItem();
                if (isSeed(content))
                {
                    return true;
                }
            }
        }

        job.setStage(Stage.NEED_SEEDS);
        return false;
    }

    private boolean hasSeedInHut()
    {
        if (worker.getWorkBuilding().getTileEntity() == null)
        {
            return false;
        }

        int size = worker.getWorkBuilding().getTileEntity().getSizeInventory();

        for (int i = 0; i < size; i++)
        {
            ItemStack stack = worker.getWorkBuilding().getTileEntity().getStackInSlot(i);
            if (stack != null)
            {
                Item content = stack.getItem();
                if (isSeed(content))
                {
                    ItemStack returnStack = InventoryUtils.setStack(worker.getInventory(), stack);

                    if (returnStack == null)
                    {
                        worker.getWorkBuilding().getTileEntity().decrStackSize(i, stack.stackSize);
                    }
                    else
                    {
                        worker.getWorkBuilding().getTileEntity().decrStackSize(i,
                                                                               stack.stackSize - returnStack.stackSize);
                    }

                    return true;
                }
            }
        }
        return false;
    }

    private boolean isStackTool(ItemStack stack)
    {
        return stack != null && (stack.getUnlocalizedName().contains(TOOL_TYPE_HOE) || stack.getItem().getToolClasses(
                stack).contains(TOOL_TYPE_SHOVEL));
    }

    private boolean isInHut(Item item)
    {
        if (worker.getWorkBuilding().getTileEntity() == null)
        {
            return false;
        }

        int size = worker.getWorkBuilding().getTileEntity().getSizeInventory();

        for (int i = 0; i < size; i++)
        {
            ItemStack stack = worker.getWorkBuilding().getTileEntity().getStackInSlot(i);
            if (stack != null)
            {
                Item content = stack.getItem();
                if (content.equals(item)
                    || content.getToolClasses(stack).contains(needItem)
                    || stack.getUnlocalizedName().contains(needItem))
                {
                    ItemStack returnStack = InventoryUtils.setStack(worker.getInventory(), stack);

                    if (returnStack == null)
                    {
                        worker.getWorkBuilding().getTileEntity().decrStackSize(i, stack.stackSize);
                    }
                    else
                    {
                        worker.getWorkBuilding().getTileEntity().decrStackSize(i,
                                                                               stack.stackSize - returnStack.stackSize);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private void dumpInventory()
    {
        if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, worker.getWorkBuilding().getLocation()))
        {
            for (int i = 0; i < worker.getInventory().getSizeInventory(); i++)
            {
                ItemStack stack = worker.getInventory().getStackInSlot(i);
                if (stack != null && !isStackTool(stack))
                {
                    if (worker.getWorkBuilding().getTileEntity() != null)
                    {
                        ItemStack returnStack = InventoryUtils.setStack(worker.getWorkBuilding().getTileEntity(),
                                                                        stack);
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
            hasHoeInHand = worker.getHeldItem().getUnlocalizedName().contains(TOOL_TYPE_HOE);
            hasSpadeInHand = worker.getHeldItem().getItem().getToolClasses(worker.getHeldItem()).contains(
                    TOOL_TYPE_SHOVEL);
        }

        int hasSpade = InventoryUtils.getFirstSlotContainingTool(worker.getInventory(), TOOL_TYPE_SHOVEL);
        int hasHoe = InventoryUtils.getFirstSlotContainingTool(worker.getInventory(), TOOL_TYPE_HOE);

        boolean Spade = hasSpade > -1 || hasSpadeInHand;
        boolean Hoe = hasHoeInHand || hasHoe > -1;

        if (!Spade)
        {
            job.addItemNeededIfNotAlready(new ItemStack(Items.iron_shovel));
            needItem = TOOL_TYPE_SHOVEL;
        }
        else if (!Hoe)
        {
            job.addItemNeededIfNotAlready(new ItemStack(Items.iron_hoe));
            needItem = TOOL_TYPE_HOE;
        }

        return Hoe && Spade;
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

    private int inventoryContains(Item item)//???
    {
        if (item == null)
        {
            return -1;
        }

        for (int slot = 0; slot < worker.getInventory().getSizeInventory(); slot++)
        {
            ItemStack stack = worker.getInventory().getStackInSlot(slot);

            if (stack != null && stack.getItem() != null)
            {
                Item content = stack.getItem();
                if (content.equals(item))
                {
                    return slot;
                }
            }
        }
        return -1;
    }

    public enum Stage
    {
        WORKING,
        MAKING_LAND,
        PLANTING,
        NEED_SEEDS,
        HARVESTING,
        SEARCHING_LAND,
        FULL_INVENTORY

    }
}