package com.minecolonies.api.colony.citizens.event;

import com.minecolonies.api.colony.ICitizenData;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.NotNull;

/**
 * Event for when a citizen was removed from the colony.
 */
public class CitizenRemovedEvent extends AbstractCitizenEvent
{
    /**
     * The damage source that caused a citizen to die.
     */
    private final @NotNull DamageSource source;

    /**
     * Citizen removed event.
     *
     * @param citizen the citizen related to the event.
     * @param source  the way the citizen went out of the colony.
     */
    public CitizenRemovedEvent(final @NotNull ICitizenData citizen, final @NotNull DamageSource source)
    {
        super(citizen);
        this.source = source;
    }

    /**
     * The damage source that caused the citizen to die.
     *
     * @return the damage source.
     */
    @NotNull
    public DamageSource getDamageSource()
    {
        return source;
    }
}
