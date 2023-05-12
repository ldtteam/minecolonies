package com.minecolonies.coremod.research;

import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a Type of Research Effect, including its identifier and effect levels.
 */
public class ResearchEffectCategory
{
    /**
     * The property name that indicates this recipe describes a research effect.
     */
    public static final String RESEARCH_EFFECT_PROP = "effect";

    /**
     * The property name that indicates this recipe describes a research effect.
     */
    public static final String RESEARCH_EFFECT_LEVELS_PROP = "levels";

    /**
     * The unique effect identifier, used to apply the effect category, and to determine translation lookups.
     */
    private final ResourceLocation effectId;

    /**
     * The optional effect category name, uses for display purposes if present.  Overrides default translation lookups.
     */
    private final TranslatableContents effectName;

    /**
     * The optional subtitle, uses for display purposes if present.
     */
    private final TranslatableContents subtitle;

    /**
     * The absolute value of each level of an effect.
     */
    private final List<Double> levelsAbsolute = new ArrayList<>();

    /**
     * The relative change of each level of an effect, as compared to the previous level.
     */
    private final List<Double> levelsRelative = new ArrayList<>();

    /**
     *  Constructor for the Research Effect Category, including Id, display name, effect type.
     * @param effectId              The unique identifier of the effect category.
     * @param effectName            The display name of the effect category.
     * @param subtitle              The optional subtitle.
     */
    public ResearchEffectCategory(final String effectId, final String effectName, final String subtitle)
    {
        this.effectId = new ResourceLocation(effectId);
        if(effectName != null)
        {
            this.effectName = new TranslatableContents(effectName);
        }
        else
        {
            this.effectName = new TranslatableContents("com." + this.effectId.getNamespace() + ".research." + this.effectId.getPath().replaceAll("[ /]", ".") + ".description");
        }
        this.subtitle = new TranslatableContents(subtitle);
        levelsAbsolute.add(0d);
        levelsRelative.add(0d);
    }

    /**
     *  Constructor for the Research Effect Category, including Id, display name, effect type.
     * @param effectId              The unique identifier of the effect category.
     */
    public ResearchEffectCategory(final String effectId, final String effectName)
    {
        this.effectId = new ResourceLocation(effectId);
        this.effectName = new TranslatableContents(effectName);
        this.subtitle = new TranslatableContents("");
        levelsAbsolute.add(0d);
        levelsRelative.add(0d);
    }

    /**
     *  Constructor for the Research Effect Category, including Id and effect type.
     * @param effectId              The unique identifier of the effect category.
     */
    public ResearchEffectCategory(final String effectId)
    {
        this.effectId = new ResourceLocation(effectId);
        this.effectName = new TranslatableContents("com." + this.effectId.getNamespace() + ".research." + this.effectId.getPath().replaceAll("[ /]",".") + ".description");
        this.subtitle = new TranslatableContents("");
        levelsAbsolute.add(0d);
        levelsRelative.add(0d);
    }

    /**
     * Adds an additional level of strength to the effect.
     * @param newVal        The value of the newest level of effect.
     */
    public void add(final double newVal)
    {
        levelsRelative.add(newVal - levelsAbsolute.get(levelsAbsolute.size() - 1));
        levelsAbsolute.add(newVal);
    }

    /**
     * Gets the relative strength of the effect for a given level of effect
     * compared to the strength of the previous level.  Generally used for display purposes.
     * @param level        The level of effect.
     * @return             The relative strength of the effect at that level.
     */
    public double getDisplay(final int level)
    {
        return this.levelsRelative.get(level);
    }

    /**
     * Gets the absolute strength of the effect for a given level
     * @param level        The level of effect.
     * @return             The absolute strength of the effect at that level.
     */
    public double get(final int level)
    {
        return this.levelsAbsolute.get(level);
    }

    /**
     * Gets the maximum registered level for the effect.
     * @return             The maximum level of the effect.
     */
    public int getMaxLevel()
    {
        return (this.levelsAbsolute.size() - 1);
    }

    /**
     * Gets the unique identifier of the effect.
     * @return             The effect Id, as a ResourceLocation.
     */
    public ResourceLocation getId()
    {
        return this.effectId;
    }

    /**
     * Gets the name identifier of the effect.
     * @return             The effect's display name, as a human-readable text or translation key.
     */
    public TranslatableContents getName()
    {
        return this.effectName;
    }

    /**
     * Gets the subtitle of the effect.
     * @return             The effect's display name, as a string.
     */
    public TranslatableContents getSubtitle()
    {
        return this.subtitle;
    }
}
