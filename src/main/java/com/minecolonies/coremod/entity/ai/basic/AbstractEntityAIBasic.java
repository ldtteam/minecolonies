package com.minecolonies.coremod.entity.ai.basic;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.colony.requestsystem.requestable.Tool;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.pathfinding.IWalkToProxy;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.IToolType;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobDeliveryman;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import com.minecolonies.coremod.entity.pathfinding.EntityCitizenWalkToProxy;
import com.minecolonies.coremod.inventory.InventoryCitizen;
import com.minecolonies.coremod.util.WorkerUtil;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.BuildingConstants.MAX_PRIO;
import static com.minecolonies.api.util.constant.CitizenConstants.*;
import static com.minecolonies.api.util.constant.Suppression.RAWTYPES;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.coremod.entity.ai.util.AIState.*;
import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

/**
 * This class provides basic ai functionality.
 *
 * @param <J> The job this ai has to fulfil
 */
public abstract class AbstractEntityAIBasic<J extends AbstractJob> extends AbstractAISkeleton<J>
{
    /**
     * The standard delay after each terminated action.
     */
    protected static final int STANDARD_DELAY = 5;

    /**
     * The block the ai is currently working at or wants to work.
     */
    @Nullable
    protected BlockPos currentWorkingLocation = null;

    /**
     * The block the ai is currently standing at or wants to stand.
     */
    @Nullable
    protected BlockPos currentStandingLocation = null;

    /**
     * The time in ticks until the next action is made.
     */
    private int delay = 0;

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
    private IWalkToProxy proxy;

    /**
     * This will count up and progressively disable the entity
     */
    private int exceptionTimer = 1;

    /**
     * The restaurant this AI usually goes to.
     */
    private BlockPos restaurant = null;

    /**
     * What he currently might be needing.
     */
    protected Predicate<ItemStack> needsCurrently = null;

