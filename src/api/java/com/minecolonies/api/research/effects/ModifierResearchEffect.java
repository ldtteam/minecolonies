package com.minecolonies.api.research.effects;

import net.minecraft.util.text.TranslationTextComponent;

/**
 * The modifier research effect, it returns a double modifier.
 */
public class ModifierResearchEffect extends AbstractResearchEffect<Double>
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
    public ModifierResearchEffect(final String id, final double effect)
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
        return new TranslationTextComponent("com.minecolonies.coremod.research.effect.modifier", this.getId(), effect * 100);
    }
}
