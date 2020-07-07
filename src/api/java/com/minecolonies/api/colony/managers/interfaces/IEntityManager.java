package com.minecolonies.api.colony.managers.interfaces;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

/**
 * Manager interface for managing entities for a colony
 */
public interface IEntityManager
{
    /**
     * Register a citizen entity with the colony
     *
     * @param citizen citizen to register
     */
    void registerCitizen(AbstractEntityCitizen citizen);

    /**
     * Unregiters a citizen with the colony
     *
     * @param citizen citizen to unregister
     */
    void unregisterCitizen(AbstractEntityCitizen citizen);

    /**
     * Read the citizens from nbt.
     *
     * @param compound the compound to read it from.
     */
    void read(@NotNull CompoundNBT compound);

    /**
     * Write the citizens to nbt.
     *
     * @param citizenCompound the compound to write it to.
     */
    void write(@NotNull CompoundNBT citizenCompound);

    /**
     * Sends packages to update the citizens.
     *
     * @param closeSubscribers the existing subscribers.
     * @param newSubscribers   new subscribers
     */
    void sendPackets(
      @NotNull Set<ServerPlayerEntity> closeSubscribers,
      @NotNull Set<ServerPlayerEntity> newSubscribers);

    /**
     * Returns a map of citizens in the colony. The map has ID as key, and citizen data as value.
     *
     * @return Map of citizens in the colony, with as key the citizen ID, and as value the citizen data.
     */
    @NotNull
    Map<Integer, ICitizenData> getCitizenMap();

    /**
     * Get citizen by ID.
     *
     * @param citizenId ID of the Citizen.
     * @return ICitizenData associated with the ID, or null if it was not found.
     */
    ICitizenData getCitizen(int citizenId);

    /**
     * Spawns a citizen with the specific citizen data.
     *
     * @param data     Data to use when spawn, null when new generation.
     * @param world    THe world.
     * @param force    True to skip max citizen test, false when not.
     * @param spawnPos the pos to spawn it at.
     * @return the new citizen.
     */
    ICitizenData spawnOrCreateCitizen(ICitizenData data, World world, BlockPos spawnPos, boolean force);

    /**
     * Creates Citizen Data for a new citizen
     *
     * @return new ICitizenData
     */
    ICitizenData createAndRegisterNewCitizenData();

    /**
     * Removes a citizen from the colony.
     *
     * @param citizen Citizen data to remove.
     */
    void removeCitizen(@NotNull ICitizenData citizen);

    /**
     * Marks citizen data dirty.
     */
    void markCitizensDirty();

    /**
     * Clear dirty from all buildings.
     */
    void clearDirty();

    /**
     * Actions to execute on a colony tick.
     *
     * @param colony the event.
     */
    void onColonyTick(IColony colony);
}
