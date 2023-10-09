package com.minecolonies.api.crafting;

import com.google.gson.JsonObject;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Used to exact match stacks when storing them.
 */
public class ExactMatchItemStorage extends ItemStorage
{
    /**
     * Creates an instance of the storage.
     *
     * @param stack             the stack.
     * @param amount            the amount.
     * @param ignoreDamageValue should the damage value be ignored?
     */
    public ExactMatchItemStorage(@NotNull final ItemStack stack, final int amount, final boolean ignoreDamageValue)
    {
        super(stack, amount, ignoreDamageValue);
    }

    /**
     * Creates an instance of the storage.
     *
     * @param stack                the stack.
     * @param ignoreDamageValue    should the damage value be ignored?
     * @param shouldIgnoreNBTValue should the nbt value be ignored?
     */
    public ExactMatchItemStorage(@NotNull final ItemStack stack, final boolean ignoreDamageValue, final boolean shouldIgnoreNBTValue)
    {
        super(stack, ignoreDamageValue, shouldIgnoreNBTValue);
    }

    /**
     * Creates an instance of the storage.
     *
     * @param stack             the stack.
     * @param ignoreDamageValue should the damage value be ignored?
     */
    public ExactMatchItemStorage(@NotNull final ItemStack stack, final boolean ignoreDamageValue)
    {
        super(stack, ignoreDamageValue);
    }

    /**
     * Creates an instance of the storage.
     *
     * @param stack the stack.
     */
    public ExactMatchItemStorage(@NotNull final ItemStack stack)
    {
        super(stack);
    }

    /**
     * Creates an instance of the storage from JSON
     *
     * @param jObject the JSON Object to parse
     */
    public ExactMatchItemStorage(@NotNull final JsonObject jObject)
    {
        super(jObject);
    }

    @Override
    public boolean equals(final Object comparisonObject)
    {
        if (this == comparisonObject)
        {
            return true;
        }
        if (comparisonObject instanceof final ExactMatchItemStorage that)
        {
            return ItemStackUtils.compareItemStacksIgnoreStackSize(that.getItemStack(), this.getItemStack(), !(super.shouldIgnoreDamageValue || that.shouldIgnoreDamageValue), !(this.shouldIgnoreNBTValue || that.shouldIgnoreNBTValue), false, true);

        }
        return false;
    }
}