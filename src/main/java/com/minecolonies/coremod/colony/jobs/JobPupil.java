package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.coremod.entity.ai.citizen.school.EntityAIWorkPupil;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Job class of the pupil.
 */
public class JobPupil extends AbstractJob<EntityAIWorkPupil, JobPupil>
{
    /**
     * Public constructor of the pupil job.
     *
     * @param entity the entity to assign to the job.
     */
    public JobPupil(final ICitizenData entity)
    {
        super(entity);
    }

    @NotNull
    @Override
    public ResourceLocation getModel()
    {
        return ModModelTypes.CHILD_ID;
    }

    /**
     * Override to add Job-specific AI tasks to the given EntityAITask list.
     */
    @NotNull
    @Override
    public EntityAIWorkPupil generateAI()
    {
        return new EntityAIWorkPupil(this);
    }
}
