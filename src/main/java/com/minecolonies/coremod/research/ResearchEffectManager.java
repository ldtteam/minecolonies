package com.minecolonies.coremod.research;

import com.minecolonies.api.research.effects.IResearchEffectManager;
import com.minecolonies.api.research.effects.IResearchEffect;
import com.minecolonies.api.util.Log;
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
    private final Map<String, IResearchEffect> effectMap = new HashMap<>();

    @Override
    public <W extends IResearchEffect> W getEffect(final String id, @NotNull final Class<W> type)
    {
        final IResearchEffect effect = effectMap.get(id);
        if (type.isInstance(effect))
        {
            return (W) effect;
        }

        return null;
    }

    @Override
    public void applyEffect(final IResearchEffect effect)
    {
        effectMap.put(effect.getId(), effect);
    }
}
