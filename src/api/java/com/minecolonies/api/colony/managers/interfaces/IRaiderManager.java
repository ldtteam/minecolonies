package com.minecolonies.api.colony.managers.interfaces;

import com.minecolonies.api.colony.ICitizenData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/**
 * Interface implementing all methods required for all raider managers.
 */
public interface IRaiderManager
{
    /**
     * Checks if the raider manager can have raider events.
     *
     * @return true if so.
     */
    boolean canHaveRaiderEvents();

    /**
     * Checks if raiders will raid tonight.
     *
     * @return true if so.
     */
    boolean willRaidTonight();

    /**
     * Set that the manager can receive raider events.
     *
     * @param canHave true or false.
     */
    void setCanHaveRaiderEvents(final boolean canHave);

    /**
     * Add a spawnPoint to the last raiders spawns.
     *
     * @param pos the position to set.
     */
    void addRaiderSpawnPoint(final BlockPos pos);

    /**
     * Set if raiders will raid tonight.
     *
     * @param willRaid true or false.
     */
    void setRaidNextNight(final boolean willRaid);

    /**
     * Set if a specific type of raiders will raid tonight.
     *
     * @param willRaid true or false.
     * @param raidType string containing the name of the raider group.
     *               Accepted names include "pirate", "egyptian", "norsemen", "barbarian", and "amazon".
     *               Defaults to "barbarian" if unsupported type is attempted.
     */
    void setRaidNextNight(final boolean willRaid, final String raidType);

    /**
     * Returns whether spies are enabled
     *
     * @return true if enabled.
     */
    boolean areSpiesEnabled();

    /**
     * Sets whether spies are enabled
     *
     * @param enabled true if enabled.
     */
    void setSpiesEnabled(boolean enabled);

    /**
     * Triggers a raid on the colony
     */
    void raiderEvent();

    /**
     * Trigger a specific type of raid on a colony.
     */
    void raiderEvent(String raidType);

    /**
     * Calculates the spawn position for raids
     *
     * @return the spawn location.
     */
    BlockPos calculateSpawnLocation();

    /**
     * Getter for the last spawn points.
     *
     * @return a copy of the list
     */
    List<BlockPos> getLastSpawnPoints();

    /**
     * Calculates the barbarian amount for raids
     *
     * @param raidLevel the colonies raidlevel
     * @return the number of barbs.
     */
    int calculateRaiderAmount(final int raidLevel);

    /**
     * Whether the colony is currently raided.
     *
     * @return true if so.
     */
    boolean isRaided();

    /**
     * Called on nightfall.
     */
    void onNightFall();

    /**
     * Returns the amount of nights since the last raid
     *
     * @return the number of nights.
     */
    int getNightsSinceLastRaid();

    /**
     * Sets the amount of nights since the last raid
     *
     * @param nightsSinceLastRaid the nights to set.
     */
    void setNightsSinceLastRaid(int nightsSinceLastRaid);

    /**
     * Whether the colony can be raided.
     *
     * @return true if possible.
     */
    boolean canRaid();

    /**
     * calculates the colonies raid level
     *
     * @return the raid level.
     */
    int getColonyRaidLevel();

    /**
     * Returns a random building for raiders to go to, groups up 3 raiders to the same position.
     *
     * @return a random building.
     */
    BlockPos getRandomBuilding();

    /**
     * Gets the difficulty modifier for raids, default difficulty is 1.0
     *
     * @return difficulty
     */
    double getRaidDifficultyModifier();

    /**
     * Called on loosing a citizen, to record deaths during raids
     * @param citizen that died
     */
    void onLostCitizen(ICitizenData citizen);

    /**
     * Writes the raid manager to nbt
     * @param compound to write to
     */
    void write(CompoundNBT compound);

    /**
     * Reads the raid manager form nbt
     * @param compound to read from
     */
    void read(CompoundNBT compound);

    /**
     * Gets the amount of citizens lost in a raid.
     *
     * @return weighted amount of list citizen
     */
    int getLostCitizen();
}
