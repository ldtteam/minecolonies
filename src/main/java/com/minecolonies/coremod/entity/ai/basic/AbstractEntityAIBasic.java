package com.minecolonies.coremod.entity.ai.basic;

import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.entity.ai.item.handling.ItemStorage;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import com.minecolonies.coremod.entity.pathfinding.WalkToProxy;
import com.minecolonies.coremod.inventory.InventoryCitizen;
import com.minecolonies.coremod.util.*;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

import static com.minecolonies.coremod.entity.ai.util.AIState.*;

/**
 * This class provides basic ai functionality.
 *
 * @param <J> The job this ai has to fulfil
 */
public abstract class AbstractEntityAIBasic<J extends AbstractJob> extends AbstractAISkeleton<J>
{

    public static final  int             EXCEPTION_TIMEOUT       = 100;
    /**
     * Time in ticks to wait until the next check for items.
     */
    private static final int             DELAY_RECHECK           = 10;
    /**
     * The default range for any walking to blocks.
     */
    private static final int             DEFAULT_RANGE_FOR_DELAY = 4;
    /**
     * The number of actions done before item dump.
     */
    private static final int             ACTIONS_UNTIL_DUMP      = 32;
    /**
     * Hit a block every x ticks when mining.
     */
    private static final int             HIT_EVERY_X_TICKS       = 5;
    /**
     * The list of all items and their quantity that were requested by the worker.
     * Warning: This list does not change, if you need to see what is currently missing,
     * look at @see #itemsCurrentlyNeeded for things the miner needs.
     * <p>
     * Will be cleared on restart, be aware!
     */
    @NotNull
    private final        List<ItemStack> itemsNeeded             = new ArrayList<>();
    /**
     * The block the ai is currently working at or wants to work.
     */
    @Nullable
    protected            BlockPos        currentWorkingLocation  = null;
    /**
     * The block the ai is currently standing at or wants to stand.
     */
    @Nullable
    protected            BlockPos        currentStandingLocation = null;
    /**
     * The time in ticks until the next action is made.
     */
    private              int             delay                   = 0;
    /**
     * A list of ItemStacks with needed items and their quantity.
     * This list is a diff between @see #itemsNeeded and
     * the players inventory and their hut combined.
     * So look here for what is currently still needed
     * to fulfill the workers needs.
     * <p>
     * Will be cleared on restart, be aware!
     */
    @NotNull
    private              List<ItemStack> itemsCurrentlyNeeded    = new ArrayList<>();
    /**
     * This flag tells if we need a shovel, will be set on tool needs.
     */
    private              boolean         needsShovel             = false;

    /**
     * This flag tells if we need an axe, will be set on tool needs.
     */
    private boolean needsAxe = false;

    /**
     * This flag tells if we need a hoe, will be set on tool needs.
     */
    private boolean needsHoe = false;

    /**
     * This flag tells if we need a pickaxe, will be set on tool needs.
     */
    private boolean needsPickaxe = false;

    /**
     * This flag tells if we need a weapon, will be set on tool needs.
     */
    private boolean needsWeapon = false;

    /**
     * The minimum pickaxe level we need to fulfill the tool request.
     */
    private int needsPickaxeLevel = -1;

    /**
     * If we have waited one delay.
     */
    private boolean hasDelayed = false;

    /**
     * A counter to dump the inventory after x actions.
     */
    private int actionsDone = 0;

    /**
     * Walk to proxy.
     */
    private WalkToProxy proxy;

