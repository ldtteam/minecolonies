package com.minecolonies.coremod.entity.ai.citizen.deliveryman;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingContainer;
import com.minecolonies.api.colony.buildings.IBuildingWorker;
import com.minecolonies.api.colony.buildings.workerbuildings.IBuildingDeliveryman;
import com.minecolonies.api.colony.buildings.workerbuildings.IWareHouse;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.Delivery;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.statemachine.AIEventTarget;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.AIBlockingEventType;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.InventoryFunctions;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.blockout.Log;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingCook;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingDeliveryman;
import com.minecolonies.coremod.colony.jobs.JobDeliveryman;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
     * The amoutn fo tries the dman has to get a random building.
     */
    private static final int TRIES_TO_GET_RANDOM_BUILDING = 3;

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
     * The last delivery of the dman.
     */
    private ILocation lastDelivery = null;

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
          new AIEventTarget(AIBlockingEventType.STATE_BLOCKING, this::checkIfExecute, IDLE),
          new AITarget(IDLE, () -> START_WORKING),
          new AITarget(START_WORKING, this::checkWareHouse, 20),
          new AITarget(PREPARE_DELIVERY, this::prepareDelivery),
          new AITarget(DELIVERY, this::deliver),
          new AITarget(GATHERING, this::gather),
          new AITarget(DUMPING, this::dump)

        );
        worker.getCitizenExperienceHandler().setSkillModifier(2 * worker.getCitizenData().getEndurance() + worker.getCitizenData().getCharisma());
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
    public IAIState gather()
    {
        if (job.getCurrentTask() != null)
        {
            return START_WORKING;
        }

        if (maximalGatherCount < 0)
        {
            maximalGatherCount = Configurations.requestSystem.minimalBuildingsToGather
                    + worker.getRandom().nextInt(
                            Math.max(1, Configurations.requestSystem.maximalBuildingsToGather - Configurations.requestSystem.minimalBuildingsToGather));
        }

        if (gatherTarget == null)
        {
            if (gatherCount == maximalGatherCount)
            {
                maximalGatherCount = Configurations.requestSystem.minimalBuildingsToGather
                        + worker.getRandom().nextInt(
                                Math.max(1, Configurations.requestSystem.maximalBuildingsToGather - Configurations.requestSystem.minimalBuildingsToGather));
                gatherCount = 0;
                return DUMPING;
            }

            gatherTarget = getRandomBuilding();
        }

        if (gatherTarget == null)
        {
            return START_WORKING;
        }

        worker.getCitizenStatusHandler().setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.gathering"));

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

                gatherCount ++;

                if (hasGathered)
                {
                    job.setReturning(true);
                    this.hasGathered = false;
                }
                else
                {
                    if (!building.isPriorityStatic())
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
     * Gets a random building from his colony.
     *
     * @return a random blockPos.
     */
    @Nullable
    private BlockPos getRandomBuilding()
    {
        if (worker.getCitizenColonyHandler().getColony() == null || getOwnBuilding() == null)
        {
            return null;
        }

        final BlockPos pos = getWeightedRandom();

        if (pos != null)
        {
            return pos;
        }

        IBuilding theBuilding = returnRandomBuilding();
        for(int i = 0; i < TRIES_TO_GET_RANDOM_BUILDING; i++)
        {
            if(theBuilding != null && (lastDelivery == null || !theBuilding.getLocation().equals(lastDelivery)))
            {
                lastDelivery = null;
                return theBuilding.getPosition();
            }
            theBuilding = returnRandomBuilding();
        }

        lastDelivery = null;
        return theBuilding == null ? null : theBuilding.getPosition();
    }

    /**
     * Calculates a random building and returns it.
     * @return a random building.
     */
    private IBuilding returnRandomBuilding()
    {
        final Collection<IBuilding> buildingList = worker.getCitizenColonyHandler().getColony().getBuildingManager().getBuildings().values();
        final Object[] buildingArray = buildingList.toArray();

        final int random = worker.getRandom().nextInt(buildingArray.length);
        final IBuilding building = (IBuilding) buildingArray[random];

        if (!building.canBeGathered())
        {
            return null;
        }
        return building;
    }

    /**
     * Gather not needed Items from building.
     *
     * @param building building to gather it from.
     * @return true when finished.
     */
    private boolean gatherFromBuilding(@NotNull final IBuilding building)
    {
        final IItemHandler handler = building.getCapability(ITEM_HANDLER_CAPABILITY, null);
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
                    && stack.getItem() instanceof ItemFood))
        {
            return false;
        }

        if (ItemStackUtils.isEmpty(handler.getStackInSlot(currentSlot)))
        {
            return false;
        }

        hasGathered = true;
        final ItemStack activeStack = handler.extractItem(currentSlot, amount, false);
        InventoryUtils.transferItemStackIntoNextBestSlotInItemHandler(activeStack, new InvWrapper(worker.getInventoryCitizen()));
        building.markDirty();
        setDelay(DUMP_AND_GATHER_DELAY);
        worker.decreaseSaturationForContinuousAction();

        worker.getCitizenItemHandler().setHeldItem(EnumHand.MAIN_HAND, SLOT_HAND);
        return false;
    }

    /**
     * Check if the worker can hold that much items.
     * It depends on his building level.
     * Level 1: 1 stack Level 2: 2 stacks, 4 stacks, 8, unlimited.
     * That's 2^buildingLevel-1.
     */
    private boolean cannotHoldMoreItems()
    {
        if (getOwnBuilding().getBuildingLevel() >= getOwnBuilding().getMaxBuildingLevel())
        {
            return false;
        }
        return InventoryUtils.getAmountOfStacksInItemHandler(new InvWrapper(worker.getInventoryCitizen())) >= Math.pow(2, getOwnBuilding().getBuildingLevel() - 1.0D) + 1;
    }

    private BlockPos getWeightedRandom()
    {
        double completeWeight = 0.0;
        for (final IBuilding building : worker.getCitizenColonyHandler().getColony().getBuildingManager().getBuildings().values())
        {
            if (!building.isBeingGathered())
            {
                completeWeight += building.getPickUpPriority();
            }
        }
        final double r = Math.random() * completeWeight;
        double countWeight = 0.0;

        final List<IBuilding> buildings = worker.getCitizenColonyHandler().getColony().getBuildingManager().getBuildings().values().stream()
                                                   .filter(building -> building.canBeGathered() && !building.isBeingGathered())
                                                   .collect(Collectors.toList());
        Collections.shuffle(buildings);
        for (final IBuilding building : buildings)
        {
            countWeight += building.getPickUpPriority();
            if (countWeight >= r)
            {
                //Don't let any other dman pick up for now.
                building.setBeingGathered(true);
                return building.getID();
            }
            else
            {
                if (!building.isPriorityStatic() && worker.getRandom().nextInt(100) <= 1)
                {
                    building.alterPickUpPriority(1);
                }
            }
        }
        return null;
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
    public IAIState dump()
    {
        worker.getCitizenStatusHandler().setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.dumping"));

        if (!worker.isWorkerAtSiteWithMove(getAndCheckWarehouse().getPosition(), MIN_DISTANCE_TO_WAREHOUSE))
        {
            return DUMPING;
        }

        getAndCheckWarehouse().getTileEntity().dumpInventoryIntoWareHouse(worker.getInventoryCitizen());
        gatherTarget = null;
        worker.getCitizenItemHandler().setHeldItem(EnumHand.MAIN_HAND, SLOT_HAND);


        if (job.isReturning())
        {
            job.setReturning(false);
        }

        return START_WORKING;
    }

    /**
     * Gets the colony's warehouse for the Deliveryman.
     */
    public IWareHouse getAndCheckWarehouse()
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
        if (job.isReturning())
        {
            return DUMPING;
        }

        final IBuildingDeliveryman deliveryHut = (getOwnBuilding() instanceof BuildingDeliveryman) ? (IBuildingDeliveryman) getOwnBuilding() : null;
        final ILocation buildingToDeliver = deliveryHut == null ? null : deliveryHut.getBuildingToDeliver();

        if (deliveryHut == null)
        {
            return START_WORKING;
        }
        else if (buildingToDeliver == null)
        {
            if (job.getCurrentTask() != null && deliveryHut != null)
            {
                final IRequest<? extends Delivery> request = job.getCurrentTask();
                deliveryHut.setBuildingToDeliver(request.getRequest().getTarget());
                return getState();
            }
            return START_WORKING;
        }

        worker.getCitizenStatusHandler().setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.delivering"));

        if (!buildingToDeliver.isReachableFromLocation(worker.getLocation()))
        {
            Log.getLogger().info(worker.getCitizenColonyHandler().getColony().getName() + ": " + worker.getName() + ": Can't inter dimension yet: ");
            return START_WORKING;
        }

        if (!worker.isWorkerAtSiteWithMove(buildingToDeliver.getInDimensionLocation(), MIN_DISTANCE_TO_WAREHOUSE))
        {
            return DELIVERY;
        }

        final TileEntity tileEntity = world.getTileEntity(buildingToDeliver.getInDimensionLocation());

        if (!(tileEntity instanceof TileEntityColonyBuilding))
        {
            job.finishRequest(true);
            return START_WORKING;
        }

        final AbstractTileEntityColonyBuilding ITileEntityColonyBuilding = (AbstractTileEntityColonyBuilding) tileEntity;
        final IBuildingContainer building = ITileEntityColonyBuilding.getBuilding();

        boolean success = true;
        boolean extracted = false;
        final InvWrapper workerInventory = new InvWrapper(worker.getInventoryCitizen());
        for (int i = 0; i < new InvWrapper(worker.getInventoryCitizen()).getSlots(); i++)
        {
            final ItemStack stack = workerInventory.extractItem(i, Integer.MAX_VALUE, false);
            if (ItemStackUtils.isEmpty(stack))
            {
                continue;
            }

            extracted = true;
            final ItemStack insertionResultStack;

            if (ITileEntityColonyBuilding.getBuilding() instanceof AbstractBuildingWorker)
            {
                insertionResultStack = InventoryUtils.forceItemStackToItemHandler(
                  ITileEntityColonyBuilding.getBuilding().getCapability(ITEM_HANDLER_CAPABILITY, null), stack, ((IBuildingWorker) building)::isItemStackInRequest);
            }
            else
            {
                insertionResultStack = InventoryUtils.forceItemStackToItemHandler(ITileEntityColonyBuilding.getBuilding().getCapability(ITEM_HANDLER_CAPABILITY, null), stack, itemStack -> false);
            }

            if (!ItemStackUtils.isEmpty(insertionResultStack))
            {
                success = false;
                if (ItemStack.areItemStacksEqual(insertionResultStack, stack))
                {
                    //same stack, we could not deliver ?
                    if (building instanceof AbstractBuildingWorker)
                    {
                        chatSpamFilter.talkWithoutSpam(COM_MINECOLONIES_COREMOD_JOB_DELIVERYMAN_NAMEDCHESTFULL,
                          building.getMainCitizen().getName());
                    }
                    else if (buildingToDeliver instanceof TileEntityColonyBuilding)
                    {
                        chatSpamFilter.talkWithoutSpam(COM_MINECOLONIES_COREMOD_JOB_DELIVERYMAN_CHESTFULL,
                          new TextComponentString(" :" + building.getSchematicName()));
                    }
                }

                //Insert the result back into the inventory so we do not loose it.
                workerInventory.insertItem(i, insertionResultStack, false);
            }
        }

        if (!extracted)
        {
            lastDelivery = deliveryHut.getBuildingToDeliver();
            worker.decreaseSaturationForContinuousAction();
            worker.getCitizenItemHandler().setHeldItem(EnumHand.MAIN_HAND, SLOT_HAND);
            deliveryHut.setBuildingToDeliver(null);
            job.finishRequest(false);

            setDelay(WAIT_DELAY);
            return DUMPING;
        }

        lastDelivery = deliveryHut.getBuildingToDeliver();
        worker.getCitizenExperienceHandler().addExperience(1.0D);
        worker.decreaseSaturationForContinuousAction();
        worker.getCitizenItemHandler().setHeldItem(EnumHand.MAIN_HAND, SLOT_HAND);
        deliveryHut.setBuildingToDeliver(null);
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
        final IBuildingWorker ownBuilding = getOwnBuilding();
        if (ownBuilding instanceof BuildingDeliveryman)
        {
            final IRequest<? extends Delivery> request = job.getCurrentTask();
            if (request != null)
            {
                if (job.isReturning())
                {
                    return DUMPING;
                }
                ((IBuildingDeliveryman) ownBuilding).setBuildingToDeliver(request.getRequest().getTarget());
                if (InventoryUtils.hasItemInItemHandler(new InvWrapper(worker.getInventoryCitizen()),
                  itemStack -> request.getRequest().getStack().isItemEqualIgnoreDurability(itemStack)))
                {
                    return DELIVERY;
                }

                return gatherItems(request);
            }
        }
        return START_WORKING;
    }

    /**
     * Gather item from chest.
     * Gathers only one stack of the item.
     *
     * @param request request to gather
     */
    private IAIState gatherItems(@NotNull final IRequest<? extends Delivery> request)
    {
        final ILocation location = request.getRequest().getStart();

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
        if (tileEntity instanceof TileEntityChest && !(tileEntity instanceof TileEntityColonyBuilding))
        {
            if (((TileEntityChest) tileEntity).numPlayersUsing == 0)
            {
                this.world.addBlockEvent(tileEntity.getPos(), tileEntity.getBlockType(), 1, 1);
                this.world.notifyNeighborsOfStateChange(tileEntity.getPos(), tileEntity.getBlockType(), true);
                this.world.notifyNeighborsOfStateChange(tileEntity.getPos().down(), tileEntity.getBlockType(), true);
                setDelay(DUMP_AND_GATHER_DELAY);
                return getState();
            }
            this.world.addBlockEvent(tileEntity.getPos(), tileEntity.getBlockType(), 1, 0);
            this.world.notifyNeighborsOfStateChange(tileEntity.getPos(), tileEntity.getBlockType(), true);
            this.world.notifyNeighborsOfStateChange(tileEntity.getPos().down(), tileEntity.getBlockType(), true);
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
                        (provider, index) -> InventoryUtils.transferXOfItemStackIntoNextFreeSlotFromProvider(provider, index, is.getCount() == 1 ? is.getMaxStackSize() : is.getCount(), new InvWrapper(worker.getInventoryCitizen()))
                      );
    }

    /**
     * Check the wareHouse for the next task.
     *
     * @return the next AiState to go to.
     */
    private IAIState checkWareHouse()
    {
        if (!worker.isWorkerAtSiteWithMove(getAndCheckWarehouse().getPosition(), MIN_DISTANCE_TO_WAREHOUSE))
        {
            return START_WORKING;
        }

        final IBuildingWorker ownBuilding = getOwnBuilding();

        //get task via colony, requestmananger
        if (job.getCurrentTask() == null)
        {
            ((IBuildingDeliveryman) ownBuilding).setBuildingToDeliver(null);
            return GATHERING;
        }
        else if (job.isReturning())
        {
            ((IBuildingDeliveryman) ownBuilding).setBuildingToDeliver(null);
            return DUMPING;
        }
        else
        {
            ((IBuildingDeliveryman) ownBuilding).setBuildingToDeliver(job.getCurrentTask().getRequest().getTarget());
        }

        return PREPARE_DELIVERY;
    }

    /**
     * Check if the deliveryman code should be executed.
     * More concretely if he has a warehouse to work at.
     *
     * @return false if should continue as planned.
     */
    private boolean checkIfExecute()
    {
        if (getAndCheckWarehouse() != null && getAndCheckWarehouse().getTileEntity() != null)
        {
            return false;
        }

        chatSpamFilter.talkWithoutSpam(COM_MINECOLONIES_COREMOD_JOB_DELIVERYMAN_NOWAREHOUSE);
        return true;
    }
}
