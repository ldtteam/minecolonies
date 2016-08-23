package com.minecolonies.entity.ai.basic;

import com.minecolonies.colony.jobs.AbstractJob;
import javax.annotation.Nonnull;

/**
 * Created by marvin on 19.05.16.
 */
public abstract class AbstractEntityAICrafting<J extends AbstractJob> extends AbstractEntityAISkill<J>
{

    /**
     * Sets up some important skeleton stuff for every ai.
     *
     * @param job the job class
     */
    protected AbstractEntityAICrafting(@Nonnull final J job)
    {
        super(job);
    }
}
