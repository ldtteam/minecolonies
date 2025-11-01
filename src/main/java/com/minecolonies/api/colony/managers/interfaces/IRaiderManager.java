package com.minecolonies.api.colony.managers.interfaces;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.colonyEvents.IColonyRaidEvent;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesRaider;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * @param raidSettings the settings for the next raid, or null to stop the next raid.
     */
    void setRaidNextNight(final RaidSettings raidSettings);

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
     * Trigger a specific type of raid on a colony.
     *
     * @param raidSettings the settings for this raid.
     */
    RaidSpawnResult raiderEvent(final @NotNull RaidSettings raidSettings);

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
     * @return amount
     */
    int getLostCitizen();

    /**
     * Called when a raider mob dies
     *
     * @param entity
     */
    void onRaiderDeath(AbstractEntityMinecoloniesRaider entity);

    void onRaidEventFinished(IColonyRaidEvent event);

    /**
     * Notify raid manager of a passing through raid.
     */
    void setPassThroughRaid();

    record RaidSettings(
        boolean forcedSpawn,
        @Nullable String raidType,
        boolean allowShips,
        @Nullable Integer raiderAmount,
        @Nullable BlockPos location)
    {
        public RaidSettings withExplicitType(final @Nullable String raidType)
        {
            return new RaidSettings(forcedSpawn, raidType, allowShips, raiderAmount, location);
        }

        public static RaidSettings defaultRaidSettings()
        {
            return new RaidSettings(false, null, true, null, null);
        }
    }
}
