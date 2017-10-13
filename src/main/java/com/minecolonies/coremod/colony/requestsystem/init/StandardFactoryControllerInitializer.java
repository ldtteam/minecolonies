package com.minecolonies.coremod.colony.requestsystem.init;

import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.token.InitializedTokenFactory;
import com.minecolonies.api.colony.requestsystem.token.StandardTokenFactory;
import com.minecolonies.coremod.colony.requestsystem.locations.EntityLocation;
import com.minecolonies.coremod.colony.requestsystem.locations.StaticLocation;
import com.minecolonies.coremod.colony.requestsystem.requesters.factories.BuildingBasedRequesterFactory;
import com.minecolonies.coremod.colony.requestsystem.requests.StandardRequestFactories;

/**
 * Initializer for the {@link StandardFactoryControllerInitializer}
 */
public final class StandardFactoryControllerInitializer
{

    /**
     * Private constructor to hide the implicit public one.
     */
    private StandardFactoryControllerInitializer()
    {
    }

    public static void onPreInit()
    {
        StandardFactoryController.getInstance().registerNewFactory(new StandardTokenFactory());
        StandardFactoryController.getInstance().registerNewFactory(new InitializedTokenFactory());
        StandardFactoryController.getInstance().registerNewFactory(new StaticLocation.Factory());
        StandardFactoryController.getInstance().registerNewFactory(new EntityLocation.Factory());
        StandardFactoryController.getInstance().registerNewFactory(new StandardRequestFactories.ItemStackFactory());
        StandardFactoryController.getInstance().registerNewFactory(new StandardRequestFactories.DeliveryFactory());
        StandardFactoryController.getInstance().registerNewFactory(new StandardRequestFactories.ToolFacatory());
        StandardFactoryController.getInstance().registerNewFactory(new BuildingBasedRequesterFactory());
    }
}
