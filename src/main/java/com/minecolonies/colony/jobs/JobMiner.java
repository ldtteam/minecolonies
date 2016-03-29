package com.minecolonies.colony.jobs;

import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.entity.ai.EntityAIWorkMiner;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

public class JobMiner extends Job
{
    private static final String                  TAG_STAGE = "Stage";
    private              EntityAIWorkMiner.Stage stage     = EntityAIWorkMiner.Stage.START_WORKING;

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

    /**
     * This method can be used to display the current status.
     * That a citizen is having.
     *
     * @return Small string to display info in name tag
     */
    @Override
    public String getNameTagDescription()
    {
        return " [" + getStage() + "]";
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

    /**
    * Returns the stage of the worker
     *
    * @return  {@link com.minecolonies.entity.ai.EntityAIWorkMiner.Stage}
    */
    public EntityAIWorkMiner.Stage getStage()
    {
        return stage;
    }

    /**
     * Sets the stage of the worker
     *
     * @param stage     {@link com.minecolonies.entity.ai.EntityAIWorkMiner.Stage} to set
     */
    public void setStage(EntityAIWorkMiner.Stage stage)
    {
        this.stage = stage;
    }

    /**
     * Adds items if job requires items not in inventory
     *
     * @param stack Stack to check if it is a required item
     */
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
