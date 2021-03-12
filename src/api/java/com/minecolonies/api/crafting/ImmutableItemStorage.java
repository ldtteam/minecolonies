package com.minecolonies.api.crafting;

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
    public void setAmount(final int amount)
    {
        throw new UnsupportedOperationException("Immutable instance of ItemStorage can't set value!");
    }
}
