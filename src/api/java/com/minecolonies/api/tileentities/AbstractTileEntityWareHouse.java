package com.minecolonies.api.tileentities;

import com.minecolonies.api.inventory.InventoryCitizen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

public abstract class AbstractTileEntityWareHouse extends TileEntityColonyBuilding
{
    /**
     * Empty constructor used to create TileEntities reflectively. Do not use.
     */
    public AbstractTileEntityWareHouse()
    {
        super();
    }

    /**
     * Constructor that creates a new warehouse tile entity.
     *
     * @param resourceName The building entry registry name for the warehouse building type this tile entity belongs to.
     */
    public AbstractTileEntityWareHouse(final ResourceLocation resourceName) {super(resourceName);}

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
