package com.minecolonies.api.eventbus.events.colony.citizens;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.eventbus.events.colony.AbstractColonyModEvent;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * Event for when a citizen was removed from the colony.
 */
public final class CitizenRemovedModEvent extends AbstractColonyModEvent
{
    /**
     * The id of the citizen.
     */
    private final int citizenId;

    /**
     * The damage source that caused a citizen to die.
     */
    @NotNull
    private final Entity.RemovalReason reason;

    /**
     * Citizen removed event.
     *
     * @param colony    the colony related to the event.
     * @param citizenId the id of the citizen.
     * @param reason    the reason the citizen was removed.
     */
    public CitizenRemovedModEvent(final @NotNull IColony colony, final int citizenId, final @NotNull Entity.RemovalReason reason)
    {
        super(colony);
        this.citizenId = citizenId;
        this.reason = reason;
    }

    /**
     * The id of the citizen.
     *
     * @return the id.
     */
    public int getCitizenId()
    {
        return citizenId;
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
