package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.colony.buildings.modules.WorkerBuildingModule;
import com.minecolonies.coremod.entity.ai.citizen.crusher.EntityAIWorkCrusher;
import org.jetbrains.annotations.NotNull;

/**
 * The crusher job class.
 */
public class JobCrusher extends AbstractJobCrafter<EntityAIWorkCrusher, JobCrusher>
{
    /**
     * Create a crusher job.
     *
     * @param entity the lumberjack.
     */
    public JobCrusher(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public int getDiseaseModifier()
    {
        final int skill = getCitizen().getCitizenSkillHandler().getLevel(getCitizen().getWorkBuilding().getModuleMatching(WorkerBuildingModule.class, m -> m.getJobEntry() == getJobRegistryEntry()).getPrimarySkill());
        return (int) ((100 - skill)/25.0);
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
        return ModModelTypes.SMELTER;
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public EntityAIWorkCrusher generateAI()
    {
        return new EntityAIWorkCrusher(this);
    }
}