    /**
     * Sets up some important skeleton stuff for every ai.
     *
     * @param job the job class
     */
    protected AbstractEntityAIBasic(@NotNull final J job)
    {
        super(job);
        super.registerTargets(
                /*
                 * Init safety checks and transition to IDLE
                 */
          new AITarget(INIT, this::initSafetyChecks),
                /*
                 * Update chestbelt and nametag
                 * Will be executed every time
                 * and does not stop execution
                 */
          new AITarget(this::updateVisualState),
                /*
                 * If waitingForSomething returns true
                 * stop execution to wait for it.
                 * this keeps the current state
                 * (returning null would not stop execution)
                 */
          new AITarget(this::waitingForSomething, this::getState),
                /*
                 * Check if any items are needed.
                 * If yes, transition to NEEDS_ITEM.
                 * and wait for new items.
                 */
          new AITarget(() -> !itemsCurrentlyNeeded.isEmpty(), this::waitForNeededItems),
                /*
                 * Wait for different tools.
                 */
          new AITarget(() -> this.needsShovel, this::waitForShovel),
          new AITarget(() -> this.needsAxe, this::waitForAxe),
          new AITarget(() -> this.needsHoe, this::waitForHoe),
          new AITarget(() -> this.needsPickaxe, this::waitForPickaxe),
          new AITarget(() -> this.needsWeapon, this::waitForWeapon),

                /*
                 * Dumps inventory as long as needs be.
                 * If inventory is dumped, execution continues
                 * to resolve state.
                 */
          new AITarget(INVENTORY_FULL, this::dumpInventory),
                /*
                 * Check if inventory has to be dumped.
                 */
          new AITarget(this::inventoryNeedsDump, INVENTORY_FULL)
        );
    }

    @Override
    protected void onException(final RuntimeException e)
    {
        Log.getLogger().info("Pausing Entity for 5 Seconds");
        this.setDelay(EXCEPTION_TIMEOUT);
    }

    /**
     * Set a delay in ticks.
     *
     * @param timeout the delay to wait after this tick.
     */
    protected final void setDelay(final int timeout)
    {
        this.delay = timeout;
    }

    /**
     * Check if we need to dump the workers inventory.
     * <p>
     * This will also ask the implementing ai
     * if we need to dump on custom reasons.
     * {@see wantInventoryDumped}
     *
     * @return true if we need to dump the inventory.
     */
    private boolean inventoryNeedsDump()
    {
        return worker.isInventoryFull()
                 || actionsDone >= getActionsDoneUntilDumping()
                 || wantInventoryDumped();
    }

