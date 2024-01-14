package com.minecolonies.api.colony.managers.interfaces;

import com.minecolonies.api.colony.ICivilianData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.entity.citizen.AbstractCivilianEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

/**
 * Manager interface for managing entities for a colony
 */
public interface IEntityManager<T extends ICivilianData>
{
    /**
     * Register a civilian entity with the colony
     *
     * @param entity civilian to register
     */
    void registerCivilian(AbstractCivilianEntity entity);

    /**
     * Unregiters a civilian with the colony
     *
     * @param entity civilian to unregister
     */
    void unregisterCivilian(AbstractCivilianEntity entity);

    /**
     * Read the civilian from nbt.
     *
     * @param compound the compound to read it from.
     */
    void read(@NotNull CompoundTag compound);

    /**
     * Write the civilian to nbt.
     *
     * @param compoundNBT the compound to write it to.
     */
    void write(@NotNull CompoundTag compoundNBT);

    /**
     * Sends packages to update the civilian.
     *
     * @param closeSubscribers the existing subscribers.
     * @param newSubscribers   new subscribers
     */
    void sendPackets(
      @NotNull Set<ServerPlayer> closeSubscribers,
      @NotNull Set<ServerPlayer> newSubscribers);

    /**
     * Returns a map of civilian in the colony. The map has ID as key, and civilian data as value.
     *
     * @return Map of civilian in the colony, with as key the civilian ID, and as value the civilian data.
     */
    @NotNull
    Map<Integer, ICivilianData> getCivilianDataMap();

    /**
     * Get civilian by ID.
     *
     * @param civilianId ID of the civilian.
     * @return ICivilianData associated with the ID, or null if it was not found.
     */
    T getCivilian(int civilianId);

    /**
     * Spawns a civilian with the specific civilian data.
     *
     * @param data     Data to use when spawn, null when new generation.
     * @param world    THe world.
     * @param spawnPos the pos to spawn it at.
     * @param force    True to skip max civilian test, false when not.
     * @return the new civilian.
     */
    T spawnOrCreateCivilian(T data, Level world, BlockPos spawnPos, boolean force);

    /**
     * Creates Civilian Data for a new civilian
     *
     * @return new ICivilianData
     */
    ICivilianData createAndRegisterCivilianData();

    /**
     * Removes a civilian from the colony.
     *
     * @param civilian data to remove.
     */
    void removeCivilian(@NotNull ICivilianData civilian);

    /**
     * Marks civilian data dirty.
     */
    void markDirty();

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
