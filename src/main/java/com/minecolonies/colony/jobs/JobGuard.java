package com.minecolonies.colony.jobs;

import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.entity.ai.citizen.guard.EntityAIMeleeGuard;
import com.minecolonies.entity.ai.citizen.guard.EntityAIRangeGuard;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * Job class of the guard.
 */
public class JobGuard extends AbstractJob
{
    /**
     * The higher the number the lower the chance to spawn a knight. Default: 3, 50% chance.
     */
    private static final int GUARD_CHANCE = 3;

    private enum GuardJob
    {
        KNIGHT,
        RANGER,
    }

    private GuardJob task = GuardJob.RANGER;

    /**
     * Public constructor of the farmer job.
     *
     * @param entity the entity to assign to the job.
     */
    public JobGuard(CitizenData entity)
    {
        super(entity);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.job.Guard";
    }

    @NotNull
    @Override
    public RenderBipedCitizen.Model getModel()
    {
        int chance = new Random().nextInt(GUARD_CHANCE);
        if(chance == 1)
        {
            task = GuardJob.KNIGHT;
            return RenderBipedCitizen.Model.KNIGHT_GUARD;
        }
        task = GuardJob.RANGER;
        return RenderBipedCitizen.Model.ARCHER_GUARD;
    }

    /**
     * Override to add Job-specific AI tasks to the given EntityAITask list
     */
    @NotNull
    @Override
    public AbstractAISkeleton generateAI()
    {
        if(task == GuardJob.KNIGHT)
        {
            return new EntityAIMeleeGuard(this);
        }
        return new EntityAIRangeGuard(this);
    }
}
