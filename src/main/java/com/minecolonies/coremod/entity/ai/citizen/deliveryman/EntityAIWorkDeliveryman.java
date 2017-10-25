package com.minecolonies.coremod.entity.ai.citizen.deliveryman;

import com.minecolonies.api.colony.requestsystem.RequestState;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.IToolType;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.blockout.Log;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.*;
import com.minecolonies.coremod.colony.jobs.JobDeliveryman;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.coremod.entity.ai.item.handling.ItemStorage;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.coremod.entity.ai.util.AIState.*;

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
     * Min distance to chest to take something out of it.
     */
    private static final int MIN_DISTANCE_TO_CHEST = 2;

    /**
     * The base movement speed of the deliveryman.
     */
    private static final double BASE_MOVEMENT_SPEED = 0.2D;

    /**
     * Delay in ticks between every inventory operation.
     */
    private static final int DUMP_AND_GATHER_DELAY = 10;

    /**
     * Warehouse the deliveryman is assigned to.
     */
    private BuildingWareHouse wareHouse = null;

    /**
     * Next target the deliveryman should gather stuff at.
     */
    private BlockPos gatherTarget = null;

    /**
     * Amount of stacks left to gather from the inventory at the gathering step.
     */
    private int currentSlot = 0;

    /**
     * Amount of stacks the worker already kept in the current gathering process.
     */
    private List<ItemStorage> alreadyKept = new ArrayList<>();

    /**
     * The inventory's slot which is held in hand.
     */
    private static final int SLOT_HAND = 0;

    /**
     * Initialize the deliveryman and add all his tasks.
     *
     * @param deliveryman the job he has.
     */
    public EntityAIWorkDeliveryman(@NotNull final JobDeliveryman deliveryman)
    {
        super(deliveryman);
        super.registerTargets(
                /**
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
        worker.setSkillModifier(2 * worker.getCitizenData().getEndurance() + worker.getCitizenData().getCharisma());
        worker.setCanPickUpLoot(true);
    }

    /**
     * Gather items from a random hut which the hut doesn't need.
     *
     * @return the next state to go to.
     */
    public AIState gather()
    {
        if (gatherTarget == null)
        {
            gatherTarget = getRandomBuilding();
        }

        if (gatherTarget == null)
        {
            return START_WORKING;
        }

        worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.gathering"));

        if (!worker.isWorkerAtSiteWithMove(gatherTarget, MIN_DISTANCE_TO_WAREHOUSE))
        {
            return GATHERING;
        }

        final Colony colony = getOwnBuilding().getColony();
        if (colony != null)
        {
            final AbstractBuilding building = colony.getBuilding(gatherTarget);
            if (building == null)
            {
                gatherTarget = null;
                return START_WORKING;
            }
            if (gatherFromBuilding(building) || cannotHoldMoreItems())
            {
                this.alreadyKept = new ArrayList<>();
                this.currentSlot = 0;
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
        if (worker.getColony() == null || getOwnBuilding() == null)
        {
            return null;
        }

        final Collection<AbstractBuilding> buildingList = worker.getColony().getBuildings().values();
        final Object[] buildingArray = buildingList.toArray();

        final int random = worker.getRandom().nextInt(buildingArray.length);
        final AbstractBuilding building = (AbstractBuilding) buildingArray[random];

        if (building instanceof BuildingWareHouse || building instanceof BuildingTownHall)
        {
            return null;
        }

        return building.getLocation();
    }

    /**
     * Gather not needed Items from building.
     *
     * @param building building to gather it from.
     * @return true when finished.
     */
    private boolean gatherFromBuilding(@NotNull final AbstractBuilding building)
    {
        if (currentSlot >= building.getTileEntity().getSizeInventory())
        {
            return true;
        }

        final ItemStack stack = building.getTileEntity().getStackInSlot(currentSlot);
        if (workerRequiresItem(building, stack, alreadyKept)
                || (building instanceof BuildingHome && stack.getItem() instanceof ItemFood))
        {
            return false;
        }

        InventoryUtils.transferItemStackIntoNextFreeSlotInItemHandlers(building.getTileEntity().getSingleChestHandler(), currentSlot, new InvWrapper(worker.getInventoryCitizen()));
        building.markDirty();
        setDelay(DUMP_AND_GATHER_DELAY);
        worker.setHeldItem(SLOT_HAND);
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
        return InventoryUtils.getAmountOfStacksInItemHandler(new InvWrapper(worker.getInventoryCitizen())) >= Math.pow(2, getOwnBuilding().getBuildingLevel() - 1.0D);
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
        return (building instanceof AbstractBuildingWorker && ((AbstractBuildingWorker) building).neededForWorker(stack))
                || buildingRequiresCertainAmountOfItem(building, stack, localAlreadyKept);
    }

    /**
     * Check if the worker requires a certain amount of that item and if the deliveryman already kept it.
     * Always leave one stack behind if the worker requires a certain amount of it. Just to be sure.
     *
     * @param building         the building of the worker.
     * @param stack            the stack to check it with.
     * @param localAlreadyKept already kept items.
     * @return true if deliveryman should leave it behind.
     */
    private static boolean buildingRequiresCertainAmountOfItem(final AbstractBuilding building, final ItemStack stack, final List<ItemStorage> localAlreadyKept)
    {
        for (final Map.Entry<ItemStorage, Integer> entry : building.getRequiredItemsAndAmount().entrySet())
        {
            if (entry.getKey().getItemStack().isItemEqual(stack))
            {
                if (localAlreadyKept.contains(entry.getKey()))
                {
                    final int index = localAlreadyKept.indexOf(entry.getKey());
                    final ItemStorage temp = localAlreadyKept.get(index);

                    if (temp.getAmount() >= entry.getValue())
                    {
                        return false;
                    }

                    localAlreadyKept.remove(index);
                    temp.setAmount(temp.getAmount() + ItemStackUtils.getSize(stack));
                    localAlreadyKept.add(temp);
                    return true;
                }

                localAlreadyKept.add(entry.getKey());
                return true;
            }
        }
        return false;
    }

    /**
     * Dump the inventory into the warehouse.
     *
     * @return the next state to go to.
     */
    public AIState dump()
    {
        worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.dumping"));

        if (!worker.isWorkerAtSiteWithMove(wareHouse.getLocation(), MIN_DISTANCE_TO_WAREHOUSE))
        {
            return DUMPING;
        }

        wareHouse.getTileEntity().dumpInventoryIntoWareHouse(worker.getInventoryCitizen());
        gatherTarget = null;
        worker.setHeldItem(SLOT_HAND);

        return START_WORKING;
    }

    /**
     * Deliver the items to the hut.
     *
     * @return the next state.
     */
    private AIState deliver()
    {
        if(job.getReturning())
        {
            return DUMPING;
        }

        final BuildingDeliveryman deliveryHut = (getOwnBuilding() instanceof BuildingDeliveryman) ? (BuildingDeliveryman) getOwnBuilding() : null;
        ILocation buildingToDeliver = deliveryHut == null ? null : deliveryHut.getBuildingToDeliver();
        if (deliveryHut == null || buildingToDeliver == null)
        {
            if(job.getCurrentTask() != null)
            {
                final IRequest request = worker.getColony().getRequestManager().getRequestForToken(job.getCurrentTask());

                buildingToDeliver = request.getRequester().getRequesterLocation();
                return getState();
            }
            return START_WORKING;
        }

        worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.delivering"));

        if(!buildingToDeliver.isReachableFromLocation(worker.getLocation()))
        {
            Log.getLogger().info(worker.getColony().getName() + ": " + worker.getName() + ": Can't inter dimension yet: ");
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

            if(tileEntity instanceof TileEntityColonyBuilding)
            {
                final AbstractBuilding building = ((TileEntityColonyBuilding) tileEntity).getBuilding();
                InventoryUtils.forceItemStackToItemHandler(new InvWrapper((TileEntityColonyBuilding) tileEntity), stack, itemStack -> );

            }



            final ItemStack insertionResultStack = buildingToDeliver.forceTransferStack(stack, world);
            if (!ItemStackUtils.isEmpty(insertionResultStack))
            {
                if (ItemStack.areItemStacksEqual(insertionResultStack, stack))
                {
                    //same stack, we could not deliver ?
                    if (buildingToDeliver instanceof AbstractBuildingWorker)
                    {
                        chatSpamFilter.talkWithoutSpam(COM_MINECOLONIES_COREMOD_JOB_DELIVERYMAN_NAMEDCHESTFULL,
                                ((AbstractBuildingWorker) buildingToDeliver).getMainWorker().getName());
                    }
                    else
                    {
                        chatSpamFilter.talkWithoutSpam(COM_MINECOLONIES_COREMOD_JOB_DELIVERYMAN_CHESTFULL,
                                new TextComponentString(" :" + buildingToDeliver.getSchematicName()));
                    }
                }

                //Insert the result back into the inventory so we do not loose it.
                workerInventory.insertItem(i, insertionResultStack, false);
            }
        }

        worker.addExperience(1.0D);
        worker.setHeldItem(SLOT_HAND);
        deliveryHut.setBuildingToDeliver(null);

        gatherTarget = buildingToDeliver.getLocation();
        return GATHERING;
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
            final IToken task = job.getCurrentTask();
            if (task != null)
            {
                if (job.getReturning())
                {
                    job.setCurrentTask(null);
                    job.setReturning(false);

                    return DUMPING;
                }
                final IRequest request = worker.getColony().getRequestManager().getRequestForToken(task);
                ((BuildingDeliveryman) ownBuilding).setBuildingToDeliver(request.getRequester().getRequesterLocation());
                if(InventoryUtils.hasItemInItemHandler(new InvWrapper(worker.getInventoryCitizen()), itemStack -> request.getDelivery().isItemEqualIgnoreDurability(itemStack)))
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
     * @param buildingToDeliver building to deliver to.
     */
    private AIState gatherItems(@NotNull final IRequest request)
    {
        final ILocation location = request.getRequester().getDeliveryLocation();

        if(!location.isReachableFromLocation(worker.getLocation()))
        {
            job.setCurrentTask(null);
            ((BuildingDeliveryman) getOwnBuilding()).setBuildingToDeliver(null);
            request.setState(worker.getColony().getRequestManager(), RequestState.CANCELLED);
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

        if(isInTileEntity(tileEntity, request.getDelivery()))
        {
            setDelay(DUMP_AND_GATHER_DELAY);
            return DELIVERY;
        }

        job.setCurrentTask(null);
        ((BuildingDeliveryman) getOwnBuilding()).setBuildingToDeliver(null);
        request.setState(worker.getColony().getRequestManager(), RequestState.CANCELLED);
        return START_WORKING;
    }

    /**
     * Check the wareHouse for the next task.
     *
     * @return the next AiState to go to.
     */
    private AIState checkWareHouse()
    {
        if (!worker.isWorkerAtSiteWithMove(wareHouse.getLocation(), MIN_DISTANCE_TO_WAREHOUSE))
        {
            return START_WORKING;
        }

        final AbstractBuildingWorker ownBuilding = getOwnBuilding();

        //get task via colony, requestmananger

        if (job.getCurrentTask() == null)
        {
            ((BuildingDeliveryman) ownBuilding).setBuildingToDeliver(null);
            return GATHERING;
        }
        else if (job.getReturning())
        {
            job.setReturning(false);
            job.setCurrentTask(null);
            ((BuildingDeliveryman) ownBuilding).setBuildingToDeliver(null);
            return DUMPING;
        }
        else
        {
            final IRequest request = worker.getColony().getRequestManager().getRequestForToken(job.getCurrentTask());
            ((BuildingDeliveryman) ownBuilding).setBuildingToDeliver(request.getRequester().getRequesterLocation());
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
        worker.setAIMoveSpeed((float) (BASE_MOVEMENT_SPEED + BASE_MOVEMENT_SPEED * worker.getLevel() / WALKING_SPEED_MULTIPLIER));

        if (wareHouse != null && wareHouse.getTileEntity() != null)
        {
            return false;
        }

        final Map<BlockPos, AbstractBuilding> buildings = job.getColony().getBuildings();
        for (final AbstractBuilding building : buildings.values())
        {
            if (building == null)
            {
                continue;
            }

            final Colony buildingColony = building.getColony();
            final Colony ownColony = worker.getColony();
            if (building instanceof BuildingWareHouse && ownColony != null && buildingColony != null && buildingColony.getID() == ownColony.getID()
                    && ((BuildingWareHouse) building).registerWithWareHouse((BuildingDeliveryman) this.getOwnBuilding()))
            {
                wareHouse = (BuildingWareHouse) building;
                return false;
            }
        }

        chatSpamFilter.talkWithoutSpam(COM_MINECOLONIES_COREMOD_JOB_DELIVERYMAN_NOWAREHOUSE);
        return true;
    }
}
