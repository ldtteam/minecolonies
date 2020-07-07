package com.minecolonies.api.colony.requestsystem.data;

import com.google.common.collect.BiMap;

/**
 * {@link IDataStore} definition for a KeyValue-Store.
 *
 * @param <K> The key type for the KV-Store.
 * @param <V> The value type for the KV-Store.
 */
public interface IIdentitiesDataStore<K, V> extends IDataStore
{

    /**
     * Method to get the identities stored in this {@link IIdentitiesDataStore}
     *
     * @return The identities.
     */
    BiMap<K, V> getIdentities();
}
