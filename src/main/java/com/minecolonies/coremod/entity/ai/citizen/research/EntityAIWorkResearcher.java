package com.minecolonies.coremod.entity.ai.citizen.research;

import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.coremod.colony.jobs.JobResearch;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import org.jetbrains.annotations.NotNull;
import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;


public class EntityAIWorkResearcher extends AbstractEntityAIInteract<JobResearch>
{
    /**
     * How often should intelligence factor into the researcher's skill modifier.
     */
    private static final int INTELLIGENCE_MULTIPLIER = 2;

    /**
     * How often should intelligence factor into the researcher's skill modifier.
     */
    private static final int DEXTERITY_MULTIPLIER = 1;

    /**
     * Constructor for the AI
     *
     * @param job the job to fulfill
     */
    public EntityAIWorkResearcher(@NotNull final JobResearch job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING, 1)
        );
        worker.getCitizenExperienceHandler().setSkillModifier(DEXTERITY_MULTIPLIER * worker.getCitizenData().getDexterity()
                                                                + INTELLIGENCE_MULTIPLIER * worker.getCitizenData().getIntelligence());

        worker.setCanPickUpLoot(true);

    }
}
