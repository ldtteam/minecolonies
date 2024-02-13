package com.minecolonies.api.colony.capability;

import com.minecolonies.api.util.ChunkLoadStorage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Capability for the colony tag for chunks
 */
public interface IChunkmanagerCapability
{
    /**
     * Get the chunkStorage at a certain location.
     *
     * @param chunkX the x chunk location.
     * @param chunkZ the z chunk location.
     * @return the storage or null.
     */
    @Nullable
    ChunkLoadStorage getChunkStorage(int chunkX, int chunkZ);

    /**
     * Add a new chunkStorage.
     *
     * @param chunkX  chunkX the x chunk location.
     * @param chunkZ  chunkX the z chunk location.
     * @param storage the new to add or update.
     * @return true if override else false.
     */
    boolean addChunkStorage(int chunkX, int chunkZ, ChunkLoadStorage storage);

    /**
     * Get all chunk storages for serialization.
     *
     * @return the storages.
     */
    Map<ChunkPos, ChunkLoadStorage> getAllChunkStorages();

    @Nullable
    static IChunkmanagerCapability getCapability(final Level level)
    {
        if (level instanceof final ServerLevel serverLevel)
        {
            return serverLevel.getDataStorage().computeIfAbsent(ChunkmanagerCapability.FACTORY, ChunkmanagerCapability.NAME);
        }
        // TODO: client getter or throw
    }
}
