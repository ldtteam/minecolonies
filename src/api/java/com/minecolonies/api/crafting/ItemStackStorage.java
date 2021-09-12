package com.minecolonies.api.crafting;

import org.jetbrains.annotations.NotNull;

import net.minecraft.item.ItemStack;

public class ItemStackStorage extends ItemStorage
{
    /**
     * Creates an instance of the storage.
     *
     * @param stack the stack.
     */
    public ItemStackStorage(@NotNull final ItemStack stack)
    {
        super(stack);
    }

    /**
     * Creates an instance of the storage.
     *
     * @param stack the stack.
     * @param qty   the size of the stack
     */
    public ItemStackStorage(@NotNull final ItemStack stack, int qty)
    {
        super(stack);
        this.setAmount(qty);
    }

    @Override
    public ItemStorage copy()
    {
        return new ItemStackStorage(stack.copy(), amount);
    }    
}
