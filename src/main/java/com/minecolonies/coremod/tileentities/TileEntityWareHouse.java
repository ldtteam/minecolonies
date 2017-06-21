package com.minecolonies.coremod.tileentities;

import com.minecolonies.api.util.constant.IToolType;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.api.util.*;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.*;
import com.minecolonies.coremod.inventory.InventoryCitizen;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_WAREHOUSE_FULL;

/**
 * Class which handles the tileEntity of our colonyBuildings.
 */
public class TileEntityWareHouse extends TileEntityColonyBuilding
{
    /**
     * Queue which contains the currentTasks to be executed by the deliveryman.
     */
    private final Queue<AbstractBuilding> taskQueue = new ConcurrentLinkedQueue<>();
    private final Set<AbstractBuilding>   taskSet   = ConcurrentHashMap.newKeySet();

    /**
     * Wait this amount of ticks before checking again.
     */
    private static final int WAIT_TICKS = 5;

    /**
     * Index of last controlled building.
     */
    private int index = 1;

    /**
     * Ticks past since the last check.
     */
    private int ticksPassed = 0;

    /**
     * Empty standard constructor.
     */
    public TileEntityWareHouse()
    {
        super();
    }

    @NotNull
    @Override
    public NBTTagCompound getUpdateTag()
    {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void update()
    {
        super.update();

        if(ticksPassed != WAIT_TICKS)
        {
            ticksPassed++;
            return;
        }
        ticksPassed = 0;

        final AbstractBuilding wareHouseBuilding = getBuilding();
        if(getColony() != null
                && wareHouseBuilding instanceof BuildingWareHouse
                && !((BuildingWareHouse) wareHouseBuilding).getRegisteredDeliverymen().isEmpty())
        {
            final Map<BlockPos, AbstractBuilding> buildingMap = getColony().getBuildings();

            if (buildingMap.size() < this.index)
            {
                this.index = 1;
            }

            int i = 1;
            for (@NotNull final Map.Entry<BlockPos, AbstractBuilding> buildingEntry : buildingMap.entrySet())
            {
                if (i == index)
                {
                    if(!taskSet.contains(buildingEntry.getValue())
                            && buildingEntry.getValue().needsAnything())
                    {
                        checkInWareHouse(buildingEntry.getValue(), true);
                    }
                    this.index++;
                }
                i++;
            }
        }
    }

    public boolean checkInWareHouse(final BuildingHome buildingEntry, final boolean addToList)
    {
        if (buildingEntry.isFoodNeeded())
        {
            if (isInHut(itemStack -> !ItemStackUtils.isEmpty(itemStack) && itemStack.getItem() instanceof ItemFood))
            {
                if (addToList)
                {
                    buildingEntry.setOnGoingDelivery(true);
                    taskQueue.add(buildingEntry);
                    taskSet.add(buildingEntry);
                }
                return true;
            }

            if (taskSet.contains(buildingEntry))
            {
                taskQueue.remove(buildingEntry);
                taskSet.remove(buildingEntry);
                buildingEntry.setOnGoingDelivery(false);
            }
        }
        return false;
    }

    /**
     * Get the first task in the taskQueue, or null if its empty.
     * @return the building which needs a delivery.
     */
    @Nullable
    public AbstractBuilding getTask()
    {
        final AbstractBuilding task = taskQueue.poll();
        if (task == null)
        {
            return null;
        }

        taskSet.remove(task);
        return task;
    }

    /**
     * Check if the required items by the building are in the wareHouse.
     * @param buildingEntry the building requesting.
     * @param addToList if is in warehouse should add to the list?
     * @return true if has something in warehouse to deliver.
     */
    public boolean checkInWareHouse(@NotNull final AbstractBuilding buildingEntry, final boolean addToList)
    {
        if(buildingEntry.areItemsNeeded())
        {
            for(final ItemStack stack : buildingEntry.getCopyOfNeededItems())
            {
                if(ItemStackUtils.isEmpty(stack)
                        || (deliveryManHasBuildingAsTask(buildingEntry)
                        && addToList))
                {
                    continue;
                }

                if(isInHut(stack))
                {
                    if(addToList)
                    {
                        buildingEntry.setOnGoingDelivery(true);
                        taskQueue.add(buildingEntry);
                        taskSet.add(buildingEntry);
                    }
                    return true;
                }
            }

            if (taskSet.contains(buildingEntry))
            {
                taskQueue.remove(buildingEntry);
                taskSet.remove(buildingEntry);
                buildingEntry.setOnGoingDelivery(false);
            }
        }

        final IToolType tool = buildingEntry.getNeedsTool();
        if(tool != ToolType.NONE)
        {
            if(isToolInHut(tool, buildingEntry))
            {
                if (addToList)
                {
                    buildingEntry.setOnGoingDelivery(true);
                    taskQueue.add(buildingEntry);
                    taskSet.add(buildingEntry);
                }
                return true;
            }

            if (taskSet.contains(buildingEntry))
            {
                taskQueue.remove(buildingEntry);
                taskSet.remove(buildingEntry);
                buildingEntry.setOnGoingDelivery(false);
            }
        }

        return buildingEntry instanceof BuildingHome && checkInWareHouse((BuildingHome) buildingEntry, addToList);
    }

    /**
     * Check if a building is being delivery by on of the warehouses deliverymen.
     * @param buildingEntry the building to check.
     * @return true if so.
     */
    private boolean deliveryManHasBuildingAsTask(@NotNull final AbstractBuilding buildingEntry)
    {
        final AbstractBuilding wareHouse = getBuilding();
        if(wareHouse instanceof BuildingWareHouse)
        {
            for(final Vec3d pos : ((BuildingWareHouse) wareHouse).getRegisteredDeliverymen())
            {
                final Colony colony = getColony();
                if(colony != null)
                {
                    final AbstractBuilding building = colony.getBuilding(new BlockPos(pos));
                    if(building instanceof BuildingDeliveryman)
                    {
                        return ((BuildingDeliveryman) building).getBuildingToDeliver() != null
                                && ((BuildingDeliveryman) building).getBuildingToDeliver().getLocation().equals(buildingEntry.getLocation());
                    }
                }
            }
        }
        return false;
    }

    /**
     * Check all chests in the worker hut for a required item.
     * @param is the type of item requested (amount is ignored)
     * @return true if a stack of that type was found
     */
    private boolean isInHut(@Nullable final ItemStack is)
    {
        return !ItemStackUtils.isEmpty(is) && isInHut(stack -> !ItemStackUtils.isEmpty(stack) && is.isItemEqual(stack));
    }

    /**
     * Check all chests in the worker hut for a required item.
     * @param itemStackSelectionPredicate the type of item requested (amount is ignored).
     * @return true if a stack of that type was found
     */
    private boolean isInHut(@NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        @Nullable final AbstractBuilding building = getBuilding();
        if(building != null)
        {
            if(isInTileEntity(building.getTileEntity(), itemStackSelectionPredicate))
            {
                return true;
            }

            for(final BlockPos pos : building.getAdditionalCountainers())
            {
                @Nullable final TileEntity entity = getWorld().getTileEntity(pos);
                if(entity instanceof TileEntityChest && isInTileEntity((TileEntityChest) entity, itemStackSelectionPredicate))
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check for a certain item and return the position of the chest containing it.
     * @param is the stack to search for.
     * @return the position or null.
     */
    @Nullable
    public BlockPos getPositionOfChestWithItemStack(@NotNull final ItemStack is)
    {
        return getPositionOfChestWithItemStack(stack -> !ItemStackUtils.isEmpty(stack) && is.isItemEqual(stack));
    }

    /**
     * Check for a certain item and return the position of the chest containing it.
     * @param itemStackSelectionPredicate the stack to search for.
     * @return the position or null.
     */
    @Nullable
    public BlockPos getPositionOfChestWithItemStack(@NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        @Nullable final AbstractBuilding building = getBuilding();

        if(building != null)
        {
            if(isInTileEntity(building.getTileEntity(), itemStackSelectionPredicate))
            {
                return building.getLocation();
            }

            for(final BlockPos pos : building.getAdditionalCountainers())
            {
                final TileEntity entity = getWorld().getTileEntity(pos);
                if(entity instanceof TileEntityChest && isInTileEntity((TileEntityChest) entity, itemStackSelectionPredicate))
                {
                    return pos;
                }
            }
        }
        return null;
    }

    /**
     * Check for a certain item and return the position of the chest containing it.
     * @param tool the tool to search for.
     * @param minLevel the minLevel of the tool
     * @param requestingBuilding the building requesting it.
     * @return the position or null.
     */
    public BlockPos getPositionOfChestWithTool(@NotNull final IToolType tool, final int minLevel, @NotNull final AbstractBuilding requestingBuilding)
    {
        @Nullable final AbstractBuilding building = getBuilding();

        if(building != null)
        {
            if(InventoryUtils.isToolInProvider(building.getTileEntity(), tool, minLevel, requestingBuilding.getBuildingLevel()))
            {
                return building.getLocation();
            }

            for(@NotNull final BlockPos pos : building.getAdditionalCountainers())
            {
                final TileEntity entity = getWorld().getTileEntity(pos);
                if (entity instanceof TileEntityChest
                        && InventoryUtils.isToolInProvider(entity, tool, minLevel, requestingBuilding.getBuildingLevel()))
                {
                    return pos;
                }
            }
        }
        return null;
    }

    /**
     * Check all chests in the worker hut for a required tool.
     * @param tool the type of tool requested (amount is ignored)
     * @param requestingBuilding the building requesting it.
     * @return true if a stack of that type was found
     */
    private boolean isToolInHut(final IToolType toolType, @NotNull final AbstractBuilding requestingBuilding)
    {
        @Nullable final AbstractBuilding building = getBuilding();

        if(building != null)
        {
            if(InventoryUtils.isToolInProvider(building.getTileEntity(), toolType, requestingBuilding.getNeededToolLevel(), requestingBuilding.getBuildingLevel()))
            {
                return true;
            }

            for(final BlockPos pos : building.getAdditionalCountainers())
            {
                @Nullable final TileEntity entity = getWorld().getTileEntity(pos);
                if(entity instanceof TileEntityChest
                        && InventoryUtils.isToolInProvider(entity, toolType, requestingBuilding.getNeededToolLevel(), requestingBuilding.getBuildingLevel()))
                {
                    return true;
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
     * @param entity the tileEntity chest or building.
     * @param itemStackSelectionPredicate the itemStack predicate.
     * @return true if found the stack.
     */
    private static boolean isInTileEntity(final TileEntityChest entity, @NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        return InventoryFunctions
                .matchFirstInProvider(
                        entity,
                        itemStackSelectionPredicate);
    }

    /**
     * Dump the inventory of a citizen into the warehouse.
     * Go through all items and search the right chest to dump it in.
     * @param inventoryCitizen the inventory of the citizen
     */
    public void dumpInventoryIntoWareHouse(@NotNull final InventoryCitizen inventoryCitizen)
    {
        for (int i = 0; i < new InvWrapper(inventoryCitizen).getSlots(); i++)
        {
            final ItemStack stack = inventoryCitizen.getStackInSlot(i);
            if(ItemStackUtils.isEmpty(stack))
            {
                continue;
            }
            @Nullable final TileEntityChest chest = searchRightChestForStack(stack);
            if(chest == null)
            {
                LanguageHandler.sendPlayersMessage(getColony().getMessageEntityPlayers(), COM_MINECOLONIES_COREMOD_WAREHOUSE_FULL);
                return;
            }
            InventoryUtils.addItemStackToProvider(chest, stack);
            new InvWrapper(inventoryCitizen).extractItem(i, Integer.MAX_VALUE, false);
        }
    }

    /**
     * Search the right chest for an itemStack.
     * @param stack the stack to dump.
     * @return the tile entity of the chest
     */
    @Nullable
    private TileEntityChest searchRightChestForStack(@NotNull final ItemStack stack)
    {
        if(InventoryUtils.findFirstSlotInProviderWith(this, stack.getItem(), stack.getItemDamage()) != -1 && InventoryUtils.getFirstOpenSlotFromProvider(this) != -1)
        {
            return this;
        }

        for(@NotNull final BlockPos pos : getBuilding().getAdditionalCountainers())
        {
            final TileEntity entity = getWorld().getTileEntity(pos);
            if(entity instanceof TileEntityChest
                    && InventoryUtils.findFirstSlotInProviderWith(entity, stack.getItem(), stack.getItemDamage()) != -1
                    && InventoryUtils.getFirstOpenSlotFromProvider(entity) != -1)
            {
                return (TileEntityChest) entity;
            }
        }

        @Nullable final TileEntityChest chest = searchChestWithSimilarItem(stack);
        return chest == null ? searchMostEmptySlot() : chest;
    }

    /**
     * Searches a chest with a similar item as the incoming stack.
     * @param stack the stack.
     * @return the entity of the chest.
     */
    @Nullable
    private TileEntityChest searchChestWithSimilarItem(final ItemStack stack)
    {
        for(@NotNull final BlockPos pos : getBuilding().getAdditionalCountainers())
        {
            final TileEntity entity = getWorld().getTileEntity(pos);
            if(entity instanceof TileEntityChest
                    && InventoryUtils.findFirstSlotInProviderWith(entity, stack.getItem(), -1) != -1
                    && InventoryUtils.getFirstOpenSlotFromProvider(entity) != -1)
            {
                return (TileEntityChest) entity;
            }
        }
        return null;
    }

    /**
     * Search for the chest with the least items in it.
     * @return the tileEntity of this chest.
     */
    @Nullable
    private TileEntityChest searchMostEmptySlot()
    {
        int freeSlots = 0;
        TileEntityChest emptiestChest = null;
        for(@NotNull final BlockPos pos : getBuilding().getAdditionalCountainers())
        {
            final TileEntity entity = getWorld().getTileEntity(pos);
            if(entity == null)
            {
                getBuilding().removeContainerPosition(pos);
                continue;
            }
            if(entity instanceof TileEntityChest && InventoryUtils.getFirstOpenSlotFromProvider(entity) != -1)
            {
                final int tempFreeSlots = ((TileEntityChest) entity).getSizeInventory() - InventoryUtils.getAmountOfStacksInProvider(entity);
                if(freeSlots < tempFreeSlots)
                {
                    freeSlots = tempFreeSlots;
                    emptiestChest = (TileEntityChest) entity;
                }
            }
        }

        return emptiestChest;
    }
}
