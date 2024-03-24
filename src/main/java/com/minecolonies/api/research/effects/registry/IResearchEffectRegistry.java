package com.minecolonies.api.research.effects.registry;

import com.minecolonies.api.IMinecoloniesAPI;
import net.minecraft.core.Registry;

public interface IResearchEffectRegistry
{
    static Registry<ResearchEffectEntry> getInstance()
    {
        return IMinecoloniesAPI.getInstance().getResearchEffectRegistry();
    }
}
