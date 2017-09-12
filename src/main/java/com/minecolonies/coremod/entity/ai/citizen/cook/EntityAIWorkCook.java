package com.minecolonies.coremod.entity.ai.citizen.cook;

import com.minecolonies.coremod.colony.jobs.JobCook;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAISkill;
import com.minecolonies.coremod.entity.ai.util.AITarget;

import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_ENTITY_BAKER_NO_FURNACES;
import static com.minecolonies.coremod.entity.ai.util.AIState.*;

/**
 * Cook AI class.
 */
public class EntityAIWorkCook extends AbstractEntityAISkill<JobCook>
{
    /**
     * How often should charisma factor into the cook's skill modifier.
     */
    private static final int CHARISMA_MULTIPLIER = 2;

    /**
     * How often should intelligence factor into the cook's skill modifier.
     */
    private static final int INTELLIGENCE_MULTIPLIER = 1;

    /**
     * Constructor for the Fisherman.
     * Defines the tasks the fisherman executes.
     *
     * @param job a fisherman job to use.
     */
    public EntityAIWorkCook(@NotNull final JobCook job)
    {
        super(job);
        super.registerTargets(
                new AITarget(IDLE, START_WORKING)
        );
        worker.setSkillModifier(
                CHARISMA_MULTIPLIER * worker.getCitizenData().getCharisma()
                        + INTELLIGENCE_MULTIPLIER * worker.getCitizenData().getIntelligence());
        worker.setCanPickUpLoot(true);
    }
}
