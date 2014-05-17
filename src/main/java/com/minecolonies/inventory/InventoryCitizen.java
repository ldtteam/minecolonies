package com.minecolonies.inventory;

import net.minecraft.inventory.IInvBasic;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;

public class InventoryCitizen extends InventoryBasic
{

    public InventoryCitizen(String InventoryTitle, boolean LocaleEnabled, int InventorySize)
    {
        super(InventoryTitle, LocaleEnabled, InventorySize);
    }

    public void addIInvBasic(IInvBasic par1iInvBasic)
    {
        super.func_110134_a(par1iInvBasic);
    }

    public void removeIInvBasic(IInvBasic par1iInvBasic)
    {
        super.func_110132_b(par1iInvBasic);
    }

    public void setInventoryTitle(String par1Str)
    {
        super.func_110133_a(par1Str);
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