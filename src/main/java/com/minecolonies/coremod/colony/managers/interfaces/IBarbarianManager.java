package com.minecolonies.coremod.colony.managers.interfaces;

import com.minecolonies.coremod.entity.ai.mobs.barbarians.AbstractEntityBarbarian;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
     * Add a spawnPoint to the last barb spawns.
     * @param pos the position to set.
     */
    void addBarbarianSpawnPoint(final BlockPos pos);

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

    /**
     * Getter for the last spawn points.
     * @return a copy of the list
     */
    List<BlockPos> getLastSpawnPoints();

    /**
     * Register a barbarian at the colony.
     * @param abstractEntityBarbarian the barbarian to register.
     */
    void registerBarbarian(@NotNull final AbstractEntityBarbarian abstractEntityBarbarian);

    /**
     * Unregister a barbarian from the colony.
     * @param world the serverWorld.
     * @param abstractEntityBarbarian the barbarian to unregister.
     */
    void unregisterBarbarian(@NotNull final AbstractEntityBarbarian abstractEntityBarbarian, final WorldServer world);

    /**
     * Gets the horde of barbarians approaching the colony.
     * @param world the serverWorld.
     * @return the list of entities.
     */
    List<AbstractEntityBarbarian> getHorde(final WorldServer world);
}
