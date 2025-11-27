package com.minecolonies.api.eventbus.events.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.ColonyPlayer;
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
     * Constructs a colony player rank change event.
     *
     * @param colony the colony related to the event.
     * @param player the colony player related to the event
     */
    public ColonyPlayerRankChangedModEvent(
        @NotNull final IColony colony, @NotNull final ColonyPlayer player)
    {
        super(colony);
        this.player = player;
    }

    /**
     * Gets the player id related to the event.
     * Gets the colony player related to the event.
     */
    @NotNull
    public ColonyPlayer getPlayer()
    {
        return player;
    }
}
