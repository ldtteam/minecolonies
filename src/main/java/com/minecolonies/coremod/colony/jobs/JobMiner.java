package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.client.render.RenderBipedCitizen;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.citizen.miner.EntityAIStructureMiner;
import net.minecraft.util.DamageSource;
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

    @NotNull
    @Override
    public RenderBipedCitizen.Model getModel()
    {
        return RenderBipedCitizen.Model.MINER;
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
    public void triggerDeathAchievement(final DamageSource source, final EntityCitizen citizen)
    {
        super.triggerDeathAchievement(source, citizen);
        if (source == DamageSource.LAVA || source == DamageSource.IN_FIRE || source == DamageSource.ON_FIRE)
        {
            citizen.getCitizenColonyHandler().getColony().getStatsManager().triggerAchievement(ModAchievements.achievementMinerDeathLava);
        }
        if (source.equals(DamageSource.FALL))
        {
            citizen.getCitizenColonyHandler().getColony().getStatsManager().triggerAchievement(ModAchievements.achievementMinerDeathFall);
        }
    }
}
