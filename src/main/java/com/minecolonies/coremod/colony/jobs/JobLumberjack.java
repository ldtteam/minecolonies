package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.client.render.RenderBipedCitizen;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.citizen.lumberjack.EntityAIWorkLumberjack;
import com.minecolonies.coremod.entity.ai.citizen.lumberjack.Tree;
import net.minecraft.nbt.NBTTagCompound;
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
    public JobLumberjack(final CitizenData entity)
    {
        super(entity);
    }

    /**
     * Restore the Job from an NBTTagCompound.
     *
     * @param compound NBTTagCompound containing saved Job data.
     */
    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        if (compound.hasKey(TAG_TREE))
        {
            tree = Tree.readFromNBT(compound.getCompoundTag(TAG_TREE));
        }
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
    public RenderBipedCitizen.Model getModel()
    {
        return RenderBipedCitizen.Model.LUMBERJACK;
    }

    /**
     * Save the Job to an NBTTagCompound.
     *
     * @param compound NBTTagCompound to save the Job to.
     */
    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        @NotNull final NBTTagCompound treeTag = new NBTTagCompound();

        if (tree != null)
        {
            tree.writeToNBT(treeTag);
        }
        compound.setTag(TAG_TREE, treeTag);
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
    public void triggerDeathAchievement(final DamageSource source, final EntityCitizen citizen)
    {
        super.triggerDeathAchievement(source, citizen);
        if (source == DamageSource.IN_WALL)
        {
            citizen.getCitizenColonyHandler().getColony().getStatsManager().triggerAchievement(ModAchievements.achievementLumberjackDeathTree);
        }
    }
}
