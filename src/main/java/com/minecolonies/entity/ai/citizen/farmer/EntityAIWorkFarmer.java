package com.minecolonies.entity.ai.citizen.farmer;

import com.minecolonies.colony.buildings.BuildingFarmer;
import com.minecolonies.colony.jobs.JobFarmer;
import com.minecolonies.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.util.BlockPosUtil;
import com.minecolonies.util.InventoryUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
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

public class EntityAIWorkFarmer extends AbstractEntityAIInteract<JobFarmer>
{
    private static final String         TOOL_TYPE_HOE     = "hoe";
    private static final String         TOOL_TYPE_SHOVEL  = "shovel";
    private static final String         RENDER_META_SEEDS = "Seeds";
    private static final Logger         logger            = LogManager.getLogger("Farmer");
    private              List<BlockPos> farmAbleLand      = new ArrayList<>();
    private              List<BlockPos> plowedLand        = new ArrayList<>();
    private              List<BlockPos> crops             = new ArrayList<>();
    private              List<BlockPos> crops2            = new ArrayList<>();
    private BlockPos currentFarmLand;
    private int    harvestCounter = 0;
    private String needItem       = "";
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

    /**
     *
     */
    private void checkForCrops()
    {
        if (crops.isEmpty() && !crops2.isEmpty())
        {
            crops.addAll(crops2);
            crops2.clear();
            job.setStage(Stage.NEED_SEEDS);
        }
    }

    @Override
    protected void updateRenderMetaData()
    {
        String renderMetaData = getRenderMetaSeeds();
        //TODO: Merge this into worker
        worker.setRenderMetadata(renderMetaData);
    }

    /**
     * @return String for seeds renderer if farmer has seeds, otherwise empty string
     */
    private String getRenderMetaSeeds()
    {
        if (hasSeed())
        {
            return RENDER_META_SEEDS;
        }
        return "";
    }

