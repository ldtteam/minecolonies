package com.minecolonies.inventory;

import net.minecraft.inventory.IInvBasic;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;

public class InventoryCitizen extends InventoryBasic
{

    public InventoryCitizen(String title, boolean localeEnabled, int size)
    {
        super(title, localeEnabled, size);
    }

    public void addIInvBasic(IInvBasic inventory)
    {
        super.func_110134_a(inventory);
    }

    public void removeIInvBasic(IInvBasic inventory)
    {
        super.func_110132_b(inventory);
    }

    public void setInventoryTitle(String title)
    {
        super.func_110133_a(title);
    }

    public ItemStack[] getAllItemsInInventory()
    {
        ItemStack[] itemStack = new ItemStack[super.getSizeInventory()];
        for(int i = 0; i < super.getSizeInventory(); i++)
        {
            itemStack[i] = super.getStackInSlot(i);
        }
        return itemStack;
    }

    public boolean clearInventory()
    {
        for(int slot = 0; slot < this.getSizeInventory(); slot++)
        {
            setInventorySlotContents(slot, null);
        }
        return true;
    }

    public int getAmountOfItemsInInventory()
    {
        int count = 0;
        for(ItemStack is : getAllItemsInInventory())
        {
            if(is != null)
            {
                count++;
            }
        }
        return count;
    }

}