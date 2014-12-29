package com.minecolonies.colony.jobs;

import com.minecolonies.colony.CitizenData;

public class JobPlaceholder extends Job
{
    public JobPlaceholder(CitizenData entity)
    {
        super(entity);
    }

    @Override
    public String getName()
    {
        return "com.minecolonies.job.Placeholder";
    }
}
