package com.minecolonies.coremod.entity.ai.basic;

import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobDeliveryman;
import com.minecolonies.coremod.entity.ai.item.handling.ItemStorage;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import com.minecolonies.coremod.entity.pathfinding.WalkToProxy;
import com.minecolonies.coremod.inventory.InventoryCitizen;
import com.minecolonies.coremod.util.*;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.wrapper.InvWrapper;
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
    /**
     * Buffer time in ticks he will accept a last attacker as valid.
     */
    protected static final int ATTACK_TIME_BUFFER = 50;

    /**
     * The maximum range to keep from the current building place.
     */
    public static final  int             EXCEPTION_TIMEOUT             = 100;
    /**
     * The maximum range to keep from the current building place.
     */
    private static final int             MAX_ADDITIONAL_RANGE_TO_BUILD = 25;
    /**
     * Time in ticks to wait until the next check for items.
     */
    private static final int             DELAY_RECHECK                 = 10;
    /**
     * The default range for any walking to blocks.
     */
    private static final int             DEFAULT_RANGE_FOR_DELAY       = 4;
    /**
     * The number of actions done before item dump.
     */
    private static final int             ACTIONS_UNTIL_DUMP            = 32;
    /**
     * Hit a block every x ticks when mining.
     */
    private static final int             HIT_EVERY_X_TICKS             = 5;
    /**
     * Diamond pickaxe level.
     */
    private static final int             DIAMOND_LEVEL                 = 3;
    /**
     * The list of all items and their quantity that were requested by the worker.
     * Warning: This list does not change, if you need to see what is currently missing,
     * look at @see #itemsCurrentlyNeeded for things the miner needs.
     * <p>
     * Will be cleared on restart, be aware!
     */
    @NotNull
    private final        List<ItemStack> itemsNeeded                   = new ArrayList<>();
    /**
     * The block the ai is currently working at or wants to work.
     */
    @Nullable
    protected            BlockPos        currentWorkingLocation        = null;
    /**
     * The block the ai is currently standing at or wants to stand.
     */
    @Nullable
    protected            BlockPos        currentStandingLocation       = null;
    /**
     * The time in ticks until the next action is made.
     */
    private              int             delay                         = 0;

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
     * This will count up and progressively disable the entity
     */
    private int exceptionTimer = 1;

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
          new AITarget(this::initSafetyChecks),
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
          new AITarget(() -> this.getOwnBuilding().areItemsNeeded(), this::waitForNeededItems),
                /*
                 * Wait for different tools.
                 */
          new AITarget(() -> this.getOwnBuilding().needsShovel(), this::waitForShovel),
          new AITarget(() -> this.getOwnBuilding().needsAxe(), this::waitForAxe),
          new AITarget(() -> this.getOwnBuilding().needsHoe(), this::waitForHoe),
          new AITarget(() -> this.getOwnBuilding().needsPickaxe(), this::waitForPickaxe),
          new AITarget(() -> this.getOwnBuilding().needsWeapon(), this::waitForWeapon),

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

    @Override
    protected void onException(final RuntimeException e)
    {
        try
        {
            final int timeout = EXCEPTION_TIMEOUT * exceptionTimer;
            this.setDelay(timeout);
            // wait for longer now
            exceptionTimer *= 2;
            if (worker != null)
            {
                final String name = this.worker.getName();
                final BlockPos workerPosition = worker.getPosition();
                final AbstractJob colonyJob = worker.getColonyJob();
                final String jobName = colonyJob == null ? "null" : colonyJob.getName();
                Log.getLogger().error("Pausing Entity " + name + " (" + jobName + ") at " + workerPosition + " for " + timeout + " Seconds because of error:");
            }
            else
            {
                Log.getLogger().error("Pausing Entity that is null for " + timeout + " Seconds because of error:");
            }

            // fix for printing the actual exception
            e.printStackTrace();
        }
        catch (RuntimeException exp)
        {
            Log.getLogger().error("Welp reporting crashed:");
            exp.printStackTrace();
            Log.getLogger().error("Caused by ai exception:");
            e.printStackTrace();
        }
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
        return (worker.isInventoryFull()
                  || actionsDone >= getActionsDoneUntilDumping()
                  || wantInventoryDumped())
                 && !(job instanceof JobDeliveryman);
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
    @Nullable
    private AIState initSafetyChecks()
    {
        if (null == getOwnBuilding())
        {
            if (getState() == INIT)
            {
                return INIT;
            }

            return IDLE;
        }

        if (getState() == INIT)
        {
            return IDLE;
        }

        return null;
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
        if (!getOwnBuilding().areItemsNeeded())
        {
            itemsNeeded.clear();
            job.clearItemsNeeded();
            return IDLE;
        }
        if (!walkToBuilding())
        {
            delay += DELAY_RECHECK;
            final ItemStack first = getOwnBuilding().getFirstNeededItem();
            //Takes one Stack from the hut if existent
            if (isInHut(first))
            {
                return NEEDS_ITEM;
            }

            if (!getOwnBuilding().hasOnGoingDelivery())
            {
                requestWithoutSpam(first.getCount() + " " + first.getDisplayName());
            }
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
        InventoryUtils.getItemHandlerAsList(new InvWrapper(worker.getInventoryCitizen())).forEach(job::removeItemNeeded);
        getOwnBuilding().setItemsCurrentlyNeeded(job.getItemsNeeded());
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
     * Check all chests in the worker hut for a required item.
     *
     * @param is the type of item requested (amount is ignored)
     * @return true if a stack of that type was found
     */
    public boolean isInHut(@Nullable final ItemStack is)
    {
        @Nullable final AbstractBuildingWorker building = getOwnBuilding();

        boolean hasItem;
        if (building != null)
        {
            hasItem = isInTileEntity(building.getTileEntity(), is);

            if (hasItem)
            {
                return true;
            }

            for (final BlockPos pos : building.getAdditionalCountainers())
            {
                final TileEntity entity = world.getTileEntity(pos);
                if (entity instanceof TileEntityChest)
                {
                    hasItem = isInTileEntity((TileEntityChest) entity, is);

                    if (hasItem)
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Request an Item without spamming the chat.
     *
     * @param chat the Item Name
     */
    private void requestWithoutSpam(@NotNull final String chat)
    {
        chatSpamFilter.requestTextStringWithoutSpam(chat);
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
     * Finds the first @see ItemStack the type of {@code is}.
     * It will be taken from the chest and placed in the workers inventory.
     * Make sure that the worker stands next the chest to not break immersion.
     * Also make sure to have inventory space for the stack.
     *
     * @param entity the tileEntity chest or building.
     * @param is     the itemStack.
     * @return true if found the stack.
     */
    public boolean isInTileEntity(TileEntityChest entity, ItemStack is)
    {
        return is != null
                && InventoryFunctions
                           .matchFirstInProviderWithAction(
                        entity,
                        stack -> stack != null && is.isItemEqualIgnoreDurability(stack),
                        this::takeItemStackFromProvider
                      );
    }

    /**
     * Finds the first @see ItemStack the type of {@code is}.
     * It will be taken from the chest and placed in the workers inventory.
     * Make sure that the worker stands next the chest to not break immersion.
     * Also make sure to have inventory space for the stack.
     *
     * @param entity the tileEntity chest or building.
     * @param itemStackSelectionPredicate the criteria.
     * @return true if found the stack.
     */
    public boolean isInTileEntity(TileEntityChest entity, @NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        return InventoryFunctions
                .matchFirstInProviderWithAction(
                        entity,
                        itemStackSelectionPredicate,
                        this::takeItemStackFromProvider
                );
    }

    /**
     * Request an Item without spamming the chat.
     *
     * @param chat the Item Name
     */
    private void requestWithoutSpam(@NotNull final TextComponentBase chat)
    {
        chatSpamFilter.requestTextComponentWithoutSpam(chat);
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
     * Finds the first @see ItemStack the type of {@code is}.
     * It will be taken from the chest and placed in the workers inventory.
     * Make sure that the worker stands next the chest to not break immersion.
     * Also make sure to have inventory space for the stack.
     *
     * @param entity    the tileEntity chest or building.
     * @param tool      the tool.
     * @param toolLevel the min tool level.
     * @return true if found the tool.
     */
    public boolean isToolInTileEntity(TileEntityChest entity, final String tool, int toolLevel)
    {
        return InventoryFunctions.matchFirstInProviderWithAction(
                entity,
                stack -> Utils.isTool(stack, tool) && InventoryUtils.hasToolLevel(stack, tool, toolLevel),
                this::takeItemStackFromProvider
        );
    }

    /**
     * Takes whatever is in that slot of the workers chest and puts it in his inventory.
     * If the inventory is full, only the fitting part will be moved.
     * Beware this method shouldn't be private, because the generic access won't work within a lambda won't work else.
     *
     * @param provider  The provider to take from.
     * @param slotIndex The slot to take.
     */
    public void takeItemStackFromProvider(@NotNull ICapabilityProvider provider, int slotIndex)
    {
        InventoryUtils.transferItemStackIntoNextFreeSlotFromProvider(provider, slotIndex, new InvWrapper(worker.getInventoryCitizen()));
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
        getOwnBuilding().setNeedsShovel(checkForTool(Utils.SHOVEL));
        return getOwnBuilding().needsShovel();
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
                                           .matchFirstInProvider(
                                                   worker,
                                       stack -> Utils.isTool(stack, tool),
                                       InventoryFunctions::doNothing
                                     );

        final int hutLevel = worker.getWorkBuilding().getBuildingLevel();
        final InventoryCitizen inventory = worker.getInventoryCitizen();
        final boolean isUsable = InventoryUtils.isToolInItemHandler(new InvWrapper(inventory), tool, hutLevel);


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
        if (!getOwnBuilding().hasOnGoingDelivery())
        {
            chatSpamFilter.talkWithoutSpam("entity.worker.toolRequest", tool, InventoryUtils.swapToolGrade(hutLevel));
        }
        return true;
    }

    /**
     * Check all chests in the worker hut for a required tool.
     *
     * @param tool the type of tool requested (amount is ignored)
     * @return true if a stack of that type was found
     */
    public boolean isToolInHut(final String tool)
    {
        @Nullable final AbstractBuildingWorker building = getOwnBuilding();

        boolean hasItem;
        if (building != null)
        {
            hasItem = isToolInTileEntity(building.getTileEntity(), tool);

            if (hasItem)
            {
                return true;
            }

            for (final BlockPos pos : building.getAdditionalCountainers())
            {
                final TileEntity entity = world.getTileEntity(pos);
                if (entity instanceof TileEntityChest)
                {
                    hasItem = isToolInTileEntity((TileEntityChest) entity, tool);

                    if (hasItem)
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Finds the first @see ItemStack the type of {@code is}.
     * It will be taken from the chest and placed in the workers inventory.
     * Make sure that the worker stands next the chest to not break immersion.
     * Also make sure to have inventory space for the stack.
     *
     * @param entity the tileEntity chest or building.
     * @param tool   the tool.
     * @return true if found the tool.
     */
    public boolean isToolInTileEntity(TileEntityChest entity, final String tool)
    {
        return InventoryFunctions.matchFirstInProviderWithAction(
                entity,
                stack -> Utils.isTool(stack, tool),
                this::takeItemStackFromProvider
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
        getOwnBuilding().setNeedsAxe(checkForTool(Utils.AXE));
        return getOwnBuilding().needsAxe();
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
        getOwnBuilding().setNeedsHoe(checkForTool(Utils.HOE));
        return getOwnBuilding().needsHoe();
    }

    /**
     * Wait for a needed pickaxe.
     *
     * @return NEEDS_PICKAXE
     */
    @NotNull
    private AIState waitForPickaxe()
    {
        if (checkForPickaxe(getOwnBuilding().getNeededPickaxeLevel()))
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
        getOwnBuilding().setNeedsPickaxe(!InventoryFunctions
                                                  .matchFirstInProvider(
                                                          worker,
                                              stack -> Utils.checkIfPickaxeQualifies(
                                                minlevel, Utils.getMiningLevel(stack, Utils.PICKAXE)),
                                              InventoryFunctions::doNothing
                                            ));

        delay += DELAY_RECHECK;

        final InventoryCitizen inventory = worker.getInventoryCitizen();
        final int hutLevel = worker.getWorkBuilding().getBuildingLevel();
        final boolean isUsable = InventoryUtils.isToolInItemHandler(new InvWrapper(inventory), Utils.PICKAXE, hutLevel);

        if (!isUsable)
        {
            getOwnBuilding().setNeedsPickaxe(true);
        }
        if (getOwnBuilding().needsPickaxe())
        {
            getOwnBuilding().setNeedsPickaxeLevel(minlevel);
            if (walkToBuilding())
            {
                return false;
            }
            if (isPickaxeInHut(minlevel))
            {
                return true;
            }
            if (!getOwnBuilding().hasOnGoingDelivery())
            {
                chatSpamFilter.talkWithoutSpam("entity.worker.pickaxeRequest",
                  InventoryUtils.swapToolGrade(minlevel),
                  InventoryUtils.swapToolGrade(hutLevel));
            }
        }

        return getOwnBuilding().needsPickaxe();
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
                && InventoryFunctions.matchFirstInProviderWithAction(
          buildingWorker.getTileEntity(),
          stack -> Utils.checkIfPickaxeQualifies(
            minlevel,
            Utils.getMiningLevel(stack, Utils.PICKAXE)
          ),
                this::takeItemStackFromProvider
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
        getOwnBuilding().setNeedsWeapon(!InventoryFunctions
                                                 .matchFirstInProvider(
                                                         worker,
                                             stack -> stack != null && Utils.doesItemServeAsWeapon(stack),
                                             InventoryFunctions::doNothing
                                           ));

        delay += DELAY_RECHECK;

        if (getOwnBuilding().needsWeapon())
        {
            if (walkToBuilding())
            {
                return false;
            }
            if (isWeaponInHut())
            {
                return true;
            }
            requestWithoutSpam(new TextComponentTranslation("com.minecolonies.coremod.job.guard.needWeapon"));
        }
        return getOwnBuilding().needsWeapon();
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
                && InventoryFunctions.matchFirstInProviderWithAction(
          buildingWorker.getTileEntity(),
          stack -> stack != null && (Utils.doesItemServeAsWeapon(stack)),
                this::takeItemStackFromProvider
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
        if (!worker.isWorkerAtSiteWithMove(getOwnBuilding().getLocation(), DEFAULT_RANGE_FOR_DELAY))
        {
            return INVENTORY_FULL;
        }

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
        return dumpOneMoreSlot(getOwnBuilding()::neededForWorker);
    }

    /**
     * Checks if the worker inventory and his building chest are full.
     *
     * @return true if both are full, else false
     */
    private boolean isInventoryAndChestFull()
    {
        @Nullable final AbstractBuildingWorker buildingWorker = getOwnBuilding();
        return InventoryUtils.isProviderFull(worker)
                 && (buildingWorker != null
                && InventoryUtils.isProviderFull(buildingWorker.getTileEntity()));
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
        final Map<ItemStorage, Integer> shouldKeep = getOwnBuilding().getRequiredItemsAndAmount();

        @Nullable final AbstractBuildingWorker buildingWorker = getOwnBuilding();

        return buildingWorker != null
                && (walkToBuilding()
                || InventoryFunctions.matchFirstInProvider(worker,
                (slot, stack) -> !(InventoryUtils.isItemStackEmpty(stack) || keepIt.test(stack)) && shouldDumpItem(alreadyKept, shouldKeep, buildingWorker, stack, slot)));
    }

    /**
     * Checks if an item should be kept and deposits the rest into his chest.
     *
     * @param alreadyKept    already kept items.
     * @param shouldKeep     items that should be kept.
     * @param buildingWorker the building of the worker.
     * @param stack          the stack being analyzed.
     * @param slot           the iteration inside the inventory.
     * @return true if should be dumped.
     */
    private boolean shouldDumpItem(
                                    @NotNull final Map<ItemStorage, Integer> alreadyKept, @NotNull final Map<ItemStorage, Integer> shouldKeep,
                                    @NotNull final AbstractBuildingWorker buildingWorker, @NotNull final ItemStack stack, final int slot)
    {
        @Nullable final ItemStack returnStack;
        int amountToKeep = 0;
        if (keptEnough(alreadyKept, shouldKeep, stack))
        {
            returnStack = InventoryUtils.addItemStackToProviderWithResult(buildingWorker.getTileEntity(), stack.copy());
        }
        else
        {
            final ItemStorage tempStorage = new ItemStorage(stack.getItem(), stack.getItemDamage(), stack.getCount(), false);
            final ItemStack tempStack = handleKeepX(alreadyKept, shouldKeep, tempStorage);
            if (InventoryUtils.isItemStackEmpty(tempStack))
            {
                return false;
            }
            amountToKeep = stack.getCount() - tempStorage.getAmount();
            returnStack = InventoryUtils.addItemStackToProviderWithResult(buildingWorker.getTileEntity(), tempStack);
        }
        if (InventoryUtils.isItemStackEmpty(returnStack))
        {
            new InvWrapper(worker.getInventoryCitizen()).extractItem(slot, stack.getCount() - amountToKeep, false);
            return amountToKeep == 0;
        }

        new InvWrapper(worker.getInventoryCitizen()).extractItem(slot, stack.getCount() - returnStack.getCount() - amountToKeep, false);

        //Check that we are not inserting into a full inventory.
        return stack.getCount() != returnStack.getCount();
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
     * @return InventoryUtils.EMPTY if should be kept entirely, else itemStack with amount which should be dumped.
     */
    @NotNull
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
            return InventoryUtils.EMPTY;
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
    public boolean checkOrRequestItems(@Nullable final ItemStack... items)
    {
        return checkOrRequestItems(true, items);
    }

    /**
     * Require that items are in the workers inventory.
     * This safeguard ensures you have said items before you execute a task.
     * Please stop execution on false returned.
     *
     * @param useItemDamage compare the itemDamage of the values.
     * @param items         the items needed
     * @return false if they are in inventory
     */
    public boolean checkOrRequestItems(final boolean useItemDamage, @Nullable final ItemStack... items)
    {
        if (items == null)
        {
            return false;
        }
        boolean allClear = true;
        for (final @Nullable ItemStack stack : items)
        {
            if (InventoryUtils.isItemStackEmpty(stack))
            {
                continue;
            }
            final int countOfItem;
            if (useItemDamage)
            {
                countOfItem = worker.getItemCountInInventory(stack.getItem(), stack.getItemDamage());
            }
            else
            {
                countOfItem = worker.getItemCountInInventory(stack.getItem(), -1);
            }
            if (countOfItem < 1)
            {
                final int itemsLeft = stack.getCount() - countOfItem;
                @NotNull final ItemStack requiredStack = new ItemStack(stack.getItem(), itemsLeft, -1);
                getOwnBuilding().addNeededItems(requiredStack);
                allClear = false;
            }
            else
            {
                getOwnBuilding().clearNeededItems();
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
     * Check and ensure that we hold the most efficient tool for the job.
     * <p>
     * If we have no tool for the job, we will request on, return immediately.
     *
     * @param target the block to mine
     * @return true if we have a tool for the job
     */
    public final boolean holdEfficientTool(@NotNull final Block target)
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
                checkForPickaxe(DIAMOND_LEVEL);
                Log.getLogger().error("Invalid tool " + tool + " not implemented as tool will require pickaxe level 4 instead.");
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

        for (int i = 0; i < new InvWrapper(worker.getInventoryCitizen()).getSlots(); i++)
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

    /**
     * Calculates the working position.
     * <p>
     * Takes a min distance from width and length.
     * <p>
     * Then finds the floor level at that distance and then check if it does contain two air levels.
     *
     * @param targetPosition the position to work at.
     * @return BlockPos position to work from.
     */
    public BlockPos getWorkingPosition(BlockPos targetPosition)
    {
        return targetPosition;
    }

    /**
     * Calculates the working position.
     * <p>
     * Takes a min distance from width and length.
     * <p>
     * Then finds the floor level at that distance and then check if it does contain two air levels.
     *
     * @param distance  the extra distance to apply away from the building.
     * @param targetPos the target position which needs to be worked.
     * @param offset    an additional offset
     * @return BlockPos position to work from.
     */
    public BlockPos getWorkingPosition(final int distance, final BlockPos targetPos, final int offset)
    {
        if (offset > MAX_ADDITIONAL_RANGE_TO_BUILD)
        {
            return targetPos;
        }

        @NotNull final EnumFacing[] directions = {EnumFacing.EAST, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.SOUTH};

        //then get a solid place with two air spaces above it in any direction.
        for (final EnumFacing direction : directions)
        {
            @NotNull final BlockPos positionInDirection = getPositionInDirection(direction, distance + offset, targetPos);
            if (EntityUtils.checkForFreeSpace(world, positionInDirection)
                    && world.getBlockState(positionInDirection.up()).getBlock() != Blocks.SAPLING)
            {
                return positionInDirection;
            }
        }

        //if necessary we can could implement calling getWorkingPosition recursively and add some "offset" to the sides.
        return getWorkingPosition(distance, targetPos, offset + 1);
    }

    /**
     * Gets a floorPosition in a particular direction.
     *
     * @param facing    the direction.
     * @param distance  the distance.
     * @param targetPos the position to work at.
     * @return a BlockPos position.
     */
    @NotNull
    private BlockPos getPositionInDirection(final EnumFacing facing, final int distance, final BlockPos targetPos)
    {
        return BlockPosUtil.getFloor(targetPos.offset(facing, distance), world);
    }
}
