package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;

//Todo: implement this class
public class JobFlorist extends AbstractJob
{
    /**
     * Initialize citizen data.
     *
     * @param entity the citizen data.
     */
    public JobFlorist(final CitizenData entity)
    {
        super(entity);
    }

    @Override
    public String getName()
    {
        return null;
    }

    @Override
    public AbstractAISkeleton<? extends AbstractJob> generateAI()
    {
        return null;
    }
}
