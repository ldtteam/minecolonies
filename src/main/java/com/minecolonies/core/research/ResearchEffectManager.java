package com.minecolonies.core.research;

import com.minecolonies.api.research.effects.IResearchEffect;
import com.minecolonies.api.research.effects.IResearchEffectManager;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

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
    private final Map<ResourceLocation, IResearchEffect<?>> effectMap = new HashMap<>();

    @Override
    public <W extends IResearchEffect<?>> W getEffect(final ResourceLocation id, @NotNull final Class<W> type)
    {
        final IResearchEffect<?> effect = effectMap.get(id);
        if (type.isInstance(effect))
        {
            return (W) effect;
        }
        return null;
    }

    @Override
    public double getEffectStrength(final ResourceLocation id)
    {
        if(effectMap.containsKey(id))
        {
            if(effectMap.get(id) instanceof GlobalResearchEffect)
            {
                return ((GlobalResearchEffect)effectMap.get(id)).getEffect();
            }
        }
        return 0;
    }

    @Override
    public void applyEffect(final IResearchEffect<?> effect)
    {
        if (effectMap.containsKey(effect.getId()))
        {
            final IResearchEffect<?> effectInMap = effectMap.get(effect.getId());
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
