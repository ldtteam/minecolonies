package com.minecolonies.coremod.colony.managers;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

/**
 * Interface implementing all methods required for all barbarianmanagers.
 */
public interface IBarbarianManager
{
    /**
     * Checks if the barbarian manager can have barbarian events.
     * @return true if so.
     */
    boolean canHaveBarbEvents();

    /**
     * Checks if the barbarian raid has been calculated already.
     * @return true if so.
     */
    boolean hasRaidBeenCalculated();

    /**
     * Checks if barbs will raid tonight.
     * @return true if so.
     */
    boolean willRaidTonight();

    /**
     * Set that the manager can receive barbarian events.
     * @param canHave true or false.
     */
    void setCanHaveBarbEvents(final boolean canHave);

    /**
     * Set if the raid has been calculated.
     * @param hasSet true or false.
     */
    void setHasRaidBeenCalculated(final boolean hasSet);

    /**
     * Set if barbarians will raid tonight.
     * @param willRaid true or false.
     */
    void setWillRaidTonight(final boolean willRaid);

    /**
     * Gets a random spot inside the colony, in the named direction, where the chunk is loaded.
     * @param directionX the first direction parameter.
     * @param directionZ the second direction paramter.
     * @return the position.
     */
    BlockPos getRandomOutsiderInDirection(final EnumFacing directionX, final EnumFacing directionZ);
}
