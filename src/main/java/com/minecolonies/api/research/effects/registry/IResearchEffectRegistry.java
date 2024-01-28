package com.minecolonies.api.research.effects.registry;

import com.minecolonies.api.IMinecoloniesAPI;
import net.minecraftforge.registries.IForgeRegistry;

public interface IResearchEffectRegistry
{
    static IForgeRegistry<ResearchEffectEntry> getInstance()
    {
        return IMinecoloniesAPI.getInstance().getResearchEffectRegistry();
    }
}
