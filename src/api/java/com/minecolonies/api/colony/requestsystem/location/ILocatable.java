package com.minecolonies.api.colony.requestsystem.location;

import org.jetbrains.annotations.NotNull;

/**
 * Interface describing objects that are locatable.
 */
public interface ILocatable
{

    /**
     * Getter to get the location of this locatable.
     * @return The location of the locatable.
     */
    @NotNull
    ILocation getLocation();
}
