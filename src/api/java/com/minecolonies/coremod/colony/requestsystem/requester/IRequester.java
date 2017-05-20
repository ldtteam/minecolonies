package com.minecolonies.coremod.colony.requestsystem.location;

import org.jetbrains.annotations.NotNull;

/**
 * Interface that describes an object that can be located in the Minecraft universe.
 */
public interface ILocatable {

    /**
     * Method to get the location of this locatable.
     * @return
     */
    @NotNull
    ILocation getLocation();
}
