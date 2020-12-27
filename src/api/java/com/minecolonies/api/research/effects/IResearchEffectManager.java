package com.minecolonies.api.research.effects;

import org.jetbrains.annotations.NotNull;

/**
 * The manager of unlocked research effects of a given colony.
 */
public interface IResearchEffectManager
{
    /**
     * Get the research effect which is assigned to a particular string.
     *
     * @param id   the id of the effect.
     * @param type it's type.
     * @param <W>  the Generic type.
     * @return one of the expected type or null.
     */
    <W extends IResearchEffect<?>> W getEffect(final String id, @NotNull final Class<W> type);

    /**
     * Get the research effect strength which is assigned to a particular string
     * or zero, if no matching effect is present.
     *
     * @param id   the id of the effect.
     * @return the strength of the effect, or zero if it isn't present.
     */
     double getEffectValue(final String id);

    /**
     * Gets whether a research effect is enabled with a non-zero value.
     *
     * @param id   the id of the effect.
     * @return true if the is greater than zero, false if less than or equal to zero or not present.
     */
    boolean getEffectBoolean(final String id);

    /**
     * Apply the effect to the research effects class.
     *
     * @param effect the effect to apply.
     */
    void applyEffect(final IResearchEffect<?> effect);

    /**
     * Clear the contents of the effect manager.
     */
    void clear();
}