    private boolean hasSeed()
    {
        for (int slot = 0; slot < getInventory().getSizeInventory(); slot++)
        {
            ItemStack stack = getInventory().getStackInSlot(slot);

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

    private static boolean isSeed(Item item)
    {
        return item.toString().contains("Seed") || item.toString().contains("potatoe") || item.toString().contains(
                "carrot");
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
            if (BlockPosUtil.isWorkerAtSiteWithMove(worker, worker.getWorkBuilding().getLocation()))
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
                    ItemStack returnStack = InventoryUtils.setStack(getInventory(), stack);

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

    private boolean hasAllTheTools()
    {
        boolean hasHoeInHand;
        boolean hasSpadeInHand;

        if (worker.getHeldItemMainhand() == null)
        {
            hasHoeInHand = false;
            hasSpadeInHand = false;
        }
        else
        {
            hasHoeInHand = worker.getHeldItemMainhand().getUnlocalizedName().contains(TOOL_TYPE_HOE);
            hasSpadeInHand = worker.getHeldItemMainhand().getItem().getToolClasses(worker.getHeldItemMainhand()).contains(
                    TOOL_TYPE_SHOVEL);
        }

        int hasSpade = InventoryUtils.getFirstSlotContainingTool(getInventory(), TOOL_TYPE_SHOVEL);
        int hasHoe   = InventoryUtils.getFirstSlotContainingTool(getInventory(), TOOL_TYPE_HOE);

        boolean spade = hasSpade > -1 || hasSpadeInHand;
        boolean hoe   = hasHoeInHand || hasHoe > -1;

        if (!spade)
        {
            job.addItemNeededIfNotAlready(new ItemStack(Items.IRON_SHOVEL));
            needItem = TOOL_TYPE_SHOVEL;
        }
        else if (!hoe)
        {
            job.addItemNeededIfNotAlready(new ItemStack(Items.IRON_HOE));
            needItem = TOOL_TYPE_HOE;
        }

        return hoe && spade;
    }

    private int inventoryContains(Item item)//???
    {
        if (item == null)
        {
            return -1;
        }

        for (int slot = 0; slot < getInventory().getSizeInventory(); slot++)
        {
            ItemStack stack = getInventory().getStackInSlot(slot);

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
                makeLand();
                break;
            case NEED_SEEDS:
                needSeeds();
                break;
            case WORKING:
                doWork();
                break;
            case PLANTING:
                planting();
                break;
            case HARVESTING:
                harvesting();
                break;
            default:
                throw new IllegalArgumentException("Invalid state for Farmer.");
        }
    }

    private void doWork()
    {
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
    }

    private void needSeeds()
    {
        if (BlockPosUtil.isWorkerAtSiteWithMove(worker, worker.getWorkBuilding().getLocation()))
        {
            delay = 200;
            logger.info("Need Seeds");

            if (hasSeed() || hasSeedInHut())
            {
                job.setStage(Stage.PLANTING);
            }
        }
    }

    private void searchFarmableLand()
    {
        BuildingFarmer b = (BuildingFarmer) (worker.getWorkBuilding());
        if (b == null)
        {
            return;
        }

        int buildingX = worker.getWorkBuilding().getLocation().getX();
        int buildingY = worker.getWorkBuilding().getLocation().getY();
        int buildingZ = worker.getWorkBuilding().getLocation().getZ();

        for (int x = buildingX - b.getFarmRadius() - 1; x <= buildingX + b.getFarmRadius() + 1; x++)
        {
            for (int z = buildingZ - b.getFarmRadius() - 1; z <= buildingZ + b.getFarmRadius() + 1; z++)
            {
                Block block = world.getBlockState(new BlockPos(x, buildingY - 1, z)).getBlock();
                if (block == Blocks.DIRT || block == Blocks.GRASS)
                {
                    if (world.isAirBlock(new BlockPos(x, buildingY + 1, z)) && (farmAbleLand.isEmpty() || !farmAbleLand.contains(new BlockPos(x, buildingY, z))))
                    {
                        farmAbleLand.add(new BlockPos(x, buildingY, z));
                    }
                }
                else if (block == Blocks.FARMLAND)
                {
                    Block blockAbove = world.getBlockState(new BlockPos(x, buildingY, z)).getBlock();

                    if (blockAbove == Blocks.WHEAT
                        || blockAbove == Blocks.POTATOES
                        || blockAbove == Blocks.CARROTS
                        || blockAbove == Blocks.MELON_STEM
                        || blockAbove == Blocks.MELON_BLOCK
                        || blockAbove == Blocks.PUMPKIN
                        || blockAbove == Blocks.PUMPKIN_STEM)
                    {
                        if (crops.isEmpty() || !crops.contains(new BlockPos(x, buildingY, z)))
                        {
                            crops.add(new BlockPos(x, buildingY, z));
                        }
                    }
                    else if (plowedLand.isEmpty() || !plowedLand.contains(new BlockPos(x, buildingY, z)))
                    {
                        plowedLand.add(new BlockPos(x, buildingY, z));
                    }
                }
            }
        }
        job.setStage(Stage.WORKING);
    }

    private void makeLand()
    {
        if (!farmAbleLand.isEmpty())
        {
            if (world.getBlockState(new BlockPos(farmAbleLand.get(0).getX(), farmAbleLand.get(0).getY() - 1, farmAbleLand.get(0).getZ())).getBlock()
                != Blocks.FARMLAND)
            {
                delay = 20;
                world.setBlockState(new BlockPos(farmAbleLand.get(0).getX(),
                                                 farmAbleLand.get(0).getY() - 1,
                                                 farmAbleLand.get(0).getZ()),
                                    Blocks.FARMLAND.getDefaultState());
                currentFarmLand = new BlockPos(farmAbleLand.get(0).getX(),
                                               farmAbleLand.get(0).getY() - 1,
                                               farmAbleLand.get(0).getZ());
            }

            if (plowedLand.isEmpty() || !plowedLand.contains(new BlockPos(farmAbleLand.get(0).getX(),
                                                                          farmAbleLand.get(0).getY(),
                                                                          farmAbleLand.get(0).getZ())))
            {
                plowedLand.add(new BlockPos(farmAbleLand.get(0).getX(),
                                            farmAbleLand.get(0).getY(),
                                            farmAbleLand.get(0).getZ()));
                farmAbleLand.remove(0);
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

    private void planting()
    {
        //Only able to plant wheat, pumpkin and melon, Potatoe and carrot
        if (!plowedLand.isEmpty())
        {
            if (world.getBlockState(plowedLand.get(0)).getBlock() == Blocks.FARMLAND)
            {
                if (world.isAirBlock(plowedLand.get(0)))
                {
                    world.setBlockState(plowedLand.get(0), Blocks.FARMLAND.getDefaultState());
                    delay = 20;
                    currentFarmLand = new BlockPos(plowedLand.get(0).getX(),
                                                   plowedLand.get(0).getY() - 1,
                                                   plowedLand.get(0).getZ());

                    int       slot = getFirstSeed();
                    ItemStack seed = getInventory().getStackInSlot(slot);
                    if (seed == null)
                    {
                        job.setStage(Stage.WORKING);
                        return;
                    }

                    if (seed.getItem() == Items.WHEAT_SEEDS)
                    {
                        BlockPosUtil.setBlock(world, plowedLand.get(0), Blocks.WHEAT);
                    }
                    else if (seed.getItem() == Items.PUMPKIN_SEEDS)
                    {
                        BlockPosUtil.setBlock(world, plowedLand.get(0), Blocks.PUMPKIN_STEM);
                    }
                    else if (seed.getItem() == Items.MELON_SEEDS)
                    {
                        BlockPosUtil.setBlock(world, plowedLand.get(0), Blocks.MELON_STEM);
                    }
                    else if (seed.getItem() == Items.POTATO)
                    {
                        BlockPosUtil.setBlock(world, plowedLand.get(0), Blocks.POTATOES);
                    }
                    else if (seed.getItem() == Items.CARROT)
                    {
                        BlockPosUtil.setBlock(world, plowedLand.get(0), Blocks.CARROTS);
                    }
                    getInventory().decrStackSize(slot, 1);
                    delay = 10;
                }

                if (crops.isEmpty() || !crops.contains(new BlockPos(plowedLand.get(0).getX(),
                                                                      plowedLand.get(0).getY(),
                                                                      plowedLand.get(0).getZ())))
                {
                    crops.add(new BlockPos(plowedLand.get(0).getX(),
                                           plowedLand.get(0).getY(),
                                           plowedLand.get(0).getZ()));
                    plowedLand.remove(0);
                }
            }
            else
            {
                if (farmAbleLand.isEmpty() || !farmAbleLand.contains(new BlockPos(plowedLand.get(0).getX(),
                                                                                  plowedLand.get(0).getY(),
                                                                                  plowedLand.get(0).getZ())))
                {
                    farmAbleLand.add(new BlockPos(plowedLand.get(0).getX(),
                                                  plowedLand.get(0).getY(),
                                                  plowedLand.get(0).getZ()));
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

    private int getFirstSeed()
    {
        for (int slot = 0; slot < getInventory().getSizeInventory(); slot++)
        {
            ItemStack stack = getInventory().getStackInSlot(slot);

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

    private void harvesting()
    {
        if (!crops.isEmpty())
        {
            delay = 10;

            Block block = BlockPosUtil.getBlock(world, crops.get(0));

            if (BlockPosUtil.isWorkerAtSiteWithMove(worker, crops.get(0)))
            {
                //todo should be age, may be different!
                if (block == Blocks.MELON_BLOCK || block == Blocks.PUMPKIN || world.getBlockState(crops.get(0)).getValue(BlockCrops.AGE)
                                                                              == 0x7)
                {
                    List<ItemStack> items = BlockPosUtil.getBlockDrops(world, crops.get(0), 0);

                    for (ItemStack item : items)
                    {
                        InventoryUtils.setStack(getInventory(), item);
                    }
                    //TODO add block break effect
                    world.setBlockToAir(crops.get(0));
                    harvestCounter++;

                    if (!plowedLand.contains(crops.get(0)))
                    {
                        plowedLand.add(new BlockPos(crops.get(0)));
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
                    ItemStack returnStack = InventoryUtils.setStack(getInventory(), stack);

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
        if (BlockPosUtil.isWorkerAtSiteWithMove(worker, worker.getWorkBuilding().getLocation()))
        {
            for (int i = 0; i < getInventory().getSizeInventory(); i++)
            {
                ItemStack stack = getInventory().getStackInSlot(i);
                if (stack != null && !isStackTool(stack) && worker.getWorkBuilding().getTileEntity() != null)
                {
                    ItemStack returnStack = InventoryUtils.setStack(worker.getWorkBuilding().getTileEntity(),
                            stack);
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
            job.setStage(Stage.WORKING);
        }
    }

    private static boolean isStackTool(ItemStack stack)
    {
        return stack != null && (stack.getUnlocalizedName().contains(TOOL_TYPE_HOE) || stack.getItem().getToolClasses(
                stack).contains(TOOL_TYPE_SHOVEL));
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
