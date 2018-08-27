package com.minecolonies.coremod.colony.managers;

import com.minecolonies.coremod.colony.managers.interfaces.IColonyHappinessManager;
import com.minecolonies.coremod.entity.citizenhandlers.CitizenHappinessHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ColonyHappinessManager implements IColonyHappinessManager
{
    @NotNull
    private Optional<Double> modifier = Optional.empty();

    /**
     * The locked happiness modifier if it is set.
     *
     * @return The locked happiness modifier.
     */
    @NotNull
    @Override
    public Optional<Double> getLockedHappinessModifier()
    {
        return modifier;
    }

    /**
     * Sets the locked happiness modifier.
     *
     * @param modifier The new locked modifier.
     */
    @Override
    public void setLockedHappinessModifier(@NotNull final Optional<Double> modifier)
    {
        this.modifier = modifier;
        if (this.modifier.isPresent())
        {
            double modifierValue = this.modifier.get();
            modifierValue = Math.max(CitizenHappinessHandler.MIN_HAPPINESS, Math.min(modifierValue, CitizenHappinessHandler.MAX_HAPPINESS));
            this.modifier = Optional.of(modifierValue);
        }
    }
}
