package com.minecolonies.entity.ai;

import com.minecolonies.colony.buildings.BuildingMiner;
import com.minecolonies.colony.jobs.JobMiner;
import com.minecolonies.entity.EntityCitizen;
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

    private static final String RENDER_META_TORCH = "Torch";
    private static final int RANGE_CHECK_AROUND_BUILDING_CHEST = 5;
    private static final int RANGE_CHECK_AROUND_BUILDING_LADDER = 3;
    private static final int RANGE_CHECK_AROUND_MINING_BLOCK = 2;
    private static final int NODE_DISTANCE = 7;
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

    /*
    Blocks that will be ignored while building shaft/node walls and are certainly safe.
     */
    public static Set<Block> notReplacedInSecuringMine = new HashSet<>(Arrays.asList(
            Blocks.cobblestone, Blocks.stone, Blocks.dirt
    ));
    private static Logger logger = LogManager.getLogger("Miner");
    //The current block to mine
    public ChunkCoordinates currentWorkingLocation;
    //the last safe location now being air
    private ChunkCoordinates currentStandingPosition;
    /**
     * The time in ticks until the next action is made
     */
    private int delay = 0;
    private String NEED_ITEM;
    /**
     * If we have waited one delay
     */
    private boolean hasDelayed = false;
    private Block hasToMine = Blocks.cobblestone;

    private List<ItemStack> itemsCurrentlyNeeded = new ArrayList<>();
    private List<ItemStack> itemsNeeded = new ArrayList<>();
    private int speechdelay = 0;
    private boolean needsShovel = false;
    private boolean needsPickaxe = false;
    private int needsPickaxeLevel = -1;
    private String speechdelaystring = "";
    private int speechrepeat = 1;
    private Node workingNode = null;


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

    private BuildingMiner getOwnBuilding() {
        return (BuildingMiner) worker.getWorkBuilding();
    }

    private boolean waitingForSomething() {
        if (delay > 0) {
            if (job.getStage() == Stage.MINING_SHAFT
                    || job.getStage() == Stage.MINING_NODE) {
                if (worker.isWorkerAtSiteWithMove(currentStandingPosition
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

    private void walkToBuilding() {
        if (worker.isWorkerAtSiteWithMove(getOwnBuilding().getLocation()
                , RANGE_CHECK_AROUND_BUILDING_CHEST)) {
            logger.info("Work can start!");
            job.setStage(Stage.PREPARING);
        } else {
            logger.info("Walking to building");
            delay += 20;
        }
    }

    private void walkToLadder() {
        if (worker.isWorkerAtSiteWithMove(getOwnBuilding().ladderLocation
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
        if (worker.isWorkerAtSiteWithMove(getOwnBuilding().getLocation()
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
            return;
        }

        if (missesItemsInInventory(
                new ItemStack(Blocks.cobblestone, 2),
                new ItemStack(Blocks.ladder)
        )) {
            return;
        }

        ChunkCoordinates safeStand = new ChunkCoordinates(
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
        ChunkCoordinates safeCobble = new ChunkCoordinates(
                getOwnBuilding().ladderLocation.posX,
                getLastLadder(getOwnBuilding().ladderLocation) - 2,
                getOwnBuilding().ladderLocation.posZ
        );

        int xOffset = 3 * getOwnBuilding().vectorX;
        int zOffset = 3 * getOwnBuilding().vectorZ;
        //Check for safe floor
        for (int x = -4 + xOffset; x <= 4 + xOffset; x++) {
            for (int z = -4 + zOffset; z <= 4 + zOffset; z++) {
                ChunkCoordinates curBlock = new ChunkCoordinates(safeCobble.posX + x,
                        safeCobble.posY, safeCobble.posZ + z);
                if (!secureBlock(curBlock, currentStandingPosition)) {
                    return;
                }
            }
        }


        if (!mineBlock(nextCobble, safeStand)
                || !mineBlock(nextLadder, safeStand)) {
            //waiting until blocks are mined
            return;
        }


        //Get ladder orientation
        int metadata = getBlockMetadata(safeStand);
        //set cobblestone
        setBlockFromInventory(nextCobble, Blocks.cobblestone);
        //set ladder
        setBlockFromInventory(nextLadder, Blocks.ladder, metadata);
        getOwnBuilding().startingLevelShaft++;
        job.setStage(Stage.CHECK_MINESHAFT);
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

        if (tool != null && !ForgeHooks.canToolHarvestBlock(curBlock, 0, tool)
                && curBlock != Blocks.bedrock) {
            logger.info("ForgeHook not in sync with EfficientTool for " + curBlock + " and " + tool);
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

    private int getFortuneOf(ItemStack tool) {
        if (tool == null) {
            return 0;
        }
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
        return fortune;
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
        int fortune = getFortuneOf(tool);

        //Dangerous TODO: validate that
        //Seems like dispatching the event manually is a bad idea? any clues?
        if (tool != null) {
            //Reduce durability if not using hand
            tool.getItem().onBlockDestroyed(tool, world, curBlock,
                    blockToMine.posX, blockToMine.posY, blockToMine.posZ, worker);
        }

        //if Tool breaks
        if (tool != null && tool.stackSize < 1) {
            worker.setCurrentItemOrArmor(0, null);
            worker.getInventory().setInventorySlotContents(worker.getInventory().getHeldItemSlot(), null);
        }

        Utils.blockBreakSoundAndEffect(world,
                blockToMine.posX, blockToMine.posY, blockToMine.posZ,
                curBlock, world.getBlockMetadata(
                        blockToMine.posX, blockToMine.posY, blockToMine.posZ
                ));
        //Don't drop bedrock but we want to mine bedrock in some cases...
        if (curBlock != Blocks.bedrock) {
            List<ItemStack> items = ChunkCoordUtils.getBlockDrops(world, blockToMine, fortune);
            for (ItemStack item : items) {
                InventoryUtils.setStack(worker.getInventory(), item);
            }
        }

        world.setBlockToAir(blockToMine.posX, blockToMine.posY, blockToMine.posZ);
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
        if (block != null && block != Blocks.air && block != Blocks.ladder) {
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
                        + Math.pow(curBlock.getDistanceSquaredToChunkCoordinates(currentWorkingLocation), 2);
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
        if (worker.isWorkerAtSiteWithMove(getOwnBuilding().getLocation()
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
        if (worker.isWorkerAtSiteWithMove(getOwnBuilding().getLocation()
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
        if (worker.isWorkerAtSiteWithMove(getOwnBuilding().getLocation()
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
                    if ((x == 0 && 0 == z)
                            || lastLadder + y <= 1) {
                        continue;
                    }
                    ChunkCoordinates curBlock = new ChunkCoordinates(ladderPos.posX + x,
                            lastLadder + y, ladderPos.posZ + z);
                    int normalizedX = x - xOffset;
                    int normalizedZ = z - zOffset;
                    if (Math.abs(normalizedX) > 3
                            || Math.abs(normalizedZ) > 3) {
                        if (!notReplacedInSecuringMine.contains(
                                world.getBlock(curBlock.posX, curBlock.posY, curBlock.posZ))) {
                            if (!mineBlock(curBlock, getOwnBuilding().getLocation())) {
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

        Level currentLevel = new Level(getOwnBuilding(), lastLadder);
        getOwnBuilding().addLevel(currentLevel);
        getOwnBuilding().currentLevel = getOwnBuilding().getLevels().size();
        //Send out update to client
        getOwnBuilding().markDirty();
        logger.info("Added new Level " + currentLevel.getDepth());
        return false;
    }

    private void doShaftBuilding() {
        if (worker.isWorkerAtSiteWithMove(getOwnBuilding().getLocation()
                , RANGE_CHECK_AROUND_BUILDING_CHEST)) {
            if (buildNextBlockInShaft()) {
                return;
            }
            getOwnBuilding().startingLevelShaft = 0;
            job.setStage(Stage.START_WORKING);
        }
    }

    private void doNodeMining() {
        Level currentLevel = getOwnBuilding().getCurrentLevel();
        if (currentLevel == null) {
            logger.warn("Current Level not set, resetting...");
            getOwnBuilding().currentLevel = getOwnBuilding().getLevels().size() - 1;
            return;
        }

        mineAtLevel(currentLevel);
    }

    private void mineAtLevel(Level currentLevel) {
        if (workingNode == null) {
            logger.info("No working node, searching for one:");
            workingNode = findNodeOnLevel(currentLevel);
            return;
        }

        int foundDirection = 0;
        Node foundNode = null;
        List<Integer> directions = Arrays.asList(1, 2, 3, 4);

        for (Integer dir : directions) {
            Optional<Node> node = tryFindNodeInDirectionofNode(currentLevel, workingNode, dir);
            if (node.isPresent() && getNodeStatusForDirection(node.get(), invertDirection(dir)) == NodeStatus.COMPLETED) {
                foundDirection = dir;
                foundNode = node.get();
                break;
            }
        }
        if (foundNode == null || foundDirection <= 0) {
            logger.info("Found no adjacent nodes, aborting...");
            workingNode = null;
            return;
        }
        int xoffset = getXDistance(foundDirection) / 2;
        int zoffset = getZDistance(foundDirection) / 2;
        if (xoffset > 0) {
            xoffset += 1;
        } else {
            xoffset -= 1;
        }
        if (zoffset > 0) {
            zoffset += 1;
        } else {
            zoffset -= 1;
        }
        ChunkCoordinates standingPosition = new ChunkCoordinates(
                workingNode.getX() + xoffset,
                currentLevel.getDepth(),
                workingNode.getZ() + zoffset);
        delay += 10;
        currentStandingPosition = standingPosition;
        if (workingNode.getStatus() == NodeStatus.IN_PROGRESS
                || workingNode.getStatus() == NodeStatus.COMPLETED
                || worker.isWorkerAtSiteWithMove(standingPosition
                , RANGE_CHECK_AROUND_MINING_BLOCK)) {
            mineNodeFromStand(workingNode, foundNode, standingPosition, foundDirection);
        }
    }

    private boolean secureBlock(ChunkCoordinates curBlock, ChunkCoordinates safeStand) {
        if (!getBlock(curBlock).getMaterial().blocksMovement() && getBlock(curBlock) != Blocks.torch) {

            if (!mineBlock(curBlock, safeStand)) {
                delay = 0;
                return false;
            }
            if (missesItemsInInventory(new ItemStack(Blocks.cobblestone))) {
                return false;
            }

            setBlockFromInventory(curBlock, Blocks.cobblestone);
            //To set it to clean stone... would be cheating
            //world.setBlock(curBlock.posX, curBlock.posY, curBlock.posZ, Blocks.stone);
            return false;
        }
        return true;
    }


    private void mineNodeFromStand(Node minenode, Node standnode, ChunkCoordinates standingPosition, int direction) {

        //Check for safe Node
        for (int x = -NODE_DISTANCE / 2; x <= NODE_DISTANCE / 2; x++) {
            for (int z = -NODE_DISTANCE / 2; z <= NODE_DISTANCE / 2; z++) {
                for (int y = 0; y <= 5; y++) {
                    ChunkCoordinates curBlock = new ChunkCoordinates(minenode.getX() + x,
                            standingPosition.posY + y, minenode.getZ() + z);
                    if (Math.abs(x) >= 2 && Math.abs(z) >= 2
                            || getBlock(curBlock) != Blocks.air
                            || y < 1 || y > 4) {
                        if (!secureBlock(curBlock, standingPosition)) {
                            return;
                        }
                    }
                }
            }
        }

        if (!mineSideOfNode(minenode, direction, standingPosition)) {
            return;
        }

        if (minenode.getStatus() == NodeStatus.AVAILABLE) {
            minenode.setStatus(NodeStatus.IN_PROGRESS);
        }

        int xoffset = getXDistance(direction) / 2;
        int zoffset = getZDistance(direction) / 2;
        if (xoffset > 0) {
            xoffset -= 1;
        } else {
            xoffset += 1;
        }
        if (zoffset > 0) {
            zoffset -= 1;
        } else {
            zoffset += 1;
        }
        ChunkCoordinates newStandingPosition = new ChunkCoordinates(
                minenode.getX() + xoffset,
                standingPosition.posY,
                minenode.getZ() + zoffset);
        currentStandingPosition = newStandingPosition;


        if (minenode.getStatus() != NodeStatus.COMPLETED) {
            //Mine middle
            //TODO: make it look nicer!
            for (int y = 1; y <= 4; y++) {
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        ChunkCoordinates curBlock = new ChunkCoordinates(minenode.getX() + x,
                                standingPosition.posY + y, minenode.getZ() + z);
                        if (getBlock(curBlock) == Blocks.torch
                                || getBlock(curBlock) == Blocks.planks
                                || getBlock(curBlock) == Blocks.fence) {
                            continue;
                        }
                        if (!mineBlock(curBlock, newStandingPosition)) {
                            return;
                        }
                    }
                }
            }
        }

        List<Integer> directions = Arrays.asList(1, 2, 3, 4);
        for (Integer dir : directions) {
            ChunkCoordinates sideStandingPosition = new ChunkCoordinates(
                    minenode.getX() + getXDistance(dir) / 3,
                    standingPosition.posY,
                    minenode.getZ() + getZDistance(dir) / 3);
            currentStandingPosition = sideStandingPosition;
            if (!mineSideOfNode(minenode, dir, sideStandingPosition)) {
                return;
            }
        }

        //Build middle
        //TODO: make it look nicer!
        for (int y = 4; y >= 2; y--) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    ChunkCoordinates curBlock = new ChunkCoordinates(minenode.getX() + x,
                            standingPosition.posY + y, minenode.getZ() + z);

                    Block material = null;
                    //Middle top block and side stands
                    if (x == 0 && z == 0 && y == 4) {
                        material = Blocks.fence;
                    }
                    //Planks topping
                    if (x == 0 && z == 0 && y == 3) {
                        material = Blocks.planks;
                    }
                    //torches at sides
                    if (((Math.abs(x) == 1 && Math.abs(z) == 0)
                            || (Math.abs(x) == 0 && Math.abs(z) == 1))
                            && y == 3
                            && getBlock(new ChunkCoordinates(minenode.getX(),
                            standingPosition.posY + y, minenode.getZ())) == Blocks.planks) {
                        material = Blocks.torch;
                    }
                    if (material == null || getBlock(curBlock) == material) {
                        continue;
                    }

                    if (missesItemsInInventory(new ItemStack(material))) {
                        return;
                    }

                    setBlockFromInventory(curBlock, material);
                    return;
                }
            }
        }

        if (minenode.getStatus() == NodeStatus.IN_PROGRESS) {
            logger.info("Mined out node middle!");
            minenode.setStatus(NodeStatus.COMPLETED);
            logger.info("Completed middle for: \n" + minenode);
        }

        logger.info("Done with node \n" + minenode);
        workingNode = null;
    }

    private boolean mineSideOfNode(Node minenode, int directon, ChunkCoordinates standingPosition) {
        if (getNodeStatusForDirection(minenode, directon) == NodeStatus.LADDER) {
            return true;
        }

        if (getNodeStatusForDirection(minenode, directon) == NodeStatus.AVAILABLE) {
            setNodeStatusForDirection(minenode, directon, NodeStatus.IN_PROGRESS);
        }

        int xoffset = getXDistance(directon) / 2;
        int zoffset = getZDistance(directon) / 2;
        int posx = 1;
        int negx = -1;
        int posz = 1;
        int negz = -1;
        if (xoffset > 0) {
            posx = xoffset;
            negx = 2;
        }
        if (xoffset < 0) {
            negx = xoffset;
            posx = -2;
        }
        if (zoffset > 0) {
            posz = zoffset;
            negz = 2;
        }
        if (zoffset < 0) {
            negz = zoffset;
            posz = -2;
        }

        //Mine side
        //TODO: make it look nicer!
        for (int y = 1; y <= 4; y++) {
            for (int x = negx; x <= posx; x++) {
                for (int z = negz; z <= posz; z++) {
                    ChunkCoordinates curBlock = new ChunkCoordinates(minenode.getX() + x,
                            standingPosition.posY + y, minenode.getZ() + z);
                    if (!mineBlock(curBlock, standingPosition)) {
                        return false;
                    }
                }
            }
        }
        if (getNodeStatusForDirection(minenode, directon) == NodeStatus.IN_PROGRESS) {
            logger.info("Mined out node entry!");
            setNodeStatusForDirection(minenode, directon, NodeStatus.COMPLETED);
            logger.info("Completed entry for: \n" + minenode);
        }
        return true;
    }

    private void setNodeStatusForDirection(Node node, int direction, NodeStatus status) {
        if (direction == 1) {
            node.setDirectionPosX(status);
        } else if (direction == 2) {
            node.setDirectionNegX(status);
        } else if (direction == 3) {
            node.setDirectionPosZ(status);
        } else if (direction == 4) {
            node.setDirectionNegZ(status);
        }
    }

    private NodeStatus getNodeStatusForDirection(Node node, int direction) {
        if (direction == 1) {
            return node.getDirectionPosX();
        } else if (direction == 2) {
            return node.getDirectionNegX();
        } else if (direction == 3) {
            return node.getDirectionPosZ();
        } else if (direction == 4) {
            return node.getDirectionNegZ();
        }
        //Cannot happen, so send something that blocks mining
        return NodeStatus.LADDER;
    }

    private int invertDirection(int direction) {
        if (direction == 1) {
            return 2;
        } else if (direction == 2) {
            return 1;
        } else if (direction == 3) {
            return 4;
        } else if (direction == 4) {
            return 3;
        }
        return 0;
    }

    private boolean isNodeInDirectionOfOtherNode(Node start, int direction, Node check) {
        return start.getX() + getXDistance(direction) == check.getX()
                && start.getZ() + getZDistance(direction) == check.getZ();
    }

    private int getXDistance(int direction) {
        if (direction == 1) {
            return NODE_DISTANCE;
        } else if (direction == 2) {
            return -NODE_DISTANCE;
        }
        return 0;
    }

    private int getZDistance(int direction) {
        if (direction == 3) {
            return NODE_DISTANCE;
        } else if (direction == 4) {
            return -NODE_DISTANCE;
        }
        return 0;
    }

    private Optional<Node> tryFindNodeInDirectionofNode(Level curlevel, Node start, int direction) {
        final Node finalCurrentNode = start;
        Optional<Node> first = new ArrayList<>(curlevel.getNodes()).parallelStream()
                .filter(check -> isNodeInDirectionOfOtherNode(finalCurrentNode, direction, check))
                .findFirst();
        return first;
    }

    private Node createNewNodeInDirectionFromNode(Node start, int direction) {
        int x = start.getX() + getXDistance(direction);
        int z = start.getZ() + getZDistance(direction);
        return new Node(x, z);
    }

    private Node findNodeOnLevel(Level currentLevel) {
        Node currentNode = currentLevel.getLadderNode();
        LinkedList<Node> visited = new LinkedList<>();
        while (currentNode != null) {
            if (visited.contains(currentNode)) {
                logger.info("Found dead end, retrying...");
                return null;
            }

            logger.info("Walking to " + currentNode);
            visited.add(currentNode);
            if (currentNode.getStatus() == NodeStatus.AVAILABLE
                    || currentNode.getStatus() == NodeStatus.IN_PROGRESS) {
                logger.info("Node was mineable");
                return currentNode;
            }

            List<Integer> directions = Arrays.asList(1, 2, 3, 4);
            Collections.shuffle(directions);
            for (Integer dir : directions) {
                logger.info("\tTesting direction " + dir);
                NodeStatus status = getNodeStatusForDirection(currentNode, dir);
                if (status == NodeStatus.AVAILABLE || status == NodeStatus.IN_PROGRESS) {
                    logger.info("\tDirection " + dir + " was mineable");
                    return currentNode;
                }
                if (status == NodeStatus.COMPLETED) {
                    logger.info("\tDirection " + dir + " was complete");
                    Optional<Node> first = tryFindNodeInDirectionofNode(currentLevel, currentNode, dir);
                    if (first.isPresent()) {
                        if (visited.contains(first.get())) {
                            continue;//Stop endless loops
                        }
                        //IDE sais unused but is indeed used for next while loop
                        //TODO: investigate
                        currentNode = first.get();
                        break; //Out of direction for loop
                    }

                    Node newnode = createNewNodeInDirectionFromNode(currentNode, dir);
                    currentLevel.addNode(newnode);
                    logger.info("\tCreated new node " + newnode);
                    return newnode;
                }
            }
        }

        return null;
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
                return;
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

        if (job.getStage() == Stage.MINING_NODE) {
            doNodeMining();
            return;
        }

        logger.info("[" + job.getStage() + "] Stopping here, old code ahead...");
        delay += 100;
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

    private int getBlockMiningDelay(Block block, ChunkCoordinates chunkCoordinates) {
        return getBlockMiningDelay(block, chunkCoordinates.posX, chunkCoordinates.posY, chunkCoordinates.posZ);
    }

    private int getBlockMiningDelay(Block block, int x, int y, int z) {
        if (worker.getHeldItem() == null) {
            return (int) block.getBlockHardness(world, x, y, z);
        }
        return (int) (worker.getHeldItem().getItem().getDigSpeed(worker.getHeldItem(), block, 0)
                * block.getBlockHardness(world, x, y, z));
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