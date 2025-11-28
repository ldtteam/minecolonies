package com.minecolonies.api.eventbus.events.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.ColonyPlayer;
import com.minecolonies.api.colony.permissions.Rank;
import org.jetbrains.annotations.NotNull;

/**
 * Colony player rank change event.
 */
public final class ColonyPlayerRankChangedModEvent extends AbstractColonyModEvent
{
    /**
     * The colony player whose rank got modified.
     */
    @NotNull
    private final ColonyPlayer player;

    /**
     * The colony player's new rank.
     */
    private final Rank newRank;

    /**
     * The colony player's old rank.
     */
    private final Rank oldRank;

    /**
     * Constructs a colony player rank change event.
     *
     * @param colony  the colony related to the event.
     * @param player  the colony player related to the event.
     * @param newRank the colony player's new rank.
     * @param oldRank the colony player's old rank.
     */
    public ColonyPlayerRankChangedModEvent(
        @NotNull final IColony colony, @NotNull final ColonyPlayer player, final Rank newRank, final Rank oldRank)
    {
        super(colony);
        this.player = player;
        this.newRank = newRank;
        this.oldRank = oldRank;
    }

    /**
     * Gets the colony player related to the event.
     */
    @NotNull
    public ColonyPlayer getPlayer()
    {
        return player;
    }

    /**
     * Gets the colony player's new rank.
     */
    public Rank getNewRank()
    {
        return newRank;
    }

    /**
     * Gets the colony player's old rank.
     */
    public Rank getOldRank()
    {
        return oldRank;
    }
}
