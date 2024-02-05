package com.minecolonies.api.colony.jobs.registry;

import com.minecolonies.api.IMinecoloniesAPI;
import net.neoforged.neoforge.registries.IForgeRegistry;

public interface IJobRegistry
{
    static IForgeRegistry<JobEntry> getInstance()
    {
        return IMinecoloniesAPI.getInstance().getJobRegistry();
    }
}
