package com.minecolonies.coremod.entity.ai.citizen.stonesmeltery;

import com.minecolonies.coremod.colony.jobs.JobStoneSmeltery;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIRequestSmelter;
import org.jetbrains.annotations.NotNull;


/**
 * Crafts furnace stone related block when needed.
 */
public class EntityAIWorkStoneSmeltery extends AbstractEntityAIRequestSmelter<JobStoneSmeltery>
{
    /**
     * Initialize the stone smeltery and add all his tasks.
     *
     * @param jobStoneSmeltery the job he has.
     */
    public EntityAIWorkStoneSmeltery(@NotNull final JobStoneSmeltery jobStoneSmeltery)
    {
        super(jobStoneSmeltery);
        worker.getCitizenExperienceHandler().setSkillModifier(2 * worker.getCitizenData().getDexterity() + worker.getCitizenData().getCharisma());
    }
}
