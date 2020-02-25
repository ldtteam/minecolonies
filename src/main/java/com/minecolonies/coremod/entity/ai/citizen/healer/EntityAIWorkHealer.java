package com.minecolonies.coremod.entity.ai.citizen.healer;

import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingHospital;
import com.minecolonies.coremod.colony.jobs.JobHealer;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import org.jetbrains.annotations.NotNull;

/**
 * Healer AI class.
 */
public class EntityAIWorkHealer extends AbstractEntityAIInteract<JobHealer>
{
    /**
     * How often should charisma factor into the cook's skill modifier.
     */
    private static final int CHARISMA_MULTIPLIER = 1;

    /**
     * How often should intelligence factor into the cook's skill modifier.
     */
    private static final int INTELLIGENCE_MULTIPLIER = 2;

    /**
     * Constructor for the Cook.
     * Defines the tasks the cook executes.
     *
     * @param job a cook job to use.
     */
    public EntityAIWorkHealer(@NotNull final JobHealer job)
    {
        super(job);
        //super.registerTargets(new AITarget(COOK_SERVE_FOOD_TO_CITIZEN, this::serveFoodToCitizen, SERVE_DELAY));
        worker.getCitizenExperienceHandler().setSkillModifier(CHARISMA_MULTIPLIER * worker.getCitizenData().getCharisma()
                + INTELLIGENCE_MULTIPLIER * worker.getCitizenData().getIntelligence());
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class getExpectedBuildingClass()
    {
        return BuildingHospital.class;
    }
}
