package com.minecolonies.api.colony.claims;

import net.minecraft.core.BlockPos;

/**
 * Why a chunk is being claimed. Only describes the intent of a single claim call; nothing here is persisted itself. What
 * actually gets recorded on {@link ClaimInfo}, and whether the claim has to check for an existing owner first, depends on
 * which concrete reason is used:
 * <ul>
 *     <li>{@link BuildingClaimReason}: claimed for a specific building. Checks for an existing owner first (fails if
 *     another colony already claims the chunk). Recorded as an entry in {@link ClaimInfo#getClaimingBuildings()}.</li>
 *     <li>{@link ForcedClaimReason}: claimed by an admin command. Skips that check entirely, stripping the chunk from any
 *     other colony that already claims it. Recorded by setting {@link ClaimInfo#isForced()}.</li>
 * </ul>
 * A single chunk can end up with more than one of these reasons true at once (for example, an admin force-claiming a chunk
 * that already has a building claim on it) &mdash; the persisted flags on {@link ClaimInfo} are independent of each other.
 * <p>
 * There's a third kind of claim, the colony's center claim, that isn't represented here at all: it can only ever be granted
 * once, when a colony is first created, so it's handled entirely inside {@code IColonyManagerCapability}'s own colony
 * creation logic rather than being exposed as a reason anyone else could pass in. That's deliberate &mdash; a center claim
 * is the one kind of claim that must never be re-grantable after the fact.
 */
public sealed interface ClaimReason permits BuildingClaimReason, ForcedClaimReason
{
    /**
     * Whether a claim with this reason has to check for an existing owner first (fails if another colony already claims
     * the chunk) or can just take the chunk unconditionally.
     *
     * @return true if the existing owner has to be checked first.
     */
    default boolean checksExistingOwner()
    {
        return true;
    }

    /**
     * Applies this reason to a claim record, recording why the chunk is claimed.
     *
     * @param claimInfo the claim record to update.
     */
    void applyTo(final ClaimInfo claimInfo);

    /**
     * A chunk is being claimed for a specific building.
     *
     * @param buildingPos the position of the building making the claim.
     * @return the reason.
     */
    static ClaimReason building(final BlockPos buildingPos)
    {
        return new BuildingClaimReason(buildingPos);
    }

    /**
     * A chunk is being claimed by an admin command, overriding any other colony's existing claim on it.
     *
     * @return the reason.
     */
    static ClaimReason forced()
    {
        return ForcedClaimReason.INSTANCE;
    }
}