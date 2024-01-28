package com.minecolonies.api.research.effects;

import net.minecraft.resources.ResourceLocation;
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
    <W extends IResearchEffect<?>> W getEffect(final ResourceLocation id, @NotNull final Class<W> type);

    /**
     * Get the research effect strength for a given ResearchEffect type,
     * or zero, if no matching effect is present.
     *
     * @param id   the id of the effect.
     * @return the strength of the effect, or zero if it isn't present.
     */
     double getEffectStrength(final ResourceLocation id);

    /**
     * Apply the effect to the research effects class.
     *
     * @param effect the effect to apply.
     */
    void applyEffect(final IResearchEffect<?> effect);

    /**
     * Clear all effects from the effect manager.
     * This should be called on modifications to the effects inside the GlobalResearchTree,
     * or on removal or disable of any effects inside a colony's LocalResearchTree.
     * Because ResearchEffect strengths may not have constantly increasingly impact,
     * and Researches themselves may not necessarily require all (or any) previous levels of an Effect be unlocked,
     * modifications to a ResearchEffect's behavior can not rely on simply rolling a single research effect level or reduce effect strength.
     * After the modifications are complete, the modifying class can then reapply the full cases of local research.
     */
    void removeAllEffects();
}
