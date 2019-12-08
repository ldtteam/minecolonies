package com.minecolonies.coremod.colony.managers;

import com.minecolonies.api.colony.managers.interfaces.IColonyHappinessManager;
import com.minecolonies.api.util.constant.HappinessConstants;
import com.minecolonies.coremod.entity.citizen.citizenhandlers.CitizenHappinessHandler;
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
            modifierValue = Math.max(HappinessConstants.MIN_HAPPINESS, Math.min(modifierValue, HappinessConstants.MAX_HAPPINESS));
            this.modifier = Optional.of(modifierValue);
        }
    }
}
