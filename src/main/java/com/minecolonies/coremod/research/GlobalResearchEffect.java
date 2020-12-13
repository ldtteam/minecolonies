package com.minecolonies.coremod.research;

import com.minecolonies.api.research.effects.IResearchEffect;
import com.minecolonies.api.util.constant.TranslationConstants;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

/**
 *  An instance of a Research Effect at a specific strength, to be applied to a specific colony.
 */
public class GlobalResearchEffect implements IResearchEffect<Float>
{
    /**
     * The absolute effect strength to apply.
     */
    private float effect;

    /**
     * The relative strength of effect to display
     */
    private double displayEffect;

    /**
     * The unique effect Id.
     */
    private final String id;

    /**
     * The optional text description of the effect.
     * If empty, a translation key will be derived from id.
     */
    private final String desc;

    /**
     * The constructor to create a new local research effect.
     *
     * @param id                   the id to unlock.
     * @param effect               the effect's absolute strength.
     * @param displayEffect        the effect's relative strength, for display purposes.
     */
    public GlobalResearchEffect(final String id, final float effect, final float displayEffect)
    {
        this.id = id;
        this.effect = effect;
        this.displayEffect = displayEffect;
        this.desc = "";
    }

    /**
     * The constructor to create a new local research effect, with a statically assigned description.
     *
     * @param id                   the id to unlock.
     * @param effect               the effect's absolute strength.
     * @param displayEffect        the effect's relative strength, for display purposes.
     * @param desc                 the effect's description, for display.
     */
    public GlobalResearchEffect(final String id, final float effect, final float displayEffect, final String desc)
    {
        this.id = id;
        this.effect = effect;
        this.displayEffect = displayEffect;
        this.desc = desc;
    }

    @Override
    public Float getEffect()
    {
        return this.effect;
    }

    @Override
    public void setEffect(Float effect)
    {
        this.effect = effect;
    }

    @Override
    public String getId() { return this.id; }

    @Override
    public TranslationTextComponent getDesc()
    {
        if(desc.isEmpty())
        {
            return new TranslationTextComponent(TranslationConstants.RESEARCH_EFFECTS + this.getId() + ".description", displayEffect, effect, Math.round(displayEffect * 100), Math.round(effect * 100));
        }
        else
        {
           return new TranslationTextComponent(this.desc, displayEffect, effect, Math.round(displayEffect * 100), Math.round(effect * 100));
        }
    }

    @Override
    public boolean overrides(@NotNull final IResearchEffect<?> other)
    {
        return Math.abs(effect) > Math.abs(((GlobalResearchEffect)other).effect);
    }
}
