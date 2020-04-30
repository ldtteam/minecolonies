package com.minecolonies.api.research.effects;

import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

/**
 * The effect of a research.
 * @param <T> the type of the value.
 */
public interface IResearchEffect<T>
{
    /**
     * Get the effect of the research.
     * @return the effect.
     */
    T getEffect();

    /**
     * Set the research effect.
     * @param effect the value to set it to.
     */
    void setEffect(T effect);

    /**
     * Getter for the ID of the effect.
     * @return the String id.
     */
    String getId();

    /**
     * Effect description.
     * @return the desc.
     */
    TranslationTextComponent getDesc();

    /**
     * Does this effect override another effect with the same id?
     * @param other the effect to check.
     * @return true if so.
     */
    boolean overrides(@NotNull final IResearchEffect other);
}
