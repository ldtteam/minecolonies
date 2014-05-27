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

    public void clearInventory()
    {
        for(int slot = 0; slot < this.getSizeInventory(); slot++)
        {
            setInventorySlotContents(slot, null);
        }
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

    /**
     * Tries to put an item into Inventory
     *
     * @param stack Item stack with items to be transferred
     * @return returns null if successful, or stack of remaining items
     */
    public ItemStack setStackInInventory(ItemStack stack)
    {
        if(stack != null)
        {
            ItemStack returnStack = stack;
            int slot;
            while((slot = containsPartialItemStack(stack)) != -1 && returnStack != null)
            {
                ItemStack current = getStackInSlot(slot);
                int spaceLeft = current.getMaxStackSize() - current.stackSize;
                if(spaceLeft > 0)
                {
                    ItemStack toBeAdded = returnStack.splitStack(Math.min(returnStack.stackSize, spaceLeft));
                    if(returnStack.stackSize == 0)
                    {
                        returnStack = null;
                    }
                    current.stackSize += toBeAdded.stackSize;
                    setInventorySlotContents(slot, current);
                }
            }

            slot = getOpenSlot();
            if(slot != -1 && returnStack != null)
            {
                setInventorySlotContents(slot, returnStack);
                returnStack = null;
            }
            return returnStack;
        }
        return stack;
    }

    /**
     * returns first open slot in the inventory
     *
     * @return slot number or -1 if none found.
     */
    private int getOpenSlot()
    {
        for(int slot = 0; slot < getSizeInventory(); slot++)
        {
            if(getStackInSlot(slot) == null)
            {
                return slot;
            }
        }
        return -1;
    }

    /**
     * returns a slot number if a chest contains given ItemStack item
     *
     * @return returns slot number if found, -1 when not found.
     */
    public int containsItemStack(ItemStack stack)
    {
        for(int i = 0; i < getSizeInventory(); i++)
        {
            ItemStack testStack = getStackInSlot(i);
            if(testStack != null && testStack.isItemEqual(stack))
            {
                return i;
            }
        }
        return -1;
    }

    /**
     * returns a slot number if a chest contains given ItemStack item that is not fully stacked
     *
     * @return returns slot number if found, -1 when not found.
     */
    private int containsPartialItemStack(ItemStack stack)
    {
        for(int i = 0; i < getSizeInventory(); i++)
        {
            ItemStack testStack = getStackInSlot(i);
            if(testStack != null && testStack.isItemEqual(stack) && testStack.stackSize != testStack.getMaxStackSize())
            {
                return i;
            }
        }
        return -1;
    }

}