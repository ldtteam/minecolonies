package com.minecolonies.coremod.colony.requestsystem;

import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.token.StandardTokenFactory;
import com.minecolonies.coremod.colony.requestsystem.requests.StandardRequestFactories;

/**
 * Initialization handler for the request system.
 */
public class RequestSystemInitializationHandler
{

    public void onPreInit()
    {
        //Standard Token system.
        StandardFactoryController.getInstance().registerNewFactory(new StandardTokenFactory());

        //Requestables
        StandardFactoryController.getInstance().registerNewFactory(new StandardRequestFactories.ItemStackFactory());
        StandardFactoryController.getInstance().registerNewFactory(new StandardRequestFactories.DeliveryFactory());
    }
}
