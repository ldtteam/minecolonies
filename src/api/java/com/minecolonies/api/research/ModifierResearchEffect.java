package com.minecolonies.api.research;

/**
 * The modifier research effect, it returns a double modifier.
 */
public class ModifierResearchEffect implements IResearchEffect<Double>
{
    /**
     * The String id of the research effect.
     */
    private final String id;

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
        this.id = id;
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
    public String getId()
    {
        return this.id;
    }
}
