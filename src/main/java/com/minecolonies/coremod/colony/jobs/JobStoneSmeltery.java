package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.citizen.stonesmeltery.EntityAIWorkStoneSmeltery;
import org.jetbrains.annotations.NotNull;

/**
 * Class of the Stone Smeltery job.
 */
public class JobStoneSmeltery extends AbstractJobCrafter
{
    /**
     * Instantiates the job for the Stone Smeltery.
     *
     * @param entity the citizen who becomes a Stone Smelter.
     */
    public JobStoneSmeltery(final CitizenData entity)
    {
        super(entity);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.StoneSmeltery";
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public AbstractAISkeleton<JobStoneSmeltery> generateAI()
    {
        return new EntityAIWorkStoneSmeltery(this);
    }
}
