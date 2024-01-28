package com.minecolonies.core.colony.jobs;

import net.minecraft.resources.ResourceLocation;
import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.core.entity.ai.workers.production.agriculture.EntityAIWorkFarmer;
import org.jetbrains.annotations.NotNull;

/**
 * Job class of the farmer, handles his fields.
 */
public class JobFarmer extends AbstractJobCrafter<EntityAIWorkFarmer, JobFarmer>
{
    /**
     * Public constructor of the farmer job.
     *
     * @param entity the entity to assign to the job.
     */
    public JobFarmer(final ICitizenData entity)
    {
        super(entity);
    }

    @NotNull
    @Override
    public ResourceLocation getModel()
    {
        return ModModelTypes.FARMER_ID;
    }

    /**
     * Override to add Job-specific AI tasks to the given EntityAITask list.
     */
    @NotNull
    @Override
    public EntityAIWorkFarmer generateAI()
    {
        return new EntityAIWorkFarmer(this);
    }
}
