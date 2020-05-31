package com.minecolonies.coremod.entity.ai.citizen.beekeeper;

import com.minecolonies.coremod.colony.jobs.JobBeekeeper;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import org.jetbrains.annotations.NotNull;

public class EntityAIWorkBeekeeper extends AbstractEntityAIInteract<JobBeekeeper> {
    /**
     * Creates the abstract part of the AI.
     * Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public EntityAIWorkBeekeeper(@NotNull JobBeekeeper job) {
        super(job);
    }
}
