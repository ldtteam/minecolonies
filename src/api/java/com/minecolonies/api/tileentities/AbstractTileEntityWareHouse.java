package com.minecolonies.api.tileentities;

import com.minecolonies.api.inventory.InventoryCitizen;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

public abstract class AbstractTileEntityWareHouse extends TileEntityColonyBuilding
{
    public AbstractTileEntityWareHouse(final TileEntityType<? extends AbstractTileEntityWareHouse> warehouse)
    {
        super(warehouse);
    }

    /**
     * Method to get the first matching ItemStack in the Warehouse.
     *
     * @param itemStackSelectionPredicate The predicate to select the ItemStack with.
     * @return The first matching ItemStack.
     */
    public abstract boolean hasMatchingItemStackInWarehouse(@NotNull Predicate<ItemStack> itemStackSelectionPredicate, int count);

    /**
     * Method used to check if this warehouse holds any of the requested itemstacks.
     *
     * @param itemStack The stack to check with to check with.
     * @param count the min count.
     * @return True when the warehouse holds a stack, false when not.
     */
    public abstract boolean hasMatchingItemStackInWarehouse(@NotNull final ItemStack itemStack, final int count);

    /**
     * Method used to check if this warehouse holds any of the requested itemstacks.
     *
     * @param itemStackSelectionPredicate The predicate to check with.
     * @return True when the warehouse holds a stack, false when not.
     */
    @NotNull
    public abstract List<ItemStack> getMatchingItemStacksInWarehouse(@NotNull Predicate<ItemStack> itemStackSelectionPredicate);

    /**
     * Dump the inventory of a citizen into the warehouse. Go through all items and search the right chest to dump it in.
     *
     * @param inventoryCitizen the inventory of the citizen
     */
    public abstract void dumpInventoryIntoWareHouse(@NotNull InventoryCitizen inventoryCitizen);
}
