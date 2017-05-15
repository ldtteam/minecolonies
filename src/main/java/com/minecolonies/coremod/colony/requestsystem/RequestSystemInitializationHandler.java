package com.minecolonies.coremod.colony.requestsystem;

import com.minecolonies.coremod.colony.requestsystem.requests.StandardRequestFactories;
import com.minecolonies.coremod.colony.requestsystem.token.StandardTokenFactory;

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
