package com.minecolonies.coremod.entity.ai.citizen.enchanter;

import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.coremod.colony.jobs.JobEnchanter;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;

/**
 * Enchanter AI class.
 */
public class EntityAIWorkEnchanter extends AbstractEntityAIInteract<JobEnchanter>
{
    /**
     * How often should intelligence factor into the enchanter's skill modifier.
     */
    private static final int INTELLIGENCE_MULTIPLIER = 2;

    /**
     * How often should intelligence factor into the enchanter's skill modifier.
     */
    private static final int CHARISMA_MULTIPLIER = 1;

    /**
     * Creates the abstract part of the AI.
     * Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public EntityAIWorkEnchanter(@NotNull final JobEnchanter job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING)
        );
        worker.getCitizenExperienceHandler().setSkillModifier(CHARISMA_MULTIPLIER * worker.getCitizenData().getCharisma()
                                                                + INTELLIGENCE_MULTIPLIER * worker.getCitizenData().getIntelligence());
        worker.setCanPickUpLoot(true);
    }
}
