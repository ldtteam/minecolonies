package com.minecolonies.colony.jobs;

import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.entity.ai.EntityAIWorkLumberjack;
import com.minecolonies.entity.ai.Tree;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.nbt.NBTTagCompound;

public class JobLumberjack extends Job
{
    public                  Tree                            tree;

    private static final    String                          TAG_STAGE   = "Stage";
    private static final    String                          TAG_TREE    = "Tree";

    public JobLumberjack(CitizenData entity)
    {
        super(entity);
    }

    @Override
    public String getName()
    {
        return "com.minecolonies.job.Lumberjack";
    }

    @Override
    public RenderBipedCitizen.Model getModel()
    {
        return RenderBipedCitizen.Model.LUMBERJACK;
    }

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

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        if(compound.hasKey(TAG_TREE))
        {
            tree = Tree.readFromNBT(compound.getCompoundTag(TAG_TREE));
        }
    }

    @Override
    public void addTasks(EntityAITasks tasks)
    {
        tasks.addTask(3, new EntityAIWorkLumberjack(this));
    }
    
}
