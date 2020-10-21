package com.minecolonies.api.colony.requestsystem.data;

import com.minecolonies.api.colony.requestsystem.token.IToken;

import java.util.LinkedList;
import java.util.Set;

/**
 * Specific datastore for couriers.
 */
public interface IRequestSystemDeliveryManJobDataStore extends IDataStore
{
    /**
     * Get the list of all scheduled deliveries.
     * @return the ordered list.
     */
    LinkedList<IToken<?>> getQueue();

    /**
     * Get a list of all the currently ongoing deliveries.
     * @return the list.
     */
    Set<IToken<?>> getOngoingDeliveries();
}
