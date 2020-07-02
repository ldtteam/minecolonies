package com.minecolonies.api.colony.permissions;

/**
 * Ranks within a colony.
 */
public enum Rank
{
    OWNER(true),
    OFFICER(true),
    FRIEND(true),
    NEUTRAL(false),
    HOSTILE(false);

    /**
     * Is the Rank a subscriber to certain events.
     */
    public final boolean isSubscriber;

    /**
     * Ranks enum constructor.
     * <p>
     * Subscribers are receiving events from the colony. They are either citizens or near enough. Ranks with true are automatically subscribed to the colony.
     *
     * @param isSubscriber boolean whether auto-subscribed to this colony.
     */
    Rank(final boolean isSubscriber)
    {
        this.isSubscriber = isSubscriber;
    }
}
