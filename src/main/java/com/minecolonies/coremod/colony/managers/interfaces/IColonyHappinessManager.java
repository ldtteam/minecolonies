package com.minecolonies.coremod.colony.managers.interfaces;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface IColonyHappinessManager
{

    /**
     * The locked happiness modifier if it is set.
     * @return The locked happiness modifier.
     */
    @NotNull
    Optional<Double> getLockedHappinessModifier();

    /**
     * Sets the locked happiness modifier.
     * @param modifier The new locked modifier.
     */
    void setLockedHappinessModifier(@NotNull final Optional<Double> modifier);

}
