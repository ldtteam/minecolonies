package com.minecolonies.coremod.colony.buildings.inventory;

import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.coremod.tileentities.TileEntityRack;
import net.minecraft.item.ItemStack;

import java.util.Set;

/**
 * Wraps all needed information returned by an item search in a class.
 */
public class ItemSearchResult
{
    /**
     * The item we've been looking for.
     */
    private ItemStorage item;

    /**
     * The rack which contains the item.
     */
    private TileEntityRack rack;

    /**
     * The list of slots within the rack which hold the matching items.
     */
    private Set<Integer> slots;

    public ItemSearchResult(ItemStorage item, TileEntityRack rack, Set<Integer> slots)
    {
        this.item = item;
        this.rack = rack;
        this.slots = slots;
    }

    /**
     * Get the itemstack
     */
    public ItemStack getItem()
    {
        return item.getItemStack();
    }

    /**
     * Get the itemstorage
     */
    public ItemStorage getItemStorage()
    {
        return item;
    }

    /**
     * Get the rack
     */
    public TileEntityRack getRack()
    {
        return rack;
    }

    /**
     * Get the set of inventory slots
     */
    public Set<Integer> getSlots()
    {
        return slots;
    }
}
