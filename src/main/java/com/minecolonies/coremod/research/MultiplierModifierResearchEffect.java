package com.minecolonies.coremod.research;

import com.minecolonies.api.research.effects.AbstractResearchEffect;
import com.minecolonies.api.research.effects.IResearchEffect;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

/**
 * The modifier multiplication research effect, it returns a double modifier.
 */
public class MultiplierModifierResearchEffect extends AbstractResearchEffect<Double>
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
    public MultiplierModifierResearchEffect(final String id, final double effect)
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
        return new TranslationTextComponent("com.minecolonies.coremod.research.effect.modifier.multiplication", this.getId(), effect * 100);
    }

    @Override
    public boolean overrides(@NotNull final IResearchEffect<?> other)
    {
        return effect > ((MultiplierModifierResearchEffect) other).effect;
    }
}
