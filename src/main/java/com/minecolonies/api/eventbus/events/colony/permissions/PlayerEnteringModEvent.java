package com.minecolonies.api.eventbus.events.colony.permissions;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.eventbus.events.colony.AbstractColonyModEvent;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Player entering colony mod event.
 */
public final class PlayerEnteringModEvent extends AbstractColonyModEvent
{
    /**
     * The player that is entering the colony.
     */
    private final Player player;

    /**
     * Whether we should show the notification for the player entering.
     */
    private boolean shouldShowNotification = true;

    /**
     * Whether spectators also show up for the notification.
     */
    private boolean shouldShowForSpectators = false;

    /**
     * Constructs a colony-based event.
     *
     * @param colony the colony related to the event.
     * @param player the player that is entering the colony.
     */
    public PlayerEnteringModEvent(final @NotNull IColony colony, final Player player)
    {
        super(colony);
        this.player = player;
    }

    /**
     * Get the player that is entering the colony.
     */
    public Player getPlayer()
    {
        return player;
    }

    /**
     * Whether we should show the notification for the player entering.
     *
     * @return true if so.
     */
    public boolean shouldShowNotification()
    {
        return shouldShowNotification && (!player.isSpectator() || shouldShowForSpectators);
    }

    /**
     * Disable sending out the notifications.
     */
    public void disableNotification()
    {
        shouldShowNotification = false;
    }

    /**
     * Disable the anti spectator check, allowing you to add custom logic under which to disable the notifications.
     */
    public void allowForSpectators()
    {
        this.shouldShowForSpectators = true;
    }
}
