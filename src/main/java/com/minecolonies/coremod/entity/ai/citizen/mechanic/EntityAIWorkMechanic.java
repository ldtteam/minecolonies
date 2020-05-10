package com.minecolonies.coremod.entity.ai.citizen.mechanic;

import com.minecolonies.coremod.colony.jobs.JobMechanic;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAICrafting;
import org.jetbrains.annotations.NotNull;

/**
 * Crafts everything else basically (redstone stuff etc)
 */
public class EntityAIWorkMechanic extends AbstractEntityAICrafting<JobMechanic>
{
    /**
     * Initialize the mechanic and add all his tasks.
     *
     * @param mechanic the job he has.
     */
    public EntityAIWorkMechanic(@NotNull final JobMechanic mechanic)
    {
        super(mechanic);
    }
}
