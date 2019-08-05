package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.BipedModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.citizen.lumberjack.EntityAIWorkLumberjack;
import com.minecolonies.coremod.entity.ai.citizen.lumberjack.Tree;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The Lumberjack job class.
 */
public class JobLumberjack extends AbstractJob
{
    private static final String TAG_TREE = "Tree";
    /**
     * The tree this lumberjack is currently working on.
     */
    @Nullable
    public Tree tree;

    /**
     * Create a lumberjack job.
     *
     * @param entity the lumberjack.
     */
    public JobLumberjack(final ICitizenData entity)
    {
        super(entity);
    }

    /**
     * Restore the Job from an CompoundNBT.
     *
     * @param compound CompoundNBT containing saved Job data.
     */
    @Override
    public void read(@NotNull final CompoundNBT compound)
    {
        super.read(compound);

        if (compound.keySet().contains(TAG_TREE))
        {
            tree = Tree.read(compound.getCompound(TAG_TREE));
        }
    }

    @Override
    public JobEntry getJobRegistryEntry()
    {
        return ModJobs.lumberjack;
    }

    /**
     * Return a Localization textContent for the Job.
     *
     * @return localization textContent String.
     */
    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.Lumberjack";
    }

    /**
     * Get the RenderBipedCitizen.Model to use when the Citizen performs this job role.
     *
     * @return Model of the citizen.
     */
    @NotNull
    @Override
    public BipedModelType getModel()
    {
        return BipedModelType.LUMBERJACK;
    }

    /**
     * Save the Job to an CompoundNBT.
     *
     * @param compound CompoundNBT to save the Job to.
     */
    @Override
    public void write(@NotNull final CompoundNBT compound)
    {
        super.write(compound);

        @NotNull final CompoundNBT treeTag = new CompoundNBT();

        if (tree != null)
        {
            tree.write(treeTag);
        }
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public AbstractAISkeleton<JobLumberjack> generateAI()
    {
        return new EntityAIWorkLumberjack(this);
    }

    @Override
    public void triggerDeathAchievement(final DamageSource source, final AbstractEntityCitizen citizen)
    {
        super.triggerDeathAchievement(source, citizen);
        if (source == DamageSource.IN_WALL)
        {
            citizen.getCitizenColonyHandler().getColony().getStatsManager().triggerAchievement(ModAchievements.achievementLumberjackDeathTree);
        }
    }
}
