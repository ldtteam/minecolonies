package com.minecolonies.entity.ai.basic;

import com.minecolonies.colony.jobs.AbstractJob;
import javax.annotation.Nonnull;

/**
 * AI class for skills
 *
 * @param <J> The job this ai has to fulfil
 */
public abstract class AbstractEntityAISkill<J extends AbstractJob> extends AbstractEntityAIBasic<J>
{

    /**
     * Sets up some important skeleton stuff for every ai.
     *
     * @param job the job class
     */
    protected AbstractEntityAISkill(@Nonnull final J job)
    {
        super(job);
    }
}
