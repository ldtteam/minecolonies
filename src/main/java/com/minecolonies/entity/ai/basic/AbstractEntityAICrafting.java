package com.minecolonies.entity.ai.basic;

import com.minecolonies.colony.jobs.Job;

/**
 * Created by marvin on 19.05.16.
 */
public abstract class AbstractEntityAICrafting<J extends Job> extends AbstractEntityAISkill<J>
{

    /**
     * Sets up some important skeleton stuff for every ai.
     *
     * @param job the job class
     */
    protected AbstractEntityAICrafting(J job)
    {
        super(job);
    }
}
