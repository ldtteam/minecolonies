package com.minecolonies.api.eventbus.events.colony.citizens;

import com.minecolonies.api.colony.ICitizenData;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * Event for when a citizen was removed from the colony.
 */
public final class CitizenRemovedModEvent extends AbstractCitizenModEvent
{
    /**
     * The damage source that caused a citizen to die.
     */
    private final @NotNull Entity.RemovalReason reason;

    /**
     * Citizen removed event.
     *
     * @param citizen the citizen related to the event.
     * @param reason  the reason the citizen was removed.
     */
    public CitizenRemovedModEvent(final @NotNull ICitizenData citizen, final @NotNull Entity.RemovalReason reason)
    {
        super(citizen);
        this.reason = reason;
    }

    /**
     * The damage source that caused the citizen to die.
     *
     * @return the damage source.
     */
    @NotNull
    public Entity.RemovalReason getRemovalReason()
    {
        return reason;
    }
}
