package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.citizen.quarryMiner.EntityAIStructureQuarryMiner;
import org.jetbrains.annotations.NotNull;

/**
 * Class used for variables regarding his job.
 */
public class JobQuarryMiner extends AbstractJobStructure
{
    /**
     * Creates a new instance of the Quarry Miner's job.
     *
     * @param entity the entity to add the job to.
     */
    public JobQuarryMiner(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public JobEntry getJobRegistryEntry()
    {
        return ModJobs.quarryMiner;
    }

    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.QuarryMiner";
    }

    @NotNull
    @Override
    public BipedModelType getModel()
    {
        //TODO: Change this?
        return BipedModelType.MINER;
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public AbstractAISkeleton<JobQuarryMiner> generateAI()
    {
        return new EntityAIStructureQuarryMiner(this);
    }

    @Override
    public int getDiseaseModifier()
    {
        return 2;
    }
}
