package com.minecolonies.core.colony.jobs;

import net.minecraft.resources.ResourceLocation;
import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.core.entity.ai.citizen.florist.EntityAIWorkFlorist;
import org.jetbrains.annotations.NotNull;

public class JobFlorist extends AbstractJob<EntityAIWorkFlorist, JobFlorist>
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

    @NotNull
    @Override
    public ResourceLocation getModel()
    {
        return ModModelTypes.FLORIST_ID;
    }

    @Override
    public EntityAIWorkFlorist generateAI()
    {
        return new EntityAIWorkFlorist(this);
    }
}
