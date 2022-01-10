package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.IModelType;
import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.citizen.school.EntityAIWorkTeacher;
import org.jetbrains.annotations.NotNull;

/**
 * Job class of the teacher.
 */
public class JobTeacher extends AbstractJob<EntityAIWorkTeacher, JobTeacher>
{
    /**
     * Public constructor of the teacher job.
     *
     * @param entity the entity to assign to the job.
     */
    public JobTeacher(final ICitizenData entity)
    {
        super(entity);
    }

    @NotNull
    @Override
    public IModelType getModel()
    {
        return ModModelTypes.TEACHER;
    }

    /**
     * Override to add Job-specific AI tasks to the given EntityAITask list.
     */
    @NotNull
    @Override
    public EntityAIWorkTeacher generateAI()
    {
        return new EntityAIWorkTeacher(this);
    }
}
