package com.minecolonies.coremod.entity.ai.citizen.stonesmeltery;

import com.minecolonies.coremod.colony.jobs.JobStoneSmeltery;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAICrafting;
import org.jetbrains.annotations.NotNull;

/**
 * Crafts furnace stone related block when needed.
 */
public class EntityAIWorkStoneSmeltery extends AbstractEntityAICrafting<JobStoneSmeltery>
{
    /**
     * Initialize the stone smeltery and add all his tasks.
     *
     * @param sawmill the job he has.
     */
    public EntityAIWorkStoneSmeltery(@NotNull final JobStoneSmeltery sawmill)
    {
        super(sawmill);
        worker.getCitizenExperienceHandler().setSkillModifier(2 * worker.getCitizenData().getDexterity() + worker.getCitizenData().getCharisma());
    }
}
