package com.minecolonies.coremod.research;

import com.minecolonies.api.research.effects.IResearchEffect;
import com.minecolonies.api.research.effects.IResearchEffectManager;
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
    private final Map<String, IResearchEffect<?>> effectMap = new HashMap<>();

    @Override
    public <W extends IResearchEffect<?>> W getEffect(final String id, @NotNull final Class<W> type)
    {
        final IResearchEffect<?> effect = effectMap.get(id);
        if (type.isInstance(effect))
        {
            return (W) effect;
        }
        return null;
    }

    @Override
    public double getEffectValue(final String id)
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
    public boolean getEffectBoolean(final String id)
    {
        if(effectMap.containsKey(id))
        {
            if(effectMap.get(id) instanceof GlobalResearchEffect)
            {
                return ((GlobalResearchEffect)effectMap.get(id)).getEffect() > 0;
            }
        }
        return false;
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
    public void clear()
    {
        effectMap.clear();
    }
}
