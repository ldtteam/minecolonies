package com.minecolonies.api.inventory.api;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This delegates to an underlying IItemHandler, but records which stacks were inserted for later inspection.
 * It can be used to determine the final resulting stacks of a crafting operation, for example.
 */
public class RecordingItemHandler implements IItemHandler
{
    private final IItemHandler underlying;
    private final List<ItemStack> inserted;

    public RecordingItemHandler(@NotNull final IItemHandler underlying)
    {
        this.underlying = underlying;
        this.inserted = new ArrayList<>();
    }

    /**
     * Gets a copy of the stacks that have been inserted into this handler.
     * @return The inserted stacks.
     */
    @NotNull
    public List<ItemStack> getInserted()
    {
        return Collections.unmodifiableList(inserted);
    }

    @Override
    public int getSlots()
    {
        return underlying.getSlots();
    }

    @Override
    public int getSlotLimit(int slot)
    {
        return underlying.getSlotLimit(slot);
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot)
    {
        return underlying.getStackInSlot(slot);
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate)
    {
        if (!simulate)
        {
            inserted.add(stack.copy());
        }
        return underlying.insertItem(slot, stack, simulate);
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        return underlying.extractItem(slot, amount, simulate);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack)
    {
        return underlying.isItemValid(slot, stack);
    }
}
