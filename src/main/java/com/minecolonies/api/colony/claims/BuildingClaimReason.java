package com.minecolonies.api.colony.claims;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * A chunk is being claimed for a specific building. Use {@link ClaimReason#building(BlockPos)} to get one.
 */
public final class BuildingClaimReason implements ClaimReason
{
    private final BlockPos buildingPos;

    BuildingClaimReason(@NotNull final BlockPos buildingPos)
    {
        this.buildingPos = buildingPos;
    }

    public BlockPos getBuildingPos()
    {
        return buildingPos;
    }

    @Override
    public void applyTo(final ClaimInfo claimInfo)
    {
        claimInfo.getClaimingBuildings().add(buildingPos);
    }
}
