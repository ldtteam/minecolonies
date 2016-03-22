package com.minecolonies.colony.jobs;

import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.entity.ai.EntityAIWorkLumberjack;
import com.minecolonies.entity.ai.Tree;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.nbt.NBTTagCompound;

public class JobLumberjack extends Job
{
    private EntityAIWorkLumberjack.Stage stage = EntityAIWorkLumberjack.Stage.IDLE;
    public Tree tree;

    private static final String TAG_STAGE = "Stage";
    private static final String TAG_TREE = "Tree";

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

        compound.setString(TAG_STAGE, stage.name());

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

        stage = EntityAIWorkLumberjack.Stage.valueOf(compound.getString(TAG_STAGE));
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

    /**
     * Returns the stage of the worker
     * @return  {@link com.minecolonies.entity.ai.EntityAIWorkLumberjack.Stage}
     */
    public EntityAIWorkLumberjack.Stage getStage()
    {
        return stage;
    }

    /**
     * Sets the stage of the worker
     * @param stage     {@link com.minecolonies.entity.ai.EntityAIWorkLumberjack.Stage} to set
     */
    public void setStage(EntityAIWorkLumberjack.Stage stage)
    {
        this.stage = stage;
    }
}
