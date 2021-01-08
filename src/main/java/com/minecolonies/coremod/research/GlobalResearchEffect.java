package com.minecolonies.coremod.research;

import com.minecolonies.api.research.effects.IResearchEffect;
import com.minecolonies.api.util.Log;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

/**
 *  An instance of a Research Effect at a specific strength, to be applied to a specific colony.
 */
public class GlobalResearchEffect implements IResearchEffect<Double>
{
    /**
     * The absolute effect strength to apply.
     */
    private double effect;

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
     * The optional subtitle text description of the effect.
     * If empty, a translation key will be derived from id.
     */
    private final String subtitle;

    /**
     * The constructor to create a new global research effect.
     *
     * @param id                   the id to unlock.
     * @param effect               the effect's absolute strength.
     * @param displayEffect        the effect's relative strength, for display purposes.
     */
    public GlobalResearchEffect(final String id, final double effect, final double displayEffect)
    {
        this.id = id;
        this.effect = effect;
        this.displayEffect = displayEffect;
        this.desc = "";
        this.subtitle = "";
    }

    /**
     * The constructor to create a new global research effect, with a statically assigned description.
     *
     * @param id                   the id to unlock.
     * @param effect               the effect's absolute strength.
     * @param displayEffect        the effect's relative strength, for display purposes.
     * @param desc                 the effect's description, for display.
     * @param subtitle             the effect's subtitle description.
     */
    public GlobalResearchEffect(final String id, final double effect, final double displayEffect, final String desc, final String subtitle)
    {
        this.id = id;
        this.effect = effect;
        this.displayEffect = displayEffect;
        this.desc = desc;
        this.subtitle = subtitle;
    }

    /**
     * The constructor to build a new global research from an array of its attributes.
     * See getAttributes for the format.
     *
     * @param effectParts              the effectParts describing the research effect.
     */
    public GlobalResearchEffect(final String[] effectParts)
    {
        if(effectParts.length < 5)
        {
            Log.getLogger().warn("Received malformed effect description from server.");
            this.id ="";
            this.effect = 0;
            this.displayEffect = 0;
            this.desc = "";
            this.subtitle = "";
            return;
        }
        this.id = effectParts[0];
        this.desc = effectParts[1];
        this.subtitle = effectParts[2];
        try
        {
            this.effect = Double.parseDouble(effectParts[3]);
            this.displayEffect = Double.parseDouble(effectParts[4]);
        }
        catch (NumberFormatException nfe)
        {
            Log.getLogger().warn("Error in received GlobalResearchEffect:" + this.id);
        }
    }

    @Override
    public Double getEffect()
    {
        return this.effect;
    }

    @Override
    public Double getDisplay() { return this.displayEffect; }

    @Override
    public void setEffect(Double effect)
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
            return new TranslationTextComponent("com." + this.id.split(":")[0] + ".research." + this.id.split(":")[1].replaceAll("[ /:]",".") + ".description",
                   displayEffect, effect, Math.round(displayEffect * 100), Math.round(effect * 100));
        }
        else
        {
           return new TranslationTextComponent(this.desc, displayEffect, effect, Math.round(displayEffect * 100), Math.round(effect * 100));
        }
    }

    @Override
    public String getSubtitle()
    {
            return this.subtitle;
    }

    @Override
    public boolean overrides(@NotNull final IResearchEffect<?> other)
    {
        return Math.abs(effect) > Math.abs(((GlobalResearchEffect)other).effect);
    }

    @Override
    public String getAttributes()
    {
        return id + "`" + desc + "`" + subtitle + "`" + effect + "`" + displayEffect;
    }
}