    /**
     * The current position the worker should walk to.
     */
    protected BlockPos walkTo = null;

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
            Init safety checks and transition to IDLE
           */
          new AITarget(this::initSafetyChecks),
          /*
            Update chestbelt and nametag
            Will be executed every time
            and does not stop execution
           */
          new AITarget(this::updateVisualState),
          /*
            If waitingForSomething returns true
            stop execution to wait for it.
            this keeps the current state
            (returning null would not stop execution)
           */
          new AITarget(this::waitingForSomething, this::getState),
          /*
            Check if any items are needed.
            If yes, transition to NEEDS_ITEM.
            and wait for new items.
           */
          new AITarget(() ->
          {
              return getState() == NEEDS_ITEM
                       || this.getOwnBuilding()
                            .hasWorkerOpenRequestsFiltered(
                              worker.getCitizenData(),
                              r -> !worker.getCitizenData().isRequestAsync(r.getToken()))
                       || this.getOwnBuilding().hasCitizenCompletedRequests(worker.getCitizenData());
          }, this::waitForRequests),
          /*
            Dumps inventory as long as needs be.
            If inventory is dumped, execution continues
            to resolve state.
           */
          new AITarget(INVENTORY_FULL, this::dumpInventory),
          /*
            Check if inventory has to be dumped.
           */
          new AITarget(this::inventoryNeedsDump, INVENTORY_FULL),
          /*
           * Reset to idle if no specific tool is needed.
           */
          new AITarget(() ->
          {
              return getState() == NEEDS_TOOL
                       && this.getOwnBuilding().getOpenRequestsOfType(
                worker.getCitizenData(),
                TypeToken.of(Tool.class)
              ).isEmpty();
          }, IDLE),
          /*
           * Called when the citizen saturation falls too low.
           */
          new AITarget(this::shouldGetFood, this::searchForFood),
          /*
           * Gather a needed item.
           */
          new AITarget(GATHERING_REQUIRED_MATERIALS, this::getNeededItem)
        );
    }

    /**
     * Retrieve a material from the building.
     * For this go to the building if no position has been set.
     * Then check for the chest with the required material and set the position and return.
     *
     * If the position has been set navigate to it.
     * On arrival transfer to inventory and return to StartWorking.
     *
     * @return the next state to transfer to.
     */
    private AIState getNeededItem()
    {
        worker.getCitizenStatusHandler().setLatestStatus(new TextComponentTranslation(COM_MINECOLONIES_COREMOD_STATUS_GATHERING));
        setDelay(STANDARD_DELAY);

        if (walkTo == null && walkToBuilding())
        {
            return getState();
        }

        if (needsCurrently == null || !InventoryUtils.hasItemInProvider(getOwnBuilding(), needsCurrently))
        {
            return getStateAfterPickUp();
        }
        else
        {
            if (walkTo == null)
            {
                final BlockPos pos = getOwnBuilding().getTileEntity().getPositionOfChestWithItemStack(needsCurrently);
                if (pos == null)
                {
                    return getStateAfterPickUp();
                }
                walkTo = pos;
            }

            if (walkToBlock(walkTo))
            {
                setDelay(2);
                return getState();
            }

            final boolean transferred = tryTransferFromPosToWorker(walkTo, needsCurrently);
            if (!transferred)
            {
                walkTo = null;
                return getStateAfterPickUp();
            }
            walkTo = null;
        }

        return getStateAfterPickUp();
    }

    /**
     * The state to transform to after picking up things.
     * @return the next state to go to.
     */
    public AIState getStateAfterPickUp()
    {
        return START_WORKING;
    }

    /**
     * Check if the citizen should get food, meaning, check if he checked for food today already and check if his saturation is decent.
     *
     * @return true if he should go search for food.
     */
    private boolean shouldGetFood()
    {
        return (worker.getCitizenData().getSaturation() <= HIGH_SATURATION
                  && !job.hasCheckedForFoodToday())
                 || worker.getCitizenData().getSaturation() <= 0;
    }

    @Nullable
    public <W extends AbstractBuildingWorker> W getOwnBuilding()
    {
        final AbstractBuildingWorker worker = getOwnBuilding(getExpectedBuildingClass());
        return worker == null ? null : (W) worker;
    }

    /**
     * Can be overridden in implementations to return the exact building type the worker expects.
     *
     * @return the building type associated with this AI's worker.
     */
    public Class getExpectedBuildingClass()
    {
        return AbstractBuildingWorker.class;
    }

    /**
     * Can be overridden in implementations to return the exact building type.
     *
     * @return the building associated with this AI's worker.
     */
    @Nullable
    public <W extends AbstractBuildingWorker> W getOwnBuilding(@NotNull final Class<W> type)
    {
        if (type.isInstance(worker.getCitizenColonyHandler().getWorkBuilding()))
        {
            return (W) worker.getCitizenColonyHandler().getWorkBuilding();
        }
        else
        {
            if (worker.getCitizenData() != null)
            {
                worker.getCitizenData().setJob(null);
            }
        }
        return null;
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
                final AbstractJob colonyJob = worker.getCitizenJobHandler().getColonyJob();
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
        catch (final RuntimeException exp)
        {
            Log.getLogger().error("Welp reporting crashed:");
            exp.printStackTrace();
            Log.getLogger().error("Caused by ai exception:");
            e.printStackTrace();
        }
    }

    private AIState searchForFood()
    {
        if (!job.hasCheckedForFoodToday())
        {
            if (walkToBuilding())
            {
                return IDLE;
            }

            job.setCheckedForFood();
            if (isInHut(itemStack -> (!ItemStackUtils.isEmpty(itemStack) && itemStack.getItem() instanceof ItemFood) || worker.getCitizenData().getSaturation() > 0))
            {
                return IDLE;
            }
        }

        if (restaurant == null)
        {
            final BlockPos goodCook = worker.getCitizenColonyHandler().getColony().getBuildingManager().getBestRestaurant(worker);

            if (goodCook == null)
            {
                chatSpamFilter.talkWithoutSpam("com.minecolonies.coremod.ai.noRestaurant");
                return getState();
            }
            restaurant = goodCook;
        }

        walkToBlock(restaurant);
        return IDLE;
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
     * Check if we need to dump the worker inventory.
     * <p>
     * This will also ask the implementing ai
     * if we need to dump on custom reasons.
     * {@see wantInventoryDumped}
     *
     * @return true if we need to dump the inventory.
     */
    private boolean inventoryNeedsDump()
    {
        return (worker.getCitizenInventoryHandler().isInventoryFull()
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
                worker.getCitizenItemHandler().hitBlockWithToolInHand(currentWorkingLocation);
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
     * If the worker has open requests their results will be queried until they all are completed
     * Also waits for DELAY_RECHECK.
     *
     * @return NEEDS_ITEM
     */
    @NotNull
    private AIState waitForRequests()
    {
        delay = DELAY_RECHECK;
        updateWorkerStatusFromRequests();
        return lookForRequests();
    }

    private void updateWorkerStatusFromRequests()
    {
        if (!getOwnBuilding().hasWorkerOpenRequests(worker.getCitizenData()) && !getOwnBuilding().hasCitizenCompletedRequests(worker.getCitizenData()))
        {
            worker.getCitizenStatusHandler().setLatestStatus();
            return;
        }

        IRequest<?> request = getOwnBuilding().getCompletedRequests(worker.getCitizenData()).stream().findFirst().orElse(null);
        if (request == null)
        {
            request = getOwnBuilding().getOpenRequests(worker.getCitizenData()).stream().findFirst().orElse(null);
        }

        worker.getCitizenStatusHandler().setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.waiting"), request.getShortDisplayString());
    }

    /**
     * Utility method to search for items currently needed.
     * Poll this until all items are there.
     */
    @NotNull
    private AIState lookForRequests()
    {
        if (!this.getOwnBuilding().hasWorkerOpenRequestsFiltered(worker.getCitizenData(), r -> !worker.getCitizenData().isRequestAsync(r.getToken()))
              && !getOwnBuilding().hasCitizenCompletedRequests(worker.getCitizenData()))
        {
            return afterRequestPickUp();
        }
        if (!walkToBuilding() && getOwnBuilding().hasCitizenCompletedRequests(worker.getCitizenData()))
        {
            delay += DELAY_RECHECK;

            @SuppressWarnings(RAWTYPES) final ImmutableList<IRequest> completedRequests = getOwnBuilding().getCompletedRequests(worker.getCitizenData());

            completedRequests.stream().filter(r -> !(r.canBeDelivered())).forEach(r -> getOwnBuilding().markRequestAsAccepted(worker.getCitizenData(), r.getToken()));
            @SuppressWarnings(RAWTYPES) final IRequest firstDeliverableRequest = completedRequests.stream().filter(IRequest::canBeDelivered).findFirst().orElse(null);

            if (firstDeliverableRequest != null)
            {
                boolean async = false;
                if (worker.getCitizenData().isRequestAsync(firstDeliverableRequest.getToken()))
                {
                    async = true;
                    job.getAsyncRequests().remove(firstDeliverableRequest.getToken());
                }

                getOwnBuilding().markRequestAsAccepted(worker.getCitizenData(), firstDeliverableRequest.getToken());

                final ItemStack deliveredItemStack = firstDeliverableRequest.getDelivery();
                final int count = InventoryUtils.getItemCountInItemHandler(new InvWrapper(worker.getInventoryCitizen()),
                        stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(deliveredItemStack, stack, true, true));
                if (count >= deliveredItemStack.getCount() && getTotalRequiredAmount(deliveredItemStack) <= count)
                {
                    return NEEDS_ITEM;
                }

                //Takes one Stack from the hut if existent
                if (InventoryUtils.getItemCountInProvider(getOwnBuilding(),
                        stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(deliveredItemStack, stack, true, true)) >= deliveredItemStack.getCount() &&
                      InventoryUtils.transferXOfFirstSlotInProviderWithIntoNextFreeSlotInItemHandler(
                        getOwnBuilding(),
                              stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(deliveredItemStack, stack, true, true),
                        deliveredItemStack.getCount(),
                        new InvWrapper(worker.getInventoryCitizen())))
                {

                    return NEEDS_ITEM;
                }
                else
                {
                    //Seems like somebody else picked up our stack.
                    //Lets try this again.
                    if (async)
                    {
                        worker.getCitizenData().createRequestAsync(firstDeliverableRequest.getRequest());
                    }
                    else
                    {
                        worker.getCitizenData().createRequest(firstDeliverableRequest.getRequest());
                    }
                }
            }
        }

        return NEEDS_ITEM;
    }

    /**
     * What to do after picking up a request.
     * @return the next state to go to.
     */
    public AIState afterRequestPickUp()
    {
        return IDLE;
    }

    /**
     * Get the total amount required for an itemStack.
     * Workers have to override this if they have more information.
     * @param deliveredItemStack the required stack.
     * @return on default the size of the stack.
     */
    public int getTotalRequiredAmount(final ItemStack deliveredItemStack)
    {
        return deliveredItemStack.getCount();
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
     * Check all chests in the workers hut for a required item matching a certain predicate
     *
     * @param is the type of item requested (amount is ignored)
     * @return true if a stack of that type was found
     */
    public boolean isInHut(@Nullable final Predicate<ItemStack> is)
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
     * Check all chests in the workers hut for a required item.
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
                    hasItem = isInTileEntity(entity, is);

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
     * It will be taken from the chest and placed in the worker inventory.
     * Make sure that the worker stands next the chest to not break immersion.
     * Also make sure to have inventory space for the stack.
     *
     * @param entity the tileEntity chest or building or rack.
     * @param is     the itemStack.
     * @return true if found the stack.
     */
    public boolean isInTileEntity(final TileEntity entity, final ItemStack is)
    {
        return is != null
                 && InventoryFunctions
                      .matchFirstInProviderWithAction(
                        entity,
                        stack -> !ItemStackUtils.isEmpty(stack) &&  ItemStackUtils.compareItemStacksIgnoreStackSize(is, stack, true, true),
                        this::takeItemStackFromProvider
                      );
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
            proxy = new EntityCitizenWalkToProxy(worker);
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
     * It will be taken from the chest and placed in the worker inventory.
     * Make sure that the worker stands next the chest to not break immersion.
     * Also make sure to have inventory space for the stack.
     *
     * @param entity                      the tileEntity chest or building.
     * @param itemStackSelectionPredicate the criteria.
     * @return true if found the stack.
     */
    public boolean isInTileEntity(final TileEntityChest entity, @NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
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
    /*
    private void requestWithoutSpam(@NotNull final TextComponentBase chat)
    {
        chatSpamFilter.requestTextComponentWithoutSpam(chat);
    }
    */

    /**
     * Finds the first @see ItemStack the type of {@code is}.
     * It will be taken from the chest and placed in the worker inventory.
     * Make sure that the worker stands next the chest to not break immersion.
     * Also make sure to have inventory space for the stack.
     *
     * @param entity   the tileEntity chest or building.
     * @param toolType the type of tool.
     * @param minLevel the min tool level.
     * @param maxLevel the max tool lev	el.
     * @return true if found the tool.
     */
    public boolean retrieveToolInTileEntity(final TileEntity entity, final IToolType toolType, final int minLevel, final int maxLevel)
    {
        if (ToolType.NONE.equals(toolType))
        {
            return false;
        }
        return InventoryFunctions.matchFirstInProviderWithAction(
          entity,
          stack -> ItemStackUtils.hasToolLevel(stack, toolType, minLevel, maxLevel),
          this::takeItemStackFromProvider
        );
    }

    /**
     * Takes whatever is in that slot of the worker chest and puts it in his inventory.
     * If the inventory is full, only the fitting part will be moved.
     * Beware this method shouldn't be private, because the generic access won't work within a lambda won't work else.
     *
     * @param provider  The provider to take from.
     * @param slotIndex The slot to take.
     */
    public void takeItemStackFromProvider(@NotNull final ICapabilityProvider provider, final int slotIndex)
    {
        InventoryUtils.transferItemStackIntoNextBestSlotFromProvider(provider, slotIndex, new InvWrapper(worker.getInventoryCitizen()));
    }

    /**
     * Ensures that we have a appropriate tool available.
     * Will set {@code needsTool} accordingly.
     *
     * @param toolType type of tool we check for.
     * @return false if we have the tool
     */
    protected boolean checkForToolOrWeapon(@NotNull final IToolType toolType)
    {
        return checkForToolOrWeapon(toolType, TOOL_LEVEL_WOOD_OR_GOLD);
    }

    protected boolean checkForToolOrWeapon(@NotNull final IToolType toolType, final int minimalLevel)
    {
        final ImmutableList<IRequest<? extends Tool>> openToolRequests =
          getOwnBuilding().getOpenRequestsOfTypeFiltered(
            worker.getCitizenData(),
            TypeToken.of(Tool.class),
            r -> r.getRequest().getToolClass().equals(toolType) && r.getRequest().getMinLevel() >= minimalLevel);
        final ImmutableList<IRequest<? extends Tool>> completedToolRequests =
          getOwnBuilding().getCompletedRequestsOfTypeFiltered(
            worker.getCitizenData(),
            TypeToken.of(Tool.class),
            r -> r.getRequest().getToolClass().equals(toolType) && r.getRequest().getMinLevel() >= minimalLevel);

        if (checkForNeededTool(toolType, minimalLevel))
        {
            if (openToolRequests.isEmpty() && completedToolRequests.isEmpty())
            {
                final Tool request = new Tool(toolType, minimalLevel, getOwnBuilding().getMaxToolLevel() < minimalLevel ? minimalLevel : getOwnBuilding().getMaxToolLevel());
                worker.getCitizenData().createRequest(request);
            }
            return true;
        }

        return false;
    }

    /**
     * Ensures that we have a appropriate tool available.
     * ASync call on the tool.
     * 
     * @param toolType  Tool type that is requested
     * @param minimalLevel min. level of the tool
     */
    protected void checkForToolorWeaponASync(@NotNull final IToolType toolType, final int minimalLevel)
    {
        final ImmutableList<IRequest<? extends Tool>> openToolRequests =
          getOwnBuilding().getOpenRequestsOfTypeFiltered(
            worker.getCitizenData(),
            TypeToken.of(Tool.class),
            r -> r.getRequest().getToolClass().equals(toolType) && r.getRequest().getMinLevel() >= minimalLevel);
        final ImmutableList<IRequest<? extends Tool>> completedToolRequests =
          getOwnBuilding().getCompletedRequestsOfTypeFiltered(
            worker.getCitizenData(),
            TypeToken.of(Tool.class),
            r -> r.getRequest().getToolClass().equals(toolType) && r.getRequest().getMinLevel() >= minimalLevel);

        if (openToolRequests.isEmpty() && completedToolRequests.isEmpty() && !hasOpenToolRequest(toolType))
        {
            final Tool request = new Tool(toolType, minimalLevel, getOwnBuilding().getMaxToolLevel() < minimalLevel ? minimalLevel : getOwnBuilding().getMaxToolLevel());
            worker.getCitizenData().createRequestAsync(request);
        }
    }

    /**
     * Cancel all requests for a certain armor type for a certain citizen.
     * @param armorType the armor type.
     */
    protected void cancelAsynchRequestForArmor(final IToolType armorType)
    {
        final List<IRequest<? extends Tool>> openRequests = getOwnBuilding().getOpenRequestsOfTypeFiltered(worker.getCitizenData(), TypeToken.of(Tool.class), iRequest -> iRequest.getRequest().getToolClass() == armorType);
        for (final IRequest token : openRequests)
        {
            worker.getCitizenColonyHandler().getColony().getRequestManager().updateRequestState(token.getToken(), RequestState.COMPLETED);
        }
    }

    /**
     * Check if there is an open request for a certain tooltype.
     * @param key the tooltype.
     * @return true if so.
     */
    private boolean hasOpenToolRequest(final IToolType key)
    {
        return getOwnBuilding().hasWorkerOpenRequestsFiltered(worker.getCitizenData(), iRequest -> iRequest.getRequest() instanceof Tool && ((Tool) iRequest.getRequest()).getToolClass() == key);
    }

    /**
     * Check if we need a tool.
     * <p>
     * Do not use it to find a pickaxe as it need a minimum level.
     *
     * @param toolType     tool required for block.
     * @param minimalLevel the minimal level.
     * @return true if we need a tool.
     */
    private boolean checkForNeededTool(@NotNull final IToolType toolType, final int minimalLevel)
    {
        final int maxToolLevel = worker.getCitizenColonyHandler().getWorkBuilding().getMaxToolLevel();
        final InventoryCitizen inventory = worker.getInventoryCitizen();
        if (InventoryUtils.isToolInItemHandler(new InvWrapper(inventory), toolType, minimalLevel, maxToolLevel))
        {
            return false;
        }

        delay += DELAY_RECHECK;
        return walkToBuilding() || !retrieveToolInHut(toolType, minimalLevel);
    }

    /**
     * Check all chests in the workers hut for a required tool.
     *
     * @param toolType     the type of tool requested (amount is ignored)
     * @param minimalLevel the minimal level the tool should have.
     * @return true if a stack of that type was found
     */
    public boolean retrieveToolInHut(final IToolType toolType, final int minimalLevel)
    {
        @Nullable final AbstractBuildingWorker building = getOwnBuilding();

        if (building != null)
        {
            if (retrieveToolInTileEntity(building.getTileEntity(), toolType, minimalLevel, getOwnBuilding().getMaxToolLevel()))
            {
                return true;
            }

            for (final BlockPos pos : building.getAdditionalCountainers())
            {
                final TileEntity entity = world.getTileEntity(pos);
                if (entity instanceof TileEntityChest && retrieveToolInTileEntity((TileEntityChest) entity, toolType, minimalLevel, getOwnBuilding().getMaxToolLevel()))
                {
                    return true;
                }
            }
        }

        return false;
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

        if(InventoryUtils.isProviderFull(getOwnBuilding()))
        {
            getOwnBuilding().alterPickUpPriority(MAX_PRIO);
            chatSpamFilter.talkWithoutSpam(COM_MINECOLONIES_COREMOD_ENTITY_WORKER_INVENTORYFULLCHEST);
        }
        else if (dumpOneMoreSlot())
        {
            delay += DELAY_RECHECK;
            return INVENTORY_FULL;
        }
        //collect items that are nice to have if they are available
        this.itemsNiceToHave().forEach(this::isInHut);
        // we dumped the inventory, reset actions done
        this.clearActionsDone();
        return afterDump();
    }

    /**
     * State to go to after dumping.
     * @return the next state.
     */
    public AIState afterDump()
    {
        return IDLE;
    }

    /**
     * Dumps one inventory slot into the building chest.
     *
     * @return true if is has to dump more.
     */
    private boolean dumpOneMoreSlot()
    {
        //Items already kept in the inventory
        final List<ItemStorage> alreadyKept = new ArrayList<>();
        @Nullable final AbstractBuildingWorker buildingWorker = getOwnBuilding();

        return buildingWorker != null
                 && (walkToBuilding() || InventoryFunctions.matchFirstInHandlerWithAction(new InvWrapper(worker.getInventoryCitizen()),
          itemStack -> !ItemStackUtils.isEmpty(itemStack) && !buildingWorker.buildingRequiresCertainAmountOfItem(itemStack, alreadyKept),
          (handler, slot) ->
            InventoryUtils.transferItemStackIntoNextBestSlotInItemHandler(
              new InvWrapper(worker.getInventoryCitizen()), slot, buildingWorker.getCapability(ITEM_HANDLER_CAPABILITY, null))
        ));
    }

    /**
     * Can be overridden by implementations to specify items useful for the worker.
     * When the worker inventory is full, he will try to keep these items.
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
     * @param pos the pos to mine
     * @return true if we have a tool for the job
     */
    public final boolean holdEfficientTool(@NotNull final Block target, final BlockPos pos)
    {
        final int bestSlot = getMostEfficientTool(target);
        if (bestSlot >= 0)
        {
            worker.getCitizenItemHandler().setHeldItem(EnumHand.MAIN_HAND, bestSlot);
            return true;
        }
        requestTool(target, pos);
        return false;
    }

    /**
     * Request the appropriate tool for this block.
     *
     * @param target the block to mine
     * @param pos the pos to mine
     */
    private void requestTool(@NotNull final Block target, final BlockPos pos)
    {
        final IToolType toolType = WorkerUtil.getBestToolForBlock(target);
        final int required = WorkerUtil.getCorrectHavestLevelForBlock(target);
        if (getOwnBuilding().getMaxToolLevel() < required)
        {
            chatSpamFilter.talkWithoutSpam(BUILDING_LEVEL_TOO_LOW,new ItemStack(target).getDisplayName(), pos.toString());
        }
        updateToolFlag(toolType, required);
    }

    /**
     * Checks if said tool of said level is usable.
     * if not, it updates the needsTool flag for said tool.
     *
     * @param toolType the tool needed
     * @param required the level needed (for pickaxe only)
     */
    private void updateToolFlag(@NotNull final IToolType toolType, final int required)
    {
        if (ToolType.PICKAXE.equals(toolType))
        {
            checkForToolOrWeapon(toolType, required);
        }
        else
        {
            checkForToolOrWeapon(toolType);
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
        final IToolType toolType = WorkerUtil.getBestToolForBlock(target);
        final int required = WorkerUtil.getCorrectHavestLevelForBlock(target);
        int bestSlot = -1;
        int bestLevel = Integer.MAX_VALUE;
        @NotNull final InventoryCitizen inventory = worker.getInventoryCitizen();
        final int maxToolLevel = worker.getCitizenColonyHandler().getWorkBuilding().getMaxToolLevel();

        for (int i = 0; i < new InvWrapper(worker.getInventoryCitizen()).getSlots(); i++)
        {
            final ItemStack item = inventory.getStackInSlot(i);
            final int level = ItemStackUtils.getMiningLevel(item, toolType);
            if (level >= required && level < bestLevel
                  && (toolType == ToolType.NONE || ItemStackUtils.verifyToolLevel(item, level, required, maxToolLevel)))
            {
                bestSlot = i;
                bestLevel = level;
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
     * this also triggers the AI to get hungry.
     * <p>
     * For example:
     * <p>
     * After x blocks, bring everything back.
     */
    protected final void incrementActionsDoneAndDecSaturation()
    {
        worker.decreaseSaturationForAction();
        actionsDone++;
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
     * Takes the position where the worker would like to work on and return the most appropriate position for it.
     * <p>
     *
     * @param targetPosition the position to work at.
     * @return BlockPos most appropiate position to work from.
     */
    public BlockPos getWorkingPosition(final BlockPos targetPosition)
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

        //if necessary we call it recursively and add some "offset" to the sides.
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

    /**
     * Requests a list of itemstacks.
     *
     * @param stacks the stacks.
     * @return true if they're in the inventory.
     */
    public boolean checkIfRequestForItemExistOrCreate(@NotNull final ItemStack... stacks)
    {
        return checkIfRequestForItemExistOrCreate(Lists.newArrayList(stacks));
    }

    /**
     * Check if any of the stacks is in the inventory.
     *
     * @param stacks the list of stacks.
     * @return true if so.
     */
    public boolean checkIfRequestForItemExistOrCreate(@NotNull final Collection<ItemStack> stacks)
    {
        return stacks.stream().allMatch(this::checkIfRequestForItemExistOrCreate);
    }

    /**
     * Check if a stack has been requested already or is in the inventory.
     * If not in the inventory and not requested already, create request
     *
     * @param stack the requested stack.
     * @return true if in the inventory, else false.
     */
    public boolean checkIfRequestForItemExistOrCreate(@NotNull final ItemStack stack)
    {
        if (InventoryUtils.hasItemInItemHandler(new InvWrapper(worker.getInventoryCitizen()),
          s -> ItemStackUtils.compareItemStacksIgnoreStackSize(s, stack)))
        {
            return true;
        }

        if (getOwnBuilding().getOpenRequestsOfTypeFiltered(worker.getCitizenData(),
          TypeConstants.DELIVERABLE,
          (IRequest<? extends IDeliverable> r) -> r.getRequest().matches(stack)).isEmpty())
        {
            final Stack stackRequest = new Stack(stack);
            worker.getCitizenData().createRequest(stackRequest);
        }

        return false;
    }

    /**
     * Requests a list of itemstacks.
     *
     * @param stacks the stacks.
     * @return true if they're in the inventory.
     */
    public boolean checkIfRequestForItemExistOrCreateAsynch(@NotNull final ItemStack... stacks)
    {
        return checkIfRequestForItemExistOrCreateAsynch(Lists.newArrayList(stacks));
    }

    /**
     * Check if any of the stacks is in the inventory.
     *
     * @param stacks the list of stacks.
     * @return true if so.
     */
    public boolean checkIfRequestForItemExistOrCreateAsynch(@NotNull final Collection<ItemStack> stacks)
    {
        return stacks.stream().allMatch(this::checkIfRequestForItemExistOrCreateAsynch);
    }

    /**
     * Check if a stack has been requested already or is in the inventory.
     * If not in the inventory and not requested already, create request
     *
     * @param stack the requested stack.
     * @return true if in the inventory, else false.
     */
    public boolean checkIfRequestForItemExistOrCreateAsynch(@NotNull final ItemStack stack)
    {
        if (InventoryUtils.hasItemInItemHandler(new InvWrapper(worker.getInventoryCitizen()),
          s -> ItemStackUtils.compareItemStacksIgnoreStackSize(s, stack) && s.getCount() >= stack.getCount()))
        {
            return true;
        }

        if (InventoryUtils.getItemCountInProvider(getOwnBuilding(),
                itemStack -> ItemStackUtils.compareItemStacksIgnoreStackSize(itemStack, stack, true, true)) >= stack.getCount() &&
              InventoryUtils.transferXOfFirstSlotInProviderWithIntoNextFreeSlotInItemHandler(
                getOwnBuilding(), itemStack -> ItemStackUtils.compareItemStacksIgnoreStackSize(itemStack, stack, true, true),
                stack.getCount(),
                new InvWrapper(worker.getInventoryCitizen())))
        {
            return true;
        }

        if (getOwnBuilding().getOpenRequestsOfTypeFiltered(worker.getCitizenData(), TypeConstants.DELIVERABLE,
          (IRequest<? extends IDeliverable> r) -> r.getRequest().matches(stack)).isEmpty())
        {
            final Stack stackRequest = new Stack(stack);
            worker.getCitizenData().createRequestAsync(stackRequest);
        }

        return false;
    }

    /**
     * Try to transfer a item matching a predicate from a position to the cook.
     *
     * @param pos       the position to transfer it from.
     * @param predicate the predicate to evaluate.
     * @return true if succesful.
     */
    public boolean tryTransferFromPosToWorker(final BlockPos pos, @NotNull final Predicate<ItemStack> predicate)
    {
        final TileEntity entity = world.getTileEntity(pos);
        if (entity == null)
        {
            return false;
        }

        return InventoryUtils.transferXOfFirstSlotInProviderWithIntoNextFreeSlotInItemHandler(
          entity,
          predicate,
          Constants.STACKSIZE,
          new InvWrapper(worker.getInventoryCitizen()));
    }
}
