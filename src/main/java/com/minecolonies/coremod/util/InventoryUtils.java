package com.minecolonies.coremod.util;

import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for the inventories.
 */
public class InventoryUtils
{
    /**
     * Private constructor to hide the implicit one.
     */
    private InventoryUtils()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Returns an inventory as list of item stacks.
     *
     * @param inventory Inventory to convert.
     * @return List of item stacks.
     */
    @NotNull
    public static List<ItemStack> getInventoryAsList(@NotNull final IInventory inventory)
    {
        @NotNull final ArrayList<ItemStack> filtered = new ArrayList<>();
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++)
        {
            final ItemStack stack = inventory.getStackInSlot(slot);
            if (stack != null && stack != ItemStack.EMPTY)
            {
                filtered.add(inventory.getStackInSlot(slot));
            }
        }
        return filtered;
    }

    /**
     * {@link #filterInventory(IInventory, Item)}.
     *
     * @param inventory Inventory to filter in
     * @param block     Block to filter
     * @return List of item stacks
     */
    @NotNull
    public static List<ItemStack> filterInventory(@NotNull final IInventory inventory, final Block block)
    {
        return filterInventory(inventory, getItemFromBlock(block));
    }

    /**
     * Filters a list of items, equal to given parameter, in an {@link IInventory}.
     *
     * @param inventory  Inventory to get items from
     * @param targetItem Item to look for
     * @return List of item stacks with the given item in inventory
     */
    @NotNull
    public static List<ItemStack> filterInventory(@NotNull final IInventory inventory, @Nullable final Item targetItem)
    {
        @NotNull final ArrayList<ItemStack> filtered = new ArrayList<>();
        if (targetItem == null)
        {
            return filtered;
        }
        //Check every inventory slot
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++)
        {
            final ItemStack stack = inventory.getStackInSlot(slot);
            if (compareItems(stack, targetItem))
            {
                filtered.add(stack);
            }
        }
        return filtered;
    }

    /**
     * Converts a Block to its Item so it can be compared.
     *
     * @param block the block to convert
     * @return an item from the registry
     */
    public static Item getItemFromBlock(final Block block)
    {
        return new ItemStack(block).getItem();
    }

    /**
     * Compares whether or not the item in an itemstack is equal to a given item.
     *
     * @param itemStack  ItemStack to check.
     * @param targetItem Item to check.
     * @return True when item in item stack is equal to target item.
     */
    private static boolean compareItems(@Nullable final ItemStack itemStack, final Item targetItem)
    {
        return itemStack != null && itemStack.getItem() == targetItem;
    }

    /**
     * Returns the index of the first occurrence of the block in the inventory.
     *
     * @param inventory Inventory to check.
     * @param block     Block to find.
     * @return Index of the first occurrence.
     */
    public static int findFirstSlotInInventoryWith(@NotNull final IInventory inventory, final Block block)
    {
        return findFirstSlotInInventoryWith(inventory, getItemFromBlock(block));
    }

    /**
     * {@link #findFirstSlotInInventoryWith(IInventory, Block)}.
     *
     * @param inventory  Inventory to check
     * @param targetItem Item to find
     * @return Index of the first occurrence
     */
    public static int findFirstSlotInInventoryWith(@NotNull final IInventory inventory, final Item targetItem)
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
     * {@link #getItemCountInInventory(IInventory, Item)}.
     *
     * @param inventory Inventory to scan
     * @param block     block to count
     * @return Amount of occurences
     */
    public static int getItemCountInInventory(@NotNull final IInventory inventory, final Block block)
    {
        return getItemCountInInventory(inventory, getItemFromBlock(block));
    }

    /**
     * Returns the amount of occurrences in the inventory.
     *
     * @param inventory  Inventory to scan
     * @param targetitem Item to count
     * @return Amount of occurences
     */
    public static int getItemCountInInventory(@NotNull final IInventory inventory, final Item targetitem)
    {
        int count = 0;
        for (@NotNull final ItemStack is : filterInventory(inventory, targetitem))
        {
            count += is.getCount();
        }
        return count;
    }

    /**
     * Checks if a player has an block in the inventory.
     * Checked by {@link #getItemCountInInventory(IInventory, Block)} &gt; 0;
     *
     * @param inventory Inventory to scan
     * @param block     Block to count
     * @return True when in inventory, otherwise false
     */
    public static boolean hasitemInInventory(@NotNull final IInventory inventory, final Block block)
    {
        return hasitemInInventory(inventory, getItemFromBlock(block));
    }


    //TODO: Check if this conversion is always safe
    //But seems like ItemStack does it right...

    /**
     * Checks if a player has an item in the inventory.
     * Checked by {@link #getItemCountInInventory(IInventory, Item)} &gt; 0;
     *
     * @param inventory Inventory to scan
     * @param item      Item to count
     * @return True when in inventory, otherwise false
     */
    public static boolean hasitemInInventory(@NotNull final IInventory inventory, final Item item)
    {
        return getItemCountInInventory(inventory, item) > 0;
    }

    /**
     * Returns if the inventory is full.
     *
     * @param inventory the inventory
     * @return true if the inventory is full
     */
    public static boolean isInventoryFull(@NotNull final IInventory inventory)
    {
        return getOpenSlot(inventory) == -1;
    }

    /**
     * returns first open slot in the inventory.
     *
     * @param inventory the inventory to check.
     * @return slot number or -1 if none found.
     */
    public static int getOpenSlot(@NotNull final IInventory inventory)
    {
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++)
        {
            if (inventory.getStackInSlot(slot) == ItemStack.EMPTY)
            {
                return slot;
            }
        }
        return -1;
    }

    /**
     * {@link #takeStackInSlot(IInventory, IInventory, int, int, boolean)}.
     * Default:
     * amount: 1
     * takeAll: true
     *
     * @param sendingInv   Inventory of sender
     * @param receivingInv Inventory of receiver
     * @param slotID       Slot ID to take from
     * @return True if item is swapped, otherwise false
     */
    public static boolean takeStackInSlot(final IInventory sendingInv, final IInventory receivingInv, final int slotID)
    {
        return takeStackInSlot(sendingInv, receivingInv, slotID, sendingInv.getStackInSlot(slotID).getCount(), true);
    }

    /**
     * Gives an item from an slot index from an inventory and puts it in a receiving inventory.
     * If <code>takeAll</code> is true, the entire slot will we transferred.
     * This only applied when at least <code>amount</code> can be taken.
     *
     * @param sendingInv   Inventory of sender
     * @param receivingInv Inventory of receiver
     * @param slotID       Slot ID to take from
     * @param amount       Amount to swap
     * @param takeAll      Whether or not the entire stack of the sender should be emptied if possible
     *                     Only applies when <code>amount</code> is sufficient
     * @return True if item is swapped, otherwise false
     */
    public static boolean takeStackInSlot(
                                           @Nullable final IInventory sendingInv, @Nullable final IInventory receivingInv,
                                           final int slotID, final int amount, final boolean takeAll)
    {
        if (receivingInv != null && sendingInv != null && slotID >= 0 && amount >= 0)
        {
            // gets itemstack in slot, and decreases stacksize
            @Nullable ItemStack stack = sendingInv.decrStackSize(slotID, amount);
            // stack is null if no itemstack was in slot
            if (stack != null && stack != ItemStack.EMPTY)
            {
                // puts stack in receiving inventory
                stack = setStack(receivingInv, stack);
                // checks for leftovers
                if (stack == null && stack != ItemStack.EMPTY)
                {
                    if (takeAll)
                    {
                        // gets itemstack in slot
                        stack = sendingInv.getStackInSlot(slotID);
                        // checks if itemstack is still in slot
                        if (stack != null && stack != ItemStack.EMPTY)
                        {
                            stack = sendingInv.decrStackSize(slotID, stack.getCount());
                            stack = setStack(receivingInv, stack);
                            setStack(sendingInv, stack);
                        }
                    }

                    // puts leftovers back in sending inventory
                    return true;
                }
                setStack(sendingInv, stack);
                return false;
            }
        }
        return false;
    }

    /**
     * Tries to put an item into Inventory.
     *
     * @param inventory the inventory to set the stack in.
     * @param stack     Item stack with items to be transferred
     * @return returns null if successful, or stack of remaining items
     */
    @Nullable
    public static ItemStack setStack(@NotNull final IInventory inventory, @Nullable final ItemStack stack)
    {
        if (stack != null && stack != ItemStack.EMPTY)
        {
            @Nullable ItemStack returnStack = stack.copy();
            int slot;
            while ((slot = containsPartialStack(inventory, stack)) != -1 && returnStack != null && returnStack != ItemStack.EMPTY)
            {
                final ItemStack current = inventory.getStackInSlot(slot);
                final int spaceLeft = current.getMaxStackSize() - current.getCount();
                if (spaceLeft > 0)
                {
                    @NotNull final ItemStack toBeAdded = returnStack.splitStack(Math.min(returnStack.getCount(), spaceLeft));
                    if (returnStack.getCount() == 0)
                    {
                        returnStack = ItemStack.EMPTY;
                    }
                    current.setCount(current.getCount() + toBeAdded.getCount());
                    inventory.setInventorySlotContents(slot, current);
                }
            }

            while ((slot = getOpenSlot(inventory)) != -1 && returnStack != null && returnStack != ItemStack.EMPTY)
            {
                inventory.setInventorySlotContents(slot, returnStack);
                if (returnStack.getCount() > inventory.getInventoryStackLimit())
                {
                    returnStack.setCount(returnStack.getCount() - inventory.getInventoryStackLimit());
                }
                else
                {
                    returnStack = ItemStack.EMPTY;
                }
            }
            return returnStack;
        }
        return ItemStack.EMPTY;
    }

    /**
     * Returns a slot number if a chest contains given ItemStack item that is not fully stacked.
     *
     * @param inventory the inventory to check.
     * @param stack     the stack to check for.
     * @return returns slot number if found, -1 when not found.
     */
    public static int containsPartialStack(@NotNull final IInventory inventory, final ItemStack stack)
    {
        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            final ItemStack testStack = inventory.getStackInSlot(i);
            if (testStack != null && testStack != ItemStack.EMPTY && testStack.isItemEqual(stack) && testStack.getCount() != testStack.getMaxStackSize())
            {
                return i;
            }
        }
        return -1;
    }

    /**
     * {@link #takeStackInSlot(IInventory, IInventory, int, int, boolean)}.
     * Default:
     * takeAll: false
     *
     * @param sendingInv   Inventory of sender
     * @param receivingInv Inventory of receiver
     * @param slotID       Slot ID to take from
     * @param amount       Amount to swap
     * @return True if item is swapped, otherwise false
     */
    public static boolean takeStackInSlot(final IInventory sendingInv, final IInventory receivingInv, final int slotID, final int amount)
    {
        return takeStackInSlot(sendingInv, receivingInv, slotID, amount, false);
    }

    /**
     * Returns all <code>ItemStack</code>s in an inventory.
     * Stores this in an array.
     *
     * @param inventory Inventory to return all item stacks from.
     * @return Array of item stacks.
     */
    @NotNull
    public static ItemStack[] getAllItemStacks(@NotNull final IInventory inventory)
    {
        @NotNull final ItemStack[] itemStack = new ItemStack[inventory.getSizeInventory()];
        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            itemStack[i] = inventory.getStackInSlot(i);
        }
        return itemStack;
    }

    /**
     * Returns the amount of item stacks in an inventory.
     * This equals {@link #getAllItemStacks(IInventory)}<code>.length();</code>.
     *
     * @param inventory Inventory to count item stacks of.
     * @return Amount of item stacks in inventory.
     */
    public static int getAmountOfStacks(@NotNull final IInventory inventory)
    {
        int count = 0;
        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            final ItemStack is = inventory.getStackInSlot(i);

            if (is != null && is != ItemStack.EMPTY)
            {
                count++;
            }
        }
        return count;
    }

    /**
     * Clears an entire inventory.
     *
     * @param inventory Inventory to clear.
     */
    public static void clear(@NotNull final IInventory inventory)
    {
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++)
        {
            inventory.setInventorySlotContents(slot, ItemStack.EMPTY);
        }
    }

    /**
     * Returns a slot number if an inventory contains given tool type.
     *
     * @param inventory the inventory to get the slot from.
     * @param tool      the tool type to look for.
     * @return slot number if found, -1 if not found.
     */
    public static int getFirstSlotContainingTool(@NotNull final IInventory inventory, @NotNull final String tool)
    {
        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            final ItemStack item = inventory.getStackInSlot(i);
            //Only classic fishingRod recognized as a fishingTool
            if (item != null && (item.getItem().getToolClasses(item).contains(tool) || ("hoe".equals(tool) && item.getUnlocalizedName().contains("hoe"))
                                   || ("rod".equals(tool) && item.getUnlocalizedName().contains("fishingRod"))))
            {
                return i;
            }
        }
        return -1;
    }

    /**
     * Adapted from {@link net.minecraft.entity.player.InventoryPlayer#addItemStackToInventory(ItemStack)}.
     *
     * @param inventory Inventory to add itemstack to.
     * @param itemStack ItemStack to add.
     * @return True if successful, otherwise false.
     */
    public static boolean addItemStackToInventory(@NotNull final IInventory inventory, @Nullable final ItemStack itemStack)
    {
        if (itemStack != null && itemStack.getCount() != 0 && itemStack.getItem() != null)
        {
            int stackSize;

            if (itemStack.isItemDamaged())
            {
                stackSize = getOpenSlot(inventory);

                if (stackSize >= 0)
                {
                    final ItemStack copy = itemStack.copy();
                    copy.setAnimationsToGo(5);
                    inventory.setInventorySlotContents(stackSize, copy);

                    itemStack.setCount(0);
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
                    stackSize = itemStack.getCount();
                    itemStack.setCount(storePartialItemStack(inventory, itemStack));
                }
                while (itemStack.getCount() > 0 && itemStack.getCount() < stackSize);


                return itemStack.getCount() < stackSize;
            }
        }
        else
        {
            return false;
        }
    }

    /**
     * Adapted from {@link net.minecraft.entity.player.InventoryPlayer#storePartialItemStack(ItemStack)}.
     * <p>
     * This function stores as many items of an ItemStack as possible in a matching slot and returns the quantity of
     * left over items.
     *
     * @param inventory Inventory to add stack to.
     * @param itemStack Item stack to store in inventory.
     * @return Leftover items in itemstack.
     */
    private static int storePartialItemStack(@NotNull final IInventory inventory, @NotNull final ItemStack itemStack)
    {
        final Item item = itemStack.getItem();
        int stackSize = itemStack.getCount();
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
                if (inventory.getStackInSlot(slot) == null && inventory.getStackInSlot(slot) != ItemStack.EMPTY)
                {
                    inventory.setInventorySlotContents(slot, itemStack.copy());
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
                if (stack == null || stack == ItemStack.EMPTY)
                {
                    stack = new ItemStack(item, 0, itemStack.getItemDamage());

                    if (itemStack.hasTagCompound())
                    {
                        stack.setTagCompound(itemStack.getTagCompound().copy());
                    }
                }

                int inventoryStackSpace = stackSize;

                if (stackSize > stack.getMaxStackSize() - stack.getCount())
                {
                    inventoryStackSpace = stack.getMaxStackSize() - stack.getCount();
                }

                if (inventoryStackSpace > inventory.getInventoryStackLimit() - stack.getCount())
                {
                    inventoryStackSpace = inventory.getInventoryStackLimit() - stack.getCount();
                }

                if (inventoryStackSpace == 0)
                {
                    return stackSize;
                }
                else
                {
                    stackSize -= inventoryStackSpace;
                    stack.setCount(stack.getCount() + inventoryStackSpace);
                    stack.setAnimationsToGo(5);
                    inventory.setInventorySlotContents(slot, stack);
                    return stackSize;
                }
            }
        }
    }

    /**
     * Adapted from {@link net.minecraft.entity.player.InventoryPlayer#storeItemStack(ItemStack)}.
     * <p>
     * find a slot to store an ItemStack in.
     *
     * @param inventory Inventory to look in.
     * @param itemStack Item Stack to look for.
     * @return Index of the item stack. If not found, returns -1.
     */
    private static int findSlotForItemStack(@NotNull final IInventory inventory, @NotNull final ItemStack itemStack)
    {
        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            final ItemStack inventoryItem = inventory.getStackInSlot(i);
            if (inventoryItem != null
                  && inventoryItem.getItem() == itemStack.getItem()
                  && inventoryItem.isStackable()
                  && inventoryItem.getCount() < inventoryItem.getMaxStackSize()
                  && inventoryItem.getCount() < inventory.getInventoryStackLimit()
                  && (!inventoryItem.getHasSubtypes() || inventoryItem.getItemDamage() == itemStack.getItemDamage())
                  && ItemStack.areItemStackTagsEqual(inventoryItem, itemStack))
            {
                return i;
            }
        }

        return -1;
    }
}
