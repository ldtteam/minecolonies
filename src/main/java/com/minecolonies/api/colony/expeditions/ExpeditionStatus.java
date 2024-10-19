package com.minecolonies.api.colony.expeditions;

/**
 * Enum describing the different statuses of an expedition.
 */
public enum ExpeditionStatus
{
    /**
     * The expedition has been created but not accepted yet.
     */
    CREATED(true),
    /**
     * The expedition has been accepted but has not been sent away just yet.
     */
    ACCEPTED(true),
    /**
     * The expedition is currently active.
     */
    ONGOING(false),
    /**
     * The expedition is finished.
     */
    FINISHED(true),
    /**
     * The expedition does not exist.
     */
    UNKNOWN(true);

    /**
     * Whether the tied visitor may be removed during the given phase of the expedition.
     */
    private final boolean mayRemoveVisitor;

    /**
     * Internal constructor.
     *
     * @param mayRemoveVisitor whether the tied visitor may be removed during the given phase of the expedition.
     */
    ExpeditionStatus(boolean mayRemoveVisitor)
    {
        this.mayRemoveVisitor = mayRemoveVisitor;
    }

    /**
     * Check whether the tied visitor may be removed during the given phase of the expedition.
     *
     * @return true if so.
     */
    public boolean mayRemoveVisitor()
    {
        return mayRemoveVisitor;
    }
}