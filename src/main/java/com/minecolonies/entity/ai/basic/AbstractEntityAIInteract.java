package com.minecolonies.entity.ai.basic;

import com.minecolonies.colony.buildings.BuildingWorker;
import com.minecolonies.colony.jobs.Job;
import com.minecolonies.entity.ai.util.AIState;
import com.minecolonies.entity.ai.util.AITarget;
import com.minecolonies.inventory.InventoryCitizen;
import com.minecolonies.util.*;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraftforge.common.ForgeHooks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import static com.minecolonies.entity.ai.util.AIState.*;

/**
 * This is the base class of all worker AIs.
 * Every AI implements this class with it's job type.
 * There are some utilities within the class:
 * - The AI will clear a full inventory at the building chest.
 * - The AI will animate mining a block (with delay)
 * - The AI will request items and tools automatically
 * (and collect them from the building chest)
 *
 * @param <J> the job type this AI has to do.
 */
public abstract class AbstractEntityAIInteract<J extends Job> extends AbstractEntityAICrafting<J>
{
    private static final int             DELAY_MODIFIER          = 50;
    /**
     * The amount of xp the entity gains per block mined.
     */
    private static final double          XP_PER_BLOCK            = 0.05;
    protected static     Random          itemRand                = new Random();
    private              boolean         needsShovel             = false;
    private              boolean         needsAxe                = false;
    private              boolean         needsHoe                = false;
    private              boolean         needsPickaxe            = false;
    private              int             needsPickaxeLevel       = -1;
    private              int             blocksMined             = 0;
    /**
     * If we have waited one delay
     */
    private              boolean         hasDelayed              = false;


