package com.minecolonies.coremod.entity.ai.citizen.deliveryman;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingContainer;
import com.minecolonies.api.colony.buildings.IBuildingWorker;
import com.minecolonies.api.colony.buildings.workerbuildings.IWareHouse;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requestable.deliveryman.Delivery;
import com.minecolonies.api.colony.requestsystem.requestable.deliveryman.IDeliverymanRequestable;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.InventoryFunctions;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingCook;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingDeliveryman;
import com.minecolonies.coremod.colony.interactionhandling.PosBasedInteractionResponseHandler;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteractionResponseHandler;
import com.minecolonies.coremod.colony.jobs.JobDeliveryman;
import com.minecolonies.coremod.colony.requestsystem.requests.StandardRequests.DeliveryRequest;
import com.minecolonies.coremod.colony.requestsystem.requests.StandardRequests.PickupRequest;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

/**
 * Delivers item at needs.
 */
public class EntityAIWorkDeliveryman extends AbstractEntityAIInteract<JobDeliveryman>
{
    /**
     * Min distance the worker should have to the warehouse to make any decisions.
     */
    private static final int MIN_DISTANCE_TO_WAREHOUSE = 5;

    /**
     * Wait 5 seconds for the worker to decide what to do.
     */
    private static final int DECISION_DELAY = TICKS_SECOND * 5;

    /**
     * The inventory's slot which is held in hand.
     */
    private static final int SLOT_HAND = 0;

    /**
     * Completing a request with a priority of at least PRIORITY_FORCING_DUMP will force a dump.
     */
    private static final int PRIORITY_FORCING_DUMP = 10;

    /**
     * Amount of stacks left to gather from the inventory at the gathering step.
     */
    private int currentSlot = 0;

    /**
     * Amount of stacks the worker already kept in the current gathering process.
     */
    private List<ItemStorage> alreadyKept = new ArrayList<>();

