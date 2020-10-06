package com.minecolonies.api.crafting;

import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Immutable ItemStorage version.
 */
public class ImmutableItemStorage extends ItemStorage
{
    /**
     * Creates an instance of the storage.
     *
     * @param storage the mutable itemstorage to create it from.
     */
    public ImmutableItemStorage(@NotNull final ItemStorage storage)
    {
        super(storage.getItemStack(), storage.ignoreDamageValue(), storage.ignoreNBT());
        super.setAmount(storage.getAmount());
    }

    @Override
    public ItemStack getItemStack()
    {
        return super.getItemStack().copy();
    }

    @Override
    public void setAmount(final int amount)
    {
        throw new UnsupportedOperationException("Immutable instance of ItemStorage can't set value!");
    }
}
