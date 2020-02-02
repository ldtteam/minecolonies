package com.minecolonies.api.research;

import net.minecraft.util.text.TranslationTextComponent;

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
}
