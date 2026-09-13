package com.minecolonies.api.colony.claims;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * A building is giving up its claim on a chunk. Use {@link UnclaimReason#building(BlockPos)} to get one.
 */
public final class BuildingUnclaimReason implements UnclaimReason
{
    private final BlockPos buildingPos;

    BuildingUnclaimReason(@NotNull final BlockPos buildingPos)
    {
        this.buildingPos = buildingPos;
    }

    public BlockPos getBuildingPos()
    {
        return buildingPos;
    }

    @Override
    public void removeFrom(final ClaimInfo claimInfo)
    {
        claimInfo.getClaimingBuildings().remove(buildingPos);
    }
}