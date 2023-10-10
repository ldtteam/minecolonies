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
     * @param stack the stack.
     */
    public ExactMatchItemStorage(@NotNull final ItemStack stack)
    {
        super(stack);
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