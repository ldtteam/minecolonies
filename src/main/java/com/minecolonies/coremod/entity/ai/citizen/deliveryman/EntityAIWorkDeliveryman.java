package com.minecolonies.coremod.entity.ai.citizen.deliveryman;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingContainer;
import com.minecolonies.api.colony.buildings.IBuildingWorker;
import com.minecolonies.api.colony.buildings.workerbuildings.IBuildingDeliveryman;
import com.minecolonies.api.colony.buildings.workerbuildings.IWareHouse;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.deliveryman.Delivery;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requestable.deliveryman.Pickup;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.InventoryFunctions;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingCook;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingDeliveryman;
import com.minecolonies.coremod.colony.interactionhandling.PosBasedInteractionResponseHandler;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteractionResponseHandler;
import com.minecolonies.coremod.colony.jobs.JobDeliveryman;
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

import static com.minecolonies.api.colony.buildings.PickUpPriorityState.AUTOMATIC;
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
     * Delay in ticks between every inventory operation.
     */
    private static final int DUMP_AND_GATHER_DELAY = 3;

    /**
     * Wait 10 seconds for the worker to gather.
     */
    private static final int WAIT_DELAY = TICKS_SECOND * 10;

    /**
     * The inventory's slot which is held in hand.
     */
    private static final int SLOT_HAND = 0;

    /**
     * Next target the deliveryman should gather stuff at.
     */
    private BlockPos gatherTarget = null;

    /**
     * Tracks how many buildings the DMan visited to gather resources.
     */
    private int gatherCount = 0;

    /**
     * Tracks what the current maximal gather count is.
     */
    private int maximalGatherCount = -1;

    /**
     * Amount of stacks left to gather from the inventory at the gathering step.
     */
    private int currentSlot = 0;

    /**
     * Amount of stacks the worker already kept in the current gathering process.
     */
    private List<ItemStorage> alreadyKept = new ArrayList<>();

    /**
     * To check if the dman gathered anything on his trip.
     */
    private boolean hasGathered = false;

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
          new AITarget(START_WORKING, this::checkIfExecute, this::decide, TICKS_SECOND),
          new AITarget(PREPARE_DELIVERY, this::prepareDelivery, 1),
          new AITarget(DELIVERY, this::deliver, 1),
          new AITarget(GATHERING, this::gather, STANDARD_DELAY),
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
     * Gather items from a random hut which the hut doesn't need.
     *
     * @return the next state to go to.
     */
    private IAIState gather()
    {
        if (job.getCurrentTask() != null)
        {
            return START_WORKING;
        }

        if (maximalGatherCount < 0)
        {
            maximalGatherCount = MineColonies.getConfig().getCommon().minimalBuildingsToGather.get()
                                   + worker.getRandom().nextInt(
              Math.max(1, MineColonies.getConfig().getCommon().maximalBuildingsToGather.get() - MineColonies.getConfig().getCommon().minimalBuildingsToGather.get()));
        }

        if (gatherTarget == null)
        {
            if (gatherCount == maximalGatherCount)
            {
                maximalGatherCount = MineColonies.getConfig().getCommon().minimalBuildingsToGather.get()
                                       + worker.getRandom().nextInt(
                  Math.max(1, MineColonies.getConfig().getCommon().maximalBuildingsToGather.get() - MineColonies.getConfig().getCommon().minimalBuildingsToGather.get()));
                gatherCount = 0;
                return DUMPING;
            }

            gatherTarget = getRandomBuilding();
        }

        if (gatherTarget == null)
        {
            return START_WORKING;
        }

        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.gathering"));

        if (!worker.isWorkerAtSiteWithMove(gatherTarget, MIN_DISTANCE_TO_WAREHOUSE))
        {
            return GATHERING;
        }

        final IColony colony = getOwnBuilding().getColony();
        if (colony != null)
        {
            final IBuilding building = colony.getBuildingManager().getBuilding(gatherTarget);
            if (building == null)
            {
                gatherTarget = null;
                return START_WORKING;
            }
            if (gatherFromBuilding(building) || cannotHoldMoreItems())
            {
                this.alreadyKept = new ArrayList<>();
                this.currentSlot = 0;
                building.setBeingGathered(false);

                gatherCount++;

                if (hasGathered)
                {
                    job.setReturning(true);
                    this.hasGathered = false;
                }
                else
                {
                    if (building.getPriorityState() == AUTOMATIC)
                    {
                        building.alterPickUpPriority(-10);
                    }
                    if (job.getCurrentTask() == null)
                    {
                        gatherTarget = null;
                        return GATHERING;
                    }
                }

                gatherCount = 0;
                return DUMPING;
            }
            currentSlot++;
            return GATHERING;
        }
        return START_WORKING;
    }

    /**
     * Gather not needed Items from building.
     *
     * @param building building to gather it from.
     * @return true when finished.
     */
    private boolean gatherFromBuilding(@NotNull final IBuilding building)
    {

        // TODO:                 building.setBeingGathered(true);
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
              || (building instanceof BuildingCook
                    && stack.getItem().isFood()))
        {
            return false;
        }

        if (ItemStackUtils.isEmpty(handler.getStackInSlot(currentSlot)))
        {
            return false;
        }

        hasGathered = true;
        final ItemStack activeStack = handler.extractItem(currentSlot, amount, false);
        InventoryUtils.transferItemStackIntoNextBestSlotInItemHandler(activeStack, worker.getInventoryCitizen());
        building.markDirty();
        setDelay(DUMP_AND_GATHER_DELAY);
        worker.decreaseSaturationForContinuousAction();

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
            return DUMPING;
        }

        getAndCheckWareHouse().getTileEntity().dumpInventoryIntoWareHouse(worker.getInventoryCitizen());
        gatherTarget = null;
        worker.getCitizenItemHandler().setHeldItem(Hand.MAIN_HAND, SLOT_HAND);


        if (job.isReturning())
        {
            job.setReturning(false);
        }

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
     *
     * @return the next state.
     */
    private IAIState deliver()
    {
        final IRequest<? extends IRequestable> request = job.getCurrentTask();

        if (request == null)
        {
            return START_WORKING;
        }

        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.delivering"));

        if (!buildingToDeliver.isReachableFromLocation(worker.getLocation()))
        {
            Log.getLogger().info(worker.getCitizenColonyHandler().getColony().getName() + ": " + worker.getName() + ": Can't inter dimension yet: ");
            return START_WORKING;
        }

        if (!worker.isWorkerAtSiteWithMove(buildingToDeliver.getInDimensionLocation(), MIN_DISTANCE_TO_WAREHOUSE))
        {
            setDelay(10);
            return DELIVERY;
        }

        final TileEntity tileEntity = world.getTileEntity(buildingToDeliver.getInDimensionLocation());

        if (!(tileEntity instanceof TileEntityColonyBuilding))
        {
            job.finishRequest(true);
            return START_WORKING;
        }

        final AbstractTileEntityColonyBuilding iTileEntityColonyBuilding = (AbstractTileEntityColonyBuilding) tileEntity;
        final IBuildingContainer building = iTileEntityColonyBuilding.getBuilding();

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

            if (iTileEntityColonyBuilding.getBuilding() instanceof AbstractBuildingWorker)
            {
                insertionResultStack = InventoryUtils.forceItemStackToItemHandler(
                  iTileEntityColonyBuilding.getBuilding().getCapability(ITEM_HANDLER_CAPABILITY, null).orElseGet(null), stack, ((IBuildingWorker) building)::isItemStackInRequest);
            }
            else
            {
                insertionResultStack =
                  InventoryUtils.forceItemStackToItemHandler(iTileEntityColonyBuilding.getBuilding().getCapability(ITEM_HANDLER_CAPABILITY, null).orElseGet(null),
                    stack,
                    itemStack -> false);
            }

            if (!ItemStackUtils.isEmpty(insertionResultStack))
            {
                success = false;
                if (ItemStack.areItemStacksEqual(insertionResultStack, stack) && worker.getCitizenData() != null)
                {
                    //same stack, we could not deliver ?
                    if (building instanceof AbstractBuildingWorker)
                    {
                        worker.getCitizenData()
                          .triggerInteraction(new PosBasedInteractionResponseHandler(new TranslationTextComponent(COM_MINECOLONIES_COREMOD_JOB_DELIVERYMAN_NAMEDCHESTFULL,
                            building.getMainCitizen().getName()),
                            ChatPriority.IMPORTANT,
                            new TranslationTextComponent(COM_MINECOLONIES_COREMOD_JOB_DELIVERYMAN_CHESTFULL),
                            building.getID()));
                    }
                    else if (buildingToDeliver instanceof TileEntityColonyBuilding)
                    {
                        worker.getCitizenData()
                          .triggerInteraction(new PosBasedInteractionResponseHandler(new TranslationTextComponent(COM_MINECOLONIES_COREMOD_JOB_DELIVERYMAN_CHESTFULL,
                            new StringTextComponent(" :" + building.getSchematicName())),
                            ChatPriority.IMPORTANT,
                            new TranslationTextComponent(COM_MINECOLONIES_COREMOD_JOB_DELIVERYMAN_CHESTFULL),
                            ((TileEntityColonyBuilding) buildingToDeliver).getPos()));
                    }
                }

                //Insert the result back into the inventory so we do not lose it.
                workerInventory.insertItem(i, insertionResultStack, false);
            }
        }

        if (!extracted)
        {
            worker.decreaseSaturationForContinuousAction();
            worker.getCitizenItemHandler().setHeldItem(Hand.MAIN_HAND, SLOT_HAND);
            job.finishRequest(false);

            setDelay(WAIT_DELAY);
            return DUMPING;
        }

        worker.getCitizenExperienceHandler().addExperience(1.0D);
        worker.decreaseSaturationForContinuousAction();
        worker.getCitizenItemHandler().setHeldItem(Hand.MAIN_HAND, SLOT_HAND);
        job.finishRequest(true);

        setDelay(WAIT_DELAY);
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
        if (currentTask == null)
        {
            return START_WORKING;
        }

        final IRequestable request = currentTask.getRequest();
        if (request instanceof Pickup)
        {
            return DELIVERY;
        }
        else if (request instanceof Delivery)
        {
            final Delivery delivery = (Delivery) request;
            if (InventoryUtils.hasItemInItemHandler(worker.getInventoryCitizen(),
              itemStack -> delivery.getStack().isItemEqualIgnoreDurability(itemStack)))
            {
                return DELIVERY;
            }

            return gatherItems(delivery);
        }
        else
        {
            // Deliveryman doesn't support other IRequestable types at the moment.
            job.finishRequest(false);
            return START_WORKING;
        }
    }

    /**
     * Gather item from chest.
     * Gathers only one stack of the item.
     *
     * @param delivery The delivery that should be made.
     * @return the next state to go into
     */
    private IAIState gatherItems(@NotNull final Delivery delivery)
    {
        final ILocation location = delivery.getStart();

        if (!location.isReachableFromLocation(worker.getLocation()))
        {
            ((IBuildingDeliveryman) getOwnBuilding()).setBuildingToDeliver(null);
            job.finishRequest(false);
            return START_WORKING;
        }

        if (walkToBlock(location.getInDimensionLocation()))
        {
            return getState();
        }

        final TileEntity tileEntity = world.getTileEntity(location.getInDimensionLocation());
        if (tileEntity instanceof ChestTileEntity && !(tileEntity instanceof TileEntityColonyBuilding))
        {
            if (((ChestTileEntity) tileEntity).numPlayersUsing == 0)
            {
                this.world.addBlockEvent(tileEntity.getPos(), tileEntity.getBlockState().getBlock(), 1, 1);
                this.world.notifyNeighborsOfStateChange(tileEntity.getPos(), tileEntity.getBlockState().getBlock());
                this.world.notifyNeighborsOfStateChange(tileEntity.getPos().down(), tileEntity.getBlockState().getBlock());
                setDelay(DUMP_AND_GATHER_DELAY);
                return getState();
            }
            this.world.addBlockEvent(tileEntity.getPos(), tileEntity.getBlockState().getBlock(), 1, 0);
            this.world.notifyNeighborsOfStateChange(tileEntity.getPos(), tileEntity.getBlockState().getBlock());
            this.world.notifyNeighborsOfStateChange(tileEntity.getPos().down(), tileEntity.getBlockState().getBlock());
        }

        if (gatherIfInTileEntity(tileEntity, request.getRequest().getStack()))
        {
            setDelay(DUMP_AND_GATHER_DELAY);
            return DELIVERY;
        }

        ((IBuildingDeliveryman) getOwnBuilding()).setBuildingToDeliver(null);
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
        if (job.isReturning())
        {
            return DUMPING;
        }

        if (job.getCurrentTask() == null)
        {
            // If there are no deliveries/pickups pending, just loiter around the warehouse.
            worker.isWorkerAtSiteWithMove(getAndCheckWareHouse().getPosition(), MIN_DISTANCE_TO_WAREHOUSE);
            return START_WORKING;
        }
        else
        {
            return PREPARE_DELIVERY;
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
