package com.minecolonies.coremod.entity.ai.citizen.deliveryman;

import com.minecolonies.api.util.constant.IToolType;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
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
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.coremod.entity.ai.util.AIState.*;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;

/**
 * Performs deliveryman work.
 * Created: July 18, 2014
 *
 * @author MrIbby
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
     * List of itemStacks the deliveryman will deliver to the requesting building.
     */
    private List<ItemStack> itemsToDeliver = new ArrayList<>();

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
                new AITarget(GATHER_IN_WAREHOUSE, this::gatherItemsFromWareHouse),
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
        if (ItemStackUtils.isEmpty(stack) || workerRequiresItem(building, stack, alreadyKept)
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
     * Check if the worker requires a certain amount of that item and if the deliveryman already kept it.
     * Always leave one stack behind if the worker requires a certain amount of it. Just to be sure.
     *
     * @param building         the building of the worker.
     * @param stack            the stack to check it with.
     * @param localAlreadyKept already kept items.
     * @return true if deliveryman should leave it behind.
     */
    private static boolean buildingRequiresCertainAmountOfItem(AbstractBuilding building, ItemStack stack, List<ItemStorage> localAlreadyKept)
    {
        for (final Map.Entry<ItemStorage, Integer> entry : building.getRequiredItemsAndAmount().entrySet())
        {
            if (entry.getKey().getItem() == stack.getItem()
                    && entry.getKey().getDamageValue() == stack.getItemDamage()
                    && !localAlreadyKept.contains(entry.getKey()))
            {
                localAlreadyKept.add(entry.getKey());
                return true;
            }
        }
        return false;
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
    public static boolean workerRequiresItem(AbstractBuilding building, ItemStack stack, List<ItemStorage> localAlreadyKept)
    {
        return (building instanceof AbstractBuildingWorker && ((AbstractBuildingWorker) building).neededForWorker(stack))
                 || buildingRequiresCertainAmountOfItem(building, stack, localAlreadyKept);
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
     * Dump the inventory into the warehouse.
     *
     * @return the next state to go to.
     */
    public AIState dump()
    {
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
        final BuildingDeliveryman deliveryHut = (getOwnBuilding() instanceof BuildingDeliveryman) ? (BuildingDeliveryman) getOwnBuilding() : null;
        final AbstractBuilding buildingToDeliver = deliveryHut==null ? null : deliveryHut.getBuildingToDeliver();
        if (deliveryHut == null || buildingToDeliver == null)
        {
            return START_WORKING;
        }

        if (!worker.isWorkerAtSiteWithMove(buildingToDeliver.getLocation(), MIN_DISTANCE_TO_WAREHOUSE))
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

            final ItemStack insertionResultStack = buildingToDeliver.forceTransferStack(stack, world);
            if (!ItemStackUtils.isEmpty(insertionResultStack))
            {
                if (ItemStack.areItemStacksEqual(insertionResultStack, stack))
                {
                    //same stack, we could not deliver ?
                    if (buildingToDeliver instanceof AbstractBuildingWorker)
                    {
                        chatSpamFilter.talkWithoutSpam(COM_MINECOLONIES_COREMOD_JOB_DELIVERYMAN_NAMEDCHESTFULL,
                            ((AbstractBuildingWorker)buildingToDeliver).getWorker().getName());
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
        buildingToDeliver.setOnGoingDelivery(false);
        deliveryHut.setBuildingToDeliver(null);

        if(buildingToDeliver instanceof BuildingHome)
        {
            ((BuildingHome) buildingToDeliver).setFoodNeeded(false);
        }

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
            final AbstractBuilding buildingToDeliver = ((BuildingDeliveryman) ownBuilding).getBuildingToDeliver();
            if (buildingToDeliver != null)
            {
                final boolean ableToDeliver = wareHouse.getTileEntity().checkInWareHouse(buildingToDeliver, false);

                if (!ableToDeliver)
                {
                    buildingToDeliver.setOnGoingDelivery(false);
                    return START_WORKING;
                }
                itemsToDeliver = buildingToDeliver.getCopyOfNeededItems();
                return GATHER_IN_WAREHOUSE;
            }
        }
        return START_WORKING;
    }

    /**
     * Check if food has to be delivered to a certain building.
     *
     * @param building building to check.
     * @return true if so.
     */
    private boolean needsToDeliverFood(@NotNull final AbstractBuilding building)
    {
        return building instanceof BuildingHome && ((BuildingHome) building).isFoodNeeded() && !hasFood(building);
    }

    private AIState gatherItemsFromWareHouse()
    {
        final AbstractBuildingWorker ownBuilding = getOwnBuilding();
        if (ownBuilding instanceof BuildingDeliveryman)
        {
            final AbstractBuilding buildingToDeliver = ((BuildingDeliveryman) ownBuilding).getBuildingToDeliver();
            if (buildingToDeliver != null)
            {
                if (itemsToDeliver.isEmpty() && hasTools(buildingToDeliver) && !needsToDeliverFood(buildingToDeliver))
                {
                    ((BuildingDeliveryman) ownBuilding).setBuildingToDeliver(null);
                    return START_WORKING;
                }

                return tryToGatherItems(buildingToDeliver);
            }
        }
        return START_WORKING;
    }

    /**
     * Check if the deliveryman has all the tools to make the delivery.
     *
     * @param buildingToDeliver the building to deliver to.
     * @return true if is ready to deliver.
     */
    private boolean hasTools(@NotNull final AbstractBuilding buildingToDeliver)
    {
        final IToolType requiredTool = buildingToDeliver.getNeedsTool();
        if (requiredTool == ToolType.NONE)
        {
            return true;
        }

        return InventoryUtils.isToolInItemHandler(new InvWrapper(worker.getInventoryCitizen()), requiredTool, buildingToDeliver.getNeededToolLevel(),
                buildingToDeliver.getBuildingLevel());
    }

    /**
     * Check if the deliveryman has all the foods to make the delivery.
     *
     * @param buildingToDeliver the building to deliver to.
     * @return true if is ready to deliver.
     */
    private boolean hasFood(AbstractBuilding buildingToDeliver)
    {
        return InventoryUtils.getItemCountInItemHandler(new InvWrapper(worker.getInventoryCitizen()),
                stack -> !ItemStackUtils.isEmpty(stack) && stack.getItem() instanceof ItemFood) > buildingToDeliver.getBuildingLevel();
    }

    /**
     * Gather item from chest.
     * Gathers only one stack of the item.
     *
     * @param buildingToDeliver building to deliver to.
     */
    private AIState tryToGatherItems(@NotNull final AbstractBuilding buildingToDeliver)
    {
        final BlockPos position;

        if (buildingToDeliver instanceof BuildingHome)
        {
            position = wareHouse.getTileEntity().getPositionOfChestWithItemStack(
                    itemStack -> !ItemStackUtils.isEmpty(itemStack) && itemStack.getItem() instanceof ItemFood);
        }
        else if (itemsToDeliver.isEmpty())
        {
            final IToolType toolType = buildingToDeliver.getNeedsTool();
            position = wareHouse.getTileEntity()
                         .getPositionOfChestWithTool(toolType,
                           buildingToDeliver.getNeededToolLevel(),
                           buildingToDeliver);
        }
        else
        {
            final ItemStack stack = itemsToDeliver.get(0);
            position = wareHouse.getTileEntity().getPositionOfChestWithItemStack(stack);
        }

        if (position == null)
        {
            ((BuildingDeliveryman) getOwnBuilding()).setBuildingToDeliver(null);
            itemsToDeliver.clear();
            return START_WORKING;
        }

        if (!worker.isWorkerAtSiteWithMove(position, MIN_DISTANCE_TO_CHEST))
        {
            setDelay(DUMP_AND_GATHER_DELAY);
            return GATHER_IN_WAREHOUSE;
        }

        return gatherItems(buildingToDeliver, position);
    }

    /**
     * Gather item from chest.
     * Gathers only one stack of the item.
     *
     * @param buildingToDeliver building to deliver to.
     */
    private AIState gatherItems(@NotNull final AbstractBuilding buildingToDeliver, @NotNull final BlockPos position)
    {
        final TileEntity tileEntity = world.getTileEntity(position);
        if (tileEntity instanceof TileEntityChest)
        {
            if (!(tileEntity instanceof TileEntityColonyBuilding))
            {
                if (((TileEntityChest) tileEntity).numPlayersUsing == 0)
                {
                    this.world.addBlockEvent(tileEntity.getPos(), tileEntity.getBlockType(), 1, 1);
                    this.world.notifyNeighborsOfStateChange(tileEntity.getPos(), tileEntity.getBlockType());
                    this.world.notifyNeighborsOfStateChange(tileEntity.getPos().down(), tileEntity.getBlockType());
                    setDelay(DUMP_AND_GATHER_DELAY);
                    return GATHER_IN_WAREHOUSE;
                }
                this.world.addBlockEvent(tileEntity.getPos(), tileEntity.getBlockType(), 1, 0);
                this.world.notifyNeighborsOfStateChange(tileEntity.getPos(), tileEntity.getBlockType());
                this.world.notifyNeighborsOfStateChange(tileEntity.getPos().down(), tileEntity.getBlockType());
            }

            if (buildingToDeliver instanceof BuildingHome)
            {
                final int extraFood = worker.getCitizenData().getSaturation() < EntityCitizen.HIGH_SATURATION ? 1 : 0;

                //Tries to extract a certain amount of the item of the chest.
                if (InventoryUtils.transferXOfFirstSlotInProviderWithIntoNextFreeSlotInItemHandler(
                        tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null),
                        itemStack -> !ItemStackUtils.isEmpty(itemStack) && itemStack.getItem() instanceof ItemFood,
                        buildingToDeliver.getBuildingLevel() + extraFood,
                        new InvWrapper(worker.getInventoryCitizen())))
                {
                    worker.setHeldItem(SLOT_HAND);
                    setDelay(DUMP_AND_GATHER_DELAY);
                    return DELIVERY;
                }

                ((BuildingDeliveryman) getOwnBuilding()).setBuildingToDeliver(null);
                itemsToDeliver.clear();
                return START_WORKING;
            }
            else if (retrieveToolInTileEntity((TileEntityChest) tileEntity, buildingToDeliver.getNeedsTool(), TOOL_LEVEL_WOOD_OR_GOLD,
                    buildingToDeliver.getBuildingLevel()))
            {
                worker.setHeldItem(SLOT_HAND);
                setDelay(DUMP_AND_GATHER_DELAY);
                return DELIVERY;
            }
            else if (!itemsToDeliver.isEmpty())
            {
                final ItemStack stack = itemsToDeliver.get(0);
                if (isInTileEntity((TileEntityChest) tileEntity, stack))
                {
                    itemsToDeliver.remove(0);
                    worker.setHeldItem(SLOT_HAND);
                    setDelay(DUMP_AND_GATHER_DELAY);
                    return DELIVERY;
                }
                ((BuildingDeliveryman) getOwnBuilding()).setBuildingToDeliver(null);
                itemsToDeliver.clear();
                return START_WORKING;
            }
        }

        setDelay(DUMP_AND_GATHER_DELAY);
        return GATHER_IN_WAREHOUSE;
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

        @Nullable final AbstractBuilding buildingToDeliver = wareHouse.getTileEntity().getTask();
        if (ownBuilding instanceof BuildingDeliveryman)
        {
            if (buildingToDeliver == null)
            {
                ((BuildingDeliveryman) ownBuilding).setBuildingToDeliver(null);
                return GATHERING;
            }
            ((BuildingDeliveryman) ownBuilding).setBuildingToDeliver(buildingToDeliver);
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
