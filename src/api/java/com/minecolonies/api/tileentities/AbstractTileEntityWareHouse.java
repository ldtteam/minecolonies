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

    public abstract boolean hasMatchingItemStackInWarehouse(@NotNull Predicate<ItemStack> itemStackSelectionPredicate, int count);

    @NotNull
    public abstract List<ItemStack> getMatchingItemStacksInWarehouse(@NotNull Predicate<ItemStack> itemStackSelectionPredicate);

    /**
     * Dump the inventory of a citizen into the warehouse.
     * Go through all items and search the right chest to dump it in.
     *
     * @param inventoryCitizen the inventory of the citizen
     */
    public abstract void dumpInventoryIntoWareHouse(@NotNull InventoryCitizen inventoryCitizen);
}
