package com.minecolonies.coremod.research;

import com.minecolonies.api.research.effects.AbstractResearchEffect;
import com.minecolonies.api.research.effects.IResearchEffect;
import com.minecolonies.api.util.constant.TranslationConstants;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

/**
 * The modifier addition research effect, it returns a double modifier.
 */
public class AdditionModifierResearchEffect extends AbstractResearchEffect<Double>
{
    /**
     * The absolute effect to apply.
     */
    private double effect;

    /**
     * The relative effect to display
     */
    private double relativeEffect;

    /**
     * The constructor to create a new modifier research effect.
     *
     * @param id     the id to unlock.
     * @param effect the effect.
     */
    public AdditionModifierResearchEffect(final String id, final double effect, final double relativeEffect)
    {
        super(id);
        this.effect = effect;
        this.relativeEffect = relativeEffect;
    }

    @Override
    public Double getEffect()
    {
        return this.effect;
    }

    @Override
    public void setEffect(final Double effect)
    {
        this.effect = effect;
    }

    @Override
    public TranslationTextComponent getDesc()
    {
        return new TranslationTextComponent(TranslationConstants.RESEARCH_EFFECTS + this.getId() + ".description", relativeEffect, effect);
    }

    @Override
    public boolean overrides(@NotNull final IResearchEffect<?> other)
    {
        return effect > ((AdditionModifierResearchEffect) other).effect;
    }
}
