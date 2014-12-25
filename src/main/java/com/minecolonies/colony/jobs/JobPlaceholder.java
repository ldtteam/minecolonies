package com.minecolonies.colony.jobs;

import com.minecolonies.colony.CitizenData;

public class JobPlaceholder extends Job
{
    public JobPlaceholder(CitizenData entity)
    {
        super(entity);
    }

    public String getName() { return "Placeholder"; }

    public boolean isNeeded() { return false; }
}
