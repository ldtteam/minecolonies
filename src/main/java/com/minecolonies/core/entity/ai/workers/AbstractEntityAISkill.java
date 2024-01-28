package com.minecolonies.core.entity.ai.workers;

import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.jobs.AbstractJob;
import org.jetbrains.annotations.NotNull;

/**
 * AI class for skills.
 *
 * @param <J> The job this ai has to fulfil.
 */
public abstract class AbstractEntityAISkill<J extends AbstractJob<?, J>, B extends AbstractBuilding> extends AbstractEntityAIBasic<J, B>
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
