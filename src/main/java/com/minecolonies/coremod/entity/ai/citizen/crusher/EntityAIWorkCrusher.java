package com.minecolonies.coremod.entity.ai.citizen.crusher;

import com.minecolonies.coremod.colony.jobs.JobCrusher;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIBasic;
import org.jetbrains.annotations.NotNull;


/**
 * Cook AI class.
 */
public class EntityAIWorkCrusher extends AbstractEntityAIBasic<JobCrusher>
{
    /**
     * How often should charisma factor into the cook's skill modifier.
     */
    private static final int STRENGTH_MULTIPLIER = 2;

    /**
     * How often should intelligence factor into the cook's skill modifier.
     */
    private static final int STRENGTH_MULTIPLIER_2 = 1;

    /**
     * Constructor for the crusher.
     * Defines the tasks the cook executes.
     *
     * @param job a crusher job to use.
     */
    public EntityAIWorkCrusher(@NotNull final JobCrusher job)
    {
        super(job);
        super.registerTargets();
        worker.getCitizenExperienceHandler().setSkillModifier(STRENGTH_MULTIPLIER * worker.getCitizenData().getStrength()
                + STRENGTH_MULTIPLIER_2 * worker.getCitizenData().getStrength());
        worker.setCanPickUpLoot(true);
    }
}
