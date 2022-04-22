package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.coremod.entity.ai.citizen.alchemist.EntityAIWorkAlchemist;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Class of the alchemist job.
 */
public class JobAlchemist extends AbstractJobCrafter<EntityAIWorkAlchemist, JobAlchemist>
{
    /**
     * Instantiates the job for the alchemist.
     *
     * @param entity the citizen who becomes an alchemist
     */
    public JobAlchemist(final ICitizenData entity)
    {
        super(entity);
    }

    @NotNull
    @Override
    public EntityAIWorkAlchemist generateAI()
    {
        return new EntityAIWorkAlchemist(this);
    }

    @NotNull
    @Override
    public ResourceLocation getModel()
    {
        return ModModelTypes.ALCHEMIST_ID;
    }
}
