package com.minecolonies.api.research.effects;

import com.minecolonies.api.research.IResearchEffect;
import net.minecraft.nbt.CompoundNBT;
import org.jetbrains.annotations.NotNull;

/**
 * The map of unlocked research effects of a given colony.
 */
public interface IResearchEffectManager
{
    /**
     * Get the research effect which is assigned to a particular string.
     * @param id the id of the effect.
     * @param type it's type.
     * @param <W> the Generic type.
     * @return one of the expected type or null.
     */
    <W extends IResearchEffect> W getEffect(final String id, @NotNull final Class<W> type);

    /**
     * Apply the effect to the research effects class.
     * @param effect the effect to apply.
     */
    void applyEffect(final IResearchEffect effect);
}
