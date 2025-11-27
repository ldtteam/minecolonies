package com.minecolonies.api.eventbus.events.colony;

import com.minecolonies.api.colony.IColony;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Colony player rank change event.
 */

public final class ColonyPlayerRankChangedModEvent extends AbstractColonyModEvent
{
    /**
     * The player whose rank got modified.
     */
    @NotNull
    private final UUID playerID;

    /**
     * Constructs a colony player rank change event.
     *
     * @param colony the colony related to the event.
     * @param playerID the player id related to the event
     */
    public ColonyPlayerRankChangedModEvent(@NotNull final IColony colony, @NotNull final UUID playerID) {
        super(colony);
        this.playerID = playerID;
    }

    /**
     * Gets the player id related to the event.
     */
    @NotNull
    public UUID getPlayerID() { return playerID; }
}
