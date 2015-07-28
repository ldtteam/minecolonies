package com.minecolonies.util;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;


public class InventoryUtils
{
    private static final String TOOL_HOE = "hoe";
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
     * @return true if itemstack in specified {@code slotID} is not null and if {@code receivingInv} received
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
    public static int getFirstSlotContainingTool(IInventory inventory, String tool)
    {
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack item = inventory.getStackInSlot(i);
            if (item != null && (item.getItem().getToolClasses(item).contains(tool) || (tool.equals("hoe") && item.getUnlocalizedName().contains("hoe"))))
            {
                return i;
            }
        }
        return -1;
    }

    /**
     * Adapted from InventoryPlayer
     *
     * @param inventory Inventory
     * @param itemStack ItemStack to add
     * @return Success
     */
    public static boolean addItemStackToInventory(IInventory inventory, final ItemStack itemStack)
    {
        if (itemStack != null && itemStack.stackSize != 0 && itemStack.getItem() != null)
        {
                int stackSize;

                if (itemStack.isItemDamaged())
                {
                    stackSize = getOpenSlot(inventory);

                    if (stackSize >= 0)
                    {
                        ItemStack copy = ItemStack.copyItemStack(itemStack);
                        copy.animationsToGo = 5;
                        inventory.setInventorySlotContents(stackSize, copy);

                        itemStack.stackSize = 0;
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
                else
                {
                    do
                    {
                        stackSize = itemStack.stackSize;
                        itemStack.stackSize = storePartialItemStack(inventory, itemStack);
                    }
                    while (itemStack.stackSize > 0 && itemStack.stackSize < stackSize);


                    return itemStack.stackSize < stackSize;
                }
        }
        else
        {
            return false;
        }
    }

    /**
     * Adapted from InventoryPlayer
     *
     * This function stores as many items of an ItemStack as possible in a matching slot and returns the quantity of
     * left over items.
     */
    private static int storePartialItemStack(IInventory inventory, ItemStack itemStack)
    {
        Item item = itemStack.getItem();
        int stackSize = itemStack.stackSize;
        int slot;

        if (itemStack.getMaxStackSize() == 1)
        {
            slot = getOpenSlot(inventory);

            if (slot < 0)
            {
                return stackSize;
            }
            else
            {
                if (inventory.getStackInSlot(slot) == null)
                {
                    inventory.setInventorySlotContents(slot, ItemStack.copyItemStack(itemStack));
                }

                return 0;
            }
        }
        else
        {
            slot = findSlotForItemStack(inventory, itemStack);

            if (slot < 0)
            {
                slot = getOpenSlot(inventory);
            }

            if (slot < 0)
            {
                return stackSize;
            }
            else
            {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (stack == null)
                {
                    stack = new ItemStack(item, 0, itemStack.getItemDamage());

                    if (itemStack.hasTagCompound())
                    {
                        stack.setTagCompound((NBTTagCompound) itemStack.getTagCompound().copy());
                    }
                }

                int inventoryStackSpace = stackSize;

                if (stackSize > stack.getMaxStackSize() - stack.stackSize)
                {
                    inventoryStackSpace = stack.getMaxStackSize() - stack.stackSize;
                }

                if (inventoryStackSpace > inventory.getInventoryStackLimit() - stack.stackSize)
                {
                    inventoryStackSpace = inventory.getInventoryStackLimit() - stack.stackSize;
                }

                if (inventoryStackSpace == 0)
                {
                    return stackSize;
                }
                else
                {
                    stackSize -= inventoryStackSpace;
                    stack.stackSize += inventoryStackSpace;
                    stack.animationsToGo = 5;
                    inventory.setInventorySlotContents(slot, stack);
                    return stackSize;
                }
            }
        }
    }

    /**
     * Adapted from InventoryPlayer - storeItemStack
     *
     * find a slot to store an ItemStack in
     */
    private static int findSlotForItemStack(IInventory inventory, ItemStack itemStack)
    {
        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            ItemStack inventoryItem = inventory.getStackInSlot(i);
            if (inventoryItem != null && inventoryItem.getItem() == itemStack.getItem() && inventoryItem.isStackable() && inventoryItem.stackSize < inventoryItem.getMaxStackSize() && inventoryItem.stackSize < inventory.getInventoryStackLimit() && (!inventoryItem.getHasSubtypes() || inventoryItem.getItemDamage() == itemStack.getItemDamage()) && ItemStack.areItemStackTagsEqual(inventoryItem, itemStack))
            {
                return i;
            }
        }

        return -1;
    }
}
