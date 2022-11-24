package com.minecolonies.api.compatibility.dynmap;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import net.minecraft.world.level.ChunkPos;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ColonyChunkClaimCalculator
{
    public static Collection<ChunkPos> getAllClaimedChunks(final IColony colony)
    {
        var claimedChunks = new HashSet<ChunkPos>();

        // Claim the protection area
        getProtectionArea(colony, claimedChunks);

        // Claim all the buildings
        colony.getBuildingManager().getBuildings().values().forEach(f -> claimBuilding(f, claimedChunks));

        return claimedChunks;
    }

    private static void getProtectionArea(final IColony colony, Set<ChunkPos> claimedChunks)
    {
        var protectionAreaCenterChunk = new ChunkPos(colony.getCenter());
        var protectionAreaRadius = IMinecoloniesAPI.getInstance().getConfig().getServer().initialColonySize.get();
        for (var i = -protectionAreaRadius; i <= protectionAreaRadius; i++)
        {
            for (var j = -protectionAreaRadius; j <= protectionAreaRadius; j++)
            {
                claimedChunks.add(new ChunkPos(protectionAreaCenterChunk.x + i, protectionAreaCenterChunk.z + j));
            }
        }
    }

    private static void claimBuilding(final IBuilding building, Set<ChunkPos> claimedChunks)
    {
        // Add the building center to the claimed chunks
        var buildingCenterChunk = new ChunkPos(building.getPosition());
        claimedChunks.add(buildingCenterChunk);

        // Add the overlaying schematic to the claimed chunks
        var corners = building.getCorners();
        var bottomLeft = new ChunkPos(corners.getA());
        var topRight = new ChunkPos(corners.getB());
        if (!bottomLeft.equals(topRight))
        {
            // Chunks do not appear on the same XZ coordinate, so this means the building uses multiple chunks
            var chunksHorizontal = ((topRight.getMinBlockX() - bottomLeft.getMinBlockX()) / 16) + 1;
            var chunksVertical = ((topRight.getMinBlockZ() - bottomLeft.getMinBlockZ()) / 16) + 1;
            for (var i = 0; i < chunksHorizontal; i++)
            {
                for (var j = 0; j < chunksVertical; j++)
                {
                    claimedChunks.add(new ChunkPos(bottomLeft.x + i, bottomLeft.z + j));
                }
            }
        }

        // Add the claim radius to the claimed chunks
        var radius = building.getClaimRadius(building.getBuildingLevel());
        for (var i = -radius; i <= radius; i++)
        {
            for (var j = -radius; j <= radius; j++)
            {
                claimedChunks.add(new ChunkPos(buildingCenterChunk.x + i, buildingCenterChunk.z + j));
            }
        }
    }
}