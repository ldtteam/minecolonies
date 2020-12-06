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
     * Check if the research effect exists and is an unlockAbility, and if so, if it has been completed.
     * @param  id   the id of the effect
     * @return null if not existing or wrong type, false if incomplete, true if complete.
     */
    Boolean hasUnlockAbilityEffect(final String id);

    /**
     * Check if the research effect exists and is an unlockAbility, and if so, if it has been completed.
     * @param  id   the id of the effect
     * @return null if not existing or wrong type, false if incomplete, true if complete.
     */
    Boolean hasUnlockBuildingEffect(final String id);

    /**
     * Apply the effect to the research effects class.
     *
     * @param effect the effect to apply.
     */
    void applyEffect(final IResearchEffect<?> effect);
}
