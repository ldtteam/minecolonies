package com.minecolonies.entity.ai.basic;

import com.minecolonies.colony.jobs.Job;

/**
 * Created by marvin on 19.05.16.
 */
public abstract class AbstractEntityAISkill<J extends Job> extends AbstractEntityAIBasic<J>
{

    /**
     * Sets up some important skeleton stuff for every ai.
     *
     * @param job the job class
     */
    protected AbstractEntityAISkill(J job)
    {
        super(job);
    }
}
