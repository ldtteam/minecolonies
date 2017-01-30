package com.minecolonies.coremod.entity.ai.basic;

import com.minecolonies.coremod.colony.jobs.AbstractJob;
import org.jetbrains.annotations.NotNull;

/**
 * Basic class for all crafting AIs.
 *
 * @param <J> the job of the AI.
 */
public abstract class AbstractEntityAICrafting<J extends AbstractJob> extends AbstractEntityAISkill<J>
{

    /**
     * Sets up some important skeleton stuff for every ai.
     *
     * @param job the job class
     */
    protected AbstractEntityAICrafting(@NotNull final J job)
    {
        super(job);
    }
}
