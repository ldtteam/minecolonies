package com.minecolonies.coremod.entity.ai.citizen.guardNew;

import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract class for all Guard AIs
 */
public class AbstractEntityAIGuard<J extends AbstractJobGuard> extends AbstractEntityAIInteract<J>
{
    /**
     * Creates the abstract part of the AI.
     * Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public AbstractEntityAIGuard(@NotNull final J job)
    {
        super(job);
    }
}
