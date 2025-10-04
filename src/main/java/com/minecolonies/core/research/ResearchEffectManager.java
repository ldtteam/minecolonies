package com.minecolonies.core.research;

import com.minecolonies.api.research.IResearchEffect;
import com.minecolonies.api.research.IResearchEffectManager;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * The map of unlocked research effects of a given colony.
 */
public class ResearchEffectManager implements IResearchEffectManager
{
    /**
     * The map of the research effects, from a string identifier to the effect.
     */
    private final Map<ResourceLocation, IResearchEffect> effectMap = new HashMap<>();

    @Override
    public double getEffectStrength(final ResourceLocation id)
    {
        final IResearchEffect effect = effectMap.get(id);
        if (effect instanceof GlobalResearchEffect globalResearchEffect)
        {
            return globalResearchEffect.getEffect();
        }
        return 0;
    }

    @Override
    public void applyEffect(final IResearchEffect effect)
    {
        final IResearchEffect effectInMap = effectMap.get(effect.getId());
        if (effectInMap != null)
        {
            if (effect.overrides(effectInMap))
            {
                effectMap.put(effect.getId(), effect);
            }
        }
        else
        {
            effectMap.put(effect.getId(), effect);
        }
    }

    @Override
    public void removeAllEffects()
    {
        effectMap.clear();
    }
}
