package com.minecolonies.coremod.colony.requestsystem.init;

import com.minecolonies.api.colony.requestsystem.StandardRequestManager;
import com.minecolonies.api.colony.requestsystem.requestable.*;
import com.minecolonies.coremod.colony.requestsystem.requests.StandardRequests;

public class RequestSystemInitializer
{

    public static void onPreInit()
    {
        StandardRequestManager.registerRequestableTypeMapping(Stack.class, StandardRequests.ItemStackRequest.class);
        StandardRequestManager.registerRequestableTypeMapping(Burnable.class, StandardRequests.BurnableRequest.class);
        StandardRequestManager.registerRequestableTypeMapping(Delivery.class, StandardRequests.DeliveryRequest.class);
        StandardRequestManager.registerRequestableTypeMapping(Food.class, StandardRequests.FoodRequest.class);
        StandardRequestManager.registerRequestableTypeMapping(Tool.class, StandardRequests.ToolRequest.class);
    }
}
