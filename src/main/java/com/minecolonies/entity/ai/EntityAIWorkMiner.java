package com.minecolonies.entity.ai;

import com.minecolonies.colony.buildings.BuildingMiner;
import com.minecolonies.colony.jobs.JobMiner;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.pathfinding.PathResult;
import com.minecolonies.inventory.InventoryCitizen;
import com.minecolonies.util.ChunkCoordUtils;
import com.minecolonies.util.InventoryUtils;
import com.minecolonies.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.ForgeHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Miner AI class
 * Created: December 20, 2014
 *
 * @author Raycoms, Kostronor
 */

public class EntityAIWorkMiner extends EntityAIWork<JobMiner> {
    //TODO ChunkCoordinates are call by reference!
    private static final String RENDER_META_TORCH = "Torch";
    private static final int RANGE_CHECK_AROUND_BUILDING_CHEST = 5;
    private static final int RANGE_CHECK_AROUND_BUILDING_LADDER = 3;
    private static final int RANGE_CHECK_AROUND_MINING_BLOCK = 2;
    /**
     * Add blocks to this list to exclude mine checks.
     * They can be mined for free. (be cautions with this)
     * <p>
     * Reasoning:
     * - Blocks.monster_egg:
     * Forge handling of this is a bit bogus, will later be removed.
     */
    public static Set<Block> canBeMined = new HashSet<>(Arrays.asList(
            Blocks.air, Blocks.fence, Blocks.planks, Blocks.ladder,
            Blocks.torch, Blocks.chest, Blocks.mob_spawner, Blocks.grass,
            Blocks.tallgrass, Blocks.cactus, Blocks.log, Blocks.log2,
            Blocks.monster_egg
    ));
    private static Logger logger = LogManager.getLogger("Miner");
    public List<ChunkCoordinates> localVein;
    //The current block to mine
    public ChunkCoordinates currentWorkingLocation;
    int neededPlanks = 64;
    int neededTorches = 4;
    //the last safe location now being air
    private ChunkCoordinates currentStandingPosition;
    /**
     * The time in ticks until the next action is made
     */
    private int delay = 0;
    private String NEED_ITEM;
    private int tryThreeTimes = 3;
    /**
     * If we have waited one delay
     */
    private boolean hasDelayed = false;
    private int currentY = 200;
    private int clear = 1;                   //Can be saved here for now
    private int blocksMined = 0;
    private Block hasToMine = Blocks.cobblestone;
    private ChunkCoordinates miningBlock;
    private ChunkCoordinates loc;

    private int clearNode = 0;
    private int canMineNode = 0;
    private int currentLevel = -1;
    private PathResult cachedPathResult;
    private List<ItemStack> itemsCurrentlyNeeded = new ArrayList<>();
    private List<ItemStack> itemsNeeded = new ArrayList<>();
    private int speechdelay = 0;
    private boolean needsShovel = false;
    private boolean needsPickaxe = false;
    private int needsPickaxeLevel = -1;
    private String speechdelaystring = "";
    private int speechrepeat = 1;



    /*
    Some logging of tool hardness
 Checking tile.stone
 Testing tile.stone on tile.tallgrass
 Requires 'null' of level -1 and diff is -1
 Testing tile.stone on tile.dirt
 Requires 'shovel' of level 0 and diff is -1
 Testing tile.stone on tile.stone
 Requires 'pickaxe' of level 0 and diff is -1
 Testing tile.stone on tile.oreIron
 Requires 'pickaxe' of level 1 and diff is -1
 Testing tile.stone on tile.oreDiamond
 Requires 'pickaxe' of level 2 and diff is -1
 Testing tile.stone on tile.obsidian
 Requires 'pickaxe' of level 3 and diff is -1
 Testing tile.stone on tile.bedrock
 Requires 'null' of level -1 and diff is -1

 Checking item.hoeWood
 Testing item.hoeWood on tile.tallgrass
 Requires 'null' of level -1 and diff is -1
 Testing item.hoeWood on tile.dirt
 Requires 'shovel' of level 0 and diff is -1
 Testing item.hoeWood on tile.stone
 Requires 'pickaxe' of level 0 and diff is -1
 Testing item.hoeWood on tile.oreIron
 Requires 'pickaxe' of level 1 and diff is -1
 Testing item.hoeWood on tile.oreDiamond
 Requires 'pickaxe' of level 2 and diff is -1
 Testing item.hoeWood on tile.obsidian
 Requires 'pickaxe' of level 3 and diff is -1
 Testing item.hoeWood on tile.bedrock
 Requires 'null' of level -1 and diff is -1

 Checking item.hatchetWood
 Testing item.hatchetWood on tile.tallgrass
 Requires 'null' of level -1 and diff is -1
 Testing item.hatchetWood on tile.dirt
 Requires 'shovel' of level 0 and diff is -1
 Testing item.hatchetWood on tile.stone
 Requires 'pickaxe' of level 0 and diff is -1
 Testing item.hatchetWood on tile.oreIron
 Requires 'pickaxe' of level 1 and diff is -1
 Testing item.hatchetWood on tile.oreDiamond
 Requires 'pickaxe' of level 2 and diff is -1
 Testing item.hatchetWood on tile.obsidian
 Requires 'pickaxe' of level 3 and diff is -1
 Testing item.hatchetWood on tile.bedrock
 Requires 'null' of level -1 and diff is -1

 Checking item.shovelWood
 Testing item.shovelWood on tile.tallgrass
 Requires 'null' of level -1 and diff is -1
 Testing item.shovelWood on tile.dirt
 Requires 'shovel' of level 0 and diff is 0
 Testing item.shovelWood on tile.stone
 Requires 'pickaxe' of level 0 and diff is -1
 Testing item.shovelWood on tile.oreIron
 Requires 'pickaxe' of level 1 and diff is -1
 Testing item.shovelWood on tile.oreDiamond
 Requires 'pickaxe' of level 2 and diff is -1
 Testing item.shovelWood on tile.obsidian
 Requires 'pickaxe' of level 3 and diff is -1
 Testing item.shovelWood on tile.bedrock
 Requires 'null' of level -1 and diff is -1

 Checking item.shovelDiamond
 Testing item.shovelDiamond on tile.tallgrass
 Requires 'null' of level -1 and diff is -1
 Testing item.shovelDiamond on tile.dirt
 Requires 'shovel' of level 0 and diff is 3
 Testing item.shovelDiamond on tile.stone
 Requires 'pickaxe' of level 0 and diff is -1
 Testing item.shovelDiamond on tile.oreIron
 Requires 'pickaxe' of level 1 and diff is -1
 Testing item.shovelDiamond on tile.oreDiamond
 Requires 'pickaxe' of level 2 and diff is -1
 Testing item.shovelDiamond on tile.obsidian
 Requires 'pickaxe' of level 3 and diff is -1
 Testing item.shovelDiamond on tile.bedrock
 Requires 'null' of level -1 and diff is -1

 Checking item.pickaxeWood
 Testing item.pickaxeWood on tile.tallgrass
 Requires 'null' of level -1 and diff is -1
 Testing item.pickaxeWood on tile.dirt
 Requires 'shovel' of level 0 and diff is -1
 Testing item.pickaxeWood on tile.stone
 Requires 'pickaxe' of level 0 and diff is 0
 Testing item.pickaxeWood on tile.oreIron
 Requires 'pickaxe' of level 1 and diff is 0
 Testing item.pickaxeWood on tile.oreDiamond
 Requires 'pickaxe' of level 2 and diff is 0
 Testing item.pickaxeWood on tile.obsidian
 Requires 'pickaxe' of level 3 and diff is 0
 Testing item.pickaxeWood on tile.bedrock
 Requires 'null' of level -1 and diff is -1

 Checking item.pickaxeStone
 Testing item.pickaxeStone on tile.tallgrass
 Requires 'null' of level -1 and diff is -1
 Testing item.pickaxeStone on tile.dirt
 Requires 'shovel' of level 0 and diff is -1
 Testing item.pickaxeStone on tile.stone
 Requires 'pickaxe' of level 0 and diff is 1
 Testing item.pickaxeStone on tile.oreIron
 Requires 'pickaxe' of level 1 and diff is 1
 Testing item.pickaxeStone on tile.oreDiamond
 Requires 'pickaxe' of level 2 and diff is 1
 Testing item.pickaxeStone on tile.obsidian
 Requires 'pickaxe' of level 3 and diff is 1
 Testing item.pickaxeStone on tile.bedrock
 Requires 'null' of level -1 and diff is -1

 Checking item.pickaxeIron
 Testing item.pickaxeIron on tile.tallgrass
 Requires 'null' of level -1 and diff is -1
 Testing item.pickaxeIron on tile.dirt
 Requires 'shovel' of level 0 and diff is -1
 Testing item.pickaxeIron on tile.stone
 Requires 'pickaxe' of level 0 and diff is 2
 Testing item.pickaxeIron on tile.oreIron
 Requires 'pickaxe' of level 1 and diff is 2
 Testing item.pickaxeIron on tile.oreDiamond
 Requires 'pickaxe' of level 2 and diff is 2
 Testing item.pickaxeIron on tile.obsidian
 Requires 'pickaxe' of level 3 and diff is 2
 Testing item.pickaxeIron on tile.bedrock
 Requires 'null' of level -1 and diff is -1

 Checking item.pickaxeDiamond
 Testing item.pickaxeDiamond on tile.tallgrass
 Requires 'null' of level -1 and diff is -1
 Testing item.pickaxeDiamond on tile.dirt
 Requires 'shovel' of level 0 and diff is -1
 Testing item.pickaxeDiamond on tile.stone
 Requires 'pickaxe' of level 0 and diff is 3
 Testing item.pickaxeDiamond on tile.oreIron
 Requires 'pickaxe' of level 1 and diff is 3
 Testing item.pickaxeDiamond on tile.oreDiamond
 Requires 'pickaxe' of level 2 and diff is 3
 Testing item.pickaxeDiamond on tile.obsidian
 Requires 'pickaxe' of level 3 and diff is 3
 Testing item.pickaxeDiamond on tile.bedrock
 Requires 'null' of level -1 and diff is -1
    */
    
    public EntityAIWorkMiner(JobMiner job) {
        super(job);
    }


    @Override
    public boolean shouldExecute() {
        return super.shouldExecute();
    }

    @Override
    public void startExecuting() {
        worker.setStatus(EntityCitizen.Status.WORKING);
        updateTask();
    }

    public boolean isLadderInitialized(BuildingMiner ownBuilding) {
        return ownBuilding.ladderLocation == null
                && ownBuilding.shaftStart == null;
    }

    private boolean checkThreeTimes() {
        if (tryThreeTimes > 0) {
            tryThreeTimes--;
        }
        return tryThreeTimes <= 0;
    }

    private String getRenderMetaTorch() {
        if (worker.hasitemInInventory(Blocks.torch)) {
            return RENDER_META_TORCH;
        }
        return "";
    }

    private void renderChestBelt() {
        String renderMetaData = getRenderMetaTorch();
        //TODO: Have pickaxe etc. displayed?
        worker.setRenderMetadata(renderMetaData);
    }

    private void initCurrentLevel() {
        if (currentLevel == -1) {
            currentLevel = getOwnBuilding().currentLevel;
        }
    }

    private int getCurrentLevel() {
        return getOwnBuilding().currentLevel;
    }

    private BuildingMiner getOwnBuilding() {
        return (BuildingMiner) worker.getWorkBuilding();
    }

    private boolean ladderNotFound() {
        if (isLadderInitialized(getOwnBuilding())) {
            if (checkThreeTimes()) {
                /*
                Not found after three updateTask calls
                Ladder is obstructed!
                */
                getOwnBuilding().foundLadder = false;
                job.setStage(Stage.SEARCHING_LADDER);
            }
            return true;
        }
        return false;
    }

    private void checkIfMineshaftIsAtBottomLimit() {
        BuildingMiner ownBuilding = getOwnBuilding();
        if (job.getStage() == Stage.MINING_NODE || job.getStage() == Stage.START_WORKING) {
            if (ownBuilding.ladderLocation.posY > ownBuilding.getMaxY()) {
                ownBuilding.clearedShaft = false;
                job.setStage(Stage.MINING_SHAFT);
            }
        }
    }

