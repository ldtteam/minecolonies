package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.citizen.miner.EntityAIStructureMiner;
import org.jetbrains.annotations.NotNull;

/**
 * Class used for variables regarding his job.
 */
public class JobMiner extends AbstractJobStructure
{
    /**
     * Creates a new instance of the miner job.
     *
     * @param entity the entity to add the job to.
     */
    public JobMiner(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public JobEntry getJobRegistryEntry()
    {
        return ModJobs.miner;
    }

    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.Miner";
    }

    @NotNull
    @Override
    public BipedModelType getModel()
    {
        return BipedModelType.MINER;
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public AbstractAISkeleton<JobMiner> generateAI()
    {
        return new EntityAIStructureMiner(this);
    }

    @Override
    public int getDiseaseModifier()
    {
        return 2;
    }
}
