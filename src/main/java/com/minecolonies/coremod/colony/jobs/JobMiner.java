package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.citizen.miner.EntityAIStructureMiner;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.research.util.ResearchConstants.FIRE_RES;

/**
 * Class used for variables regarding his job.
 */
public class JobMiner extends AbstractJobStructure<EntityAIStructureMiner, JobMiner>
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
    public EntityAIStructureMiner generateAI()
    {
        return new EntityAIStructureMiner(this);
    }

    @Override
    public int getDiseaseModifier()
    {
        return 2;
    }

    @Override
    public boolean ignoresDamage(@NotNull final DamageSource damageSource)
    {
        if (damageSource == DamageSource.LAVA || damageSource == DamageSource.IN_FIRE || damageSource == DamageSource.ON_FIRE)
        {
            return getColony().getResearchManager().getResearchEffects().getEffectStrength(FIRE_RES) > 0;
        }

        return super.ignoresDamage(damageSource);
    }
}
