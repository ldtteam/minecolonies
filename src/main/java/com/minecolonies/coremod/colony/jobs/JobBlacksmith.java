package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.coremod.client.render.BipedModelType;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.ICitizenData;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.citizen.blacksmith.EntityAIWorkBlacksmith;
import org.jetbrains.annotations.NotNull;

/**
 * Class of the Blacksmith job.
 */
public class JobBlacksmith extends AbstractJobCrafter<EntityAIWorkBlacksmith, JobBlacksmith>
{
    /**
     * Instantiates the job for the Blacksmith.
     *
     * @param entity the citizen who becomes a Sawmill
     */
    public JobBlacksmith(final ICitizenData entity)
    {
        super(entity);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.Blacksmith";
    }

    @NotNull
    @Override
    public BipedModelType getModel()
    {
        return BipedModelType.BLACKSMITH;
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public AbstractAISkeleton<JobBlacksmith> generateAI()
    {
        return new EntityAIWorkBlacksmith(this);
    }
}
