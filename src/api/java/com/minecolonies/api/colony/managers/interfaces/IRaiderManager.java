package com.minecolonies.api.colony.managers.interfaces;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.jetbrains.annotations.NotNull;

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
     * Register a raider at the colony.
     * @param raider the raider to register.
     */
    void registerRaider(@NotNull final AbstractEntityMinecoloniesMob raider);

    /**
     * Unregister a raider from the colony.
     * @param raider the raider to unregister.
     * @param world the serverWorld.
     */
    void unregisterRaider(@NotNull final AbstractEntityMinecoloniesMob raider, final WorldServer world);

    /**
     * Gets the horde of raiders approaching the colony.
     * @param world the serverWorld.
     * @return the list of entities.
     */
    List<AbstractEntityMinecoloniesMob> getHorde(final WorldServer world);

    /**
     * Register a certain raider origin schematic to the colony..
     * @param ship the ship description.
     * @param position the position.
     * @param worldTime the world time at spawn.
     */
    void registerRaiderOriginSchematic(final String ship, final BlockPos position, final long worldTime);

    /**
     * Reads the raider manager from NBT.
     * @param compound the compound to read it from.
     */
    void readFromNBT(@NotNull NBTTagCompound compound);

    /**
     * Called on colony tick.
     * @param colony the colony being ticked.
     */
    void onColonyTick(@NotNull final IColony colony);

    /**
     * Writes the raider manager to NBT.
     * @param compound the compound to write it to.
     */
    void writeToNBT(@NotNull NBTTagCompound compound);
}
