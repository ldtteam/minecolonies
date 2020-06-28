package com.minecolonies.coremod.research;

import com.minecolonies.api.research.effects.AbstractResearchEffect;
import com.minecolonies.api.research.effects.IResearchEffect;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

/**
 * The modifier addition research effect, it returns a double modifier.
 */
public class AdditionModifierResearchEffect extends AbstractResearchEffect<Double>
{
    /**
     * The effect to apply.
     */
    private double effect;

    /**
     * The constructor to create a new modifier research effect.
     * @param id the id to unlock.
     * @param effect the effect.
     */
    public AdditionModifierResearchEffect(final String id, final double effect)
    {
        super(id);
        this.effect = effect;
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
        return new TranslationTextComponent("com.minecolonies.coremod.research.effect.modifier.addition", this.getId(), effect);
    }

    @Override
    public boolean overrides(@NotNull final IResearchEffect<?> other)
    {
        return effect > ((AdditionModifierResearchEffect) other).effect;
    }
}
