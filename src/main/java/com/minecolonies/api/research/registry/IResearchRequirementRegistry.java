package com.minecolonies.api.research.registry;

import com.minecolonies.api.IMinecoloniesAPI;
import net.neoforged.neoforge.registries.IForgeRegistry;

public interface IResearchRequirementRegistry
{

    static IForgeRegistry<ResearchRequirementEntry> getInstance()
    {
        return IMinecoloniesAPI.getInstance().getResearchRequirementRegistry();
    }
}
