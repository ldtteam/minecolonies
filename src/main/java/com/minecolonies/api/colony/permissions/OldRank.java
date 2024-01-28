package com.minecolonies.api.colony.permissions;

/**
 * Ranks within a colony.
 */
public enum OldRank
{
    OWNER(true),
    OFFICER(true),
    FRIEND(true),
    NEUTRAL(false),
    HOSTILE(false);

    /**
     * Is the OldRank a subscriber to certain events.
     */
    public final boolean isSubscriber;

    /**
     * Ranks enum constructor.
     * <p>
     * Subscribers are receiving events from the colony. They are either citizens or near enough. Ranks with true are automatically subscribed to the colony.
     *
     * @param isSubscriber boolean whether auto-subscribed to this colony.
     */
    OldRank(final boolean isSubscriber)
    {
        this.isSubscriber = isSubscriber;
    }
}
