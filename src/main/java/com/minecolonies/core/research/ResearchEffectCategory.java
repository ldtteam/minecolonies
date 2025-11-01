package com.minecolonies.core.research;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a Type of Research Effect, including its identifier and effect levels.
 */
public class ResearchEffectCategory
{
    /**
     * The unique effect identifier, used to apply the effect category, and to determine translation lookups.
     */
    private final ResourceLocation effectId;

    /**
     * The optional effect category name, uses for display purposes if present.  Overrides default translation lookups.
     */
    private final String effectName;

    /**
     * The optional subtitle, uses for display purposes if present.
     */
    private final String subtitle;

    /**
     * The absolute value of each level of an effect.
     */
    private final List<Double> levelsAbsolute = new ArrayList<>();

    /**
     * The relative change of each level of an effect, as compared to the previous level.
     */
    private final List<Double> levelsRelative = new ArrayList<>();

    /**
     * Constructor for the research effect Category, including effect id, display name, effect type.
     *
     * @param effectId   the unique identifier of the effect category.
     * @param effectName the display name of the effect category.
     * @param subtitle   the optional subtitle.
     */
    public ResearchEffectCategory(final ResourceLocation effectId, final String effectName, final String subtitle, final List<Double> levels)
    {
        this.effectId = effectId;
        this.effectName = effectName;
        this.subtitle = subtitle;
        levelsAbsolute.add(0d);
        levelsRelative.add(0d);
        levels.forEach(level -> {
            levelsRelative.add(level - levelsAbsolute.get(levelsAbsolute.size() - 1));
            levelsAbsolute.add(level);
        });
    }

    /**
     * Gets the relative strength of the effect for a given level of effect
     * compared to the strength of the previous level. Generally used for display purposes.
     *
     * @param level the level of effect.
     * @return the relative strength of the effect at that level.
     */
    public double getDisplay(final int level)
    {
        return this.levelsRelative.get(level);
    }

    /**
     * Gets the absolute strength of the effect for a given level
     *
     * @param level the level of effect.
     * @return the absolute strength of the effect at that level.
     */
    public double get(final int level)
    {
        return this.levelsAbsolute.get(level);
    }

    /**
     * Gets the maximum registered level for the effect.
     *
     * @return the maximum level of the effect.
     */
    public int getMaxLevel()
    {
        return (this.levelsAbsolute.size() - 1);
    }

    /**
     * Gets the unique identifier of the effect.
     *
     * @return the effect id, as a {@link ResourceLocation}.
     */
    public ResourceLocation getId()
    {
        return this.effectId;
    }

    /**
     * Gets the name identifier of the effect.
     *
     * @return the effect's display name, as a human-readable text or translation key.
     */
    public String getName()
    {
        return this.effectName;
    }

    /**
     * Gets the subtitle of the effect.
     *
     * @return the effect's display name, as a string.
     */
    public String getSubtitle()
    {
        return this.subtitle;
    }
}