    /**
     * Initialize the deliveryman and add all his tasks.
     *
     * @param deliveryman the job he has.
     */
    public EntityAIWorkDeliveryman(@NotNull final JobDeliveryman deliveryman)
    {
        super(deliveryman);
        super.registerTargets(
          /*
           * Check if tasks should be executed.
           */
          new AITarget(IDLE, () -> START_WORKING, 1),
          new AITarget(START_WORKING, this::checkIfExecute, this::decide, DECISION_DELAY),
          new AITarget(PREPARE_DELIVERY, this::prepareDelivery, STANDARD_DELAY),
          new AITarget(DELIVERY, this::deliver, STANDARD_DELAY),
          new AITarget(PICKUP, this::pickup, STANDARD_DELAY),
          new AITarget(DUMPING, this::dump, TICKS_SECOND)

        );
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class getExpectedBuildingClass()
    {
        return BuildingDeliveryman.class;
    }

    /**
     * Pickup items from a hut that has requested a pickup.
     *
     * @return the next state to go to.
     */
    private IAIState pickup()
    {
        final IRequest<? extends IDeliverymanRequestable> currentTask = job.getCurrentTask();

        if (!(currentTask instanceof PickupRequest))
        {
            // The current task has changed since the Decision-state. Restart.
            return START_WORKING;
        }

        if (cannotHoldMoreItems())
        {
            this.alreadyKept = new ArrayList<>();
            this.currentSlot = 0;
            return DUMPING;
        }

        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.gathering"));

        final BlockPos pickupTarget = currentTask.getRequester().getLocation().getInDimensionLocation();
        if (!worker.isWorkerAtSiteWithMove(pickupTarget, MIN_DISTANCE_TO_WAREHOUSE))
        {
            setDelay(WALK_DELAY);
            return PICKUP;
        }

        final IBuildingWorker ownBuilding = getOwnBuilding();
        if (ownBuilding == null)
        {
            return START_WORKING;
        }

        final IBuilding pickupBuilding = ownBuilding.getColony().getBuildingManager().getBuilding(pickupTarget);
        if (pickupBuilding == null)
        {
            return START_WORKING;
        }

        if (pickupFromBuilding(pickupBuilding))
        {
            this.alreadyKept = new ArrayList<>();
            this.currentSlot = 0;
            job.finishRequest(true);

            if (currentTask.getRequest().getPriority() >= PRIORITY_FORCING_DUMP)
            {
                return DUMPING;
            }
            else
            {
                return START_WORKING;
            }
        }

        currentSlot++;
        return PICKUP;
    }

    /**
     * Gather not needed Items from building.
     *
     * @param building building to gather it from.
     * @return true when finished.
     */
    private boolean pickupFromBuilding(@NotNull final IBuilding building)
    {
        if (cannotHoldMoreItems())
        {
            return false;
        }

        final IItemHandler handler = building.getCapability(ITEM_HANDLER_CAPABILITY, null).orElseGet(null);
        if (handler == null)
        {
            return false;
        }

        if (currentSlot >= handler.getSlots())
        {
            return true;
        }

        final ItemStack stack = handler.getStackInSlot(currentSlot);

        if (stack.isEmpty())
        {
            return false;
        }

        final int amount = workerRequiresItem(building, stack, alreadyKept);
        if (amount <= 0
              || (building instanceof BuildingCook && stack.getItem().isFood()))
        {
            return false;
        }

        if (ItemStackUtils.isEmpty(handler.getStackInSlot(currentSlot)))
        {
            return false;
        }

        final ItemStack activeStack = handler.extractItem(currentSlot, amount, false);
        InventoryUtils.transferItemStackIntoNextBestSlotInItemHandler(activeStack, worker.getInventoryCitizen());
        building.markDirty();
        worker.decreaseSaturationForContinuousAction();

        // The worker gets a little bit of exp for every itemstack he grabs.
        worker.getCitizenExperienceHandler().addExperience(0.01D);
        worker.getCitizenItemHandler().setHeldItem(Hand.MAIN_HAND, SLOT_HAND);
        return false;
    }

    /**
     * Check if the worker can hold that much items.
     * It depends on his building level.
     * Level 1: 1 stack Level 2: 2 stacks, 4 stacks, 8, unlimited.
     * That's 2^buildingLevel-1.
     *
     * @return whether this deliveryman can hold more items
     */
    private boolean cannotHoldMoreItems()
    {
        if (getOwnBuilding().getBuildingLevel() >= getOwnBuilding().getMaxBuildingLevel())
        {
            return false;
        }
        return InventoryUtils.getAmountOfStacksInItemHandler(worker.getInventoryCitizen()) >= Math.pow(2, getOwnBuilding().getBuildingLevel() - 1.0D) + 1;
    }

    /**
     * Check if worker of a certain building requires the item now.
     * Or the builder for the current task.
     *
     * @param building         the building to check for.
     * @param stack            the stack to stack with.
     * @param localAlreadyKept already kept resources.
     * @return the amount which can get dumped.
     */
    public static int workerRequiresItem(final IBuilding building, final ItemStack stack, final List<ItemStorage> localAlreadyKept)
    {
        return building.buildingRequiresCertainAmountOfItem(stack, localAlreadyKept, false);
    }

    /**
     * Dump the inventory into the warehouse.
     *
     * @return the next state to go to.
     */
    private IAIState dump()
    {
        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.dumping"));

        if (!worker.isWorkerAtSiteWithMove(getAndCheckWareHouse().getPosition(), MIN_DISTANCE_TO_WAREHOUSE))
        {
            setDelay(WALK_DELAY);
            return DUMPING;
        }

        getAndCheckWareHouse().getTileEntity().dumpInventoryIntoWareHouse(worker.getInventoryCitizen());
        worker.getCitizenItemHandler().setHeldItem(Hand.MAIN_HAND, SLOT_HAND);

        return START_WORKING;
    }

    /**
     * Gets the colony's warehouse for the Deliveryman.
     *
     * @return the warehouse. null if no warehouse registered.
     */
    @Nullable
    private IWareHouse getAndCheckWareHouse()
    {
        for (final IWareHouse wareHouse : job.getColony().getBuildingManager().getWareHouses())
        {
            if (wareHouse.registerWithWareHouse(this.getOwnBuilding()))
            {
                return wareHouse;
            }
        }
        return null;
    }

    /**
     * Deliver the items to the hut.
     * TODO: Current precondition: The dman's inventory may only consist of the requested itemstack.
     *
     * @return the next state.
     */
    private IAIState deliver()
    {
        final IRequest<? extends IDeliverymanRequestable> currentTask = job.getCurrentTask();

        if (!(currentTask instanceof DeliveryRequest))
        {
            // The current task has changed since the Decision-state.
            // Since prepareDelivery() was called earlier, go dumping first and then restart.
            return DUMPING;
        }

        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.delivering"));

        final ILocation targetBuildingLocation = ((Delivery) currentTask.getRequest()).getTarget();
        if (!targetBuildingLocation.isReachableFromLocation(worker.getLocation()))
        {
            Log.getLogger().info(worker.getCitizenColonyHandler().getColony().getName() + ": " + worker.getName() + ": Can't inter dimension yet: ");
            return START_WORKING;
        }

        if (!worker.isWorkerAtSiteWithMove(targetBuildingLocation.getInDimensionLocation(), MIN_DISTANCE_TO_WAREHOUSE))
        {
            setDelay(WALK_DELAY);
            return DELIVERY;
        }

        final TileEntity tileEntity = world.getTileEntity(targetBuildingLocation.getInDimensionLocation());

        if (!(tileEntity instanceof TileEntityColonyBuilding))
        {
            // TODO: Non-Colony deliveries are unsupported yet. Fix that at some point in time.
            job.finishRequest(true);
            return START_WORKING;
        }

        final IBuildingContainer targetBuilding = ((AbstractTileEntityColonyBuilding) tileEntity).getBuilding();

        boolean success = true;
        boolean extracted = false;
        final IItemHandler workerInventory = worker.getInventoryCitizen();
        for (int i = 0; i < workerInventory.getSlots(); i++)
        {
            final ItemStack stack = workerInventory.extractItem(i, Integer.MAX_VALUE, false);
            if (ItemStackUtils.isEmpty(stack))
            {
                continue;
            }

            extracted = true;
            final ItemStack insertionResultStack;

            // TODO: Please only push items into the target that were actually requested.
            if (targetBuilding instanceof AbstractBuildingWorker)
            {
                insertionResultStack = InventoryUtils.forceItemStackToItemHandler(
                  targetBuilding.getCapability(ITEM_HANDLER_CAPABILITY, null).orElseGet(null), stack, ((IBuildingWorker) targetBuilding)::isItemStackInRequest);
            }
            else
            {
                // Buildings that are not inherently part of the request system, but just receive a delivery, cannot have their items replaced.
                // Therefore, the keep-predicate always returns true.
                insertionResultStack =
                  InventoryUtils.forceItemStackToItemHandler(targetBuilding.getCapability(ITEM_HANDLER_CAPABILITY, null).orElseGet(null),
                    stack,
                    itemStack -> true);
            }

            if (!ItemStackUtils.isEmpty(insertionResultStack))
            {
                // A stack was replaced (meaning the inventory didn't have enough space).

                if (ItemStack.areItemStacksEqual(insertionResultStack, stack) && worker.getCitizenData() != null)
                {
                    // The replaced stack is the same as the one we tried to put into the inventory.
                    // Meaning, replacing failed.
                    success = false;

                    if (targetBuilding instanceof AbstractBuildingWorker)
                    {
                        worker.getCitizenData()
                          .triggerInteraction(new PosBasedInteractionResponseHandler(new TranslationTextComponent(COM_MINECOLONIES_COREMOD_JOB_DELIVERYMAN_NAMEDCHESTFULL,
                            targetBuilding.getMainCitizen().getName()),
                            ChatPriority.IMPORTANT,
                            new TranslationTextComponent(COM_MINECOLONIES_COREMOD_JOB_DELIVERYMAN_CHESTFULL),
                            targetBuilding.getID()));
                    }
                    else
                    {
                        worker.getCitizenData()
                          .triggerInteraction(new PosBasedInteractionResponseHandler(new TranslationTextComponent(COM_MINECOLONIES_COREMOD_JOB_DELIVERYMAN_CHESTFULL,
                            new StringTextComponent(" :" + targetBuilding.getSchematicName())),
                            ChatPriority.IMPORTANT,
                            new TranslationTextComponent(COM_MINECOLONIES_COREMOD_JOB_DELIVERYMAN_CHESTFULL),
                            targetBuildingLocation.getInDimensionLocation()));
                    }
                }

                //Insert the result back into the inventory so we do not lose it.
                workerInventory.insertItem(i, insertionResultStack, false);
            }
        }

        if (!extracted)
        {
            // This can only happen if the dman's inventory was completely empty.
            // Let the retry-system handle this case.
            worker.decreaseSaturationForContinuousAction();
            worker.getCitizenItemHandler().setHeldItem(Hand.MAIN_HAND, SLOT_HAND);
            job.finishRequest(false);

            // No need to go dumping in this case.
            return START_WORKING;
        }

        worker.getCitizenExperienceHandler().addExperience(1.0D);
        worker.decreaseSaturationForContinuousAction();
        worker.getCitizenItemHandler().setHeldItem(Hand.MAIN_HAND, SLOT_HAND);
        job.finishRequest(true);

        return success ? START_WORKING : DUMPING;
    }

    /**
     * Prepare deliveryman for delivery.
     * Check if the building still needs the item and if the required items are still in the warehouse.
     *
     * @return the next state to go to.
     */
    private IAIState prepareDelivery()
    {
        final IRequest<? extends IRequestable> currentTask = job.getCurrentTask();
        final IRequestable request = currentTask.getRequest();
        if (!(request instanceof Delivery))
        {
            // The current task has changed since the Decision-state.
            // Restart.
            return START_WORKING;
        }

        final Delivery delivery = (Delivery) request;
        if (InventoryUtils.hasItemInItemHandler(worker.getInventoryCitizen(),
          itemStack -> delivery.getStack().isItemEqualIgnoreDurability(itemStack)))
        {
            return DELIVERY;
        }

        final ILocation location = delivery.getStart();

        if (!location.isReachableFromLocation(worker.getLocation()))
        {
            job.finishRequest(false);
            return START_WORKING;
        }

        if (walkToBlock(location.getInDimensionLocation()))
        {
            return PREPARE_DELIVERY;
        }

        final TileEntity tileEntity = world.getTileEntity(location.getInDimensionLocation());
        if (tileEntity instanceof ChestTileEntity && !(tileEntity instanceof TileEntityColonyBuilding))
        {
            if (((ChestTileEntity) tileEntity).numPlayersUsing == 0)
            {
                this.world.addBlockEvent(tileEntity.getPos(), tileEntity.getBlockState().getBlock(), 1, 1);
                this.world.notifyNeighborsOfStateChange(tileEntity.getPos(), tileEntity.getBlockState().getBlock());
                this.world.notifyNeighborsOfStateChange(tileEntity.getPos().down(), tileEntity.getBlockState().getBlock());
                return PREPARE_DELIVERY;
            }
            this.world.addBlockEvent(tileEntity.getPos(), tileEntity.getBlockState().getBlock(), 1, 0);
            this.world.notifyNeighborsOfStateChange(tileEntity.getPos(), tileEntity.getBlockState().getBlock());
            this.world.notifyNeighborsOfStateChange(tileEntity.getPos().down(), tileEntity.getBlockState().getBlock());
        }

        if (gatherIfInTileEntity(tileEntity, delivery.getStack()))
        {
            return DELIVERY;
        }

        job.finishRequest(false);
        return START_WORKING;
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
    public boolean gatherIfInTileEntity(final TileEntity entity, final ItemStack is)
    {
        return is != null
                 && InventoryFunctions
                      .matchFirstInProviderWithAction(
                        entity,
                        stack -> !ItemStackUtils.isEmpty(stack) && ItemStackUtils.compareItemStacksIgnoreStackSize(is, stack, true, true),
                        (provider, index) -> InventoryUtils.transferXOfItemStackIntoNextFreeSlotFromProvider(provider,
                          index,
                          is.getCount(),
                          worker.getInventoryCitizen())
                      );
    }

    /**
     * Check the wareHouse for the next task.
     *
     * @return the next AiState to go to.
     */
    private IAIState decide()
    {
        final IRequest<? extends IDeliverymanRequestable> currentTask = job.getCurrentTask();
        if (currentTask == null)
        {
            // If there are no deliveries/pickups pending, just loiter around the warehouse.
            if (!worker.isWorkerAtSiteWithMove(getAndCheckWareHouse().getPosition(), MIN_DISTANCE_TO_WAREHOUSE))
            {
                setDelay(WALK_DELAY);
                return START_WORKING;
            }
            else
            {
                if (!worker.getInventoryCitizen().isEmpty())
                {
                    return DUMPING;
                }
                else
                {
                    return START_WORKING;
                }
            }
        }
        if (currentTask instanceof DeliveryRequest)
        {
            // Before a delivery can be made, the inventory first needs to be dumped.
            if (!worker.getInventoryCitizen().isEmpty())
            {
                return DUMPING;
            }
            else
            {
                return PREPARE_DELIVERY;
            }
        }
        else
        {
            return PICKUP;
        }
    }

    /**
     * Check if the deliveryman code should be executed.
     * More concretely if he has a warehouse to work at.
     *
     * @return false if should continue as planned.
     */
    private boolean checkIfExecute()
    {
        final IWareHouse wareHouse = getAndCheckWareHouse();
        if (wareHouse != null && wareHouse.getTileEntity() != null)
        {
            job.setActive(true);
            return true;
        }

        job.setActive(false);
        if (worker.getCitizenData() != null)
        {
            worker.getCitizenData()
              .triggerInteraction(new StandardInteractionResponseHandler(new TranslationTextComponent(COM_MINECOLONIES_COREMOD_JOB_DELIVERYMAN_NOWAREHOUSE),
                ChatPriority.BLOCKING));
        }
        return false;
    }
}
