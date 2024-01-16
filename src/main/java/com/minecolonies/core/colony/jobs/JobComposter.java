package com.minecolonies.core.colony.jobs;

import net.minecraft.resources.ResourceLocation;
import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.core.colony.buildings.modules.WorkerBuildingModule;
import com.minecolonies.core.entity.ai.citizen.composter.EntityAIWorkComposter;
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

    /**
     * Get the RenderBipedCitizen.Model to use when the Citizen performs this job role.
     *
     * @return Model of the citizen.
     */
    @NotNull
    @Override
    public ResourceLocation getModel()
    {
        return ModModelTypes.COMPOSTER_ID;
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
