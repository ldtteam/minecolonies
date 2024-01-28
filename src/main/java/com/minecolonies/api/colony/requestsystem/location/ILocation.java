package com.minecolonies.api.colony.requestsystem.location;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Interface used to describe locations in the world.
 */
public interface ILocation
{

    /**
     * Method to get the location in the dimension
     *
     * @return The location.
     */
    @NotNull
    BlockPos getInDimensionLocation();

    /**
     * Method to get the dimension of the location.
     *
     * @return The dimension of the location.
     */
    @NotNull
    ResourceKey<Level> getDimension();

    /**
     * Method to check if this location is reachable from the other.
     *
     * @param location The check if it is reachable from here.
     * @return True when reachable, false when not.
     */
    boolean isReachableFromLocation(@NotNull ILocation location);
}
