package com.minecolonies.api.colony.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Capability for the colony tag for chunks
 */
public interface IColonyTagCapability
{
    /**
     * Remove a colony from the list. Only relevant in non dynamic claiming.
     *
     * @param chunk the chunk to remove it from.
     * @param id    the id to remove.
     */
    void removeColony(final int id, final LevelChunk chunk);

    /**
     * Add a new colony to the chunk. Only relevant in non dynamic claiming.
     *
     * @param chunk the chunk to add it to.
     * @param id    the id to add.
     */
    void addColony(final int id, final LevelChunk chunk);

    /**
     * Get a list of colonies with a static claim.
     *
     * @return a list of their ids.
     */
    @NotNull
    List<Integer> getStaticClaimColonies();

    /**
     * Set the owning colony.
     *
     * @param chunk the chunk to set it for.
     * @param id    the id to set.
     */
    void setOwningColony(final int id, final LevelChunk chunk);

    /**
     * Get the owning colony.
     *
     * @return the id of it.
     */
    int getOwningColony();

    /**
     * Reset the capability.
     *
     * @param chunk the chunk to reset.
     */
    void reset(final LevelChunk chunk);

    /**
     * Add the building claim of a certain building.
     *
     * @param colonyId the colony id.
     * @param pos      the position of the building.
     * @param chunk    the chunk to add the claim for.
     */
    void addBuildingClaim(final int colonyId, final BlockPos pos, final LevelChunk chunk);

    /**
     * Remove the building claim of a certain building.
     *
     * @param colonyId the colony id.
     * @param pos      the position of the building.
     * @param chunk    the chunk to remove it from.
     */
    void removeBuildingClaim(final int colonyId, final BlockPos pos, final LevelChunk chunk);

    /**
     * Sets all close colonies.
     *
     * @param colonies the set of colonies.
     */
    void setStaticColonyClaim(final List<Integer> colonies);

    /**
     * Get the claiming buildings map.
     *
     * @return the entire map.
     */
    @NotNull
    Map<Integer, Set<BlockPos>> getAllClaimingBuildings();

    @Nullable
    static IColonyTagCapability getCapability(final LevelChunk chunk)
    {
        if (chunk.getLevel() instanceof final ServerLevel serverLevel)
        {
            return serverLevel.getDataStorage().computeIfAbsent(ColonyTagCapability.FACTORY, ColonyTagCapability.NAME).get(chunk);
        }
        // TODO: client getter or throw
    }
}
