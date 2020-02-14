package com.minecolonies.api.colony.managers.interfaces;

import com.minecolonies.api.colony.IColony;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/**
 * Interface implementing all methods required for all raider managers.
 */
public interface IRaiderManager
{
    /**
     * Checks if the raider manager can have raider events.
     * @return true if so.
     */
    boolean canHaveRaiderEvents();

    /**
     * Checks if the raider raid has been calculated already.
     * @return true if so.
     */
    boolean hasRaidBeenCalculated();

    /**
     * Checks if raiders will raid tonight.
     * @return true if so.
     */
    boolean willRaidTonight();

    /**
     * Set that the manager can receive raider events.
     * @param canHave true or false.
     */
    void setCanHaveRaiderEvents(final boolean canHave);

    /**
     * Add a spawnPoint to the last raiders spawns.
     * @param pos the position to set.
     */
    void addRaiderSpawnPoint(final BlockPos pos);

    /**
     * Set if the raid has been calculated.
     * @param hasSet true or false.
     */
    void setHasRaidBeenCalculated(final boolean hasSet);

    /**
     * Set if raiders will raid tonight.
     * @param willRaid true or false.
     */
    void setWillRaidTonight(final boolean willRaid);

    /**
     * Returns whether spies are enabled
     *
     * @return
     */
    boolean areSpiesEnabled();

    /**
     * Sets whether spies are enabled
     * @param enabled
     */
    void setSpiesEnabled(boolean enabled);

    /**
     * Triggers a raid on the colony
     */
    void raiderEvent();

    /**
     * Calculates the spawn position for raids
     * @return
     */
    BlockPos calculateSpawnLocation();

    /**
     * Gets a random spot inside the colony, in the named direction, where the chunk is loaded.
     * @param directionX the first direction parameter.
     * @param directionZ the second direction paramter.
     * @return the position.
     */
    BlockPos getRandomOutsiderInDirection(final EnumFacing directionX, final EnumFacing directionZ);

    /**
     * Getter for the last spawn points.
     * @return a copy of the list
     */
    List<BlockPos> getLastSpawnPoints();

    /**
     * Calculates the barbarian amount for raids
     *
     * @return
     */
    int calcBarbarianAmount();

    /**
     * Whether the colony is currently raided.
     * @return
     */
    boolean isRaided();

    /**
     * Called on nightfall.
     */
    void onNightFall();

    /**
     * Returns the amount of nights since the last raid
     * @return
     */
    int getNightsSinceLastRaid();

    /**
     * Sets the amount of nights since the last raid
     * @param nightsSinceLastRaid
     */
    void setNightsSinceLastRaid(int nightsSinceLastRaid);

    /**
     * Tries to raid the colony, if possible.
     * @param colony
     */
    void tryToRaidColony(final IColony colony);

    /**
     * Whether the colony can be raided.
     * @return
     */
    boolean canRaid();

    /**
     * Returns true when it is time to raid
     * @return
     */
    boolean isItTimeToRaid();

    /**
     * calculates the colonies raid level
     * @return
     */
    int getColonyRaidLevel();
}
