package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.Model;
import com.minecolonies.api.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.colony.CitizenData;
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
    public JobMiner(final CitizenData entity)
    {
        super(entity);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.Miner";
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

    @NotNull
    @Override
    public Model getModel()
    {
        return Model.MINER;
    }
}
