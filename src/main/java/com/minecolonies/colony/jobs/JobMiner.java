package com.minecolonies.colony.jobs;

import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.entity.ai.EntityAIWorkMiner;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;

import java.util.List;

public class JobMiner extends Job
{
    public List<ChunkCoordinates> vein;//TODO do these need to be saved?
    public int veinId=0;
    private EntityAIWorkMiner.Stage stage = EntityAIWorkMiner.Stage.START_WORKING;
    private static final String TAG_STAGE = "Stage";

    public JobMiner(CitizenData entity)
    {
        super(entity);
    }

    @Override
    public String getName(){ return "com.minecolonies.job.Miner"; }

    @Override
    public RenderBipedCitizen.Model getModel()
    {
        return RenderBipedCitizen.Model.MINER;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setString(TAG_STAGE, stage.name());
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        stage = EntityAIWorkMiner.Stage.valueOf(compound.getString(TAG_STAGE));
    }

    @Override
    public void addTasks(EntityAITasks tasks)
    {
        tasks.addTask(3, new EntityAIWorkMiner(this));
    }

    public void setStage(EntityAIWorkMiner.Stage stage)
    {
        this.stage = stage;
    }

    public EntityAIWorkMiner.Stage getStage()
    {
        return stage;
    }

    public void addItemNeededIfNotAlready(ItemStack stack)
    {
        List<ItemStack> itemsNeeded = super.getItemsNeeded();

        //check if stack is already in itemsNeeded
        for(ItemStack neededItem : itemsNeeded)
        {
            if(stack.isItemEqual(neededItem))
            {
                return;
            }
        }
        addItemNeeded(stack);
    }
}
