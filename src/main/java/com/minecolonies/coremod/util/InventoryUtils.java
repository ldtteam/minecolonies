package com.minecolonies.coremod.util;

import com.minecolonies.coremod.inventory.InventoryCitizen;
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

    public static final int FREE_TOOL_CHOICE_LEVEL   = 4;
    public static final int EFFECT_TOOL_CHOICE_LEVEL = 2;

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
            if (stack != null)
            {
                filtered.add(inventory.getStackInSlot(slot));
            }
        }
        return filtered;
    }

    /**
     * {@link #filterInventory(IInventory, Item, int)}.
     *
     * @param inventory Inventory to filter in
     * @param block     Block to filter
     * @param itemDamage the damage value.
     * @return List of item stacks
     */
    @NotNull
    public static List<ItemStack> filterInventory(@NotNull final IInventory inventory, final Block block, int itemDamage)
    {
        return filterInventory(inventory, getItemFromBlock(block), itemDamage);
    }

    /**
     * Filters a list of items, equal to given parameter, in an {@link IInventory}.
     *
     * @param inventory  Inventory to get items from
     * @param targetItem Item to look for
     * @param itemDamage the damage value.
     * @return List of item stacks with the given item in inventory
     */
    @NotNull
    public static List<ItemStack> filterInventory(@NotNull final IInventory inventory, @Nullable final Item targetItem, int itemDamage)
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
            if (compareItems(stack, targetItem, itemDamage))
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
     * @param itemDamage the item damage value.
     * @return True when item in item stack is equal to target item.
     */
    private static boolean compareItems(@Nullable final ItemStack itemStack, final Item targetItem, int itemDamage)
    {
        return itemStack != null && itemStack.getItem() == targetItem && (itemStack.getItemDamage() == itemDamage || itemDamage == -1);
    }

    /**
     * Returns the index of the first occurrence of the block in the inventory.
     *
     * @param inventory Inventory to check.
     * @param block     Block to find.
     * @param itemDamage the damage value.
     * @return Index of the first occurrence.
     */
    public static int findFirstSlotInInventoryWith(@NotNull final IInventory inventory, final Block block, int itemDamage)
    {
        return findFirstSlotInInventoryWith(inventory, getItemFromBlock(block), itemDamage);
    }

    /**
     * {@link #findFirstSlotInInventoryWith(IInventory, Block, int)}.
     *
     * @param inventory  Inventory to check
     * @param targetItem Item to find
     * @param itemDamage the damage value.
     * @return Index of the first occurrence
     */
    public static int findFirstSlotInInventoryWith(@NotNull final IInventory inventory, final Item targetItem, int itemDamage)
    {
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++)
        {
            if (compareItems(inventory.getStackInSlot(slot), targetItem, itemDamage))
            {
                return slot;
            }
        }
        return -1;
        //TODO: Later harden contract to remove compare on slot := -1
        //throw new IllegalStateException("Item "+targetItem.getUnlocalizedName() + " not found in Inventory!");
    }

    /**
     * {@link #getItemCountInInventory(IInventory, Item, int)}.
     *
     * @param inventory Inventory to scan
     * @param block     block to count
     * @param itemDamage the damage value
     * @return Amount of occurences
     */
    public static int getItemCountInInventory(@NotNull final IInventory inventory, final Block block, int itemDamage)
    {
        return getItemCountInInventory(inventory, getItemFromBlock(block), itemDamage);
    }

    /**
     * Returns the amount of occurrences in the inventory.
     *
     * @param inventory  Inventory to scan
     * @param targetitem Item to count
     * @param itemDamage the item damage value.
     * @return Amount of occurences
     */
    public static int getItemCountInInventory(@NotNull final IInventory inventory, final Item targetitem, int itemDamage)
    {
        int count = 0;
        for (@NotNull final ItemStack is : filterInventory(inventory, targetitem, itemDamage))
        {
            count += is.stackSize;
        }
        return count;
    }

    /**
     * Checks if a player has an block in the inventory.
     * Checked by {@link #getItemCountInInventory(IInventory, Block, int)} &gt; 0;
     *
     * @param inventory Inventory to scan
     * @param block     Block to count
     * @param itemDamage the damage value.
     * @return True when in inventory, otherwise false
     */
    public static boolean hasitemInInventory(@NotNull final IInventory inventory, final Block block, int itemDamage)
    {
        return hasitemInInventory(inventory, getItemFromBlock(block), itemDamage);
    }


    //TODO: Check if this conversion is always safe
    //But seems like ItemStack does it right...

    /**
     * Checks if a player has an item in the inventory.
     * Checked by {@link #getItemCountInInventory(IInventory, Item, int)} &gt; 0;
     *
     * @param inventory Inventory to scan
     * @param item      Item to count
     * @param itemDamage the damage value of the item.
     * @return True when in inventory, otherwise false
     */
    public static boolean hasitemInInventory(@NotNull final IInventory inventory, final Item item, int itemDamage)
    {
        return getItemCountInInventory(inventory, item, itemDamage) > 0;
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
            if (inventory.getStackInSlot(slot) == null)
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
        return takeStackInSlot(sendingInv, receivingInv, slotID, 1, true);
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
            if (stack != null)
            {
                // puts stack in receiving inventory
                stack = setStack(receivingInv, stack);
                // checks for leftovers
                if (stack == null)
                {
                    if (takeAll)
                    {
                        // gets itemstack in slot
                        stack = sendingInv.getStackInSlot(slotID);
                        // checks if itemstack is still in slot
                        if (stack != null)
                        {
                            stack = sendingInv.decrStackSize(slotID, stack.stackSize);
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
        if (stack != null)
        {
            @Nullable ItemStack returnStack = stack.copy();
            int slot;
            while ((slot = containsPartialStack(inventory, stack)) != -1 && returnStack != null)
            {
                final ItemStack current = inventory.getStackInSlot(slot);
                final int spaceLeft = current.getMaxStackSize() - current.stackSize;
                if (spaceLeft > 0)
                {
                    @NotNull final ItemStack toBeAdded = returnStack.splitStack(Math.min(returnStack.stackSize, spaceLeft));
                    if (returnStack.stackSize == 0)
                    {
                        returnStack = null;
                    }
                    current.stackSize += toBeAdded.stackSize;
                    inventory.setInventorySlotContents(slot, current);
                }
            }

            while ((slot = getOpenSlot(inventory)) != -1 && returnStack != null)
            {
                inventory.setInventorySlotContents(slot, returnStack);
                if (returnStack.stackSize > inventory.getInventoryStackLimit())
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
            if (testStack != null && testStack.isItemEqual(stack) && testStack.stackSize != testStack.getMaxStackSize())
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

            if (is != null)
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
            inventory.setInventorySlotContents(slot, null);
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
        if (itemStack != null && itemStack.stackSize != 0 && itemStack.getItem() != null)
        {
            int stackSize;

            if (itemStack.isItemDamaged())
            {
                stackSize = getOpenSlot(inventory);

                if (stackSize >= 0)
                {
                    final ItemStack copy = ItemStack.copyItemStack(itemStack);
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
                        stack.setTagCompound(itemStack.getTagCompound().copy());
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

    /**
     * Verifies if there is one tool with an acceptable level
     * in a worker's inventory.
     *
     * @param tool      the type of tool needed
     * @param inventory the worker's inventory
     * @param hutLevel  the worker's hut level
     * @return true if tool is acceptable
     */
    public static boolean hasToolLevel(final String tool, @NotNull final InventoryCitizen inventory, final int hutLevel)
    {
        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            final ItemStack item = inventory.getStackInSlot(i);
            final int level = Utils.getMiningLevel(item, tool);

            if (Utils.isTool(item, tool) && verifyToolLevel(item, level, hutLevel))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifies if an item has an appropriated grade.
     *
     * @param item     the type of tool needed
     * @param level    the type of tool needed
     * @param hutLevel the worker's hut level
     * @return true if tool is acceptable
     */
    public static boolean verifyToolLevel(final ItemStack item, int level, final int hutLevel)
    {
        if (item == null || hutLevel > FREE_TOOL_CHOICE_LEVEL)
        {
            return true;
        }
        else if (item.isItemEnchanted() && hutLevel <= EFFECT_TOOL_CHOICE_LEVEL)
        {
            return false;
        }
        else if (hutLevel >= level)
        {
            return true;
        }

        return false;
    }

    /**
     * Assigns a string containing the grade of the toolGrade.
     *
     * @param toolGrade the number of the grade of a tool
     * @return a string corresponding to the tool
     */
    public static String swapToolGrade(final int toolGrade)
    {
        switch (toolGrade)
        {
            case 0:
                return "Wood or Gold";
            case 1:
                return "Stone";
            case 2:
                return "Iron";
            case 3:
                return "Diamond";
            default:
                return "";
        }
    }
}
