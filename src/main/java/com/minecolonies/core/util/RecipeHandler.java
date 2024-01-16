package com.minecolonies.core.util;

/**
 * Recipe storage for minecolonies.
 */
public final class RecipeHandler
{

    /**
     * Private constructor to hide the implicit public one.
     */
    private RecipeHandler()
    {
    }

    /**
     * Initialize all recipes for minecolonies.
     *
     * @param enableInDevelopmentFeatures if we want development recipes.
     * @param supplyChests                if we want supply chests or direct town hall crafting.
     */
    public static void init(final boolean enableInDevelopmentFeatures, final boolean supplyChests)
    {
        // Register the hust
        //todo disable indev features
        //todo disable supplychest if config
    }
}
