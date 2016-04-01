package com.minecolonies.inventory;

import net.minecraft.inventory.IInvBasic;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;

/**
 * Basic inventory for the citizens
 */
public class InventoryCitizen extends InventoryBasic
{
    private int heldItem;

    /**
     * Creates the inventory of the citizen
     *
     * @param title             Title of the inventory
     * @param localeEnabled     Boolean whether the inventory has a custom name
     * @param size              Size of the inventory
     */
    public InventoryCitizen(String title, boolean localeEnabled, int size)
    {
        super(title, localeEnabled, size);
    }

    /**
     * Adds item to the inventory
     *
     * @param inventory     Inventory item to add
     */
    public void addIInvBasic(IInvBasic inventory)
    {
        func_110134_a(inventory);
    }

    /**
     * Removes item from inventory
     *
     * @param inventory     Inventory item to remove
     */
    public void removeIInvBasic(IInvBasic inventory)
    {
        func_110132_b(inventory);
    }

    /**
     * Sets the inventory name
     *
     * @param name          Name of the inventory
     */
    public void setInventoryName(String name)
    {
        func_110133_a(name);
    }

    /**
     * Set item to be held by citizen
     *
     * @param slot          Slot index with item to be held by citizen
     */
    public void setHeldItem(int slot)
    {
        this.heldItem = slot;
    }

    /**
     * Returns the item that is currently being held by citizen
     *
     * @return              {@link ItemStack} currently being held by citizen
     */
    public ItemStack getHeldItem()
    {
        return getStackInSlot(heldItem);
    }

    /**
     * Gets slot that hold item that is being held by citizen
     *
     * @return              Slot index of held item
     * @see                 {@link #getHeldItem()}
     */
    public int getHeldItemSlot()
    {
        return heldItem;
    }
}