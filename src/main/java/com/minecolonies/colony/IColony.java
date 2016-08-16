package com.minecolonies.colony;

import com.minecolonies.colony.permissions.IPermissions;
import net.minecraft.util.BlockPos;
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
     *
     * @param w         World to check
     * @param pos		Block Position
     * @return          True if inside colony, otherwise false
     */
    boolean isCoordInColony(World w, BlockPos pos);

    /**
     * Returns the squared (x, z) distance to the center
     *
     * @param pos		Block Position
     * @return          Squared distance to the center in (x, z) direction
     */
    float getDistanceSquared(BlockPos pos);

    /**
     * Returns whether or not the colony has a town hall
     *
     * @return  whether or not the colony has a town hall
     */
    boolean hasTownHall();
}
