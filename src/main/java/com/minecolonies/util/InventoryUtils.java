package com.minecolonies.util;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;

public class InventoryUtils
{
    /**
     * Tries to put an item into Inventory
     *
     * @param stack Item stack with items to be transferred
     * @return returns null if successful, or stack of remaining items
     */
    public static ItemStack setStack(IInventory inventory, ItemStack stack)
    {
        if(stack != null)
        {
            ItemStack returnStack = stack;
            int slot;
            while((slot = containsPartialStack(inventory, stack)) != -1 && returnStack != null)
            {
                ItemStack current = inventory.getStackInSlot(slot);
                int spaceLeft = current.getMaxStackSize() - current.stackSize;
                if(spaceLeft > 0)
                {
                    ItemStack toBeAdded = returnStack.splitStack(Math.min(returnStack.stackSize, spaceLeft));
                    if(returnStack.stackSize == 0)
                    {
                        returnStack = null;
                    }
                    current.stackSize += toBeAdded.stackSize;
                    inventory.setInventorySlotContents(slot, current);
                }
            }

            while((slot = getOpenSlot(inventory)) != -1 && returnStack != null)
            {
                inventory.setInventorySlotContents(slot, returnStack);
                if(returnStack.stackSize > inventory.getInventoryStackLimit())
                {
                    returnStack.stackSize -= inventory.getInventoryStackLimit();
                }
                else
                {
                    returnStack = null;
                }
            }
            return returnStack;
        }
        return null;
    }

    /**
     * returns first open slot in the inventory
     *
     * @return slot number or -1 if none found.
     */
    public static int getOpenSlot(IInventory inventory)
    {
        for(int slot = 0; slot < inventory.getSizeInventory(); slot++)
        {
            if(inventory.getStackInSlot(slot) == null)
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
    public static int containsStack(IInventory inventory, ItemStack stack)
    {
        for(int i = 0; i < inventory.getSizeInventory(); i++)
        {
            ItemStack testStack = inventory.getStackInSlot(i);
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
    public static int containsPartialStack(IInventory inventory, ItemStack stack)
    {
        for(int i = 0; i < inventory.getSizeInventory(); i++)
        {
            ItemStack testStack = inventory.getStackInSlot(i);
            if(testStack != null && testStack.isItemEqual(stack) && testStack.stackSize != testStack.getMaxStackSize())
            {
                return i;
            }
        }
        return -1;
    }

    public static boolean takeStackInSlot(IInventory sendingInv, IInventory receivingInv, int slotID, int amount)
    {
        return takeStackInSlot(sendingInv, receivingInv, slotID, amount, false);
    }

    /**
     * @param takeAll Whether or not {@code receiving} will take the rest if possible
     * @return true if itemstack in specified {@code slotID} is not null and if {@code receiving} received
     * at least {@code amount} of itemstack
     */
    public static boolean takeStackInSlot(IInventory sendingInv, IInventory receivingInv, int slotID, int amount, boolean takeAll)
    {
        if(receivingInv != null && slotID >= 0 && amount >= 0)
        {
            ItemStack stack = sendingInv.decrStackSize(slotID, amount); // gets itemstack in slot, and decreases stacksize
            if(stack != null) // stack is null if no itemstack was in slot
            {
                stack = setStack(receivingInv, stack); // puts stack in receiving inventory
                if(stack != null) // checks for leftovers
                {
                    setStack(sendingInv, stack); // puts leftovers back in sending inventory
                    return false;
                }
                else
                {
                    if(takeAll)
                    {
                        stack = sendingInv.getStackInSlot(slotID); // gets itemstack in slot
                        if(stack != null) // checks if itemstack is still in slot
                        {
                            stack = sendingInv.decrStackSize(slotID, stack.stackSize);
                            stack = setStack(receivingInv, stack);
                            setStack(sendingInv, stack);
                        }
                    }

                    return true;
                }
            }
        }
        return false;
    }

    public static ItemStack[] getAllItemStacks(IInventory inventory)
    {
        ItemStack[] itemStack = new ItemStack[inventory.getSizeInventory()];
        for(int i = 0; i < inventory.getSizeInventory(); i++)
        {
            itemStack[i] = inventory.getStackInSlot(i);
        }
        return itemStack;
    }

    public static int getAmountOfStacks(IInventory inventory)
    {
        int count = 0;
        for(ItemStack is : getAllItemStacks(inventory))
        {
            if(is != null)
            {
                count++;
            }
        }
        return count;
    }

    public static void clear(IInventory inventory)
    {
        for(int slot = 0; slot < inventory.getSizeInventory(); slot++)
        {
            inventory.setInventorySlotContents(slot, null);
        }
    }

    /**
     * returns a slot number if an inventory contains given tool type
     *
     * @return returns slot number if found, -1 if not found.
     */
    public static int getFirstSlotContainingTool(IInventory inventory, Class<? extends ItemTool> tool)
    {
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack item = inventory.getStackInSlot(i);
            if (item != null && item.getItem().getClass().isAssignableFrom(tool)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * returns total number of uses left for given tool type
     *
     * @return returns total uses left for the given tool class - 0 if no tools of that type are found
     */
    public static int inventoryToolUsesLeft(IInventory inventory, Class<? extends ItemTool> tool)
    {
        int usesLeft = 0;
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack item = inventory.getStackInSlot(i);
            if (item != null && item.getItem().getClass().isAssignableFrom(tool)) {
                usesLeft += (item.getMaxDamage() - item.getItemDamage());
            }
        }
        return usesLeft;
    }
}
