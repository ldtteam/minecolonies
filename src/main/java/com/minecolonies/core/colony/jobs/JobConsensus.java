package com.minecolonies.core.colony.jobs;

import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.core.entity.ai.workers.education.EntityAIConsensus;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Class of the Chef job.
 */
public class JobConsensus extends AbstractJob<EntityAIConsensus, JobConsensus>
{
    public int view = 0;

    /**
     * Instantiates the job for the Chef.
     *
     * @param entity the citizen who becomes a Chef.
     */
    public JobConsensus(final ICitizenData entity)
    {
        super(entity);
    }

    @NotNull
    @Override
    public ResourceLocation getModel()
    {
        return ModModelTypes.BUILDER_ID;
    }

    @Override
    public CompoundTag serializeNBT(final HolderLookup.@NotNull Provider provider)
    {
        return super.serializeNBT(provider);
    }

    @Override
    public void deserializeNBT(final HolderLookup.@NotNull Provider provider, final CompoundTag compound)
    {
        super.deserializeNBT(provider, compound);
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public EntityAIConsensus generateAI()
    {
        return new EntityAIConsensus(this);
    }
}
