package com.minecolonies.api.compatibility;

import net.minecraft.world.item.ItemStack;

import java.util.Collection;

/**
 * An interface (and placeholder) to intercept callouts to JEI when it is not running.
 */
public interface IJeiProxy
{
    /**
     * Check if JEI is loaded.
     * @return true if so.
     */
    default boolean isLoaded()
    {
        return false;
    }

    /**
     * Open JEI filtered to showing recipes for any of the specified items.
     * @param stacks the items to filter to.
     * @return true if JEI was shown.
     */
    default boolean showRecipes(final Collection<ItemStack> stacks)
    {
        return false;
    }
}
