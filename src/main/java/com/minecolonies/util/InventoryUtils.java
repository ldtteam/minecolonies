package com.minecolonies.util;

import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;


public class InventoryUtils {

    /**
     * Filters a list of items, equal to given parameter, in an {@link IInventory}
     *
     * @param inventory     Inventory to get items from
     * @param targetItem    Item to look for
     * @return              List of item stacks with the given item in inventory
     */
    public static List<ItemStack> filterInventory(IInventory inventory, Item targetItem){
        ArrayList<ItemStack> filtered = new ArrayList<>();
        if(targetItem == null){
            return filtered;
        }
        //Check every inventory slot
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++){
            ItemStack stack = inventory.getStackInSlot(slot);
            if(compareItems(stack,targetItem)){
                filtered.add(stack);
            }
        }
        return filtered;
    }

    /**
     * Returns an inventory as list of item stacks
     *
     * @param inventory     Inventory to convert
     * @return              List of item stacks
     */
    public static List<ItemStack> getInventoryAsList(IInventory inventory)
    {
        ArrayList<ItemStack> filtered = new ArrayList<>();
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++)
        {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack != null)
            {
                filtered.add(inventory.getStackInSlot(slot));
            }
        }
        return filtered;
    }

    /**
     * @see {@link #filterInventory(IInventory, Item)}
     *
     * @param inventory     Inventory to filter in
     * @param block         Block to filter
     * @return              List of item stacks
     */
    public static List<ItemStack> filterInventory(IInventory inventory, Block block)
    {
        return filterInventory(inventory, getItemFromBlock(block));
    }

    /**
     * Compares whether or not the item in an itemstack is equal to a given item
     *
     * @param itemStack     ItemStack to check
     * @param targetItem    Item to check
     * @return              True when item in item stack is equal to target item
     */
    private static boolean compareItems(ItemStack itemStack, Item targetItem)
    {
        return itemStack != null && itemStack.getItem() == targetItem;
    }

    /**
     * Returns the index of the first occurrence of the block in the inventory
     *
     * @param inventory     Inventory to check
     * @param block         Block to find
     * @return              Index of the first occurrence
     */
    public static int findFirstSlotInInventoryWith(IInventory inventory, Block block)
    {
        return findFirstSlotInInventoryWith(inventory, getItemFromBlock(block));
    }

    /**
     * @see {@link #findFirstSlotInInventoryWith(IInventory, Block)}
     *
     * @param inventory     Inventory to check
     * @param targetItem    Item to find
     * @return              Index of the first occurrence
     */
    public static int findFirstSlotInInventoryWith(IInventory inventory, Item targetItem)
    {
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++)
        {
            if (compareItems(inventory.getStackInSlot(slot), targetItem))
            {
                return slot;
            }
        }
        return -1;
        //TODO: Later harden contract to remove compare on slot := -1
        //throw new IllegalStateException("Item "+targetItem.getUnlocalizedName() + " not found in Inventory!");
    }

    /**
     * Returns the amount of occurrences in the inventory
     *
     * @param inventory     Inventory to scan
     * @param targetitem    Item to count
     * @return              Amount of occurences
     */
    public static int getItemCountInInventory(IInventory inventory, Item targetitem)
    {
        int count = 0;
        for (ItemStack is : filterInventory(inventory, targetitem))
        {
            count += is.stackSize;
        }
        return count;
    }

    /**
     * @see {@link #getItemCountInInventory(IInventory, Item)}
     *
     * @param inventory     Inventory to scan
     * @param block         block to count
     * @return              Amount of occurences
     */
    public static int getItemCountInInventory(IInventory inventory, Block block){
        return getItemCountInInventory(inventory, getItemFromBlock(block));
    }

    /**
     * Checks if a player has an item in the inventory
     * Checked by {@link #getItemCountInInventory(IInventory, Item)} > 0;
     *
     * @param inventory     Inventory to scan
     * @param item          Item to count
     * @return              True when in inventory, otherwise false
     */
    public static boolean hasitemInInventory(IInventory inventory, Item item){
        return getItemCountInInventory(inventory, item)>0;
    }

    /**
     * Checks if a player has an block in the inventory
     * Checked by {@link #getItemCountInInventory(IInventory, Block)} > 0;
     *
     * @param inventory     Inventory to scan
     * @param block         Block to count
     * @return              True when in inventory, otherwise false
     */
    public static boolean hasitemInInventory(IInventory inventory, Block block)
    {
        return hasitemInInventory(inventory, getItemFromBlock(block));
    }


    //TODO: Check if this conversion is always safe
    //But seems like ItemStack does it right...
    /**
     * Converts a Block to its Item so it can be compared.
     *
     * @param block the block to convert
     * @return      an item from the registry
     */
    public static Item getItemFromBlock(Block block){
        return new ItemStack(block).getItem();
    }

    /**
     * Tries to put an item into Inventory
     *
     * @param stack Item stack with items to be transferred
     * @return      returns null if successful, or stack of remaining items
     */
    public static ItemStack setStack(IInventory inventory, ItemStack stack)
    {
        if (stack != null)
        {
            ItemStack returnStack = stack.copy();
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
     * @return      slot number or -1 if none found.
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
     * @return      returns slot number if found, -1 when not found.
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
     * @return      returns slot number if found, -1 when not found.
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

    /**
     * @see {@link #takeStackInSlot(IInventory, IInventory, int, int, boolean)}
     * Default:
     *      amount: 1
     *      takeAll: true
     *
     * @param sendingInv        Inventory of sender
     * @param receivingInv      Inventory of receiver
     * @param slotID            Slot ID to take from
     * @return                  True if item is swapped, otherwise false
     */
    public static boolean takeStackInSlot(IInventory sendingInv, IInventory receivingInv, int slotID){
        return takeStackInSlot(sendingInv, receivingInv, slotID, 1, true);
    }

    /**
     * @see {@link #takeStackInSlot(IInventory, IInventory, int, int, boolean)}
     * Default:
     *      takeAll: false
     *
     * @param sendingInv        Inventory of sender
     * @param receivingInv      Inventory of receiver
     * @param slotID            Slot ID to take from
     * @param amount            Amount to swap
     * @return                  True if item is swapped, otherwise false
     */
    public static boolean takeStackInSlot(IInventory sendingInv, IInventory receivingInv, int slotID, int amount)
    {
        return takeStackInSlot(sendingInv, receivingInv, slotID, amount, false);
    }

    /**
     * Gives an item from an slot index from an inventory and puts it in a receiving inventory
     * If <code>takeAll</code> is true, the entire slot will we transferred.
     * This only applied when at least <code>amount</code> can be taken.
     *
     * @param sendingInv        Inventory of sender
     * @param receivingInv      Inventory of receiver
     * @param slotID            Slot ID to take from
     * @param amount            Amount to swap
     * @param takeAll           Whether or not the entire stack of the sender should be emptied if possible
     *                          Only applies when <code>amount</code> is sufficient
     * @return                  True if item is swapped, otherwise false
     */
    public static boolean takeStackInSlot(IInventory sendingInv, IInventory receivingInv,
                                          int slotID, int amount, boolean takeAll)
    {
        if(receivingInv != null && sendingInv != null && slotID >= 0 && amount >= 0)
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

    /**
     * Returns all <code>ItemStack</code>s in an inventory.
     * Stores this in an array
     *
     * @param inventory     Inventory to return all item stacks from
     * @return              Array of item stacks
     */
    public static ItemStack[] getAllItemStacks(IInventory inventory)
    {
        ItemStack[] itemStack = new ItemStack[inventory.getSizeInventory()];
        for(int i = 0; i < inventory.getSizeInventory(); i++)
        {
            itemStack[i] = inventory.getStackInSlot(i);
        }
        return itemStack;
    }

    /**
     * Returns the amount of item stacks in an inventory
     * This equals {@link #getAllItemStacks(IInventory)}<code>.length();</code>
     *
     * @param inventory     Inventory to count item stacks of
     * @return              Amount of item stacks in inventory
     */
    public static int getAmountOfStacks(IInventory inventory)
    {
        int count = 0;
        for(int i = 0; i < inventory.getSizeInventory(); i++)
        {
            ItemStack is = inventory.getStackInSlot(i);

            if(is != null)
            {
                count++;
            }
        }
        return count;
    }

    /**
     * Clears an entire inventory
     *
     * @param inventory     Inventory to clear
     */
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
     * @return      slot number if found, -1 if not found.
     */
    public static int getFirstSlotContainingTool(IInventory inventory, String tool)
    {
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack item = inventory.getStackInSlot(i);
            //Only classic fishingRod recognized as a fishingTool
            if (item != null && (item.getItem().getToolClasses(item).contains(tool) || (tool.equals("hoe") && item.getUnlocalizedName().contains("hoe")) || (tool.equals("rod") && item.getUnlocalizedName().contains("fishingRod"))))
            {
                return i;
            }
        }
        return -1;
    }

    /**
     * Adapted from {@link net.minecraft.entity.player.InventoryPlayer#addItemStackToInventory(ItemStack)}
     *
     * @param inventory     Inventory to add itemstack to
     * @param itemStack     ItemStack to add
     * @return              True if successful, otherwise false
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
     * Adapted from {@link net.minecraft.entity.player.InventoryPlayer#storePartialItemStack(ItemStack)}
     * <p>
     * This function stores as many items of an ItemStack as possible in a matching slot and returns the quantity of
     * left over items.
     *
     * @param inventory     Inventory to add stack to
     * @param itemStack     Item stack to store in inventory
     * @return              Leftover items in itemstack
     */
    private static int storePartialItemStack(IInventory inventory, ItemStack itemStack)
    {
        Item item      = itemStack.getItem();
        int  stackSize = itemStack.stackSize;
        int  slot;

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
     * Adapted from {@link net.minecraft.entity.player.InventoryPlayer#storeItemStack(ItemStack)}
     * <p>
     * find a slot to store an ItemStack in
     *
     * @param inventory     Inventory to look in
     * @param itemStack     Item Stack to look for
     * @return              Index of the item stack. If not found, returns -1
     */
    private static int findSlotForItemStack(IInventory inventory, ItemStack itemStack)
    {
        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            ItemStack inventoryItem = inventory.getStackInSlot(i);
            if (inventoryItem != null
                && inventoryItem.getItem() == itemStack.getItem()
                && inventoryItem.isStackable()
                && inventoryItem.stackSize < inventoryItem.getMaxStackSize()
                && inventoryItem.stackSize < inventory.getInventoryStackLimit()
                && (!inventoryItem.getHasSubtypes() || inventoryItem.getItemDamage() == itemStack.getItemDamage())
                && ItemStack.areItemStackTagsEqual(inventoryItem, itemStack))
            {
                return i;
            }
        }

        return -1;
    }
}
