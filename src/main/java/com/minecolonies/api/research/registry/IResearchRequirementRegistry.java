package com.minecolonies.api.research.registry;

import com.minecolonies.api.IMinecoloniesAPI;
import net.minecraft.core.Registry;

public interface IResearchRequirementRegistry
{

    static Registry<ResearchRequirementEntry> getInstance()
    {
        return IMinecoloniesAPI.getInstance().getResearchRequirementRegistry();
    }
}
