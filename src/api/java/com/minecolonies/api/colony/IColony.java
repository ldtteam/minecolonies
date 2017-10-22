package com.minecolonies.api.colony;

import com.minecolonies.api.colony.permissions.IPermissions;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Interface of the Colony and ColonyView which will have to implement the following methods.
 */
public interface IColony
{

    /**
     * Returns the position of the colony.
     *
     * @return pos of the colony.
     */
    BlockPos getCenter();

    /**
     * Returns the name of the colony.
     *
     * @return Name of the colony.
     */
    String getName();

    /**
     * Returns the permissions of the colony.
     *
     * @return {@link IPermissions} of the colony.
     */
    IPermissions getPermissions();

    /**
     * Determine if a given chunk coordinate is considered to be within the colony's bounds.
     *
     * @param w   World to check.
     * @param pos Block Position.
     * @return True if inside colony, otherwise false.
     */
    boolean isCoordInColony(World w, BlockPos pos);

    /**
     * Returns the squared (x, z) distance to the center.
     *
     * @param pos Block Position.
     * @return Squared distance to the center in (x, z) direction.
     */
    long getDistanceSquared(BlockPos pos);

    /**
     * Returns whether or not the colony has a town hall.
     *
     * @return whether or not the colony has a town hall.
     */
    boolean hasTownHall();

    /**
     * returns this colonies unique id.
     *
     * @return an int representing the id.
     */
    int getID();

    /**
     * Check if the colony has a warehouse.
     * @return true if so.
     */
    boolean hasWarehouse();

    /**
     * Get the last contact of a player to the colony in hours.
     * @return an integer with a describing value.
     */
    int getLastContactInHours();

    /**
     * get whether there will be a raid in the colony tonight.
     * @return true if so.
     */
    boolean hasWillRaidTonight();

    /**
     * return the colony's world.
     * @return world.
     */
    World getWorld();

    /**
     * return whether or not a colony is able to be automatically deleted.
     * @return true if so.
     */
    boolean canBeAutoDeleted();

    /**
     * return whether or not a colony is allowed to have barbarian events triggered.
     * @return true if so.
     */
    boolean isCanHaveBarbEvents();

    /**
     * return whether or not the colony has had it's "RaidTonight" calculated yet.
     * @return true if so.
     */
    boolean isHasRaidBeenCalculated();
}
