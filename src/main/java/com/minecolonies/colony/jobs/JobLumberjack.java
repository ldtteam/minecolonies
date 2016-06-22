package com.minecolonies.colony.jobs;

import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.entity.ai.citizen.lumberjack.EntityAIWorkLumberjack;
import com.minecolonies.entity.ai.citizen.lumberjack.Tree;
import net.minecraft.nbt.NBTTagCompound;

/**
 * The Lumberjack job class
 */
public class JobLumberjack extends Job
{
    private static final String TAG_TREE = "Tree";
    /**
     * The tree this lumberjack is currently working on
     */
    public Tree tree;

    /**
     * Create a lumberjack job
     *
     * @param entity the lumberjack
     */
    public JobLumberjack(CitizenData entity)
    {
        super(entity);
    }

    /**
     * Return a Localization textContent for the Job
     *
     * @return localization textContent String
     */
    @Override
    public String getName()
    {
        return "com.minecolonies.job.Lumberjack";
    }

    /**
     * Get the RenderBipedCitizen.Model to use when the Citizen performs this job role.
     *
     * @return Model of the citizen
     */
    @Override
    public RenderBipedCitizen.Model getModel()
    {
        return RenderBipedCitizen.Model.LUMBERJACK;
    }

    /**
     * Save the Job to an NBTTagCompound
     *
     * @param compound NBTTagCompound to save the Job to
     */
    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        NBTTagCompound treeTag = new NBTTagCompound();

        if(tree != null)
        {
            tree.writeToNBT(treeTag);
        }
    }

    /**
     * Restore the Job from an NBTTagCompound
     *
     * @param compound NBTTagCompound containing saved Job data
     */
    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        if(compound.hasKey(TAG_TREE))
        {
            tree = Tree.readFromNBT(compound.getCompoundTag(TAG_TREE));
        }
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @Override
    public AbstractAISkeleton generateAI()
    {
        return new EntityAIWorkLumberjack(this);
    }

}
