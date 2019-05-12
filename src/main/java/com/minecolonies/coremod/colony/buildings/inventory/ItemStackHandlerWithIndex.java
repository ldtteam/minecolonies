package com.minecolonies.coremod.colony.buildings.inventory;

import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.Predicate;

/**
 * ItemStackHandler which also keeps an index of its contents.
 * It also can push any changes to a meta(upper) index, to combine multiple handlers.
 *
 * @param <Value> The type of the meta index, not required.
 */
public class ItemStackHandlerWithIndex<Value> extends ItemStackHandler
{
    /**
     * Index for stored Items and their slot.
     */
    @NotNull
    private ItemStorageIndex<Integer> index;

    /**
     * A meta index which is to be updated. Does not have to be set.
     */
    @Nullable
    private ItemStorageIndex<Value> meta;

    /**
     * The reference to put into the meta index.
     */
    @Nullable
    private Value metaIndexValue;

    /**
     * Counting used slots for storage
     */
    private int usedSlots = 0;

    /**
     * Create a new ItemStackHandlerWithIndex
     *
     * @param size Size of the handler to create
     */
    public ItemStackHandlerWithIndex(int size)
    {
        super(size);
        index = new ItemStorageIndex<>();
    }

    /**
     * Sets the meta index and the reference value for it
     */
    public void setMetaIndex(ItemStorageIndex<Value> meta, Value metaIndexValue)
    {
        if (meta != null && this.meta != meta)
        {
            this.meta = meta;
            this.metaIndexValue = metaIndexValue;

            // Set the handlers content for the meta index
            for (ItemStorage store : index.getIndexMap().keySet())
            {
                meta.addToIndex(store, metaIndexValue);
            }
        }
    }

    /**
     * Clears the meta index reference and its content.
     */
    public void clearMetaIndex()
    {
        if (meta == null)
        {
            return;
        }

        // Remove stored items in the meta index
        for (ItemStorage store : index.getIndexMap().keySet())
        {
            meta.removeFromIndex(store, metaIndexValue);
        }

        // Remove references
        meta = null;
        metaIndexValue = null;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack pstack)
    {
        if (!isValidSlot(slot))
        {
            return;
        }

        setStorageInSlot(slot, new ItemStorage(pstack));
    }

    /**
     * Adds the storage to the index and the stack to the inventory.
     *
     * @param slot    Slot the item is added at
     * @param storage ItemStorage to add
     */
    public void setStorageInSlot(Integer slot, ItemStorage storage)
    {
        if (!isValidSlot(slot))
        {
            return;
        }

        // Overwriting old stack
        if (!ItemStackUtils.isEmpty(getStackInSlot(slot)))
        {
            ItemStorage store = new ItemStorage(getStackInSlot(slot));
            index.removeFromIndex(store, slot);

            if (meta != null)
            {
                meta.removeFromIndex(store, metaIndexValue);
            }
            usedSlots--;
        }

        // Some operations on forge side do not use extractitem cleanly, but instead use it to simulate and
        // then set the slot to empty via this call, so gotta support setting empty stacks.
        if (!ItemStackUtils.isEmpty(storage.getItemStack()))
        {
            // Adding stack to indexes
            index.addToIndex(storage, slot);

            if (meta != null)
            {
                meta.addToIndex(storage, metaIndexValue);
            }
            usedSlots++;
        }
        super.setStackInSlot(slot, storage.getItemStack());
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
    {
        if (!isValidSlot(slot) || ItemStackUtils.isEmpty(stack))
        {
            return stack;
        }

        int insert = getStackLimit(slot, stack);

        if (getStackInSlot(slot) != ItemStack.EMPTY)
        {
            if (!getStackInSlot(slot).equals(stack))
            {
                return stack;
            }
            insert -= getStackInSlot(slot).getCount();
        }

        // The actual amount we're inserting.
        insert = Math.min(insert, stack.getCount());

        if (insert <= 0)
        {
            return stack;
        }

        // Return the remainder right away when simulating
        if (simulate)
        {
            ItemStack returnStack = stack.copy();
            returnStack.setCount(stack.getCount() - insert);
            return returnStack;
        }

        // Modify the inventory contents
        if (getStackInSlot(slot) != ItemStack.EMPTY)
        {
            getStackInSlot(slot).grow(insert);
            onContentsChanged(slot);

            if (stack.getCount() - insert <= 0)
            {
                return ItemStack.EMPTY;
            }

            // Return the remainder
            ItemStack remainder = stack.copy();
            remainder.setCount(stack.getCount() - insert);

            return remainder;
        }
        else
        {
            setStackInSlot(slot, stack);
            return ItemStack.EMPTY;
        }
    }

    @Nonnull
    @Override
    public ItemStack extractItem(final int slot, final int amount, final boolean simulate)
    {
        if (!isValidSlot(slot) || ItemStackUtils.isEmpty(getStackInSlot(slot)))
        {
            return ItemStack.EMPTY;
        }


        int toExtract = Math.min(amount, getStackInSlot(slot).getCount());

        if (simulate)
        {
            ItemStack simulateStack = getStackInSlot(slot).copy();
            simulateStack.setCount(toExtract);
            return simulateStack;
        }

        ItemStack returnStack;
        // Returning whole stack
        if (toExtract >= getStackInSlot(slot).getCount())
        {

            returnStack = getStackInSlot(slot);
            index.removeFromIndex(new ItemStorage(returnStack), slot);
            if (meta != null)
            {
                meta.removeFromIndex(new ItemStorage(returnStack), metaIndexValue);
            }
            this.stacks.set(slot, ItemStack.EMPTY);
            usedSlots--;
        }
        else
        {
            getStackInSlot(slot).setCount(getStackInSlot(slot).getCount() - toExtract);
            returnStack = getStackInSlot(slot).copy();
            returnStack.setCount(toExtract);
        }
        onContentsChanged(slot);
        return returnStack;
    }

    /**
     * Get the index of this stackhandler, contains Itemstorage and the slot where it is at.
     */
    public ItemStorageIndex<Integer> getIndex()
    {
        return index;
    }

    /**
     * Check whether the inventory is empty.
     */
    public boolean isEmpty()
    {
        return index.getIndexMap().isEmpty();
    }

    /**
     * Returns a set of slots which contain the given item. Null if none
     */
    public Set<Integer> getSlotsForItem(ItemStorage item)
    {
        return index.getValueForKey(item);
    }

    /**
     * Gets a list of slots which match the given ItemStackPredicate.
     *
     * @param itemStackSelectionPredicate predicate to filter stacks with.
     * @return List of Inventory slots containing stacks which match the predicate.
     */
    public Set<Integer> getSlotsForItemStackPredicate(@NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        return index.getValueForItemStackPredicate(itemStackSelectionPredicate);
    }

    /**
     * Increases the size of the inventory.
     *
     * @param newSize new size to set
     */
    public void increaseSizeTo(int newSize)
    {
        if (getSlots() >= newSize)
        {
            return;
        }

        NonNullList<ItemStack> tempList = NonNullList.withSize(newSize, ItemStack.EMPTY);

        for (ItemStack stack : stacks)
        {
            tempList.add(stack);
        }

        stacks = tempList;
    }

    public int getUsedSlots()
    {
        return usedSlots;
    }

    @Override
    protected void validateSlotIndex(int slot)
    {
    }

    /**
     * Checks whether the slot is valid.
     *
     * @param slot Given Inventory slot.
     * @return True/false
     */
    public boolean isValidSlot(int slot)
    {
        if (slot < 0 || slot >= stacks.size())
        {
            return false;
        }
        return true;
    }
}
