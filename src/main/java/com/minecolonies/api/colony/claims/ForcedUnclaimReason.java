package com.minecolonies.api.colony.claims;

/**
 * An admin is clearing the forced flag on a chunk. Use {@link UnclaimReason#forced()} to get the instance.
 */
public final class ForcedUnclaimReason implements UnclaimReason
{
    static final ForcedUnclaimReason INSTANCE = new ForcedUnclaimReason();

    private ForcedUnclaimReason()
    {
    }

    @Override
    public void removeFrom(final ClaimInfo claimInfo)
    {
        claimInfo.setForced(false);
    }
}