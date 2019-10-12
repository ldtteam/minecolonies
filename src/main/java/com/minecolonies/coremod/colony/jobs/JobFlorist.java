package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.client.render.RenderBipedCitizen;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.citizen.florist.EntityAIWorkFlorist;
import net.minecraft.nbt.NBTBase;
import org.jetbrains.annotations.NotNull;

//Todo: implement this class
public class JobFlorist extends AbstractJob
{
    /**
     * Initialize citizen data.
     *
     * @param entity the citizen data.
     */
    public JobFlorist(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public JobEntry getJobRegistryEntry()
    {
        return ModJobs.florist;
    }

    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.florist";
    }

    /**
     * Get the RenderBipedCitizen.Model to use when the Citizen performs this job role.
     *
     * @return Model of the citizen.
     */
    @NotNull
    @Override
    public BipedModelType getModel()
    {
        return BipedModelType.COMPOSTER;
    }

    @Override
    public AbstractAISkeleton<? extends AbstractJob> generateAI()
    {
        return new EntityAIWorkFlorist(this);
    }
}
