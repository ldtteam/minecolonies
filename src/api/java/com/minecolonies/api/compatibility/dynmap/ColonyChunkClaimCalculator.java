package com.minecolonies.api.compatibility.dynmap;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.ChunkPos;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class responsible for calculating all claims that are claimed by a colony.
 */
public class ColonyChunkClaimCalculator
{
    private ColonyChunkClaimCalculator() {}

    /**
     * Fetches a collection of {@link ChunkPos}'s depending on what chunks a colony has claimed.
     *
     * @param colony The colony to calculate the claimed chunks for.
     * @return A collection of claimed chunks.
     */
    public static Collection<ChunkPos> getAllClaimedChunks(final IColony colony)
    {
        HashSet<ChunkPos> claimedChunks = new HashSet<>();

        // Claim the protection area
        getProtectionArea(colony, claimedChunks);

        // Claim all the buildings
        colony.getBuildingManager().getBuildings().values().forEach(f -> claimBuilding(f, claimedChunks));

        return claimedChunks;
    }

    /**
     * Calculate the claimed chunks regarding the protection area (the initial claim size after creating the colony)
     *
     * @param colony        The colony to calculate the protection area for.
     * @param claimedChunks The internal set to add the chunks to.
     */
    private static void getProtectionArea(final IColony colony, Set<ChunkPos> claimedChunks)
    {
        ChunkPos protectionAreaCenterChunk = new ChunkPos(colony.getCenter());
        int protectionAreaRadius = IMinecoloniesAPI.getInstance().getConfig().getServer().initialColonySize.get();
        for (int i = -protectionAreaRadius; i <= protectionAreaRadius; i++)
        {
            for (int j = -protectionAreaRadius; j <= protectionAreaRadius; j++)
            {
                claimedChunks.add(new ChunkPos(protectionAreaCenterChunk.x + i, protectionAreaCenterChunk.z + j));
            }
        }
    }

    /**
     * Calculate the claimed chunks regarding all buildings in the colony.
     *
     * @param building      Which building in the colony to check for.
     * @param claimedChunks The internal set to add the chunks to.
     */
    private static void claimBuilding(final IBuilding building, Set<ChunkPos> claimedChunks)
    {
        // Add the building center to the claimed chunks
        ChunkPos buildingCenterChunk = new ChunkPos(building.getPosition());
        claimedChunks.add(buildingCenterChunk);

        // Add the overlaying schematic to the claimed chunks
        Tuple<BlockPos, BlockPos> corners = building.getCorners();
        ChunkPos bottomLeft = new ChunkPos(corners.getA());
        ChunkPos topRight = new ChunkPos(corners.getB());
        if (!bottomLeft.equals(topRight))
        {
            // Chunks do not appear on the same XZ coordinate, so this means the building uses multiple chunks
            int chunksHorizontal = ((topRight.getMinBlockX() - bottomLeft.getMinBlockX()) / 16) + 1;
            int chunksVertical = ((topRight.getMinBlockZ() - bottomLeft.getMinBlockZ()) / 16) + 1;
            for (int i = 0; i < chunksHorizontal; i++)
            {
                for (int j = 0; j < chunksVertical; j++)
                {
                    claimedChunks.add(new ChunkPos(bottomLeft.x + i, bottomLeft.z + j));
                }
            }
        }

        // Add the claim radius to the claimed chunks
        int radius = building.getClaimRadius(building.getBuildingLevel());
        for (int i = -radius; i <= radius; i++)
        {
            for (int j = -radius; j <= radius; j++)
            {
                claimedChunks.add(new ChunkPos(buildingCenterChunk.x + i, buildingCenterChunk.z + j));
            }
        }
    }
}