    private boolean waitingForSomething() {
        if (delay > 0) {
            if (job.getStage() == Stage.MINING_NODE
                    || job.getStage() == Stage.MINING_VEIN) {

                worker.hitBlockWithToolInHand(miningBlock);
            }
            if (job.getStage() == Stage.MINING_SHAFT) {
                if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, currentStandingPosition
                        , RANGE_CHECK_AROUND_MINING_BLOCK)) {
                    worker.hitBlockWithToolInHand(currentWorkingLocation);
                } else {
                    //Don't decrease delay as we are just walking...
                    return true;
                }
            }
            delay--;
            return true;
        }
        return false;
    }

    /*
    TODO: Not done, some things are weird...
     */
    private void tryContinueMining(BuildingMiner ownBuilding) {
        if (ownBuilding.levels != null) {
            if (ownBuilding.startingLevelNode == 5) {
                if (canMineNode <= 0) {
                    /*
                    12 fences == 29 planks + 1 Torch  -> 3 Nodes
                    TODO: Calculation definitely wrong :o
                    */
                    if (worker.getItemCountInInventory(ownBuilding.floorBlock) >= 30
                            && worker.hasitemInInventory(Items.coal)
                            || worker.getItemCountInInventory(Blocks.torch) >= 3) {
                        canMineNode = 3;
                    } else {
                        if (worker.hasitemInInventory(Items.coal)
                                && worker.getItemCountInInventory(Blocks.torch) < 3) {
                            job.addItemNeeded(new ItemStack(Items.coal));
                        } else {
                            job.addItemNeeded(new ItemStack(ownBuilding.floorBlock));
                        }
                    }

                } else {
                    mineNode(ownBuilding);
                }
            } else {
                mineNode(ownBuilding);
            }
        } else {
            createShaft(ownBuilding, ownBuilding.vectorX, ownBuilding.vectorZ);
        }
    }

    private void workAgain() {
        BuildingMiner ownBuilding = getOwnBuilding();
        switch (job.getStage()) {
            case MINING_NODE:
                tryContinueMining(ownBuilding);
                break;
            case INVENTORY_FULL:
                dumpInventory(ownBuilding);
                break;
            case SEARCHING_LADDER:
                findLadder(ownBuilding);
                break;
            case MINING_VEIN:
                mineVein(ownBuilding);
                break;
            case FILL_VEIN:
                fillVein();
                break;
            case MINING_SHAFT:
                createShaft(ownBuilding, ownBuilding.vectorX, ownBuilding.vectorZ);
                break;
            case START_WORKING:
                if (!ownBuilding.foundLadder) {
                    job.setStage(Stage.SEARCHING_LADDER);
                } else if (ownBuilding.activeNode != null) {
                    job.setStage(Stage.MINING_NODE);
                } else if (!ownBuilding.clearedShaft) {
                    job.setStage(Stage.MINING_SHAFT);
                } else {
                    job.setStage(Stage.MINING_NODE);
                }
                break;
        }
    }

    private boolean isItemNeeded(ItemStack neededItem) {
        BuildingMiner ownBuilding = getOwnBuilding();
        if (isMinerTool(neededItem)) {
            return hasAllTheTools() || isInHut(ownBuilding, neededItem.getItem());
        }
        if (neededItem.getItem().equals(new ItemStack(Blocks.torch).getItem()) || neededItem.getItem().equals(Items.coal)) {
            if (worker.hasitemInInventory(Blocks.torch)) {
                return false;
            }
            //TODO: Move Inventory handling to worker
            int slot = worker.findFirstSlotInInventoryWith(Items.coal);
            if (slot != -1) {
                worker.getInventory().decrStackSize(slot, 1);
                ItemStack stack = new ItemStack(neededItem.getItem(), 4);
                InventoryUtils.addItemStackToInventory(worker.getInventory(), stack);
                job.removeItemNeeded(neededItem);
                return false;
            } else if (isInHut(ownBuilding, Items.coal) || isInHut(ownBuilding, Blocks.torch)) {
                return false;
            }
            return true;
        }
        if (isInHut(ownBuilding, neededItem.getItem()) || worker.hasitemInInventory(neededItem.getItem())) {
            //TODO: Move item compare to lib
            if (ownBuilding.isFloorBlock(neededItem.getItem())) {
                if (worker.getItemCountInInventory(ownBuilding.floorBlock) >= getNumFloorNeeded()) {
                    return false;
                }
                worker.sendLocalizedChat("entity.miner.messageMoreBlocks", neededItem.getDisplayName());
            } else {
                return false;
            }
        }
        return true;
    }

    private int getNumFloorNeeded() {
        int numFloorNeeded;
        switch (job.getStage()) {
            case MINING_SHAFT:
                numFloorNeeded = 64;
                break;
            case MINING_NODE:
                numFloorNeeded = 30;
                break;
            default:
                numFloorNeeded = 0;
        }
        return numFloorNeeded;
    }

    private void askForNeededItems() {

        BuildingMiner ownBuilding = getOwnBuilding();
        if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, ownBuilding.getLocation())) {
            List<ItemStack> itemsNeeded = new ArrayList<>(job.getItemsNeeded());

            //TODO: Perhaps only one needed Item per update? should be enough...
            for (ItemStack neededItem : itemsNeeded) {
                if (isItemNeeded(neededItem)) {
                    //TODO: More sophisticated, wait until asking again etc.
                    worker.sendLocalizedChat("entity.miner.messageNeedBlockAndItem", neededItem.getDisplayName());
                    return;
                } else {
                    job.removeItemNeeded(neededItem);
                }
            }
            delay = 50;
        }
    }

    private void walkToBuilding() {
        if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, getOwnBuilding().getLocation()
                , RANGE_CHECK_AROUND_BUILDING_CHEST)) {
            logger.info("Work can start!");
            job.setStage(Stage.PREPARING);
        } else {
            logger.info("Walking to building");
            delay += 20;
        }
    }
    
    private void walkToLadder() {
        if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, getOwnBuilding().ladderLocation
                , RANGE_CHECK_AROUND_BUILDING_LADDER)) {
            logger.info("Checking the mine now!");
            job.setStage(Stage.CHECK_MINESHAFT);
        } else {
            logger.info("Walking to ladder");
            delay += 20;
        }
    }

    /**
     * Dump the miners inventory into his building chest.
     * Only useful tools are kept!
     * Only dumps one block at a time!
     */
    private boolean dumpOneMoreSlot() {
        if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, getOwnBuilding().getLocation()
                , RANGE_CHECK_AROUND_BUILDING_CHEST)) {
            //Iterate over worker inventory
            for (int i = 0; i < worker.getInventory().getSizeInventory(); i++) {
                ItemStack stack = worker.getInventory().getStackInSlot(i);
                //Check if it is a useful tool
                if (stack != null && !isMiningTool(stack)) {
                    if (getOwnBuilding().getTileEntity() != null) {
                        //Put it in Building chest
                        ItemStack returnStack = InventoryUtils.setStack(getOwnBuilding().getTileEntity(), stack);
                        if (returnStack == null) {
                            worker.getInventory().decrStackSize(i, stack.stackSize);
                        } else {
                            worker.getInventory().decrStackSize(i, stack.stackSize - returnStack.stackSize);
                        }
                    }
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    /**
     * Checks if this tool is useful for the miner.
     */
    private boolean isMiningTool(ItemStack itemStack) {
        return isPickaxe(itemStack) || isShovel(itemStack);
    }

    /**
     * Checks if this ItemStack can be used as a Pickaxe.
     */
    private boolean isPickaxe(ItemStack itemStack) {
        return getMiningLevel(itemStack, "pickaxe") >= 0;
    }

    /**
     * Checks if this ItemStack can be used as a Shovel.
     */
    private boolean isShovel(ItemStack itemStack) {
        return getMiningLevel(itemStack, "shovel") >= 0;
    }

    private void lookForLadder() {
        BuildingMiner buildingMiner = getOwnBuilding();
        int posX = buildingMiner.getLocation().posX;
        int posY = buildingMiner.getLocation().posY + 2;
        int posZ = buildingMiner.getLocation().posZ;
        for (int y = posY - 10; y < posY; y++) {
            for (int x = posX - 10; x < posX + 10; x++) {
                for (int z = posZ - 10; z < posZ + 10; z++) {

                    if (buildingMiner.foundLadder && buildingMiner.ladderLocation != null) {
                        if (world.getBlock(buildingMiner.ladderLocation.posX,
                                buildingMiner.ladderLocation.posY,
                                buildingMiner.ladderLocation.posZ) == Blocks.ladder) {
                            job.setStage(Stage.LADDER_FOUND);
                            return;
                        } else {
                            buildingMiner.foundLadder = false;
                            buildingMiner.ladderLocation = null;
                        }
                    }
                    if (world.getBlock(x, y, z).equals(Blocks.ladder)) {
                        int firstLadderY = getFirstLadder(x, y, z);
                        buildingMiner.ladderLocation = new ChunkCoordinates(x, firstLadderY, z);
                        logger.info("Found topmost ladder at x:" + x + " y: " + firstLadderY + " z: " + z);
                        delay += 10;
                        validateLadderOrientation();
                    }
                }
            }
        }
    }

    private void validateLadderOrientation() {
        BuildingMiner buildingMiner = getOwnBuilding();
        int x = buildingMiner.ladderLocation.posX;
        int y = buildingMiner.ladderLocation.posY;
        int z = buildingMiner.ladderLocation.posZ;

        //TODO: for 1.8 change to getBlockState
        int ladderOrientation = world.getBlockMetadata(x, y, z);
        //http://minecraft.gamepedia.com/Ladder

        if (ladderOrientation == 4) {
            //West
            buildingMiner.vectorX = 1;
            buildingMiner.vectorZ = 0;
        } else if (ladderOrientation == 5) {
            //East
            buildingMiner.vectorX = -1;
            buildingMiner.vectorZ = 0;
        } else if (ladderOrientation == 3) {
            //South
            buildingMiner.vectorZ = 1;
            buildingMiner.vectorX = 0;
        } else if (ladderOrientation == 2) {
            //North
            buildingMiner.vectorZ = -1;
            buildingMiner.vectorX = 0;
        } else {
            logger.info("Ladder not really working... trying fallback!");
            throw new IllegalStateException("Ladder metadata was " + ladderOrientation);
        }
        buildingMiner.cobbleLocation = new ChunkCoordinates(x - buildingMiner.vectorX, y, z - buildingMiner.vectorZ);
        buildingMiner.shaftStart = new ChunkCoordinates(x, getLastLadder(x, y, z) - 1, z);
        buildingMiner.foundLadder = true;
    }

    private void requestTool(Block curblock) {
        if (Objects.equals(curblock.getHarvestTool(0), "shovel")) {
            job.setStage(Stage.PREPARING);
            needsShovel = true;
        }
        if (Objects.equals(curblock.getHarvestTool(0), "pickaxe")) {
            job.setStage(Stage.PREPARING);
            needsPickaxe = true;
            needsPickaxeLevel = curblock.getHarvestLevel(0);
        }
    }

    private void doShaftMining() {

        currentWorkingLocation = getNextBlockInShaftToMine();
        if (currentWorkingLocation == null) {
            //TODO: Do something to advance ladder...
            logger.info("Finished with one layer!");
            advanceLadder();
            return;
        }

        //Note for future me:
        //we have to return; on false of this method
        //but omitted because end of method.
        mineBlock(currentWorkingLocation, currentStandingPosition);
    }

    private boolean missesItemsInInventory(ItemStack... items) {
        boolean allClear = true;
        for (ItemStack stack : items) {
            int countOfItem = worker.getItemCountInInventory(stack.getItem());
            if (countOfItem < stack.stackSize) {
                int itemsLeft = stack.stackSize - countOfItem;
                ItemStack requiredStack = new ItemStack(stack.getItem(), itemsLeft);
                itemsCurrentlyNeeded.add(requiredStack);
                allClear = false;
            }
        }
        if (allClear) {
            return false;
        }
        itemsNeeded.clear();
        for (ItemStack stack : items) {
            itemsNeeded.add(stack);
        }
        job.setStage(Stage.PREPARING);
        return true;
    }


    private void advanceLadder() {

        if (getOwnBuilding().startingLevelShaft >= 5) {
            job.setStage(Stage.BUILD_SHAFT);
            logger.info("We have to build a new level!");
            return;
        }

        if (missesItemsInInventory(
                new ItemStack(Blocks.cobblestone),
                new ItemStack(Blocks.ladder)
        )) {
            return;
        }

        ChunkCoordinates safestand = new ChunkCoordinates(
                getOwnBuilding().ladderLocation.posX,
                getLastLadder(getOwnBuilding().ladderLocation),
                getOwnBuilding().ladderLocation.posZ
        );
        ChunkCoordinates nextLadder = new ChunkCoordinates(
                getOwnBuilding().ladderLocation.posX,
                getLastLadder(getOwnBuilding().ladderLocation) - 1,
                getOwnBuilding().ladderLocation.posZ
        );
        ChunkCoordinates nextCobble = new ChunkCoordinates(
                getOwnBuilding().cobbleLocation.posX,
                getLastLadder(getOwnBuilding().ladderLocation) - 1,
                getOwnBuilding().cobbleLocation.posZ
        );

        if (!mineBlock(nextCobble, safestand)
                || !mineBlock(nextLadder, safestand)) {
            //waiting until blocks are mined
            return;
        }

        //Get ladder orientation
        int metadata = getBlockMetadata(safestand);
        //set cobblestone
        setBlockFromInventory(nextCobble, Blocks.cobblestone);
        //set ladder
        setBlockFromInventory(nextLadder, Blocks.ladder, metadata);
        getOwnBuilding().startingLevelShaft++;
    }

    /**
     * Checks for the right tools and waits for an appropriate delay.
     *
     * @param blockToMine the block to mine eventually
     * @param safeStand   a safe stand to mine from (AIR Block!)
     */
    private boolean checkMiningLocation(ChunkCoordinates blockToMine, ChunkCoordinates safeStand) {

        Block curBlock = world.getBlock(blockToMine.posX,
                blockToMine.posY, blockToMine.posZ);

        if (!holdEfficientTool(curBlock)) {
            //We are missing a tool to harvest this block...
            requestTool(curBlock);
            logger.info("We are missing a tool!");
            return true;
        }

        ItemStack tool = worker.getHeldItem();

        if (curBlock.getHarvestLevel(0)
                < getMiningLevel(tool, curBlock.getHarvestTool(0))) {
            //We have to high of a tool...
            //TODO: request lower tier tools
        }

        if (!ForgeHooks.canToolHarvestBlock(curBlock, 0, tool)) {
            logger.info("ForgeHook not in sync with EfficientTool...");
        }
        currentWorkingLocation = blockToMine;
        currentStandingPosition = safeStand;
        if (!hasDelayed) {
            delay += getBlockMiningDelay(curBlock, blockToMine);
            hasDelayed = true;
            return true;
        }
        hasDelayed = false;
        return false;
    }

    /**
     * Will simulate mining a block with particles ItemDrop etc.
     * Attention:
     * Because it simulates delay, it has to be called 2 times.
     * So make sure the code path up to this function is reachable a second time.
     * And make sure to immediately exit the update function when this returns false.
     */
    private boolean mineBlock(ChunkCoordinates blockToMine, ChunkCoordinates safeStand) {
        Block curBlock = world.getBlock(blockToMine.posX, blockToMine.posY, blockToMine.posZ);
        if (curBlock == null || curBlock == Blocks.air) {
            //no need to mine block...
            return true;
        }

        if (checkMiningLocation(blockToMine, safeStand)) {
            //we have to wait for delay
            return false;
        }

        ItemStack tool = worker.getHeldItem();


        //calculate fortune enchantment
        int fortune = 0;
        if (tool.isItemEnchanted()) {
            NBTTagList t = tool.getEnchantmentTagList();

            for (int i = 0; i < t.tagCount(); i++) {
                short id = t.getCompoundTagAt(i).getShort("id");
                if (id == 35) {
                    fortune = t.getCompoundTagAt(i).getShort("lvl");
                }
            }
        }

        //Dangerous TODO: validate that
        //Seems like dispatching the event manually is a bad idea? any clues?
        tool.getItem().onBlockDestroyed(tool, world, curBlock,
                blockToMine.posX, blockToMine.posY, blockToMine.posZ, worker);

        //if Tool breaks
        if (tool.stackSize < 1) {
            worker.setCurrentItemOrArmor(0, null);
            worker.getInventory().setInventorySlotContents(worker.getInventory().getHeldItemSlot(), null);
        }

        Utils.blockBreakSoundAndEffect(world,
                blockToMine.posX, blockToMine.posY, blockToMine.posZ,
                curBlock, world.getBlockMetadata(
                        blockToMine.posX, blockToMine.posY, blockToMine.posZ
                ));


        List<ItemStack> items = ChunkCoordUtils.getBlockDrops(world, blockToMine, fortune);
        for (ItemStack item : items) {
            InventoryUtils.setStack(worker.getInventory(), item);
        }

        world.setBlockToAir(blockToMine.posX, blockToMine.posY, blockToMine.posZ);
        blocksMined += 1;
        return true;
    }

    /**
     * Calculates the next non-air block to mine.
     * Will take the nearest block it finds.
     */
    private ChunkCoordinates getNextBlockInShaftToMine() {

        ChunkCoordinates ladderPos = getOwnBuilding().ladderLocation;
        int lastLadder = getLastLadder(ladderPos);
        if (currentWorkingLocation == null) {
            currentWorkingLocation = new ChunkCoordinates(
                    ladderPos.posX, lastLadder + 1, ladderPos.posZ);
        }
        Block block = getBlock(currentWorkingLocation);
        if(block != null && block != Blocks.air && block != Blocks.ladder){
            return currentWorkingLocation;
        }
        currentStandingPosition = currentWorkingLocation;
        ChunkCoordinates nextBlockToMine = null;
        double bestDistance = Double.MAX_VALUE;

        int xOffset = 3 * getOwnBuilding().vectorX;
        int zOffset = 3 * getOwnBuilding().vectorZ;

        //7x7 shaft find nearest block
        //Beware from positive to negative! to draw the miner to a wall to go down
        for (int x = 3 + xOffset; x >= -3 + xOffset; x--) {
            for (int z = -3 + zOffset; z <= 3 + zOffset; z++) {
                if (x == 0 && 0 == z) {
                    continue;
                }
                ChunkCoordinates curBlock = new ChunkCoordinates(ladderPos.posX + x,
                        lastLadder, ladderPos.posZ + z);
                double distance = curBlock.getDistanceSquaredToChunkCoordinates(ladderPos)
                        + Math.pow(curBlock.getDistanceSquaredToChunkCoordinates(currentWorkingLocation),2);
                if (distance < bestDistance
                        && !world.isAirBlock(curBlock.posX, curBlock.posY, curBlock.posZ)) {
                    nextBlockToMine = curBlock;
                    bestDistance = distance;
                }
            }
        }
        //find good looking standing position
        bestDistance = Double.MAX_VALUE;
        if (nextBlockToMine != null) {
            for (int x = 1; x >= -1; x--) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && 0 == z) {
                        continue;
                    }
                    ChunkCoordinates curBlock = new ChunkCoordinates(nextBlockToMine.posX + x,
                            lastLadder, nextBlockToMine.posZ + z);
                    double distance = curBlock.getDistanceSquaredToChunkCoordinates(ladderPos);
                    if (distance < bestDistance
                            && world.isAirBlock(curBlock.posX, curBlock.posY, curBlock.posZ)) {
                        currentStandingPosition = curBlock;
                        bestDistance = distance;
                    }
                }
            }
        }
        return nextBlockToMine;
    }

    private void syncNeededItemsWithInventory() {
        job.clearItemsNeeded();
        itemsNeeded.forEach(job::addItemNeeded);
        InventoryUtils.getInventoryAsList(worker.getInventory()).forEach(job::removeItemNeeded);
        itemsCurrentlyNeeded = new ArrayList<>(job.getItemsNeeded());
    }

    private void lookForNeededItems() {
        syncNeededItemsWithInventory();
        if (itemsCurrentlyNeeded.isEmpty()) {
            itemsNeeded.clear();
            job.clearItemsNeeded();
            return;
        }
        if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, getOwnBuilding().getLocation()
                , RANGE_CHECK_AROUND_BUILDING_CHEST)) {
            delay += 10;
            ItemStack first = itemsCurrentlyNeeded.get(0);
            //Takes one Stack from the hut if existent
            if (isInHut(first)) {
                return;
            }
            requestWithoutSpam(first.getDisplayName());
        }
    }

    private void takeItemStackFromChest(IInventory chest, ItemStack stack, int slot) {
        ItemStack returnStack = InventoryUtils.setStack(worker.getInventory(), stack);
        if (returnStack == null) {
            chest.decrStackSize(slot, stack.stackSize);
        } else {
            chest.decrStackSize(slot, stack.stackSize - returnStack.stackSize);
        }
    }

    private boolean isInHut(ItemStack is) {
        BuildingMiner buildingMiner = getOwnBuilding();
        if (buildingMiner.getTileEntity() == null) {
            return false;
        }
        int size = buildingMiner.getTileEntity().getSizeInventory();
        for (int i = 0; i < size; i++) {
            ItemStack stack = buildingMiner.getTileEntity().getStackInSlot(i);
            if (stack != null) {
                Item content = stack.getItem();
                if (content == is.getItem()) {
                    takeItemStackFromChest(buildingMiner.getTileEntity(), stack, i);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isShovelInHut() {
        BuildingMiner buildingMiner = getOwnBuilding();
        if (buildingMiner.getTileEntity() == null) {
            return false;
        }
        int size = buildingMiner.getTileEntity().getSizeInventory();
        for (int i = 0; i < size; i++) {
            ItemStack stack = buildingMiner.getTileEntity().getStackInSlot(i);
            if (stack != null && isShovel(stack)) {
                takeItemStackFromChest(buildingMiner.getTileEntity(), stack, i);
                return true;
            }
        }
        return false;
    }

    private boolean isPickaxeInHut(int minlevel) {
        BuildingMiner buildingMiner = getOwnBuilding();
        if (buildingMiner.getTileEntity() == null) {
            return false;
        }
        int size = buildingMiner.getTileEntity().getSizeInventory();
        for (int i = 0; i < size; i++) {
            ItemStack stack = buildingMiner.getTileEntity().getStackInSlot(i);
            int level = getMiningLevel(stack, "pickaxe");
            if (stack != null && checkIfPickaxeQualifies(minlevel, level)) {
                takeItemStackFromChest(buildingMiner.getTileEntity(), stack, i);
                return true;
            }
        }
        return false;
    }

    private boolean checkIfPickaxeQualifies(int minlevel, int level) {
        if (minlevel < 0) {
            return true;
        }
        if (minlevel == 0) {
            if (level >= 0 && level <= 1) {
                return true;
            }
        } else if (level >= minlevel) {
            return true;
        }
        return false;
    }

    private void requestWithoutSpam(String chat) {
        talkWithoutSpam("entity.miner.messageNeedBlockAndItem", chat);
    }

    private void talkWithoutSpam(String key, String chat) {
        String curstring = key + chat;
        if (Objects.equals(speechdelaystring, curstring)) {
            if (speechdelay > 0) {
                speechdelay--;
                return;
            }
            speechrepeat++;
        } else {
            speechdelay = 0;
            speechrepeat = 1;
        }
        worker.sendLocalizedChat(key, chat);
        speechdelaystring = key + chat;

        speechdelay = (int) Math.pow(30, speechrepeat);
        if (delay < 20) {
            delay = 20;
        }
    }

    private void checkForPickaxe(int minlevel) {
        //Check for a pickaxe
        boolean twoPickaxes = false;
        for (ItemStack is : InventoryUtils.getInventoryAsList(worker.getInventory())) {
            int level = getMiningLevel(is, "pickaxe");
            //Lower tools preferred
            if (checkIfPickaxeQualifies(minlevel, level)) {
                needsPickaxe = false;
                return;
            }
            //When we have two Pickaxes, ignore efficiency
            if (level >= minlevel) {
                if (twoPickaxes) {
                    needsPickaxe = false;
                    return;
                }
                twoPickaxes = true;
            }
        }
        delay += 20;
        if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, getOwnBuilding().getLocation()
                , RANGE_CHECK_AROUND_BUILDING_CHEST)) {
            if (isPickaxeInHut(minlevel)) {
                return;
            }
            requestWithoutSpam("Pickaxe at least level " + minlevel);
        }

    }

    private void checkForShovel() {
        //Check for a shovel
        needsShovel = InventoryUtils.getInventoryAsList(worker.getInventory())
                .stream().noneMatch(this::isShovel);

        if (!needsShovel) {
            return;
        }
        delay += 20;
        if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, getOwnBuilding().getLocation()
                , RANGE_CHECK_AROUND_BUILDING_CHEST)) {
            if (isShovelInHut()) {
                return;
            }
            requestWithoutSpam("Shovel");
        }

    }

    private boolean buildNextBlockInShaft() {
        ChunkCoordinates ladderPos = getOwnBuilding().ladderLocation;
        int lastLadder = getLastLadder(ladderPos) + 1;

        int xOffset = 3 * getOwnBuilding().vectorX;
        int zOffset = 3 * getOwnBuilding().vectorZ;
        //TODO: Really ugly building code, change to schematics

        //make area around it safe
        for (int x = -5 + xOffset; x <= 5 + xOffset; x++) {
            for (int z = -5 + zOffset; z <= 5 + zOffset; z++) {
                for (int y = 5; y >= -7; y--) {
                    if (x == 0 && 0 == z) {
                        continue;
                    }
                    ChunkCoordinates curBlock = new ChunkCoordinates(ladderPos.posX + x,
                            lastLadder + y, ladderPos.posZ + z);
                    int normalizedX = x - xOffset;
                    int normalizedZ = z - zOffset;
                    if (Math.abs(normalizedX) > 3
                            || Math.abs(normalizedZ) > 3) {
                        if (world.getBlock(curBlock.posX, curBlock.posY, curBlock.posZ) != Blocks.cobblestone) {
                            if (!mineBlock(curBlock, worker.getHomePosition())) {
                                delay = 1;
                                return true;
                            }
                            if (missesItemsInInventory(new ItemStack(Blocks.cobblestone))) {
                                return true;
                            }
                            setBlockFromInventory(curBlock, Blocks.cobblestone);
                            return true;
                        }

                    }
                }
            }
        }

        //Build the planks
        for (int x = -3 + xOffset; x <= 3 + xOffset; x++) {
            for (int z = -3 + zOffset; z <= 3 + zOffset; z++) {
                if (x == 0 && 0 == z) {
                    continue;
                }
                ChunkCoordinates curBlock = new ChunkCoordinates(ladderPos.posX + x,
                        lastLadder, ladderPos.posZ + z);
                int normalizedX = x - xOffset;
                int normalizedZ = z - zOffset;
                if (Math.abs(normalizedX) >= 2
                        || Math.abs(normalizedZ) >= 2) {
                    if (world.getBlock(curBlock.posX, curBlock.posY, curBlock.posZ) != Blocks.planks) {
                        delay += 10;
                        if (missesItemsInInventory(new ItemStack(Blocks.planks))) {
                            return true;
                        }
                        setBlockFromInventory(curBlock, Blocks.planks);
                        return true;
                    }

                }
            }
        }
        //Build fence
        for (int x = -3 + xOffset; x <= 3 + xOffset; x++) {
            for (int z = -3 + zOffset; z <= 3 + zOffset; z++) {
                if (x == 0 && 0 == z) {
                    continue;
                }
                ChunkCoordinates curBlock = new ChunkCoordinates(ladderPos.posX + x,
                        lastLadder + 1, ladderPos.posZ + z);
                int normalizedX = x - xOffset;
                int normalizedZ = z - zOffset;
                if ((Math.abs(normalizedX) == 2 && Math.abs(normalizedZ) < 3)
                        || (Math.abs(normalizedZ) == 2 && Math.abs(normalizedX) < 3)) {
                    if (world.getBlock(curBlock.posX, curBlock.posY, curBlock.posZ) != Blocks.fence) {
                        delay += 10;
                        if (missesItemsInInventory(new ItemStack(Blocks.fence))) {
                            return true;
                        }
                        setBlockFromInventory(curBlock, Blocks.fence);
                        return true;
                    }

                }
            }
        }
        //Build torches
        for (int x = -3 + xOffset; x <= 3 + xOffset; x++) {
            for (int z = -3 + zOffset; z <= 3 + zOffset; z++) {
                if (x == 0 && 0 == z) {
                    continue;
                }
                ChunkCoordinates curBlock = new ChunkCoordinates(ladderPos.posX + x,
                        lastLadder + 2, ladderPos.posZ + z);
                int normalizedX = x - xOffset;
                int normalizedZ = z - zOffset;
                if (Math.abs(normalizedX) == 2
                        && Math.abs(normalizedZ) == 2) {
                    if (world.getBlock(curBlock.posX, curBlock.posY, curBlock.posZ) != Blocks.torch) {
                        delay += 10;
                        if (missesItemsInInventory(new ItemStack(Blocks.torch))) {
                            return true;
                        }
                        setBlockFromInventory(curBlock, Blocks.torch);
                        return true;
                    }

                }
            }
        }
        return false;
    }

    private void doShaftBuilding() {
        if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, getOwnBuilding().getLocation()
                , RANGE_CHECK_AROUND_BUILDING_CHEST)) {
            if (buildNextBlockInShaft()) {
                return;
            }
            getOwnBuilding().startingLevelShaft = 0;
            job.setStage(Stage.START_WORKING);
        }

    }

    @Override
    public void updateTask() {
        //Something fatally wrong? Wait for init...
        if (null == getOwnBuilding()) {
            return;
        }

        //Update torch in chestbelt etc.
        renderChestBelt();

        //Mining animation while delay is decreasing.
        if (waitingForSomething()) {
            return;
        }

        //Miner wants to work but is not at building
        if (job.getStage() == Stage.START_WORKING) {
            walkToBuilding();
            return;
        }

        //Miner is at building and prepares for work
        if (job.getStage() == Stage.PREPARING) {
            if (!getOwnBuilding().foundLadder) {
                job.setStage(Stage.SEARCHING_LADDER);
                return;
            }
            //We need Items as it seems
            if (!itemsCurrentlyNeeded.isEmpty()) {
                lookForNeededItems();
                delay += 10;
                return;
            }
            //We need tools
            if (needsShovel) {
                checkForShovel();
                delay += 10;
                return;
            }
            if (needsPickaxe) {
                checkForPickaxe(needsPickaxeLevel);
                delay += 10;
                return;
            }

            job.setStage(Stage.CHECK_MINESHAFT);
        }

        //Miner is at building and dumps Inventory
        if (job.getStage() == Stage.INVENTORY_FULL) {


            if (dumpOneMoreSlot()) {
                delay += 10;
            } else {
                job.setStage(Stage.PREPARING);
            }

            return;
        }

        //Check for full inventory
        if (worker.isInventoryFull()) {
            job.setStage(Stage.INVENTORY_FULL);
            return;
        }

        //Looking for the ladder to walk to
        if (job.getStage() == Stage.SEARCHING_LADDER) {
            lookForLadder();
            return;
        }

        //Walking to the ladder to check out the mine
        if (job.getStage() == Stage.LADDER_FOUND) {
            walkToLadder();
            return;
        }

        //Standing on top of the ladder, checking out mine
        if (job.getStage() == Stage.CHECK_MINESHAFT) {
            //TODO: check if mineshaft needs repairing!

            //Check if we reached the mineshaft depth limit
            if (getLastLadder(getOwnBuilding().ladderLocation) < getOwnBuilding().getMaxY()) {
                job.setStage(Stage.MINING_NODE);
                getOwnBuilding().clearedShaft = true;
            }
            job.setStage(Stage.MINING_SHAFT);
            getOwnBuilding().clearedShaft = false;
            return;
        }

        if (job.getStage() == Stage.MINING_SHAFT) {
            doShaftMining();
            return;
        }

        if (job.getStage() == Stage.BUILD_SHAFT) {
            doShaftBuilding();
            return;
        }
        
        logger.info("[" + job.getStage() + "] Stopping here, old code ahead...");
        delay += 100;
        return;
        /*
        //Something fatally wrong? Wait for init...
        if (null == getOwnBuilding()) {
            return;
        }

        //Update torch in chestbelt etc.
        renderChestBelt();

        //TODO: Hack until currentLevel gets accessed over getter
        initCurrentLevel();

        //
        //Check if mineshaft ladder exists
        //TODO: check if MINING_NODE is really necessary
        //TODO: check if we have to rebuild some levels
        //
        if (job.getStage() != Stage.MINING_NODE && ladderNotFound()) {
            return;
        }

        //Check for mineshaft or node mining
        checkIfMineshaftIsAtBottomLimit();

        //Mining animation while delay is decreasing.
        if (waitingForSomething()) {
            return;
        }

        //Do we miss one needed item?
        if (job.isMissingNeededItem()) {
            askForNeededItems();
            return;
        }

        //All okay, work!
        workAgain();
        */
    }


    private int unsignVector(int i) {
        if (i == 0) {
            return 0;
        } else {
            return 1;
        }
    }

    private void mineNode(BuildingMiner buildingMiner) {
        if (buildingMiner.levels.size() <= currentLevel) {
            if (buildingMiner.clearedShaft) {
                currentLevel = buildingMiner.currentLevel = buildingMiner.levels.size() - 1;
                return;
            }

            if (currentLevel != buildingMiner.currentLevel) {
                currentLevel = buildingMiner.currentLevel;
            }

            buildingMiner.activeNode = null;
            job.setStage(Stage.MINING_SHAFT);
            return;
        }

        if (worker.getItemCountInInventory(buildingMiner.floorBlock) <= 10) {
            canMineNode = 0;
            job.addItemNeeded(new ItemStack(buildingMiner.floorBlock));
            return;
        } else if (worker.getItemCountInInventory(Blocks.torch) < 3 && !worker.hasitemInInventory(Items.coal)) {
            canMineNode = 0;
            job.addItemNeeded(new ItemStack(Items.coal));
            return;
        }

        if (buildingMiner.levels.get(currentLevel).getNodes().size() == 0) {
            buildingMiner.currentLevel++;
            currentLevel++;

            if (currentLevel >= buildingMiner.levels.size()) {
                buildingMiner.currentLevel = 0;
                currentLevel = 0;
            }
            return;
        }

        int depth = buildingMiner.levels.get(currentLevel).getDepth();

        if (buildingMiner.activeNode == null || buildingMiner.activeNode.getStatus() == Node.Status.COMPLETED || buildingMiner.activeNode.getStatus() == Node.Status.AVAILABLE) {
            currentLevel = buildingMiner.currentLevel;
            if (buildingMiner.levels.get(currentLevel).getNodes().size() == 0) {
                buildingMiner.currentLevel++;
                currentLevel++;

                if (currentLevel >= buildingMiner.levels.size()) {
                    buildingMiner.currentLevel = 0;
                    currentLevel = 0;
                }
                return;
            }

            int rand1 = (int) Math.floor(Math.random() * 4);
            int randomNum;

            if (buildingMiner.levels.get(currentLevel).getNodes() == null) {
                if (buildingMiner.levels.size() < currentLevel + 1) {
                    buildingMiner.currentLevel = currentLevel = buildingMiner.levels.size() - 1;
                    buildingMiner.activeNode = null;
                    job.setStage(Stage.MINING_SHAFT);
                    return;
                } else {
                    buildingMiner.currentLevel++;
                    currentLevel++;
                    return;
                }
            } else if (rand1 == 1) {
                randomNum = (int) Math.floor(Math.random() * buildingMiner.levels.get(currentLevel).getNodes().size());
            } else if (rand1 == 2) {
                randomNum = (int) (Math.random() * 3);
            } else {
                randomNum = buildingMiner.levels.get(currentLevel).getNodes().size() - 1;
            }

            if (buildingMiner.levels.get(currentLevel).getNodes().size() > randomNum) {
                Node node = buildingMiner.levels.get(currentLevel).getNodes().get(randomNum);

                if (node.getStatus() == Node.Status.AVAILABLE) {
                    int x = node.getX();
                    int y = buildingMiner.levels.get(currentLevel).getDepth();
                    int z = node.getZ();
                    Block block = world.getBlock(x, y, z);

                    if (buildingMiner.activeNode != null && job.getStage() == Stage.MINING_NODE && (block.isAir(world, x + node.getVectorX(), y, z + node.getVectorZ()) || !canWalkOn(x + node.getVectorX(), y, z + node.getVectorZ()))) {
                        logger.info("Removed Node because of Air Node: " + buildingMiner.active + " x: " + x + " z: " + z + " vectorX: " + buildingMiner.activeNode.getVectorX() + " vectorZ: " + buildingMiner.activeNode.getVectorZ());
                        buildingMiner.levels.get(currentLevel).getNodes().remove(randomNum);
                        return;
                    }

                    if (node.getX() > buildingMiner.shaftStart.posX + buildingMiner.getMaxX() || node.getZ() > buildingMiner.shaftStart.posZ + buildingMiner.getMaxZ() || node.getX() < buildingMiner.shaftStart.posX - buildingMiner.getMaxX() || node.getZ() < buildingMiner.shaftStart.posZ - buildingMiner.getMaxZ()) {
                        buildingMiner.levels.get(currentLevel).getNodes().remove(randomNum);
                        return;
                    }

                    logger.info("Starting Node: " + randomNum);

                    loc = new ChunkCoordinates(node.getX(), depth, node.getZ());
                    buildingMiner.activeNode = node;
                    buildingMiner.active = randomNum;
                    buildingMiner.activeNode.setStatus(Node.Status.IN_PROGRESS);
                    clearNode = 0;
                    buildingMiner.startingLevelNode = 0;
                    buildingMiner.markDirty();
                }
            }
        } else if (buildingMiner.activeNode.getStatus() == Node.Status.IN_PROGRESS) {
            if (loc == null) {
                loc = new ChunkCoordinates(buildingMiner.activeNode.getX() + buildingMiner.startingLevelNode * buildingMiner.activeNode.getVectorX(), depth, buildingMiner.activeNode.getZ() + buildingMiner.startingLevelNode * buildingMiner.activeNode.getVectorZ());
            }

            if (cachedPathResult != null && !cachedPathResult.isComputing()) {
                if (!cachedPathResult.getPathReachesDestination()) {

                    //TODO: Big hairy ball of if's because of nullpointer
                    Level level = buildingMiner.levels.get(currentLevel);
                    if (level != null) {
                        List<Node> nodes = level.getNodes();
                        if (nodes != null) {
                            Node node = nodes.get(buildingMiner.active);
                            if (node != null) {
                                node.setStatus(Node.Status.COMPLETED);
                            } else {
                                logger.info("Current level active node is null...");
                            }
                            nodes.remove(buildingMiner.active);
                        } else {
                            logger.info("Current level nodelist is null...");
                        }
                    } else {
                        logger.info("Current level is null...");
                    }
                    buildingMiner.activeNode.setStatus(Node.Status.COMPLETED);
                    currentLevel = buildingMiner.currentLevel;
                    logger.info("Unreachable Node!");
                    buildingMiner.markDirty();
                }
                cachedPathResult = null;
            }

            if (cachedPathResult == null && worker.getNavigator().noPath()) {
                if (Utils.isWorkerAtSiteWithMove(worker, loc.posX - buildingMiner.activeNode.getVectorX(), loc.posY - 1, loc.posZ - buildingMiner.activeNode.getVectorZ())) {
                    int uVX = 0;
                    int uVZ = 0;

                    if (buildingMiner.startingLevelNode == 5) {
                        buildingMiner.levels.get(currentLevel).getNodes().get(buildingMiner.active).setStatus(Node.Status.COMPLETED);
                        buildingMiner.activeNode.setStatus(Node.Status.COMPLETED);
                        buildingMiner.levels.get(currentLevel).getNodes().remove(buildingMiner.active);

                        if (buildingMiner.activeNode.getVectorX() == 0) {
                            if (!world.isAirBlock(buildingMiner.activeNode.getX() + 2, buildingMiner.levels.get(currentLevel).getDepth(), buildingMiner.activeNode.getZ() + 4 * buildingMiner.activeNode.getVectorZ())) {
                                buildingMiner.levels.get(currentLevel).addNewNode(buildingMiner.activeNode.getX() + 2, buildingMiner.activeNode.getZ() + 4 * buildingMiner.activeNode.getVectorZ(), unsignVector(buildingMiner.activeNode.getVectorZ()), unsignVector(buildingMiner.activeNode.getVectorX()));
                            }

                            if (!world.isAirBlock(buildingMiner.activeNode.getX() - 2, buildingMiner.levels.get(currentLevel).getDepth(), buildingMiner.activeNode.getZ() + 4 * buildingMiner.activeNode.getVectorZ())) {
                                buildingMiner.levels.get(currentLevel).addNewNode(buildingMiner.activeNode.getX() - 2, buildingMiner.activeNode.getZ() + 4 * buildingMiner.activeNode.getVectorZ(), -unsignVector(buildingMiner.activeNode.getVectorZ()), -unsignVector(buildingMiner.activeNode.getVectorX()));
                            }
                        } else {
                            if (!world.isAirBlock(buildingMiner.activeNode.getX() + 4 * buildingMiner.activeNode.getVectorX(), buildingMiner.levels.get(currentLevel).getDepth(), buildingMiner.activeNode.getZ() + 2)) {
                                buildingMiner.levels.get(currentLevel).addNewNode(buildingMiner.activeNode.getX() + 4 * buildingMiner.activeNode.getVectorX(), buildingMiner.activeNode.getZ() + 2, unsignVector(buildingMiner.activeNode.getVectorZ()), unsignVector(buildingMiner.activeNode.getVectorX()));
                            }

                            if (!world.isAirBlock(buildingMiner.activeNode.getX() + 4 * buildingMiner.activeNode.getVectorX(), buildingMiner.levels.get(currentLevel).getDepth(), buildingMiner.activeNode.getZ() - 2)) {
                                buildingMiner.levels.get(currentLevel).addNewNode(buildingMiner.activeNode.getX() + 4 * buildingMiner.activeNode.getVectorX(), buildingMiner.activeNode.getZ() - 2, -unsignVector(buildingMiner.activeNode.getVectorZ()), -unsignVector(buildingMiner.activeNode.getVectorX()));
                            }
                        }

                        if (!world.isAirBlock((buildingMiner.activeNode.getX() + 5 * buildingMiner.activeNode.getVectorX()), buildingMiner.levels.get(currentLevel).getDepth(), buildingMiner.activeNode.getZ() + 5 * buildingMiner.activeNode.getVectorZ())) {
                            buildingMiner.levels.get(currentLevel).addNewNode(buildingMiner.activeNode.getX() + 5 * buildingMiner.activeNode.getVectorX(), buildingMiner.activeNode.getZ() + 5 * buildingMiner.activeNode.getVectorZ(), buildingMiner.activeNode.getVectorX(), buildingMiner.activeNode.getVectorZ());
                        }
                        logger.info("Finished Node: " + buildingMiner.active);
                        currentLevel = buildingMiner.currentLevel;

                        buildingMiner.markDirty();
                    } else {
                        if (buildingMiner.activeNode.getVectorX() == 0) {
                            uVX = 1;
                        } else {
                            uVZ = 1;
                        }

                        switch (clearNode) {
                            case 0:
                                clearNode += mineCarefully(loc.posX, loc.posY + 1, loc.posZ, 0, 0, true, false, false, buildingMiner);
                                break;
                            case 1:
                                clearNode += mineCarefully(loc.posX - uVX, loc.posY + 1, loc.posZ - uVZ, -uVX, -uVZ, true, false, true, buildingMiner);
                                break;
                            case 2:
                                clearNode += mineCarefully(loc.posX + uVX, loc.posY + 1, loc.posZ + uVZ, uVX, uVZ, true, false, true, buildingMiner);
                                break;
                            case 3:
                                clearNode += mineCarefully(loc.posX + uVX, loc.posY, loc.posZ + uVZ, uVX, uVZ, false, false, true, buildingMiner);
                                break;
                            case 4:
                                clearNode += mineCarefully(loc.posX, loc.posY, loc.posZ, 0, 0, false, false, false, buildingMiner);
                                break;
                            case 5:
                                clearNode += mineCarefully(loc.posX - uVX, loc.posY, loc.posZ - uVZ, -uVX, -uVZ, false, false, true, buildingMiner);
                                break;
                            case 6:
                                clearNode += mineCarefully(loc.posX - uVX, loc.posY - 1, loc.posZ - uVZ, -uVX, -uVZ, false, true, true, buildingMiner);
                                break;
                            case 7:
                                clearNode += mineCarefully(loc.posX, loc.posY - 1, loc.posZ, 0, 0, false, true, false, buildingMiner);
                                break;
                            case 8:
                                clearNode += mineCarefully(loc.posX + uVX, loc.posY - 1, loc.posZ + uVZ, uVX, uVZ, false, true, true, buildingMiner);
                                break;
                            case 9:
                                if (buildingMiner.startingLevelNode == 2) {
                                    int neededPlanks = 10;
                                    canMineNode -= 1;

                                    world.setBlock(loc.posX, loc.posY + 1, loc.posZ, Blocks.planks);
                                    world.setBlock(loc.posX - uVX, loc.posY + 1, loc.posZ - uVZ, Blocks.planks);
                                    world.setBlock(loc.posX + uVX, loc.posY + 1, loc.posZ + uVZ, Blocks.planks);
                                    world.setBlock(loc.posX + uVX, loc.posY, loc.posZ + uVZ, Blocks.fence);
                                    world.setBlock(loc.posX - uVX, loc.posY, loc.posZ - uVZ, Blocks.fence);
                                    world.setBlock(loc.posX - uVX, loc.posY - 1, loc.posZ - uVZ, Blocks.fence);
                                    world.setBlock(loc.posX + uVX, loc.posY - 1, loc.posZ + uVZ, Blocks.fence);

                                    int meta = 0;

                                    if (buildingMiner.activeNode.getVectorZ() < 0) {
                                        meta = 3;
                                    } else if (buildingMiner.activeNode.getVectorZ() > 0) {
                                        meta = 4;
                                    } else if (buildingMiner.activeNode.getVectorX() < 0) {
                                        meta = 1;
                                    } else if (buildingMiner.activeNode.getVectorX() > 0) {
                                        meta = 2;
                                    }

                                    while (neededPlanks > 0) {
                                        int slot = worker.findFirstSlotInInventoryWith(buildingMiner.floorBlock);
                                        int size = worker.getInventory().getStackInSlot(slot).stackSize;

                                        if (size > neededPlanks) {
                                            worker.getInventory().decrStackSize(slot, 10);
                                            neededPlanks = 0;
                                        } else {
                                            worker.getInventory().decrStackSize(slot, size);
                                            neededPlanks -= size;
                                        }
                                    }

                                    if (worker.getItemCountInInventory(Blocks.torch) > 0) {
                                        int slot = worker.findFirstSlotInInventoryWith(Blocks.torch);
                                        worker.getInventory().decrStackSize(slot, 1);
                                    } else if (worker.getItemCountInInventory(Items.coal) > 0) {
                                        int slot = worker.findFirstSlotInInventoryWith(Items.coal);
                                        worker.getInventory().decrStackSize(slot, 1);
                                    }

                                    world.setBlock(loc.posX - buildingMiner.activeNode.getVectorX(), loc.posY + 1, loc.posZ - buildingMiner.activeNode.getVectorZ(), Blocks.torch, meta, 0x3);
                                }
                                buildingMiner.startingLevelNode += 1;
                                loc.set(loc.posX + buildingMiner.activeNode.getVectorX(), loc.posY, loc.posZ + buildingMiner.activeNode.getVectorZ());

                                clearNode = 0;
                                buildingMiner.markDirty();
                                break;
                        }
                    }
                } else {
                    cachedPathResult = worker.getNavigator().moveToXYZ(loc.posX - buildingMiner.activeNode.getVectorX(), loc.posY - 1, loc.posZ - buildingMiner.activeNode.getVectorZ(), 2.0F);
                }
            }
        }
    }

    private int mineCarefully(int x, int y, int z, int uVX, int uVZ, boolean above, boolean side, boolean under, BuildingMiner b) {
        Block block = world.getBlock(x, y, z);

        if (above) {
            checkAbove(x, y + 1, z);
        }
        if (under) {
            checkUnder(x, y - 1, z);
        }

        if (side && isALiquid(x + uVX, y, z + uVZ)) {
            setBlockFromInventory(x + uVX, y, z + uVZ, Blocks.cobblestone);
        }

        if (doMining(b, block, x, y, z)) {
            return 1;
        } else {
            return 0;
        }
    }

    private boolean isALiquid(int x, int y, int z) {
        return world.getBlock(x, y, z).getMaterial().isLiquid();
    }

    private void checkAbove(int x, int y, int z) {
        Block blockAbove = world.getBlock(x, y, z);
        isValuable(x, y, z);
        if (blockAbove == Blocks.sand || blockAbove == Blocks.gravel || !canWalkOn(x, y, z)) {
            setBlockFromInventory(x, y, z, Blocks.cobblestone);
        }
    }

    private void checkUnder(int x, int y, int z) {
        isValuable(x, y, z);
        ifNotAirSetBlock(x, y, z, Blocks.cobblestone);
    }

    private boolean isMinerTool(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        return stack.getItem().getToolClasses(stack).contains("pickaxe")
                || stack.getItem().getToolClasses(stack).contains("shovel");
    }

    private boolean isInHut(BuildingMiner b, Block block) {
        return isInHut(b, InventoryUtils.getItemFromBlock(block));
    }

    private boolean isInHut(BuildingMiner b, Item item) {

        if (b.getTileEntity() == null) {
            return false;
        }

        if (item == Items.coal && isInHut(b, Blocks.torch)) {
            return true;
        }

        int size = b.getTileEntity().getSizeInventory();

        for (int i = 0; i < size; i++) {
            ItemStack stack = b.getTileEntity().getStackInSlot(i);
            if (stack != null) {
                Item content = stack.getItem();
                if (content.equals(item) || content.getToolClasses(stack).contains(NEED_ITEM)) {
                    ItemStack returnStack = InventoryUtils.setStack(worker.getInventory(), stack);

                    if (returnStack == null) {
                        b.getTileEntity().decrStackSize(i, stack.stackSize);
                    } else {
                        b.getTileEntity().decrStackSize(i, stack.stackSize - returnStack.stackSize);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private void dumpInventory(BuildingMiner b) {
        if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, b.getLocation())) {
            for (int i = 0; i < worker.getInventory().getSizeInventory(); i++) {
                ItemStack stack = worker.getInventory().getStackInSlot(i);
                if (stack != null && !isMinerTool(stack)) {
                    if (b.getTileEntity() != null) {
                        ItemStack returnStack = InventoryUtils.setStack(b.getTileEntity(), stack);
                        if (returnStack == null) {
                            worker.getInventory().decrStackSize(i, stack.stackSize);
                        } else {
                            worker.getInventory().decrStackSize(i, stack.stackSize - returnStack.stackSize);
                        }
                    }
                }
            }
            job.setStage(Stage.START_WORKING);
            blocksMined = 0;
        }
    }

    private void mineVein(BuildingMiner b) {
        if (job.vein == null) {
            job.setStage(Stage.START_WORKING);
            return;
        }

        if (job.vein.size() == 0) {
            job.vein = null;
            job.veinId = 0;
            job.setStage(Stage.FILL_VEIN);
        } else {
            if (localVein == null) {
                localVein = new ArrayList<>();
            }

            //Can finish Mining vein in every distance at the moment
            ChunkCoordinates nextLoc = job.vein.get(0);
            localVein.add(nextLoc);
            Block block = ChunkCoordUtils.getBlock(world, nextLoc);
            int x = nextLoc.posX;
            int y = nextLoc.posY;
            int z = nextLoc.posZ;

            if (!doMining(b, block, nextLoc.posX, nextLoc.posY, nextLoc.posZ)) {
                return;
            }

            job.vein.remove(0);

            ifNotAirSetBlock(x, y - 1, z, Blocks.dirt);

            if (world.getBlock(x, y, z) == Blocks.sand || world.getBlock(x, y, z) == Blocks.gravel) {
                setBlockFromInventory(x, y + 1, z, Blocks.dirt);
            }

            avoidCloseLiquid(x, y, z);
        }
    }

    private void fillVein() {

        if (localVein == null || localVein.size() == 0) {
            localVein = null;
            job.setStage(Stage.START_WORKING);
        } else {
            ChunkCoordinates nextLoc = localVein.get(0);
            int x = nextLoc.posX;
            int y = nextLoc.posY;
            int z = nextLoc.posZ;
            localVein.remove(0);

            ifNotAirSetBlock(x, y, z, Blocks.cobblestone);
        }
    }

    private void avoidCloseLiquid(int centerX, int centerY, int centerZ) {
        for (int x = centerX - 3; x <= centerX + 3; x++) {
            for (int z = centerZ - 3; z <= centerZ + 3; z++) {
                for (int y = centerY; y <= centerY + 3; y++) {
                    Block block = world.getBlock(x, y, z);

                    if (block.getMaterial().isLiquid()) {
                        setBlockFromInventory(x, y, z, Blocks.dirt);
                    }
                }
            }
        }
    }

    private boolean hasAllTheTools() {
        boolean hasPickAxeInHand;
        boolean hasSpadeInHand;

        if (worker.getHeldItem() == null) {
            hasPickAxeInHand = false;
            hasSpadeInHand = false;
        } else {
            hasPickAxeInHand = worker.getHeldItem().getItem().getToolClasses(worker.getHeldItem()).contains("pickaxe");
            hasSpadeInHand = worker.getHeldItem().getItem().getToolClasses(worker.getHeldItem()).contains("shovel");
        }

        int hasSpade = InventoryUtils.getFirstSlotContainingTool(worker.getInventory(), "shovel");
        int hasPickAxe = InventoryUtils.getFirstSlotContainingTool(worker.getInventory(), "pickaxe");

        boolean Spade = hasSpade > -1 || hasSpadeInHand;
        boolean Pickaxe = hasPickAxeInHand || hasPickAxe > -1;

        if (!Spade) {
            job.addItemNeededIfNotAlready(new ItemStack(Items.iron_shovel));
            NEED_ITEM = "shovel";
        } else if (!Pickaxe) {
            job.addItemNeededIfNotAlready(new ItemStack(Items.iron_pickaxe));
            NEED_ITEM = "pickaxe";
        }

        if (!Pickaxe || !Spade) {
            return false;
        }

        boolean canMine = false;

        if (canBeMined.contains(hasToMine)) {
            canMine = true;
        }


        String tool = hasToMine.getHarvestTool(0);
        int level = getMiningLevel(worker.getHeldItem(), tool);
        int required = hasToMine.getHarvestLevel(0);

        if (level > required) {
            holdEfficientTool(hasToMine);
        }

        if (!canMine) {
            canMine = ForgeHooks.canToolHarvestBlock(hasToMine, 0, worker.getHeldItem());
        }
        if (!canMine) {
            if (hasPickAxeInHand) {
                holdShovel();
            } else {
                if (!holdEfficientTool(hasToMine)) {
                    return false;
                }
            }
            canMine = ForgeHooks.canToolHarvestBlock(hasToMine, 0, worker.getHeldItem());
        }
        return canMine;
    }

    int getMiningLevel(ItemStack stack, String tool) {
        if (tool == null) {
            return stack == null ? 0 : 1; //empty hand is best on blocks who don't care (0 better 1)
        }
        if (stack == null) {
            return -1;
        }
        return stack.getItem().getHarvestLevel(stack, tool);
    }

    void holdShovel() {
        worker.setHeldItem(InventoryUtils.getFirstSlotContainingTool(worker.getInventory(), "shovel"));
    }

    int getMostEfficientTool(Block target) {
        String tool = target.getHarvestTool(0);
        int required = target.getHarvestLevel(0);
        int bestSlot = -1;
        int bestLevel = Integer.MAX_VALUE;
        InventoryCitizen inventory = worker.getInventory();
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack item = inventory.getStackInSlot(i);
            int level = getMiningLevel(item, tool);
            if (level >= required && level < bestLevel) {
                bestSlot = i;
                bestLevel = level;
            }
        }
        return bestSlot;
    }


    boolean holdEfficientTool(Block target) {
        int bestSlot = getMostEfficientTool(target);
        if (bestSlot >= 0) {
            worker.setHeldItem(bestSlot);
            return true;
        }
        return false;
    }

    @Override
    public boolean continueExecuting() {
        return super.continueExecuting();
    }

    @Override
    public void resetTask() {
        super.resetTask();
    }

    private void findLadder(BuildingMiner buildingMiner) {
        int posX = buildingMiner.getLocation().posX;
        int posY = buildingMiner.getLocation().posY + 2;
        int posZ = buildingMiner.getLocation().posZ;

        for (int x = posX - 10; x < posX + 10; x++) {
            for (int z = posZ - 10; z < posZ + 10; z++) {
                for (int y = posY - 10; y < posY; y++) {
                    if (buildingMiner.foundLadder) {
                        job.setStage(Stage.MINING_SHAFT);
                        return;
                    } else if (world.getBlock(x, y, z).equals(Blocks.ladder)) {//Parameters unused
                        int lastY = getLastLadder(x, y, z);
                        buildingMiner.ladderLocation = new ChunkCoordinates(x, lastY, z);
                        logger.info("Found ladder at x:" + x + " y: " + lastY + " z: " + z);
                        delay = 10;

                        if (currentWorkingLocation == null) {
                            currentWorkingLocation = new ChunkCoordinates(buildingMiner.ladderLocation.posX, buildingMiner.ladderLocation.posY, buildingMiner.ladderLocation.posZ);
                        }
                        if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, buildingMiner.ladderLocation)) {
                            buildingMiner.cobbleLocation = new ChunkCoordinates(x, lastY, z);

                            if (world.getBlock(buildingMiner.ladderLocation.posX - 1, buildingMiner.ladderLocation.posY, buildingMiner.ladderLocation.posZ).equals(Blocks.cobblestone))//Parameters unused
                            {
                                buildingMiner.cobbleLocation = new ChunkCoordinates(x - 1, lastY, z);
                                buildingMiner.vectorX = 1;
                                buildingMiner.vectorZ = 0;
                                logger.info("Found cobble - West");
                                //West
                            } else if (world.getBlock(buildingMiner.ladderLocation.posX + 1, buildingMiner.ladderLocation.posY, buildingMiner.ladderLocation.posZ).equals(Blocks.cobblestone))//Parameters unused
                            {
                                buildingMiner.cobbleLocation = new ChunkCoordinates(x + 1, lastY, z);
                                buildingMiner.vectorX = -1;
                                buildingMiner.vectorZ = 0;
                                logger.info("Found cobble - East");
                                //East
                            } else if (world.getBlock(buildingMiner.ladderLocation.posX, buildingMiner.ladderLocation.posY, buildingMiner.ladderLocation.posZ - 1).equals(Blocks.cobblestone))//Parameters unused
                            {
                                buildingMiner.cobbleLocation = new ChunkCoordinates(x, lastY, z - 1);
                                buildingMiner.vectorZ = 1;
                                buildingMiner.vectorX = 0;
                                logger.info("Found cobble - South");
                                //South
                            } else if (world.getBlock(buildingMiner.ladderLocation.posX, buildingMiner.ladderLocation.posY, buildingMiner.ladderLocation.posZ + 1).equals(Blocks.cobblestone))//Parameters unused
                            {
                                buildingMiner.cobbleLocation = new ChunkCoordinates(x, lastY, z + 1);
                                buildingMiner.vectorZ = -1;
                                buildingMiner.vectorX = 0;
                                logger.info("Found cobble - North");
                                //North
                            }
                            //world.setBlockToAir(ladderLocation.posX, ladderLocation.posY - 1, ladderLocation.posZ);
                            currentWorkingLocation = new ChunkCoordinates(buildingMiner.ladderLocation.posX, buildingMiner.ladderLocation.posY - 1, buildingMiner.ladderLocation.posZ);
                            buildingMiner.shaftStart = new ChunkCoordinates(buildingMiner.ladderLocation.posX, buildingMiner.ladderLocation.posY - 1, buildingMiner.ladderLocation.posZ);
                            buildingMiner.foundLadder = true;
                            hasAllTheTools();
                            job.setStage(Stage.START_WORKING);
                            buildingMiner.markDirty();
                        }
                    }
                }
            }
        }
    }

    private void createShaft(BuildingMiner b, int vectorX, int vectorZ) {
        if (b.clearedShaft) {
            job.setStage(Stage.START_WORKING);
            return;
        }

        if (b.ladderLocation == null) {
            job.setStage(Stage.SEARCHING_LADDER);
            return;
        }

        if (currentWorkingLocation == null) {
            currentWorkingLocation = new ChunkCoordinates(b.ladderLocation.posX, b.ladderLocation.posY - 1, b.ladderLocation.posZ);
        }

        int x = currentWorkingLocation.posX;
        int y = currentWorkingLocation.posY;
        int z = currentWorkingLocation.posZ;
        currentY = currentWorkingLocation.posY;

        if (y <= b.getMaxY()) {
            b.clearedShaft = true;
            job.setStage(Stage.MINING_NODE);
        }

        //Needs 39+25 Planks + 4 Torches + 14 fence 5 to create floor-structure
        if (b.startingLevelShaft % 5 == 0 && b.startingLevelShaft != 0) {
            if ((worker.getItemCountInInventory(b.floorBlock) >= 64)
                    && (worker.hasitemInInventory(Items.coal) || worker.getItemCountInInventory(Blocks.torch) >= 4)) {
                if (clear < 50) {
                    switch (clear) {
                        case 1:

                            break;
                        case 2:
                            world.setBlock(x, y + 3, z, b.floorBlock);
                            world.setBlock(x, y + 4, z, b.fenceBlock);
                            break;
                        case 6:
                            world.setBlock(x, y + 4, z, b.fenceBlock);
                        case 7:
                            world.setBlock(x, y + 3, z, b.floorBlock);
                            break;
                        case 8:
                            if (vectorX == 0) {
                                x += 1;
                                z -= 7 * vectorZ;
                            } else if (vectorZ == 0) {
                                z += 1;
                                x -= 7 * vectorX;
                            }
                            world.setBlock(x, y + 3, z, b.floorBlock);
                            break;
                        case 9:
                            world.setBlock(x, y + 3, z, b.floorBlock);
                            world.setBlock(x, y + 4, z, b.fenceBlock);
                            break;
                        case 13:
                            world.setBlock(x, y + 4, z, b.fenceBlock);
                        case 14:
                            world.setBlock(x, y + 3, z, b.floorBlock);
                            break;
                        case 15:
                            if (vectorX == 0) {
                                x += 1;
                                z -= 7 * vectorZ;
                            } else if (vectorZ == 0) {
                                z += 1;
                                x -= 7 * vectorX;
                            }
                            world.setBlock(x, y + 3, z, b.floorBlock);
                            break;
                        case 16:
                            world.setBlock(x, y + 3, z, b.floorBlock);
                            world.setBlock(x, y + 4, z, b.fenceBlock);
                            world.setBlock(x, y + 5, z, Blocks.torch);
                            break;
                        case 17:
                        case 18:
                        case 19:
                            world.setBlock(x, y + 3, z, b.floorBlock);
                            world.setBlock(x, y + 4, z, b.fenceBlock);
                            break;
                        case 20:
                            world.setBlock(x, y + 4, z, b.fenceBlock);
                            world.setBlock(x, y + 5, z, Blocks.torch);
                        case 21:
                            world.setBlock(x, y + 3, z, b.floorBlock);
                            break;
                        case 22:
                            if (vectorX == 0) {
                                x += 1;
                                z -= 7 * vectorZ;
                            } else if (vectorZ == 0) {
                                z += 1;
                                x -= 7 * vectorX;
                            }
                        case 23:
                        case 24:
                        case 25:
                        case 26:
                        case 27:
                        case 28:
                            world.setBlock(x, y + 3, z, b.floorBlock);
                            break;
                        case 29:
                            if (vectorX == 0) {
                                x = x - 4;
                                z -= 7 * vectorZ;
                            } else if (vectorZ == 0) {
                                z = z - 4;
                                x -= 7 * vectorX;
                            }
                            world.setBlock(x, y + 3, z, b.floorBlock);
                            break;
                        case 30:
                            world.setBlock(x, y + 4, z, b.fenceBlock);
                            world.setBlock(x, y + 3, z, b.floorBlock);
                            break;
                        case 34:
                            world.setBlock(x, y + 4, z, b.fenceBlock);
                        case 35:
                            world.setBlock(x, y + 3, z, b.floorBlock);
                            break;
                        case 36:
                            if (vectorX == 0) {
                                x -= 1;
                                z -= 7 * vectorZ;
                            } else if (vectorZ == 0) {
                                z -= 1;
                                x -= 7 * vectorX;
                            }
                            world.setBlock(x, y + 3, z, b.floorBlock);
                            break;
                        case 37:
                            world.setBlock(x, y + 3, z, b.floorBlock);
                            world.setBlock(x, y + 4, z, b.fenceBlock);
                            world.setBlock(x, y + 5, z, Blocks.torch);
                            break;
                        case 38:
                        case 39:
                        case 40:
                            world.setBlock(x, y + 3, z, b.floorBlock);
                            world.setBlock(x, y + 4, z, b.fenceBlock);
                            break;
                        case 41:
                            world.setBlock(x, y + 4, z, b.fenceBlock);
                            world.setBlock(x, y + 5, z, Blocks.torch);
                        case 42:
                            world.setBlock(x, y + 3, z, b.floorBlock);
                            break;
                        case 43:
                            if (vectorX == 0) {
                                x -= 1;
                                z -= 7 * vectorZ;
                            } else if (vectorZ == 0) {
                                z -= 1;
                                x -= 7 * vectorX;
                            }
                        case 44:
                        case 45:
                        case 46:
                        case 47:
                        case 48:
                        case 49:
                            world.setBlock(x, y + 3, z, b.floorBlock);
                            break;
                    }
                    x = x + vectorX;
                    z = z + vectorZ;

                    currentWorkingLocation.set(x, y, z);
                    clear += 1;
                } else if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, b.ladderLocation)) {
                    while (neededPlanks > 0) {
                        int slot = worker.findFirstSlotInInventoryWith(b.floorBlock);
                        int size = worker.getInventory().getStackInSlot(slot).stackSize;
                        //TODO: Will decrement to much if stacksize > neededPlanks
                        worker.getInventory().decrStackSize(slot, size);
                        neededPlanks -= size;
                    }

                    if (worker.hasitemInInventory(Items.coal)) {
                        int slot = worker.findFirstSlotInInventoryWith(Items.coal);
                        worker.getInventory().decrStackSize(slot, 1);
                    } else if (worker.getItemCountInInventory(Blocks.torch) >= 4) {
                        while (neededTorches > 0) {
                            int slot = worker.findFirstSlotInInventoryWith(Blocks.torch);
                            int size = worker.getInventory().getStackInSlot(slot).stackSize;

                            if (size > 4) {
                                worker.getInventory().decrStackSize(slot, 4);
                                neededTorches -= 4;
                            } else {
                                worker.getInventory().decrStackSize(slot, size);
                                neededTorches -= size;
                            }
                        }
                    }

                    if (b.levels == null) {
                        b.levels = new ArrayList<>();
                    }
                    if (vectorX == 0) {
                        b.levels.add(new Level(b.shaftStart.posX, y + 5, b.shaftStart.posZ + 3 * vectorZ, b));

                    } else if (vectorZ == 0) {
                        b.levels.add(new Level(b.shaftStart.posX + 3 * vectorX, y + 5, b.shaftStart.posZ, b));

                    }
                    clear = 1;
                    b.startingLevelShaft++;
                    currentWorkingLocation.set(b.shaftStart.posX, b.ladderLocation.posY - 1, b.shaftStart.posZ);

                    b.markDirty();
                }
            } else {
                if (worker.getItemCountInInventory(b.floorBlock) >= 64) {
                    job.addItemNeeded(new ItemStack(Items.coal));
                } else {
                    job.addItemNeeded(new ItemStack(b.floorBlock));
                }
            }
        }
        //Mining shaft
        else if (worker.hasitemInInventory(Blocks.dirt) && worker.hasitemInInventory(Blocks.cobblestone)) {
            if (clear >= 50) {
                if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, b.ladderLocation)) {
                    b.cobbleLocation.set(b.cobbleLocation.posX, b.ladderLocation.posY - 1, b.cobbleLocation.posZ);
                    b.ladderLocation.set(b.shaftStart.posX, b.ladderLocation.posY - 1, b.shaftStart.posZ);

                    clear = 1;
                    b.startingLevelShaft++;
                    b.markDirty();

                    currentWorkingLocation.set(b.shaftStart.posX, currentWorkingLocation.posY - 1, b.shaftStart.posZ);
                }
            } else if (Utils.isWorkerAtSiteWithMove(worker, x, y, z, 3)) {
                worker.getLookHelper().setLookPosition(x, y, z, 90f, worker.getVerticalFaceSpeed());
                hasToMine = world.getBlock(x, y, z);

                if (hasAllTheTools()) {
                    if (!world.getBlock(x, y, z).isAir(world, x, y, z)) {
                        if (!doMining(b, world.getBlock(x, y, z), x, y, z) || job.getStage() != Stage.MINING_SHAFT) {
                            return;
                        }
                    }

                    if (clear < 50) {
                        //Check if Block after End is empty (Block of Dungeons...)
                        if ((clear - 1) % 7 == 0) {
                            isValuable(x - vectorX, y, z - vectorZ);
                            ifNotAirSetBlock(x - vectorX, y, z - vectorZ, Blocks.cobblestone);
                        } else if (clear % 7 == 0) {
                            isValuable(x + vectorX, y, z + vectorZ);
                            ifNotAirSetBlock(x + vectorX, y, z + vectorZ, Blocks.cobblestone);
                        }

                        switch (clear) {
                            case 1:
                                int meta = world.getBlockMetadata(b.ladderLocation.posX, b.ladderLocation.posY + 1, b.ladderLocation.posZ);
                                setBlockFromInventory(b.cobbleLocation.posX, b.ladderLocation.posY - 1, b.cobbleLocation.posZ, Blocks.cobblestone);
                                world.setBlock(b.ladderLocation.posX, b.ladderLocation.posY - 1, b.ladderLocation.posZ, Blocks.ladder, meta, 0x3);
                                break;
                            case 7:
                            case 14:
                                if (vectorX == 0) {
                                    x += 1;
                                    z -= 7 * vectorZ;
                                } else if (vectorZ == 0) {
                                    z += 1;
                                    x -= 7 * vectorX;
                                }
                                break;
                            case 21:
                                if (vectorX == 0) {
                                    x += 1;
                                    z -= 7 * vectorZ;
                                } else if (vectorZ == 0) {
                                    z += 1;
                                    x -= 7 * vectorX;
                                }
                            case 22:
                            case 23:
                            case 24:
                            case 25:
                            case 26:
                            case 27:
                                if (vectorX == 0) {
                                    isValuable(x + 1, y, z);
                                    ifNotAirSetBlock(x + 1, y, z, Blocks.cobblestone);
                                } else if (vectorZ == 0) {
                                    isValuable(x, y, z + 1);
                                    ifNotAirSetBlock(x, y, z + 1, Blocks.cobblestone);
                                }
                                break;
                            case 28:
                                if (vectorX == 0) {
                                    isValuable(x + 1, y, z);
                                    ifNotAirSetBlock(x + 1, y, z, Blocks.cobblestone);
                                } else if (vectorZ == 0) {
                                    isValuable(x, y, z + 1);
                                    ifNotAirSetBlock(x, y, z + 1, Blocks.cobblestone);
                                }
                                if (vectorX == 0) {
                                    x = x - 4;
                                    z -= 7 * vectorZ;
                                } else if (vectorZ == 0) {
                                    z = z - 4;
                                    x -= 7 * vectorX;
                                }
                                break;
                            case 35:
                                if (clear == 35) {
                                    if (vectorX == 0) {
                                        x -= 1;
                                        z -= 7 * vectorZ;
                                    } else if (vectorZ == 0) {
                                        z -= 1;
                                        x -= 7 * vectorX;
                                    }
                                }
                                break;
                            case 42:
                                if (clear == 42) {
                                    if (vectorX == 0) {
                                        x -= 1;
                                        z -= 7 * vectorZ;
                                    } else if (vectorZ == 0) {
                                        z -= 1;
                                        x -= 7 * vectorX;
                                    }
                                }
                            case 43:
                            case 44:
                            case 45:
                            case 46:
                            case 47:
                            case 48:
                            case 49:
                                if (vectorX == 0) {
                                    isValuable(x - 1, y, z);
                                    ifNotAirSetBlock(x - 1, y, z, Blocks.cobblestone);
                                } else if (vectorZ == 0) {
                                    isValuable(x, y, z - 1);
                                    ifNotAirSetBlock(x, y, z - 1, Blocks.cobblestone);
                                }
                                break;
                        }
                        x = x + vectorX;
                        z = z + vectorZ;

                        ifNotAirSetBlock(x, y - 1, z, Blocks.dirt);

                        currentWorkingLocation.set(x, y, z);
                        clear += 1;
                    }
                }
            }
        } else {
            if (!worker.hasitemInInventory(Blocks.cobblestone)) {
                job.addItemNeeded(new ItemStack(Blocks.cobblestone));
            } else {
                job.addItemNeeded(new ItemStack(Blocks.dirt));
            }
        }
    }

    private int getBlockMiningDelay(Block block, ChunkCoordinates chunkCoordinates) {
        return getBlockMiningDelay(block, chunkCoordinates.posX, chunkCoordinates.posY, chunkCoordinates.posZ);
    }

    private int getBlockMiningDelay(Block block, int x, int y, int z) {
        return (int) (worker.getHeldItem().getItem().getDigSpeed(worker.getHeldItem(), block, 0)
                * block.getBlockHardness(world, x, y, z));
    }

    private void ifNotAirSetBlock(int x, int y, int z, Block block) {
        if (!canWalkOn(x, y, z)) {
            setBlockFromInventory(x, y, z, block);
        }
    }

    private void setBlockFromInventory(int x, int y, int z, Block block) {
        world.setBlock(x, y, z, block);
        int slot = worker.findFirstSlotInInventoryWith(block);

        if (slot == -1) {
            if (block == Blocks.torch) {
                int slot2 = worker.findFirstSlotInInventoryWith(Items.coal);
                if (slot2 != -1) {
                    worker.getInventory().decrStackSize(slot, 1);
                    ItemStack stack = new ItemStack(block, 4);
                    InventoryUtils.addItemStackToInventory(worker.getInventory(), stack);
                }
            } else {

                job.addItemNeeded(new ItemStack(block));
                return;
            }
        }
        worker.getInventory().decrStackSize(slot, 1);
    }

    private void setBlockFromInventory(ChunkCoordinates location, Block block) {
        setBlockFromInventory(location, block, 0);
    }

    private void setBlockFromInventory(ChunkCoordinates location, Block block, int metadata) {
        int slot = worker.findFirstSlotInInventoryWith(block);
        if (slot != -1) {
            worker.getInventory().decrStackSize(slot, 1);
            //Flag 1+2 is needed for updates
            world.setBlock(location.posX, location.posY, location.posZ, block, metadata, 3);
        }
    }

    private int getBlockMetadata(ChunkCoordinates loc) {
        return world.getBlockMetadata(loc.posX, loc.posY, loc.posZ);
    }

    private Block getBlock(ChunkCoordinates loc) {
        return world.getBlock(loc.posX, loc.posY, loc.posZ);
    }

    private boolean doMining(BuildingMiner b, Block block, int x, int y, int z) {
        if (!hasAllTheTools()) {
            return false;
        }

        if (job.getStage() == Stage.MINING_NODE && b.activeNode == null) {
            return false;
        }

        ChunkCoordinates bk = new ChunkCoordinates(x, y, z);

        if (InventoryUtils.getOpenSlot(worker.getInventory()) == -1)    //inventory has an open slot - this doesn't account for slots with non full stacks
        {                                                               //also we still may have problems if the block drops multiple items
            job.setStage(Stage.INVENTORY_FULL);
            return false;
        }
        /*if(b.activeNode!=null && job.getStage() == Stage.MINING_NODE && (block.isAir(world,x+b.activeNode.getVectorX(),y,z+b.activeNode.getVectorZ()) || !canWalkOn(x+b.activeNode.getVectorX(),y,z+b.activeNode.getVectorZ()))) //-164 58 -225
        {
            b.levels.get(currentLevel).getNodes().get(b.active).setStatus(Node.Status.COMPLETED);
            b.activeNode.setStatus(Node.Status.COMPLETED);
            logger.info("Finished because of Air Node: " + b.active + " x: " + x + " z: " + z + " vectorX: " + b.activeNode.getVectorX() + " vectorZ: " + b.activeNode.getVectorZ());
            b.levels.get(currentLevel).getNodes().remove(b.active);

            return true;
        }*/

        if (job.getStage() == Stage.MINING_NODE && b.shaftStart.posX == x && b.shaftStart.posZ == z) {
            b.activeNode.setStatus(Node.Status.COMPLETED);
            b.levels.get(currentLevel).getNodes().get(b.active).setStatus(Node.Status.COMPLETED);
            logger.info("Finished because of Ladder Node: " + b.active);
            b.levels.get(currentLevel).getNodes().remove(b.active);
            return true;
        }

        if (job.getStage() == Stage.MINING_NODE) {
            isValuable(x, y + 1, z);
            isValuable(x, y - 1, z);
            isValuable(x + b.activeNode.getVectorZ(), y, z + b.activeNode.getVectorX());
            isValuable(x - b.activeNode.getVectorZ(), y, z - b.activeNode.getVectorX());
        }

        if (currentY == 200) {
            currentY = y;
        }

        if (block == Blocks.dirt || block == Blocks.gravel || block == Blocks.sand || block == Blocks.clay || block == Blocks.grass) {
            holdShovel();
        } else {
            holdEfficientTool(block);
        }
        hasToMine = block;
        ItemStack tool = worker.getInventory().getHeldItem();
        if (tool == null || !hasAllTheTools()) {
            return false;
        } else if (!ForgeHooks.canToolHarvestBlock(block, 0, tool) && !canBeMined.contains(hasToMine)) {
            hasToMine = block;
            return false;
        } else {
            avoidCloseLiquid(x, y, z);

            if (!hasDelayed) {
                miningBlock = new ChunkCoordinates(x, y, z);
                delay = getBlockMiningDelay(block, x, y, z);
                hasDelayed = true;
                return false;
            }
            miningBlock = null;
            hasDelayed = false;

            tool.getItem().onBlockDestroyed(tool, world, block, x, y, z, worker);//Dangerous
            if (tool.stackSize < 1)//if Tool breaks
            {
                worker.setCurrentItemOrArmor(0, null);
                worker.getInventory().setInventorySlotContents(worker.getInventory().getHeldItemSlot(), null);
            }

            Utils.blockBreakSoundAndEffect(world, x, y, z, block, world.getBlockMetadata(x, y, z));

            if (job.vein == null) {
                if (!isValuable(x, y, z)) {
                    int fortune = 0;
                    if (tool.isItemEnchanted()) {
                        NBTTagList t = tool.getEnchantmentTagList();

                        for (int i = 0; i < t.tagCount(); i++) {
                            short id = t.getCompoundTagAt(i).getShort("id");
                            if (id == 35) {
                                fortune = t.getCompoundTagAt(i).getShort("lvl");
                            }
                        }
                    }

                    List<ItemStack> items = ChunkCoordUtils.getBlockDrops(world, bk, fortune);
                    for (ItemStack item : items) {
                        InventoryUtils.setStack(worker.getInventory(), item);
                    }

                    world.setBlockToAir(x, y, z);
                    blocksMined += 1;
                }
            } else {
                int fortune = 0;
                if (tool.isItemEnchanted()) {
                    NBTTagList t = tool.getEnchantmentTagList();

                    for (int i = 0; i < t.tagCount(); i++) {
                        short id = t.getCompoundTagAt(0).getShort("id");
                        if (id == 35) {
                            fortune = t.getCompoundTagAt(0).getShort("lvl");
                        }
                    }
                }

                List<ItemStack> items = ChunkCoordUtils.getBlockDrops(world, bk, fortune);
                for (ItemStack item : items) {
                    InventoryUtils.setStack(worker.getInventory(), item);
                }

                world.setBlockToAir(x, y, z);
                blocksMined += 1;
            }

            if (job.getStage() == Stage.MINING_VEIN && MathHelper.floor_double(worker.getPosition().xCoord) == x && MathHelper.floor_double(worker.getPosition().yCoord) == y + 1 && MathHelper.floor_double(worker.getPosition().zCoord) == z) {
                setBlockFromInventory(x, y, z, Blocks.cobblestone);
            }

            if ((y < currentY && job.getStage() != Stage.MINING_NODE) && job.getStage() != Stage.MINING_VEIN) {
                setBlockFromInventory(x, y, z, Blocks.cobblestone);
            }

            if (job.getStage() != Stage.MINING_VEIN) {
                ifNotAirSetBlock(x, y - 1, z, Blocks.cobblestone);
            }
        }
        hasAllTheTools();

        if (blocksMined == 150) {
            job.setStage(Stage.INVENTORY_FULL);
        }

        return true;
    }

    private void findVein(int x, int y, int z) {
        job.setStage(Stage.MINING_VEIN);

        for (int x1 = x - 1; x1 <= x + 1; x1++) {
            for (int z1 = z - 1; z1 <= z + 1; z1++) {
                for (int y1 = y - 1; y1 <= y + 1; y1++) {
                    if (isValuable(x1, y1, z1)) {
                        ChunkCoordinates ore = new ChunkCoordinates(x1, y1, z1);
                        if (!job.vein.contains(ore)) {
                            job.vein.add(ore);
                        }
                    }
                }
            }
        }

        if ((job.veinId < job.vein.size())) {
            ChunkCoordinates v = job.vein.get(job.veinId++);

            findVein(v.posX, v.posY, v.posZ);
        }
    }

    private int getLastLadder(ChunkCoordinates chunkCoordinates) {
        return getLastLadder(chunkCoordinates.posX,
                chunkCoordinates.posY, chunkCoordinates.posZ);
    }

    private int getLastLadder(int x, int y, int z) {
        if (world.getBlock(x, y, z).isLadder(world, x, y, z, null)) {
            return getLastLadder(x, y - 1, z);
        } else {
            return y + 1;
        }
    }

    private int getFirstLadder(ChunkCoordinates chunkCoordinates) {
        return getFirstLadder(chunkCoordinates.posX,
                chunkCoordinates.posY, chunkCoordinates.posZ);
    }

    private int getFirstLadder(int x, int y, int z) {
        if (world.getBlock(x, y, z).isLadder(world, x, y, z, null)) {
            return getFirstLadder(x, y + 1, z);
        } else {
            return y - 1;
        }
    }

    private boolean canWalkOn(int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        return block.getMaterial().isSolid() && !block.equals(Blocks.web) && !world.isAirBlock(x, y, z);
    }

    private boolean isValuable(int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        String findOre = block.toString();

        if (job.vein == null && (findOre.contains("ore") || findOre.contains("Ore"))) {
            job.vein = new ArrayList<>();
            job.vein.add(new ChunkCoordinates(x, y, z));
            logger.info("Found ore");
            findVein(x, y, z);
            logger.info("finished finding ores: " + job.vein.size());

            job.setStage(Stage.MINING_VEIN);
        }

        return findOre.contains("ore") || findOre.contains("Ore");
    }

    public enum Stage {
        INVENTORY_FULL,
        SEARCHING_LADDER,
        MINING_VEIN,
        MINING_SHAFT,
        START_WORKING,
        MINING_NODE,
        PREPARING, START_MINING, LADDER_FOUND, CHECK_MINESHAFT, BUILD_SHAFT, FILL_VEIN
    }
}