    /**
     * Calculates after how many actions the ai should dump it's inventory.
     * <p>
     * Override this to change the value.
     *
     * @return the number of actions done before item dump.
     */
    protected int getActionsDoneUntilDumping()
    {
        return ACTIONS_UNTIL_DUMP;
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
     * Check for null on important variables to prevent crashes.
     *
     * @return IDLE if all ready, else stay in INIT
     */
    @NotNull
    private AIState initSafetyChecks()
    {
        //Something fatally wrong? Wait for re-init...
        if (null == getOwnBuilding())
        {
            return INIT;
        }
        return IDLE;
    }

    /**
     * Can be overridden in implementations to return the exact building type.
     *
     * @return the building associated with this AI's worker.
     */
    @Nullable
    protected AbstractBuildingWorker getOwnBuilding()
    {
        return worker.getWorkBuilding();
    }

    /**
     * Updates the visual state of the worker.
     * Updates render meta data.
     * Updates the current state on the nametag.
     *
     * @return null to execute more targets.
     */
    private AIState updateVisualState()
    {
        //Update the current state the worker is in.
        job.setNameTag(this.getState().toString());
        //Update torch, seeds etc. in chestbelt etc.
        updateRenderMetaData();
        return null;
    }

    /**
     * Can be overridden in implementations.
     * <p>
     * Here the AI can check if the chestBelt has to be re rendered and do it.
     */
    protected void updateRenderMetaData()
    {
        worker.setRenderMetadata("");
    }

    /**
     * This method will return true if the AI is waiting for something.
     * In that case, don't execute any more AI code, until it returns false.
     * Call this exactly once per tick to get the delay right.
     * The worker will move and animate correctly while he waits.
     *
     * @return true if we have to wait for something
     *
     * @see #currentStandingLocation @see #currentWorkingLocation
     * @see #DEFAULT_RANGE_FOR_DELAY @see #delay
     */
    private boolean waitingForSomething()
    {
        if (delay > 0)
        {
            if (currentStandingLocation != null
                  && !worker.isWorkerAtSiteWithMove(currentStandingLocation, DEFAULT_RANGE_FOR_DELAY))
            {
                //Don't decrease delay as we are just walking...
                return true;
            }
            if (delay % HIT_EVERY_X_TICKS == 0)
            {
                worker.hitBlockWithToolInHand(currentWorkingLocation);
            }
            delay--;
            return true;
        }
        clearWorkTarget();
        return false;
    }

    /**
     * Remove the current working block and it's delay.
     */
    private void clearWorkTarget()
    {
        this.currentStandingLocation = null;
        this.currentWorkingLocation = null;
        this.delay = 0;
    }

    /**
     * Looks for needed items as long as not all of them are there.
     * Also waits for DELAY_RECHECK.
     *
     * @return NEEDS_ITEM
     */
    @NotNull
    private AIState waitForNeededItems()
    {
        delay = DELAY_RECHECK;
        return lookForNeededItems();
    }

    /**
     * Utility method to search for items currently needed.
     * Poll this until all items are there.
     */
    @NotNull
    private AIState lookForNeededItems()
    {
        syncNeededItemsWithInventory();
        if (itemsCurrentlyNeeded.isEmpty())
        {
            itemsNeeded.clear();
            job.clearItemsNeeded();
            return IDLE;
        }
        if (!walkToBuilding())
        {
            delay += DELAY_RECHECK;
            final ItemStack first = itemsCurrentlyNeeded.get(0);
            //Takes one Stack from the hut if existent
            if (isInHut(first))
            {
                return NEEDS_ITEM;
            }

            requestWithoutSpam(first.stackSize + " " + first.getDisplayName());
        }
        return NEEDS_ITEM;
    }

    /**
     * Updates the itemsCurrentlyNeeded with current values.
     */
    private void syncNeededItemsWithInventory()
    {
        job.clearItemsNeeded();
        itemsNeeded.forEach(job::addItemNeeded);
        InventoryUtils.getInventoryAsList(worker.getInventoryCitizen()).forEach(job::removeItemNeeded);
        itemsCurrentlyNeeded = new ArrayList<>(job.getItemsNeeded());
    }

    /**
     * Walk the worker to it's building chest.
     * Please return immediately if this returns true.
     *
     * @return false if the worker is at his building
     */
    protected final boolean walkToBuilding()
    {
        @Nullable final AbstractBuildingWorker ownBuilding = getOwnBuilding();
        //Return true if the building is null to stall the worker
        return ownBuilding == null
                 || walkToBlock(ownBuilding.getLocation());
    }

    /**
     * Finds the first @see ItemStack the type of {@code is}.
     * It will be taken from the chest and placed in the workers inventory.
     * Make sure that the worker stands next the chest to not break immersion.
     * Also make sure to have inventory space for the stack.
     *
     * @param is the type of item requested (amount is ignored)
     * @return true if a stack of that type was found
     */
    public boolean isInHut(@Nullable final ItemStack is)
    {
        @Nullable final AbstractBuildingWorker buildingMiner = getOwnBuilding();
        return buildingMiner != null
                 && is != null
                 && InventoryFunctions
                      .matchFirstInInventory(
                        buildingMiner.getTileEntity(),
                        stack -> stack != null && is.isItemEqual(stack),
                        this::takeItemStackFromChest
                      );
    }

    /**
     * Request an Item without spamming the chat.
     *
     * @param chat the Item Name
     */
    private void requestWithoutSpam(@NotNull final String chat)
    {
        chatSpamFilter.requestWithoutSpam(chat);
    }

    /**
     * Sets the block the AI is currently walking to.
     *
     * @param stand where to walk to
     * @return true while walking to the block
     */
    protected final boolean walkToBlock(@NotNull final BlockPos stand)
    {
        return walkToBlock(stand, DEFAULT_RANGE_FOR_DELAY);
    }

    /**
     * Sets the block the AI is currently walking to.
     *
     * @param stand where to walk to
     * @param range how close we need to be
     * @return true while walking to the block
     */
    protected final boolean walkToBlock(@NotNull final BlockPos stand, final int range)
    {
        if (proxy == null)
        {
            proxy = new WalkToProxy(worker);
        }
        if (proxy.walkToBlock(stand, range))
        {
            workOnBlock(null, stand, DELAY_RECHECK);
            return true;
        }
        return false;
    }

    /**
     * Sets the block the AI is currently working on.
     * This block will receive animation hits on delay.
     *
     * @param target  the block that will be hit
     * @param stand   the block the worker will walk to
     * @param timeout the time in ticks to hit the block
     */
    private void workOnBlock(@Nullable final BlockPos target, @Nullable final BlockPos stand, final int timeout)
    {
        this.currentWorkingLocation = target;
        this.currentStandingLocation = stand;
        this.delay = timeout;
    }

    /**
     * Takes whatever is in that slot of the workers chest and puts it in his inventory.
     * If the inventory is full, only the fitting part will be moved.
     * Beware this method shouldn't be private, because the generic access won't work within a lambda won't work else.
     *
     * @param slot the slot in the buildings inventory
     */
    public void takeItemStackFromChest(final int slot)
    {
        @Nullable final AbstractBuildingWorker ownBuilding = getOwnBuilding();
        if (ownBuilding == null)
        {
            return;
        }
        InventoryUtils.takeStackInSlot(ownBuilding.getTileEntity(), worker.getInventoryCitizen(), slot);
    }

    /**
     * Wait for a needed shovel.
     *
     * @return NEEDS_SHOVEL
     */
    @NotNull
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
    private boolean checkForShovel()
    {
        needsShovel = checkForTool(Utils.SHOVEL);
        return needsShovel;
    }

    /**
     * Ensures that we have a tool available.
     *
     * @param tool tool required for block
     * @return true if we have a tool
     */
    private boolean checkForTool(@NotNull String tool)
    {
        final boolean needsTool = !InventoryFunctions
                                     .matchFirstInInventory(
                                       worker.getInventoryCitizen(),
                                       stack -> Utils.isTool(stack, tool),
                                       InventoryFunctions::doNothing
                                     );

        final int hutLevel = worker.getWorkBuilding().getBuildingLevel();
        final InventoryCitizen inventory = worker.getInventoryCitizen();
        final boolean isUsable = InventoryUtils.hasToolLevel(tool, inventory, hutLevel);


        if (!needsTool && isUsable)
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
        chatSpamFilter.talkWithoutSpam(LanguageHandler.format("entity.worker.toolRequest", tool, InventoryUtils.swapToolGrade(hutLevel)));
        return true;
    }

    private boolean isToolInHut(final String tool)
    {
        @Nullable final AbstractBuildingWorker buildingWorker = getOwnBuilding();
        return buildingWorker != null
                 && InventoryFunctions.matchFirstInInventory(
          buildingWorker.getTileEntity(),
          stack -> Utils.isTool(stack, tool),
          this::takeItemStackFromChest
        );
    }

    /**
     * Wait for a needed axe.
     *
     * @return NEEDS_AXE
     */
    @NotNull
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
    @NotNull
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
    @NotNull
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
    private boolean checkForPickaxe(final int minlevel)
    {
        //Check for a pickaxe
        needsPickaxe = !InventoryFunctions
                          .matchFirstInInventory(
                            worker.getInventoryCitizen(),
                            stack -> Utils.checkIfPickaxeQualifies(
                              minlevel, Utils.getMiningLevel(stack, Utils.PICKAXE)),
                            InventoryFunctions::doNothing
                          );

        delay += DELAY_RECHECK;

        final InventoryCitizen inventory = worker.getInventoryCitizen();
        final int hutLevel = worker.getWorkBuilding().getBuildingLevel();
        final boolean isUsable = InventoryUtils.hasToolLevel(Utils.PICKAXE, inventory, hutLevel);

        if (!isUsable)
        {
            needsPickaxe = true;
        }
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
            chatSpamFilter.talkWithoutSpam(LanguageHandler.format("entity.worker.pickaxeRequest", InventoryUtils.swapToolGrade(minlevel), InventoryUtils.swapToolGrade(hutLevel)));
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
    private boolean isPickaxeInHut(final int minlevel)
    {
        @Nullable final AbstractBuildingWorker buildingWorker = getOwnBuilding();
        return buildingWorker != null
                 && InventoryFunctions.matchFirstInInventory(
          buildingWorker.getTileEntity(),
          stack -> Utils.checkIfPickaxeQualifies(
            minlevel,
            Utils.getMiningLevel(stack, Utils.PICKAXE)
          ),
          this::takeItemStackFromChest
        );
    }

    /**
     * Wait for a needed pickaxe.
     *
     * @return NEEDS_PICKAXE
     */
    @NotNull
    private AIState waitForWeapon()
    {
        if (checkForWeapon())
        {
            delay += DELAY_RECHECK;
            return NEEDS_WEAPON;
        }
        return IDLE;
    }

    /**
     * Ensures that we have a pickaxe available.
     * Will set {@code needsPickaxe} accordingly.
     *
     * @return true if we have a pickaxe
     */
    public boolean checkForWeapon()
    {
        //Check for a pickaxe
        needsWeapon = !InventoryFunctions
                         .matchFirstInInventory(
                           worker.getInventoryCitizen(),
                           stack -> stack != null && Utils.doesItemServeAsWeapon(stack),
                           InventoryFunctions::doNothing
                         );

        delay += DELAY_RECHECK;

        if (needsWeapon)
        {
            if (walkToBuilding())
            {
                return false;
            }
            if (isWeaponInHut())
            {
                return true;
            }
            requestWithoutSpam(LanguageHandler.format("com.minecolonies.coremod.job.guard.needWeapon"));
        }
        return needsWeapon;
    }

    /**
     * Looks for a weapon.
     * The pickaxe will be taken from the chest.
     * Make sure that the worker stands next the chest to not break immersion.
     * Also make sure to have inventory space for the sword.
     *
     * @return true if a weapon was found
     */
    private boolean isWeaponInHut()
    {
        @Nullable final AbstractBuildingWorker buildingWorker = getOwnBuilding();
        return buildingWorker != null
                 && InventoryFunctions.matchFirstInInventory(
          buildingWorker.getTileEntity(),
          stack -> stack != null && (Utils.doesItemServeAsWeapon(stack)),
          this::takeItemStackFromChest
        );
    }

    /**
     * Walk to building and dump inventory.
     * If inventory is dumped, continue execution
     * so that the state can be resolved.
     *
     * @return INVENTORY_FULL | IDLE
     */
    @NotNull
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
        this.itemsNiceToHave().forEach(this::isInHut);
        // we dumped the inventory, reset actions done
        this.clearActionsDone();
        return IDLE;
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
     * Checks if the worker inventory and his building chest are full.
     *
     * @return true if both are full, else false
     */
    private boolean isInventoryAndChestFull()
    {
        @Nullable final AbstractBuildingWorker buildingWorker = getOwnBuilding();
        return InventoryUtils.isInventoryFull(worker.getInventoryCitizen())
                 && (buildingWorker != null
                       && InventoryUtils.isInventoryFull(buildingWorker.getTileEntity()));
    }

    /**
     * Can be overridden by implementations to specify items useful for the worker.
     * When the workers inventory is full, he will try to keep these items.
     * ItemStack amounts are ignored, the first stack found will be taken.
     *
     * @return a list with items nice to have for the worker
     */
    @NotNull
    protected List<ItemStack> itemsNiceToHave()
    {
        return new ArrayList<>();
    }

    /**
     * Clear the amount of blocks mined.
     * Call this when dumping into the chest.
     */
    private void clearActionsDone()
    {
        this.actionsDone = 0;
    }

    /**
     * Dumps one inventory slot into the building chest.
     *
     * @param keepIt used to test it that stack should be kept
     * @return true if is has to dump more.
     */
    private boolean dumpOneMoreSlot(@NotNull final Predicate<ItemStack> keepIt)
    {
        //Items already kept in the inventory
        final Map<ItemStorage, Integer> alreadyKept = new HashMap<>();
        final Map<ItemStorage, Integer> shouldKeep = this.needXForWorker();

        @Nullable final AbstractBuildingWorker buildingWorker = getOwnBuilding();

        return buildingWorker != null
                 && (walkToBuilding()
                       || InventoryFunctions.matchFirstInInventory(worker.getInventoryCitizen(),
          (i, stack) -> !(stack == null || keepIt.test(stack)) && shouldDumpItem(alreadyKept, shouldKeep, buildingWorker, stack, i)));
    }

    /**
     * Override this method if you want to keep an amount of items in inventory.
     * When the inventory is full, everything get's dumped into the building chest.
     * But you can use this method to hold some stacks back.
     *
     * @return a list of objects which should be kept.
     */
    protected Map<ItemStorage, Integer> needXForWorker()
    {
        return new HashMap<>();
    }

    /**
     * Checks if an item should be kept and deposits the rest into his chest.
     *
     * @param alreadyKept    already kept items.
     * @param shouldKeep     items that should be kept.
     * @param buildingWorker the building of the worker.
     * @param stack          the stack being analyzed.
     * @param i              the iteration inside the inventory.
     * @return true if should be dumped.
     */
    private boolean shouldDumpItem(
                                    @NotNull final Map<ItemStorage, Integer> alreadyKept, @NotNull final Map<ItemStorage, Integer> shouldKeep,
                                    @NotNull final AbstractBuildingWorker buildingWorker, @NotNull final ItemStack stack, final int i)
    {
        @Nullable final ItemStack returnStack;
        int amountToKeep = 0;
        if (keptEnough(alreadyKept, shouldKeep, stack))
        {
            returnStack = InventoryUtils.setStack(buildingWorker.getTileEntity(), stack);
        }
        else
        {
            final ItemStorage tempStorage = new ItemStorage(stack.getItem(), stack.getItemDamage(), stack.stackSize, false);
            final ItemStack tempStack = handleKeepX(alreadyKept, shouldKeep, tempStorage);
            if (tempStack == null || tempStack.stackSize == 0)
            {
                return false;
            }
            amountToKeep = stack.stackSize - tempStorage.getAmount();
            returnStack = InventoryUtils.setStack(buildingWorker.getTileEntity(), tempStack);
        }
        if (returnStack == null)
        {
            worker.getInventoryCitizen().decrStackSize(i, stack.stackSize - amountToKeep);
            return amountToKeep == 0;
        }
        worker.getInventoryCitizen().decrStackSize(i, stack.stackSize - returnStack.stackSize - amountToKeep);
        //Check that we are not inserting into a full inventory.
        return stack.stackSize != returnStack.stackSize;
    }

    /**
     * Checks if enough items have been marked as to be kept already.
     *
     * @param alreadyKept kept items.
     * @param shouldKeep  items to keep.
     * @param stack       stack to analyse.
     * @return true if the the item shouldn't be kept.
     */
    private static boolean keptEnough(@NotNull final Map<ItemStorage, Integer> alreadyKept, @NotNull final Map<ItemStorage, Integer> shouldKeep, @NotNull final ItemStack stack)
    {
        final ArrayList<Map.Entry<ItemStorage, Integer>> tempKeep = new ArrayList<>(shouldKeep.entrySet());
        for (final Map.Entry<ItemStorage, Integer> tempEntry : tempKeep)
        {
            final ItemStorage tempStorage = tempEntry.getKey();
            if (tempStorage != null && tempStorage.getItem() == stack.getItem() && tempStorage.getDamageValue() != stack.getItemDamage())
            {
                shouldKeep.put(new ItemStorage(stack.getItem(), stack.getItemDamage(), 0, tempStorage.ignoreDamageValue()), tempEntry.getValue());
                break;
            }
        }
        final ItemStorage tempStorage = new ItemStorage(stack.getItem(), stack.getItemDamage(), 0, false);

        //Check first if the the item shouldn't be kept if it should be kept check if we already kept enough of them.
        return shouldKeep.get(tempStorage) == null
                 || (alreadyKept.get(tempStorage) != null
                       && alreadyKept.get(tempStorage) >= shouldKeep.get(tempStorage));
    }

    /**
     * Handle the cases when X items should be kept.
     *
     * @param alreadyKept already kept items.
     * @param shouldKeep  to keep items.
     * @param tempStorage item to analyze.
     * @return null if should be kept entirely, else itemStack with amount which should be dumped.
     */
    private static ItemStack handleKeepX(
                                          @NotNull final Map<ItemStorage, Integer> alreadyKept,
                                          @NotNull final Map<ItemStorage, Integer> shouldKeep, @NotNull final ItemStorage tempStorage)
    {
        int amountKept = 0;
        if (alreadyKept.get(tempStorage) != null)
        {
            amountKept = alreadyKept.remove(tempStorage);
        }

        if (shouldKeep.get(tempStorage) >= (tempStorage.getAmount() + amountKept))
        {
            alreadyKept.put(tempStorage, tempStorage.getAmount() + amountKept);
            return null;
        }
        alreadyKept.put(tempStorage, shouldKeep.get(tempStorage));
        final int dump = tempStorage.getAmount() + amountKept - shouldKeep.get(tempStorage);

        //Create tempStack with the amount of items that should be dumped.
        return new ItemStack(tempStorage.getItem(), dump, tempStorage.getDamageValue());
    }


    /**
     * Require that items are in the workers inventory.
     * This safeguard ensures you have said items before you execute a task.
     * Please stop execution on false returned.
     *
     * @param items the items needed
     * @return false if they are in inventory
     */
    protected boolean checkOrRequestItems(@Nullable final ItemStack... items)
    {
        return checkOrRequestItems(true, items);
    }

    /**
     * Require that items are in the workers inventory.
     * This safeguard ensures you have said items before you execute a task.
     * Please stop execution on false returned.
     *
     * @param useItemDamage compare the itemDamage of the values.
     * @param items the items needed
     * @return false if they are in inventory
     */
    protected boolean checkOrRequestItems(final boolean useItemDamage, @Nullable final ItemStack... items)
    {
        if (items == null)
        {
            return false;
        }
        boolean allClear = true;
        for (final @Nullable ItemStack stack : items)
        {
            if (stack == null || stack.getItem() == null)
            {
                continue;
            }
            final int countOfItem;
            if(useItemDamage)
            {
                countOfItem = worker.getItemCountInInventory(stack.getItem(), stack.getItemDamage());
            }
            else
            {
                countOfItem = worker.getItemCountInInventory(stack.getItem(), -1);
            }
            if (countOfItem < 1)
            {
                final int itemsLeft = stack.stackSize - countOfItem;
                @NotNull final ItemStack requiredStack = new ItemStack(stack.getItem(), itemsLeft);
                itemsCurrentlyNeeded.add(requiredStack);
                allClear = false;
            }
            else
            {
                itemsCurrentlyNeeded.clear();
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

    /**
     * Calculate the citizens inventory.
     *
     * @return A InventoryCitizen matching this ai's citizen.
     */
    @NotNull
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
    protected boolean neededForWorker(@Nullable final ItemStack stack)
    {
        return false;
    }

    /**
     * Check and ensure that we hold the most efficient tool for the job.
     * <p>
     * If we have no tool for the job, we will request on, return immediately.
     *
     * @param target the block to mine
     * @return true if we have a tool for the job
     */
    protected final boolean holdEfficientTool(@NotNull final Block target)
    {
        final int bestSlot = getMostEfficientTool(target);
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
    private void requestTool(@NotNull final Block target)
    {
        final String tool = target.getHarvestTool(target.getDefaultState());
        final int required = target.getHarvestLevel(target.getDefaultState());
        updateToolFlag(tool, required);
    }

    /**
     * Checks if said tool of said level is usable.
     * if not, it updates the needsTool flag for said tool.
     *
     * @param tool     the tool needed
     * @param required the level needed (for pickaxe only)
     */
    private void updateToolFlag(@NotNull final String tool, final int required)
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
                Log.getLogger().error("Invalid tool " + tool + " not implemented as tool!");
        }
    }

    /**
     * Calculates the most efficient tool to use
     * on that block.
     *
     * @param target the Block type to mine
     * @return the slot with the best tool
     */
    private int getMostEfficientTool(@NotNull final Block target)
    {
        final String tool = target.getHarvestTool(target.getDefaultState());
        final int required = target.getHarvestLevel(target.getDefaultState());
        int bestSlot = -1;
        int bestLevel = Integer.MAX_VALUE;
        @NotNull final InventoryCitizen inventory = worker.getInventoryCitizen();
        final int hutLevel = worker.getWorkBuilding().getBuildingLevel();

        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            final ItemStack item = inventory.getStackInSlot(i);
            final int level = Utils.getMiningLevel(item, tool);

            if (level >= required && level < bestLevel)
            {
                if (tool == null || InventoryUtils.verifyToolLevel(item, level, hutLevel))
                {
                    bestSlot = i;
                    bestLevel = level;
                }
            }
        }

        return bestSlot;
    }

    /**
     * Will delay one time and pass through the second time.
     * Use for convenience instead of SetDelay
     *
     * @param time the time to wait
     * @return true if you should wait
     */
    protected final boolean hasNotDelayed(final int time)
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
     * Tell the ai that you have done one more action.
     * <p>
     * if the actions exceed a certain number,
     * the ai will dump it's inventory.
     * <p>
     * For example:
     * <p>
     * After x blocks, bring everything back.
     */
    protected final void incrementActionsDone()
    {
        actionsDone++;
    }
}
