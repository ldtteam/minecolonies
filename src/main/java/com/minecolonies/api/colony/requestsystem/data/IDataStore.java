package com.minecolonies.api.colony.requestsystem.data;

import com.minecolonies.api.colony.requestsystem.token.IToken;

/**
 * Core class that describes the datastores that are part of the RS.
 */
public interface IDataStore
{
    /**
     * Method to get the {@link IToken} used to identify this {@link IDataStore}.
     *
     * @return The {@link IToken} for this {@link IDataStore}
     */
    IToken<?> getId();

    /**
     * Method to set the {@link IToken} used to identify this {@link IDataStore}. Used during creation to setup the initial Id.
     *
     * @param id The new Id.
     */
    void setId(IToken<?> id);
}
