package com.minecolonies.api.eventbus.events.colony.citizens;

import com.minecolonies.api.colony.ICitizenData;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.NotNull;

/**
 * Event for when a citizen died in any colony.
 */
public final class CitizenDiedModEvent extends AbstractCitizenModEvent
{
    /**
     * The damage source that caused a citizen to die.
     */
    private final @NotNull DamageSource source;

    /**
     * Citizen died event.
     *
     * @param citizen the citizen related to the event.
     * @param source  the damage source the citizen died from.
     */
    public CitizenDiedModEvent(final @NotNull ICitizenData citizen, final @NotNull DamageSource source)
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
