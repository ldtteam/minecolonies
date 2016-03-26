package com.minecolonies.colony;

import com.minecolonies.colony.permissions.IPermissions;
import net.minecraft.world.World;

public interface IColony
{
    /**
     * Returns the name of the colony
     *
     * @return  Name of the colony
     */
    String getName();

    /**
     * Returns the permissions of the colony
     *
     * @return {@link IPermissions} of the colony
     */
    IPermissions getPermissions();

    /**
     * Determine if a given chunk coordinate is considered to be within the colony's bounds
     * Calls {@link #isCoordInColony(World, int, int, int)}
     *
     * @param w         World to check
     * @param x         x-coordinate
     * @param y         y-coordinate
     * @param z         z-coordinate
     * @return          True if inside colony, otherwise false
     */
    boolean isCoordInColony(World w, int x, int y, int z);

    /**
     * Returns the squared (x, z) distance to the center
     *
     * @param x         x-coordinate
     * @param y         y-coordinate
     * @param z         z-coordinate
     * @return          Squared distance to the center in (x, z) direction
     */
    float getDistanceSquared(int x, int y, int z);

    /**
     * Returns whether or not the colony has a town hall
     *
     * @return  whether or not the colony has a town hall
     */
    boolean hasTownhall();
}
