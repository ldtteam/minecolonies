package com.minecolonies.coremod.research;

import com.minecolonies.api.util.constant.TranslationConstants;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;

public class ResearchEffectCategory
{
    /**
     * The property name that indicates this recipe describes a research effect.
     */
    public static final String RESEARCH_EFFECT_PROP = "effect";

    /**
     * The property name for research effect types.
     */
    public static final String RESEARCH_EFFECT_TYPE_PROP = "effectType";

    /**
     * The property name that indicates this recipe describes a research effect.
     */
    public static final String RESEARCH_EFFECT_LEVELS_PROP = "levels";

    private final String effectId;
    private final String effectName;
    private final String effectType;
    private final List<Float> levelsAbsolute = new ArrayList<>();
    private final List<Float> levelsRelative = new ArrayList<>();

    public ResearchEffectCategory(String effectId, String effectName, String effectType)
    {
        this.effectId = effectId;
        this.effectName = effectName;
        this.effectType = effectType;
        levelsAbsolute.add(0f);
        levelsRelative.add(0f);
    }

    public ResearchEffectCategory(String effectId, String effectType)
    {
        this.effectId = effectId;
        this.effectName = "";
        this.effectType = effectType;
        levelsAbsolute.add(0f);
        levelsRelative.add(0f);
    }

    public void add(float newVal)
    {
        levelsRelative.add(newVal - levelsAbsolute.get(levelsAbsolute.size() - 1));
        levelsAbsolute.add(newVal);
    }

    public float getRelative(int level)
    {
        return this.levelsRelative.get(level);
    }

    public float getAbsolute(int level)
    {
        return this.levelsAbsolute.get(level);
    }

    public int getMaxLevel()
    {
        return (this.levelsAbsolute.size() - 1);
    }

    public String getType() { return this.effectType;}

    public String getId()
    {
        return this.effectId;
    }
}