    /**
     * Creates the abstract part of the AI.
     * Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public AbstractEntityAIInteract(J job)
    {
        super(job);
        super.registerTargets(
                /**
                 * Check if any items are needed.
                 * If yes, transition to NEEDS_ITEM.
                 * and wait for new items.
                 */
                new AITarget(() -> !itemsCurrentlyNeeded.isEmpty(),
                             this::waitForNeededItems),
                /**
                 * Wait for different tools.
                 */
                new AITarget(() -> this.needsShovel, this::waitForShovel),
                new AITarget(() -> this.needsAxe, this::waitForAxe),
                new AITarget(() -> this.needsHoe, this::waitForHoe),
                new AITarget(() -> this.needsPickaxe, this::waitForPickaxe),
                /**
                 * Dumps inventory as long as needs be.
                 * If inventory is dumped, execution continues
                 * to resolve state.
                 */
                new AITarget(INVENTORY_FULL, this::dumpInventory),
                /**
                 * Check if inventory has to be dumped.
                 */
                new AITarget(() -> worker.isInventoryFull() || wantInventoryDumped(),
                             () -> INVENTORY_FULL)
                             );
    }

    /**
     * Has to be overridden by classes to specify when to dump inventory.
     * Always dump on inventory full.
     *
     * @return true if inventory needs to be dumped now
     */
    protected boolean wantInventoryDumped()
    {
        return false;
    }


    /**
     * Wait for a needed shovel.
     *
     * @return NEEDS_SHOVEL
     */
    private AIState waitForShovel()
    {
        if (checkForShovel())
        {
            delay += DELAY_RECHECK;
            return NEEDS_SHOVEL;
        }
        return IDLE;
    }

    /**
     * Ensures that we have a shovel available.
     * Will set {@code needsShovel} accordingly.
     *
     * @return true if we have a shovel
     */
    protected boolean checkForShovel()
    {
        needsShovel = checkForTool(Utils.SHOVEL);
        return needsShovel;
    }

    private boolean checkForTool(String tool)
    {
        boolean needsTool = !InventoryFunctions
                .matchFirstInInventory(
                        worker.getInventoryCitizen(),
                        stack -> Utils.isTool(stack, tool),
                        InventoryFunctions::doNothing);
        if (!needsTool)
        {
            return false;
        }
        delay += DELAY_RECHECK;
        if (walkToBuilding())
        {
            return true;
        }
        if (isToolInHut(tool))
        {
            return false;
        }
        requestWithoutSpam(tool);
        return true;
    }

    private boolean isToolInHut(String tool)
    {
        BuildingWorker buildingMiner = getOwnBuilding();
        return InventoryFunctions
                .matchFirstInInventory(
                        buildingMiner.getTileEntity(),
                        stack -> Utils.isTool(stack, tool),
                        this::takeItemStackFromChest);

    }

    /**
     * Wait for a needed axe.
     *
     * @return NEEDS_AXE
     */
    private AIState waitForAxe()
    {
        if (checkForAxe())
        {
            delay += DELAY_RECHECK;
            return NEEDS_AXE;
        }
        return IDLE;
    }

    /**
     * Ensures that we have an axe available.
     * Will set {@code needsAxe} accordingly.
     *
     * @return true if we have an axe
     */
    protected boolean checkForAxe()
    {
        needsAxe = checkForTool(Utils.AXE);
        return needsAxe;
    }

    /**
     * Wait for a needed hoe.
     *
     * @return NEEDS_HOE
     */
    private AIState waitForHoe()
    {
        if (checkForHoe())
        {
            delay += DELAY_RECHECK;
            return NEEDS_HOE;
        }
        return IDLE;
    }

    /**
     * Ensures that we have a hoe available.
     * Will set {@code needsHoe} accordingly.
     *
     * @return true if we have a hoe
     */
    protected boolean checkForHoe()
    {
        needsHoe = checkForTool(Utils.HOE);
        return needsHoe;
    }

    /**
     * Wait for a needed pickaxe.
     *
     * @return NEEDS_PICKAXE
     */
    private AIState waitForPickaxe()
    {
        if (checkForPickaxe(needsPickaxeLevel))
        {
            delay += DELAY_RECHECK;
            return NEEDS_PICKAXE;
        }
        return IDLE;
    }

    /**
     * Ensures that we have a pickaxe available.
     * Will set {@code needsPickaxe} accordingly.
     *
     * @param minlevel the minimum pickaxe level needed.
     * @return true if we have a pickaxe
     */
    protected boolean checkForPickaxe(int minlevel)
    {
        //Check for a pickaxe
        needsPickaxe = !InventoryFunctions
                .matchFirstInInventory(
                        worker.getInventoryCitizen(),
                        stack -> Utils.checkIfPickaxeQualifies(
                                minlevel, Utils.getMiningLevel(stack, Utils.PICKAXE)),
                        InventoryFunctions::doNothing);

        delay += DELAY_RECHECK;

        if (needsPickaxe)
        {
            needsPickaxeLevel = minlevel;
            if (walkToBuilding())
            {
                return false;
            }
            if (isPickaxeInHut(minlevel))
            {
                return true;
            }
            requestWithoutSpam("Pickaxe at least level " + minlevel);
        }
        return needsPickaxe;
    }

    /**
     * Looks for a pickaxe to mine a block of {@code minLevel}.
     * The pickaxe will be taken from the chest.
     * Make sure that the worker stands next the chest to not break immersion.
     * Also make sure to have inventory space for the pickaxe.
     *
     * @param minlevel the needed pickaxe level
     * @return true if a pickaxe was found
     */
    private boolean isPickaxeInHut(int minlevel)
    {
        BuildingWorker buildingMiner = getOwnBuilding();
        return InventoryFunctions
                .matchFirstInInventory(
                        buildingMiner.getTileEntity(),
                        stack -> Utils.checkIfPickaxeQualifies(
                                minlevel,
                                Utils.getMiningLevel(
                                        stack,
                                        Utils.PICKAXE)),
                        this::takeItemStackFromChest);
    }

    /**
     * Walk to building and dump inventory.
     * If inventory is dumped, continue execution
     * so that the state can be resolved.
     *
     * @return INVENTORY_FULL | IDLE
     */
    private AIState dumpInventory()
    {
        if (dumpOneMoreSlot())
        {
            delay += DELAY_RECHECK;
            return INVENTORY_FULL;
        }
        if (isInventoryAndChestFull())
        {
            chatSpamFilter.talkWithoutSpam("entity.worker.inventoryFullChestFull");
        }
        //collect items that are nice to have if they are available
        itemsNiceToHave().forEach(this::isInHut);
        return IDLE;
    }

    /**
     * Can be overridden by implementations to specify items useful for the worker.
     * When the workers inventory is full, he will try to keep these items.
     * ItemStack amounts are ignored, the first stack found will be taken.
     *
     * @return a list with items nice to have for the worker
     */
    protected List<ItemStack> itemsNiceToHave()
    {
        return new ArrayList<>();
    }

    /**
     * Dump the workers inventory into his building chest.
     * Only useful tools are kept!
     * Only dumps one block at a time!
     */
    private boolean dumpOneMoreSlot()
    {
        return dumpOneMoreSlot(this::neededForWorker);
    }

    /**
     * Dumps one inventory slot into the building chest.
     *
     * @param keepIt used to test it that stack should be kept
     * @return true if is has to dump more.
     */
    private boolean dumpOneMoreSlot(Predicate<ItemStack> keepIt)
    {
        return walkToBuilding()
               || InventoryFunctions.matchFirstInInventory(
                worker.getInventoryCitizen(), (i, stack) -> {
                    if (stack == null || keepIt.test(stack))
                    {
                        return false;
                    }
                    ItemStack returnStack = InventoryUtils.setStack(getOwnBuilding().getTileEntity(), stack);
                    if (returnStack == null)
                    {
                        worker.getInventoryCitizen().decrStackSize(i, stack.stackSize);
                        return true;
                    }
                    worker.getInventoryCitizen().decrStackSize(i, stack.stackSize - returnStack.stackSize);
                    //Check that we are not inserting
                    // into a full inventory.
                    return stack.stackSize != returnStack.stackSize;
                });
    }

    /**
     * Checks if the worker inventory and his building chest are full
     *
     * @return true if both are full, else false
     */
    private boolean isInventoryAndChestFull()
    {
        return InventoryUtils.isInventoryFull(worker.getInventoryCitizen())
               && InventoryUtils.isInventoryFull(worker.getWorkBuilding().getTileEntity());
    }

    /**
     * Require that items are in the workers inventory.
     * This safegate ensurs you have said items before you execute a task.
     * Please stop execution on false returned.
     *
     * @param items the items needed
     * @return false if they are in inventory
     */
    protected boolean checkOrRequestItems(ItemStack... items)
    {
        boolean allClear = true;
        for (ItemStack stack : items)
        {
            if (stack == null || stack.getItem() == null)
            {
                continue;
            }
            int countOfItem = worker.getItemCountInInventory(stack.getItem());
            if (countOfItem < stack.stackSize)
            {
                int       itemsLeft     = stack.stackSize - countOfItem;
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
        return true;
    }

    protected InventoryCitizen getInventory()
    {
        return worker.getInventoryCitizen();
    }

    /**
     * Override this method if you want to keep some items in inventory.
     * When the inventory is full, everything get's dumped into the building chest.
     * But you can use this method to hold some stacks back.
     *
     * @param stack the stack to decide on
     * @return true if the stack should remain in inventory
     */
    protected boolean neededForWorker(ItemStack stack)
    {
        return false;
    }

    protected final void setDelay(int timeout)
    {
        this.delay = timeout;
    }

    protected final boolean holdEfficientTool(Block target)
    {
        int bestSlot = getMostEfficientTool(target);
        if (bestSlot >= 0)
        {
            worker.setHeldItem(bestSlot);
            return true;
        }
        requestTool(target);
        return false;
    }

    /**
     * Request the appropriate tool for this block.
     *
     * @param target the block to mine
     */
    private void requestTool(Block target)
    {
        String tool     = target.getHarvestTool(target.getDefaultState());
        int    required = target.getHarvestLevel(target.getDefaultState());
        updateToolFlag(tool, required);
    }

    /**
     * checks if said tool of said level is usable.
     * if not, it updates the needsTool falg
     * for said tool.
     *
     * @param tool     the tool needed
     * @param required the level needed (for pickaxe)
     */
    private void updateToolFlag(String tool, int required)
    {
        switch (tool)
        {
            case Utils.AXE:
                checkForAxe();
                break;
            case Utils.SHOVEL:
                checkForShovel();
                break;
            case Utils.HOE:
                checkForHoe();
                break;
            case Utils.PICKAXE:
                checkForPickaxe(required);
                break;
            default:
                Log.logger.error("Invalid tool " + tool + " not implemented as tool!");
        }
    }

    /**
     * Calculates the most efficient tool to use
     * on that block.
     *
     * @param target the Block type to mine
     * @return the slot with the best tool
     */
    private int getMostEfficientTool(Block target)
    {
        String           tool      = target.getHarvestTool(target.getDefaultState());
        int              required  = target.getHarvestLevel(target.getDefaultState());
        int              bestSlot  = -1;
        int              bestLevel = Integer.MAX_VALUE;
        InventoryCitizen inventory = worker.getInventoryCitizen();
        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            ItemStack item  = inventory.getStackInSlot(i);
            int       level = Utils.getMiningLevel(item, tool);
            if (level >= required && level < bestLevel)
            {
                bestSlot = i;
                bestLevel = level;
            }
        }
        return bestSlot;
    }

    /**
     * Calculate how long it takes to mine this block.
     *
     * @param block the block type
     * @param pos   coordinate
     * @return the delay in ticks
     */
    private int getBlockMiningDelay(Block block, BlockPos pos)
    {
        if (worker.getHeldItem() == null)
        {
            return (int) block.getBlockHardness(world, pos);
        }
        return (int) ((DELAY_MODIFIER - worker.getLevel()) * block.getBlockHardness(world, pos)
                      / (worker.getHeldItem().getItem().getDigSpeed(worker.getHeldItem(), block.getDefaultState())));
    }

    /**
     * Checks for the right tools and waits for an appropriate delay.
     *
     * @param blockToMine the block to mine eventually
     * @param safeStand   a safe stand to mine from (AIR Block!)
     */
    private boolean checkMiningLocation(BlockPos blockToMine, BlockPos safeStand)
    {
        Block curBlock = world.getBlockState(blockToMine).getBlock();

        if (!holdEfficientTool(curBlock))
        {
            //We are missing a tool to harvest this block...
            return true;
        }

        ItemStack tool = worker.getHeldItem();

        if (curBlock.getHarvestLevel(curBlock.getDefaultState()) < Utils.getMiningLevel(tool, curBlock.getHarvestTool(curBlock.getDefaultState())))
        {
            //We have to high of a tool...
            //TODO: request lower tier tools
        }

        if (tool != null && !ForgeHooks.canToolHarvestBlock(world, blockToMine, tool) && curBlock != Blocks.bedrock)
        {
            Log.logger.info("ForgeHook not in sync with EfficientTool for " + curBlock + " and " + tool + "\n"
                            + "Please report to MineColonies with this text to add support!");
        }

        if (walkToBlock(safeStand))
        {
            return true;
        }
        currentWorkingLocation = blockToMine;
        currentStandingLocation = safeStand;


        return hasNotDelayed(getBlockMiningDelay(curBlock, blockToMine));
    }

    /**
     * Will delay one time and pass through the second time.
     * Use for convenience instead of SetDelay
     *
     * @param time the time to wait
     * @return true if you should wait
     */
    protected final boolean hasNotDelayed(int time)
    {
        if (!hasDelayed)
        {
            setDelay(time);
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
     *
     * @param blockToMine the block that should be mined
     * @return true once we're done
     */
    protected final boolean mineBlock(BlockPos blockToMine)
    {
        return mineBlock(blockToMine, new BlockPos((int) worker.posX, (int) worker.posY, (int) worker.posZ));
    }

    /**
     * Will simulate mining a block with particles ItemDrop etc.
     * Attention:
     * Because it simulates delay, it has to be called 2 times.
     * So make sure the code path up to this function is reachable a second time.
     * And make sure to immediately exit the update function when this returns false.
     *
     * @param blockToMine the block that should be mined
     * @param safeStand   the block we want to stand on to do that
     * @return true once we're done
     */
    protected final boolean mineBlock(BlockPos blockToMine, BlockPos safeStand)
    {
        //todo partially replace with methods in EntityCitizen
        Block curBlock = world.getBlockState(blockToMine).getBlock();
        if (curBlock == null || curBlock.equals(Blocks.air))
        {
            //no need to mine block...
            return true;
        }

        if (BlockUtils.shouldNeverBeMessedWith(curBlock))
        {
            Log.logger.warn("Trying to mine block " + curBlock + " which is not allowed!");
            //This will endlessly loop... If this warning comes up, check your blocks first...
            return false;
        }

        if (checkMiningLocation(blockToMine, safeStand))
        {
            //we have to wait for delay
            return false;
        }

        ItemStack tool = worker.getHeldItem();

        //calculate fortune enchantment
        int fortune = Utils.getFortuneOf(tool);

        if (tool != null)
        {
            //Reduce durability if not using hand
            //todo: maybe merge with worker methods, have to find a clean way for this later
            worker.damageItemInHand(1);
        }

        //if Tool breaks
        if (tool != null && tool.stackSize < 1)
        {
            worker.setCurrentItemOrArmor(0, null);
            worker.getInventoryCitizen().setInventorySlotContents(worker.getInventoryCitizen().getHeldItemSlot(), null);
        }

        Utils.blockBreakSoundAndEffect(world, blockToMine, curBlock,
                                       curBlock.getMetaFromState(world.getBlockState(blockToMine)));

        List<ItemStack> items = BlockPosUtil.getBlockDrops(world, blockToMine, fortune);
        for (ItemStack item : items)
        {
            InventoryUtils.setStack(worker.getInventoryCitizen(), item);
        }


        world.setBlockToAir(blockToMine);
        worker.addExperience(XP_PER_BLOCK);
        blocksMined++;
        return true;
    }

    /**
     * Will return the number of blocks mined.
     * Counting from the last time dumping inventory.
     * Useful for calculating when to return to chest.
     *
     * @return the number of blocks mined
     */
    public final int getBlocksMined()
    {
        return blocksMined;
    }

    /**
     * Clear the amount of blocks mined.
     * Call this when dumping into the chest.
     */
    public final void clearBlocksMined()
    {
        this.blocksMined = 0;
    }

}
