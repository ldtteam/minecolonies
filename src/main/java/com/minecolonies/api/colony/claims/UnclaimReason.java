package com.minecolonies.api.colony.claims;

import net.minecraft.core.BlockPos;

/**
 * Why a chunk's claim is being released. Only describes the intent of a single unclaim call; nothing here is persisted
 * itself. There's no reason for releasing a center claim &mdash; a colony's center claim can only ever go away when the
 * whole colony is deleted, which wipes its claims directly rather than going through a per-chunk release.
 */
public sealed interface UnclaimReason permits BuildingUnclaimReason, ForcedUnclaimReason
{
    /**
     * Removes this reason from a claim record, releasing the claim it recorded.
     *
     * @param claimInfo the claim record to update.
     */
    void removeFrom(final ClaimInfo claimInfo);

    /**
     * A building is giving up its claim on a chunk.
     *
     * @param buildingPos the position of the building giving up its claim.
     * @return the reason.
     */
    static UnclaimReason building(final BlockPos buildingPos)
    {
        return new BuildingUnclaimReason(buildingPos);
    }

    /**
     * An admin is clearing the forced flag on a chunk (the admin-override part of a claim made with
     * {@link ClaimReason#forced()}), leaving any building claim on it untouched.
     *
     * @return the reason.
     */
    static UnclaimReason forced()
    {
        return ForcedUnclaimReason.INSTANCE;
    }
}