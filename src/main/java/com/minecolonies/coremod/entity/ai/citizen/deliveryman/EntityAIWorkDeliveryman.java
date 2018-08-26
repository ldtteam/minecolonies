package com.minecolonies.coremod.entity.ai.citizen.deliveryman;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.Delivery;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.blockout.Log;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.*;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingCook;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingDeliveryman;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingWareHouse;
import com.minecolonies.coremod.colony.jobs.JobDeliveryman;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import net.minecraft.entity.SharedMonsterAttributes;
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

import java.util.*;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.CitizenConstants.BASE_MOVEMENT_SPEED;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.coremod.entity.ai.util.AIState.*;
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
     * Walking speed double at this level.
     */
    private static final double WALKING_SPEED_MULTIPLIER = 25;

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
          new AITarget(this::checkIfExecute, IDLE),
          new AITarget(IDLE, () -> START_WORKING),
          new AITarget(START_WORKING, this::checkWareHouse),
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
    public AIState gather()
    {
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

        final Colony colony = getOwnBuilding().getColony();
        if (colony != null)
        {
            final AbstractBuilding building = colony.getBuildingManager().getBuilding(gatherTarget);
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
                    this.hasGathered = false;
                    building.alterPickUpPriority(1);
                }
                else
                {
                    building.alterPickUpPriority(-1);

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

        AbstractBuilding theBuilding = returnRandomBuilding();
        for(int i = 0; i < TRIES_TO_GET_RANDOM_BUILDING; i++)
        {
            if(theBuilding != null && (lastDelivery == null || !theBuilding.getDeliveryLocation().equals(lastDelivery)))
            {
                lastDelivery = null;
                return theBuilding.getLocation();
            }
            theBuilding = returnRandomBuilding();
        }

        lastDelivery = null;
        return theBuilding == null ? null : theBuilding.getLocation();
    }

    /**
     * Calculates a random building and returns it.
     * @return a random building.
     */
    private AbstractBuilding returnRandomBuilding()
    {
        final Collection<AbstractBuilding> buildingList = worker.getCitizenColonyHandler().getColony().getBuildingManager().getBuildings().values();
        final Object[] buildingArray = buildingList.toArray();

        final int random = worker.getRandom().nextInt(buildingArray.length);
        final AbstractBuilding building = (AbstractBuilding) buildingArray[random];

        if (building instanceof BuildingWareHouse || building instanceof BuildingTownHall)
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
    private boolean gatherFromBuilding(@NotNull final AbstractBuilding building)
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
        if (workerRequiresItem(building, stack, alreadyKept)
              || (building instanceof BuildingCook
                    && !ItemStackUtils.isEmpty(stack)
                    && stack.getItem() instanceof ItemFood))
        {
            return false;
        }

        if (ItemStackUtils.isEmpty(handler.getStackInSlot(currentSlot)))
        {
            return false;
        }

        hasGathered = true;
        InventoryUtils.transferItemStackIntoNextFreeSlotInItemHandler(handler, currentSlot, new InvWrapper(worker.getInventoryCitizen()));
        building.markDirty();
        setDelay(DUMP_AND_GATHER_DELAY);
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
        for (final AbstractBuilding building : worker.getCitizenColonyHandler().getColony().getBuildingManager().getBuildings().values())
        {
            if (!building.isBeingGathered())
            {
                completeWeight += building.getPickUpPriority();
            }
        }
        final double r = Math.random() * completeWeight;
        double countWeight = 0.0;

        final List<AbstractBuilding> buildings = worker.getCitizenColonyHandler().getColony().getBuildingManager().getBuildings().values().stream()
                                                   .filter(building -> !(building instanceof BuildingWareHouse || building instanceof BuildingTownHall || building.isBeingGathered()))
                                                   .collect(Collectors.toList());
        Collections.shuffle(buildings);
        for (final AbstractBuilding building : buildings)
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
                building.alterPickUpPriority(1);
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
     * @return true if required.
     */
    public static boolean workerRequiresItem(final AbstractBuilding building, final ItemStack stack, final List<ItemStorage> localAlreadyKept)
    {
        return building.buildingRequiresCertainAmountOfItem(stack, localAlreadyKept);
    }

    /**
     * Dump the inventory into the warehouse.
     *
     * @return the next state to go to.
     */
    public AIState dump()
    {
        worker.getCitizenStatusHandler().setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.dumping"));

        if (!worker.isWorkerAtSiteWithMove(getWareHouse().getLocation(), MIN_DISTANCE_TO_WAREHOUSE))
        {
            return DUMPING;
        }

        getWareHouse().getTileEntity().dumpInventoryIntoWareHouse(worker.getInventoryCitizen());
        gatherTarget = null;
        worker.getCitizenItemHandler().setHeldItem(EnumHand.MAIN_HAND, SLOT_HAND);

        final Set<IToken> finallyAssignedTokens = worker.getCitizenColonyHandler().getColony().getRequestManager().getPlayerResolver()
                .getAllAssignedRequests().stream().collect(Collectors.toSet());

        finallyAssignedTokens.forEach(iToken -> worker.getCitizenColonyHandler().getColony().getRequestManager().reassignRequest(iToken, ImmutableList.of()));

        if (job.isReturning())
        {
            job.setReturning(false);
        }

        return START_WORKING;
    }

    /**
     * Gets the colony's warehouse for the Deliveryman.
     */
    public BuildingWareHouse getWareHouse()
    {
        final Map<BlockPos, AbstractBuilding> buildings = job.getColony().getBuildingManager().getBuildings();
        for (final AbstractBuilding building : buildings.values())
        {
            if (building == null)
            {
                continue;
            }

            final Colony buildingColony = building.getColony();
            final Colony ownColony = worker.getCitizenColonyHandler().getColony();
            if (building instanceof BuildingWareHouse && ownColony != null && buildingColony != null && buildingColony.getID() == ownColony.getID()
                  && ((BuildingWareHouse) building).registerWithWareHouse((BuildingDeliveryman) this.getOwnBuilding()))
            {
                return (BuildingWareHouse) building;
            }
        }
        return null;
    }

    /**
     * Deliver the items to the hut.
     *
     * @return the next state.
     */
    private AIState deliver()
    {
        if (job.isReturning())
        {
            return DUMPING;
        }

        final BuildingDeliveryman deliveryHut = (getOwnBuilding() instanceof BuildingDeliveryman) ? (BuildingDeliveryman) getOwnBuilding() : null;
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

        final InvWrapper workerInventory = new InvWrapper(worker.getInventoryCitizen());
        for (int i = 0; i < new InvWrapper(worker.getInventoryCitizen()).getSlots(); i++)
        {
            final ItemStack stack = workerInventory.extractItem(i, Integer.MAX_VALUE, false);
            if (ItemStackUtils.isEmpty(stack))
            {
                continue;
            }


            final TileEntity tileEntity = world.getTileEntity(buildingToDeliver.getInDimensionLocation());
            final ItemStack insertionResultStack;

            if (tileEntity instanceof TileEntityColonyBuilding && ((TileEntityColonyBuilding) tileEntity).getBuilding() instanceof AbstractBuildingWorker)
            {
                final AbstractBuildingContainer building = ((TileEntityColonyBuilding) tileEntity).getBuilding();
                building.alterPickUpPriority(1);
                insertionResultStack = InventoryUtils.forceItemStackToItemHandler(
                  new InvWrapper((TileEntityColonyBuilding) tileEntity), stack, ((AbstractBuildingWorker) building)::isItemStackInRequest);
            }
            else
            {
                insertionResultStack = InventoryUtils.forceItemStackToItemHandler(new InvWrapper((TileEntityColonyBuilding) tileEntity), stack, itemStack -> false);
            }

            if (!ItemStackUtils.isEmpty(insertionResultStack))
            {
                if (ItemStack.areItemStacksEqual(insertionResultStack, stack))
                {
                    //same stack, we could not deliver ?
                    if (buildingToDeliver instanceof TileEntityColonyBuilding && ((TileEntityColonyBuilding) tileEntity).getBuilding() instanceof AbstractBuildingWorker)
                    {
                        chatSpamFilter.talkWithoutSpam(COM_MINECOLONIES_COREMOD_JOB_DELIVERYMAN_NAMEDCHESTFULL,
                          ((AbstractBuildingWorker) ((TileEntityColonyBuilding) tileEntity).getBuilding()).getMainCitizen().getName());
                    }
                    else if (buildingToDeliver instanceof TileEntityColonyBuilding)
                    {
                        chatSpamFilter.talkWithoutSpam(COM_MINECOLONIES_COREMOD_JOB_DELIVERYMAN_CHESTFULL,
                          new TextComponentString(" :" + ((TileEntityColonyBuilding) tileEntity).getBuilding().getSchematicName()));
                    }
                    else
                    {
                        chatSpamFilter.talkWithoutSpam(COM_MINECOLONIES_COREMOD_JOB_DELIVERYMAN_CHESTFULL,
                          new TextComponentString(buildingToDeliver.getInDimensionLocation().toString()));
                    }
                }

                //Insert the result back into the inventory so we do not loose it.
                workerInventory.insertItem(i, insertionResultStack, false);
            }
        }

        lastDelivery = deliveryHut.getBuildingToDeliver();
        worker.getCitizenExperienceHandler().addExperience(1.0D);
        worker.getCitizenItemHandler().setHeldItem(EnumHand.MAIN_HAND, SLOT_HAND);
        deliveryHut.setBuildingToDeliver(null);
        job.finishRequest(true);

        setDelay(WAIT_DELAY);
        return START_WORKING;
    }

    /**
     * Prepare deliveryman for delivery.
     * Check if the building still needs the item and if the required items are still in the warehouse.
     *
     * @return the next state to go to.
     */
    private AIState prepareDelivery()
    {
        final AbstractBuildingWorker ownBuilding = getOwnBuilding();
        if (ownBuilding instanceof BuildingDeliveryman)
        {
            final IRequest<? extends Delivery> request = job.getCurrentTask();
            if (request != null)
            {
                if (job.isReturning())
                {
                    return DUMPING;
                }
                ((BuildingDeliveryman) ownBuilding).setBuildingToDeliver(request.getRequest().getTarget());
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
    private AIState gatherItems(@NotNull final IRequest<? extends Delivery> request)
    {
        final ILocation location = request.getRequest().getStart();

        if (!location.isReachableFromLocation(worker.getLocation()))
        {
            ((BuildingDeliveryman) getOwnBuilding()).setBuildingToDeliver(null);
            job.finishRequest(false);
            return START_WORKING;
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

        if (isInTileEntity(tileEntity, request.getRequest().getStack()))
        {
            setDelay(DUMP_AND_GATHER_DELAY);
            return DELIVERY;
        }

        ((BuildingDeliveryman) getOwnBuilding()).setBuildingToDeliver(null);
        job.finishRequest(true);
        return START_WORKING;
    }

    /**
     * Check the wareHouse for the next task.
     *
     * @return the next AiState to go to.
     */
    private AIState checkWareHouse()
    {
        if (!worker.isWorkerAtSiteWithMove(getWareHouse().getLocation(), MIN_DISTANCE_TO_WAREHOUSE))
        {
            return START_WORKING;
        }
        worker.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(BASE_MOVEMENT_SPEED + BASE_MOVEMENT_SPEED * worker.getCitizenExperienceHandler().getLevel() / WALKING_SPEED_MULTIPLIER);

        final AbstractBuildingWorker ownBuilding = getOwnBuilding();

        //get task via colony, requestmananger

        if (job.getCurrentTask() == null)
        {
            ((BuildingDeliveryman) ownBuilding).setBuildingToDeliver(null);
            return GATHERING;
        }
        else if (job.isReturning())
        {
            ((BuildingDeliveryman) ownBuilding).setBuildingToDeliver(null);
            return DUMPING;
        }
        else
        {
            ((BuildingDeliveryman) ownBuilding).setBuildingToDeliver(job.getCurrentTask().getRequest().getTarget());
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
        if (getWareHouse() != null && getWareHouse().getTileEntity() != null)
        {
            return false;
        }

        final Map<BlockPos, AbstractBuilding> buildings = job.getColony().getBuildingManager().getBuildings();
        for (final AbstractBuilding building : buildings.values())
        {
            if (building == null)
            {
                continue;
            }

            final Colony buildingColony = building.getColony();
            final Colony ownColony = worker.getCitizenColonyHandler().getColony();
            if (building instanceof BuildingWareHouse && ownColony != null && buildingColony != null && buildingColony.getID() == ownColony.getID()
                  && ((BuildingWareHouse) building).registerWithWareHouse((BuildingDeliveryman) this.getOwnBuilding()))
            {
                return false;
            }
        }

        chatSpamFilter.talkWithoutSpam(COM_MINECOLONIES_COREMOD_JOB_DELIVERYMAN_NOWAREHOUSE);
        return true;
    }
}
