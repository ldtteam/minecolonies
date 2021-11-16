package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.colony.buildings.modules.WorkerBuildingModule;
import com.minecolonies.coremod.entity.ai.citizen.composter.EntityAIWorkComposter;
import org.jetbrains.annotations.NotNull;

public class JobComposter extends AbstractJob<EntityAIWorkComposter, JobComposter>
{

    /**
     * Initialize citizen data.
     *
     * @param entity the citizen data.
     */
    public JobComposter(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public JobEntry getJobRegistryEntry()
    {
        return ModJobs.composter;
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
    public EntityAIWorkComposter generateAI()
    {
        return new EntityAIWorkComposter(this);
    }

    @Override
    public int getDiseaseModifier()
    {
        final int skill = getCitizen().getCitizenSkillHandler().getLevel(getCitizen().getWorkBuilding().getModuleMatching(WorkerBuildingModule.class, m -> m.getJobEntry() == this.getJobRegistryEntry()).getPrimarySkill());
        return (int) ((100 - skill)/25.0);
    }
}
