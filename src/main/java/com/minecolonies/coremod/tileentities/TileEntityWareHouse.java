package com.minecolonies.coremod.tileentities;

import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.inventory.InventoryCitizen;
import com.minecolonies.coremod.util.InventoryFunctions;
import com.minecolonies.coremod.util.InventoryUtils;
import com.minecolonies.coremod.util.Utils;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Class which handles the tileEntity of our colonyBuildings.
 */
public class TileEntityWareHouse extends TileEntityColonyBuilding
{
    /**
     * List which contains the currentTasks to be executed by the deliveryman.
     */
    private final CopyOnWriteArrayList<AbstractBuilding> list = new CopyOnWriteArrayList<>();

    /**
     * Index of last controlled building.
     */
    private int index = 1;

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

        if(getColony() != null)
        {
            Map<BlockPos, AbstractBuilding> buildingMap = getColony().getBuildings();

            if (buildingMap.size() < this.index)
            {
                this.index = 1;
            }

            int i = 1;
            for (Map.Entry<BlockPos, AbstractBuilding> buildingEntry : buildingMap.entrySet())
            {
                if (i == index)
                {
                    if(buildingEntry.getValue() instanceof AbstractBuildingWorker && !list.contains(buildingEntry.getValue()) && ((AbstractBuildingWorker) buildingEntry.getValue()).needsAnything())
                    {
                        checkInWareHouse((AbstractBuildingWorker) buildingEntry.getValue());
                    }
                    this.index++;
                }
                i++;
            }
        }
    }

    /**
     * Get the first task in the list.
     * @return the building which needs a delivery.
     */
    @Nullable
    public AbstractBuilding getTask()
    {
        if(list.isEmpty())
        {
            return null;
        }
        return list.remove(0);
    }

    /**
     * Check if the required items by the building are in the wareHouse.
     * @param buildingEntry the building requesting.
     * @return true if has something in warehouse to deliver.
     */
    public boolean checkInWareHouse(final AbstractBuilding buildingEntry)
    {
        if(buildingEntry.areItemsNeeded())
        {
            for(ItemStack stack : buildingEntry.getNeededItems())
            {
                if(isInHut(stack))
                {
                    buildingEntry.setOnGoingDelivery(true);
                    list.add(buildingEntry);
                    return true;
                }
            }
        }

        String tool = buildingEntry.getRequiredTool();
        if(!tool.isEmpty() && isToolInHut(tool))
        {
            buildingEntry.setOnGoingDelivery(true);
            list.add(buildingEntry);
            return true;
        }
        return false;
    }

    /**
     * Check all chests in the worker hut for a required item.
     * @param is the type of item requested (amount is ignored)
     * @return true if a stack of that type was found
     */
    public boolean isInHut(@Nullable final ItemStack is)
    {
        @Nullable final AbstractBuilding building = getBuilding();

        boolean hasItem;
        if(building != null)
        {
            hasItem = isInTileEntity(building.getTileEntity(), is);

            if(hasItem)
            {
                return true;
            }

            for(BlockPos pos : building.getAdditionalCountainers())
            {
                TileEntity entity = worldObj.getTileEntity(pos);
                if(entity instanceof TileEntityChest)
                {
                    hasItem = isInTileEntity((TileEntityChest) entity, is);

                    if(hasItem)
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Check for a certain item and return the position of the chest containing it.
     * @param stack the stack to search for.
     * @return the position or null.
     */
    @Nullable
    public BlockPos getPositionOfChestWithItemStack(ItemStack stack)
    {
        @Nullable final AbstractBuilding building = getBuilding();

        if(building != null)
        {
            if(isInTileEntity(building.getTileEntity(), stack))
            {
                return building.getLocation();
            }

            for(BlockPos pos : building.getAdditionalCountainers())
            {
                TileEntity entity = worldObj.getTileEntity(pos);
                if(entity instanceof TileEntityChest && isInTileEntity((TileEntityChest) entity, stack))
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
     * @param minLevel the minLevel of the pickaxe
     * @return the position or null.
     */
    public BlockPos getPositionOfChestWithTool(String tool, int minLevel)
    {
        @Nullable final AbstractBuilding building = getBuilding();

        if(building != null)
        {
            if((minLevel != -1 && InventoryUtils.isPickaxeInTileEntity(building.getTileEntity(), minLevel)) || InventoryUtils.isToolInTileEntity(building.getTileEntity(), tool))
            {
                return building.getLocation();
            }

            for(BlockPos pos : building.getAdditionalCountainers())
            {
                TileEntity entity = worldObj.getTileEntity(pos);
                if (entity instanceof TileEntityChest && ((minLevel != -1 && InventoryUtils.isPickaxeInTileEntity((TileEntityChest) entity, minLevel))
                        || InventoryUtils.isToolInTileEntity((TileEntityChest) entity, tool)))
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
     * @return true if a stack of that type was found
     */
    public boolean isToolInHut(final String tool)
    {
        @Nullable final AbstractBuilding building = getBuilding();

        boolean hasItem;
        if(building != null)
        {
            if(tool.equals(Utils.PICKAXE))
            {
                hasItem = InventoryUtils.isPickaxeInTileEntity(building.getTileEntity(), building.getNeededPickaxeLevel());
            }
            else
            {
                hasItem = InventoryUtils.isToolInTileEntity(building.getTileEntity(), tool);
            }

            if(hasItem)
            {
                return true;
            }

            for(BlockPos pos : building.getAdditionalCountainers())
            {
                TileEntity entity = worldObj.getTileEntity(pos);
                if(entity instanceof TileEntityChest)
                {
                    if(tool.equals(Utils.PICKAXE))
                    {
                        hasItem = InventoryUtils.isPickaxeInTileEntity(building.getTileEntity(), building.getNeededPickaxeLevel());
                    }
                    else
                    {
                        hasItem = InventoryUtils.isToolInTileEntity((TileEntityChest) entity, tool);
                    }

                    if(hasItem)
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
     * @param entity the tileEntity chest or building.
     * @param is the itemStack.
     * @return true if found the stack.
     */
    public boolean isInTileEntity(TileEntityChest entity, ItemStack is)
    {
        return is != null
                && InventoryFunctions
                .matchFirstInInventoryWithInventory(
                        entity,
                        stack -> stack != null && is.isItemEqual(stack),
                        InventoryFunctions::doNothing
                );
    }

    @Override
    public void readFromNBT(final NBTTagCompound compound)
    {
        super.readFromNBT(compound);
    }

    @NotNull
    @Override
    public NBTTagCompound writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        return compound;
    }

    /**
     * Dump the inventory of a citizen into the warehouse.
     * Go through all items and search the right chest to dump it in.
     * @param inventoryCitizen
     */
    public void dumpInventoryIntoWareHouse(@NotNull final InventoryCitizen inventoryCitizen)
    {
        for(int i = 0; i < inventoryCitizen.getSizeInventory(); i++)
        {
            ItemStack stack = inventoryCitizen.getStackInSlot(i);

            if(stack == null || stack.getItem() == null || stack.stackSize == 0)
            {
                continue;
            }
            TileEntityChest chest = searchRightChestForStack(stack);
            if(chest == null)
            {
                //todo notify player
                return;
            }
            InventoryUtils.addItemStackToInventory(chest, stack);
            inventoryCitizen.removeStackFromSlot(i);
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
        if(InventoryUtils.findFirstSlotInInventoryWith(this, stack.getItem(), stack.getItemDamage()) != -1 && InventoryUtils.getOpenSlot(this) != -1)
        {
            return this;
        }

        for(BlockPos pos : getBuilding().getAdditionalCountainers())
        {
            TileEntity entity = worldObj.getTileEntity(pos);
            if(entity instanceof TileEntityChest
                    && InventoryUtils.findFirstSlotInInventoryWith((TileEntityChest) entity, stack.getItem(), stack.getItemDamage()) != -1
                    && InventoryUtils.getOpenSlot(this) != -1)
            {
                return this;
            }
        }

        return searchMostEmptySlot();
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
        for(BlockPos pos : getBuilding().getAdditionalCountainers())
        {
            TileEntity entity = worldObj.getTileEntity(pos);
            if(entity instanceof TileEntityChest && InventoryUtils.getOpenSlot(this) != -1)
            {
                int tempFreeSlots = ((TileEntityChest) entity).getSizeInventory() - InventoryUtils.getAmountOfStacks((IInventory) entity);
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
