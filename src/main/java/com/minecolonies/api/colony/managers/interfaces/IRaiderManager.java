package com.minecolonies.api.colony.managers.interfaces;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.entity.mobs.AbstractEntityRaiderMob;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.List;

/**
 * Interface implementing all methods required for all raider managers.
 */
public interface IRaiderManager
{
    public enum RaidSpawnResult
    {
        SUCCESS,
        TOO_SMALL,
        CANNOT_RAID,
        NO_SPAWN_POINT,
        ERROR
    }

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
     * Set if raiders will raid tonight.
     *
     * @param willRaid true or false.
     */
    default void setRaidNextNight(final boolean willRaid)
    {
        setRaidNextNight(willRaid, "", true);
    }

    /**
     * Set if raiders will raid tonight.
     *
     * @param willRaid true or false.
     */
    void setRaidNextNight(final boolean willRaid, final String raidType, final boolean allowShips);

    /**
     * Set if a specific type of raiders will raid tonight.
     *
     * @param willRaid true or false.
     * @param raidType string containing the name of the raider group.
     *               Accepted names include "pirate", "egyptian", "norsemen", "barbarian", and "amazon".
     *               Defaults to "barbarian" if unsupported type is attempted.
     */
    default void setRaidNextNight(final boolean willRaid, final String raidType)
    {
        setRaidNextNight(willRaid, raidType, true);
    }

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
     * @param raidType the type of raid (or empty).
     * @param overrideConfig if it should override the config to allow raiders.
     */
    default RaidSpawnResult raiderEvent(String raidType, final boolean overrideConfig)
    {
        return raiderEvent(raidType, overrideConfig, true);
    }

    /**
     * Trigger a specific type of raid on a colony.
     * @param raidType the type of raid (or empty).
     * @param forced if it is forced to spawn.
     * @param allowShips if ship spawns are allowed.
     */
    RaidSpawnResult raiderEvent(String raidType, final boolean forced, final boolean allowShips);

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
    void write(CompoundTag compound);

    /**
     * Reads the raid manager form nbt
     * @param compound to read from
     */
    void read(CompoundTag compound);

    /**
     * Gets the amount of citizens lost in a raid.
     *
     * @return weighted amount of list citizen
     */
    int getLostCitizen();

    /**
     * Called when a raider mob dies
     *
     * @param entity
     */
    void onRaiderDeath(AbstractEntityRaiderMob entity);

    /**
     * Notify raid manager of a passing through raid.
     */
    void setPassThroughRaid();
}
