package com.minecolonies.coremod.research;

import com.minecolonies.api.util.constant.TranslationConstants;
import net.minecraft.util.text.TranslationTextComponent;

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
    private final String effectId;

    /**
     * The optional effect category name, uses for display purposes if present.  Overrides translation lookups.
     */
    private final String effectName;

    /**
     * The absolute value of each level of an effect.
     */
    private final List<Float> levelsAbsolute = new ArrayList<>();

    /**
     * The relative change of each level of an effect, as compared to the previous level.
     */
    private final List<Float> levelsRelative = new ArrayList<>();

    /**
     *  Constructor for the Research Effect Category, including Id, display name, effect type.
     * @param effectId              The unique identifier of the effect category.
     * @param effectName            The display name of the effect category.
     */
    public ResearchEffectCategory(final String effectId, final String effectName)
    {
        this.effectId = effectId;
        this.effectName = effectName;
        levelsAbsolute.add(0f);
        levelsRelative.add(0f);
    }

    /**
     *  Constructor for the Research Effect Category, including Id and effect type.
     * @param effectId              The unique identifier of the effect category.
     */
    public ResearchEffectCategory(final String effectId)
    {
        this.effectId = effectId;
        this.effectName = "";
        levelsAbsolute.add(0f);
        levelsRelative.add(0f);
    }

    /**
     * Adds an additional level of strength to the effect.
     * @param newVal        The value of the newest level of effect.
     */
    public void add(final float newVal)
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
    public float getDisplay(final int level)
    {
        return this.levelsRelative.get(level);
    }

    /**
     * Gets the absolute strength of the effect for a given level
     * @param level        The level of effect.
     * @return             The absolute strength of the effect at that level.
     */
    public float get(final int level)
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
     * @return             The effect Id, as a string.
     */
    public String getId()
    {
        return this.effectId;
    }
}
