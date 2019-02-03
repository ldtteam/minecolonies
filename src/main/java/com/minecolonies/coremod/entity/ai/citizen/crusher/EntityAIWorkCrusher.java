package com.minecolonies.coremod.entity.ai.citizen.crusher;

import com.minecolonies.coremod.colony.jobs.AbstractJobCrafter;
import com.minecolonies.coremod.colony.jobs.JobCrusher;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAICrafting;
import com.minecolonies.coremod.entity.ai.statemachine.AITarget;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.coremod.entity.ai.statemachine.states.AIWorkerState.*;

/**
 * Crusher AI class.
 */
public class EntityAIWorkCrusher<J extends AbstractJobCrafter> extends AbstractEntityAICrafting<JobCrusher>
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
        super.registerTargets(
          new AITarget(IDLE, START_WORKING)
        );
        worker.getCitizenExperienceHandler().setSkillModifier(STRENGTH_MULTIPLIER * worker.getCitizenData().getStrength()
                + STRENGTH_MULTIPLIER_2 * worker.getCitizenData().getStrength());
        worker.setCanPickUpLoot(true);

        //TODO GUI with all three blocks (cobble => Gravel, Gravel => Sand, Sand => Clay) and qty to be produced per day
        //TODO building needs to store how much to do for each (off = 0)
        //TODO AI checks if it has anything to do (else it complains)
        //TODO If AI has something to do it async requests all inputs (with x amount per day, x qty also depends on building level)
        //TODO worker hits the block on his worker block and after some time it produced 1/2 of the goal block.
        //TODO particle effect on crushing
    }
}
