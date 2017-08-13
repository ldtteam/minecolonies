package com.minecolonies.coremod.craftingsystem;

import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.token.StandardTokenFactory;

/**
 * Initialization handler for the crafting system.
 */
public final class CraftingSystemInitializationHandler
{
    /**
     * Private constructor to hide implicit one.
     */
    private CraftingSystemInitializationHandler()
    {
        /**
         * Intentionally left empty.
         */
    }

    public static void onPreInit()
    {
        //Standard Token system.
        StandardFactoryController.getInstance().registerNewFactory(new StandardTokenFactory());
    }
}
