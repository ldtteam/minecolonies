package com.minecolonies.coremod.entity.ai.basic;

import com.minecolonies.coremod.colony.jobs.AbstractJob;
import org.jetbrains.annotations.NotNull;

/**
 * AI class for skills.
 *
 * @param <J> The job this ai has to fulfil.
 */
public abstract class AbstractEntityAISkill<J extends AbstractJob> extends AbstractEntityAIBasic<J>
{

    /**
     * Sets up some important skeleton stuff for every ai.
     *
     * @param job the job class.
     */
    protected AbstractEntityAISkill(@NotNull final J job)
    {
        super(job);
    }
}
