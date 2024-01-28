package com.minecolonies.api.research.registry;

import com.minecolonies.api.IMinecoloniesAPI;
import net.minecraftforge.registries.IForgeRegistry;

public interface IResearchRequirementRegistry
{

    static IForgeRegistry<ResearchRequirementEntry> getInstance()
    {
        return IMinecoloniesAPI.getInstance().getResearchRequirementRegistry();
    }
}
