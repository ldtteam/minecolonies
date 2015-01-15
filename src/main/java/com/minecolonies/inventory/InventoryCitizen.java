package com.minecolonies.inventory;

import net.minecraft.inventory.IInvBasic;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class InventoryCitizen extends InventoryBasic
{
    private int heldItem;

    public InventoryCitizen(String title, boolean localeEnabled, int size)
    {
        super(title, localeEnabled, size);
    }

    public void addIInvBasic(IInvBasic inventory)
    {
        func_110134_a(inventory);
    }

    public void removeIInvBasic(IInvBasic inventory)
    {
        func_110132_b(inventory);
    }

    public void setInventoryName(String name)
    {
        func_110133_a(name);
    }

    public void setHeldItem(int slot)
    {
        this.heldItem = slot;
    }

    public ItemStack getHeldItem()
    {
        return getStackInSlot(heldItem);
    }

    public int getHeldItemSlot()
    {
        return heldItem;
    }
}