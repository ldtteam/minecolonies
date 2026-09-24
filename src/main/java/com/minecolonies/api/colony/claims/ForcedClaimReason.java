package com.minecolonies.api.colony.claims;

/**
 * A chunk is being claimed by an admin command, overriding any other colony's existing claim on it. Use
 * {@link ClaimReason#forced()} to get the instance.
 */
public final class ForcedClaimReason implements ClaimReason
{
    static final ForcedClaimReason INSTANCE = new ForcedClaimReason();

    private ForcedClaimReason()
    {
    }

    @Override
    public boolean checksExistingOwner()
    {
        return false;
    }

    @Override
    public void applyTo(final ClaimInfo claimInfo)
    {
        claimInfo.setForced(true);
    }
}
