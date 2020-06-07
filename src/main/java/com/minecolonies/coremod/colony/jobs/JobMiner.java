package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.citizen.miner.EntityAIStructureMiner;
import com.minecolonies.coremod.research.UnlockAbilityResearchEffect;
import net.minecraft.util.DamageSource;
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
            final UnlockAbilityResearchEffect researchEffect = getColony().getResearchManager().getResearchEffects().getEffect(FIRE_RES, UnlockAbilityResearchEffect.class);
            if (researchEffect != null && researchEffect.getEffect())
            {
                return true;
            }
        }

        return super.ignoresDamage(damageSource);
    }
}
