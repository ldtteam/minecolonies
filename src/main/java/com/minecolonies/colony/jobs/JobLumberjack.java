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
        //TODO save tree - So we don't have half chopped trees
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        stage = EntityAIWorkLumberjack.Stage.valueOf(compound.getString(TAG_STAGE));
        //TODO load tree
    }

    @Override
    public void addTasks(EntityAITasks tasks)
    {
        tasks.addTask(3, new EntityAIWorkLumberjack(this));
    }

    public EntityAIWorkLumberjack.Stage getStage()
    {
        return stage;
    }

    public void setStage(EntityAIWorkLumberjack.Stage stage)
    {
        this.stage = stage;
    }
}
