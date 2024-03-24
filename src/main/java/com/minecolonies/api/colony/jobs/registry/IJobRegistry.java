package com.minecolonies.api.colony.jobs.registry;

import com.minecolonies.api.IMinecoloniesAPI;
import net.minecraft.core.Registry;

public interface IJobRegistry
{
    static Registry<JobEntry> getInstance()
    {
        return IMinecoloniesAPI.getInstance().getJobRegistry();
    }
}
