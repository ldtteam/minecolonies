package com.minecolonies.colony.jobs;

import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.entity.ai.EntityAIWorkFarmer;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

public class JobFarmer extends Job
{
    private static final    String                      TAG_STAGE   = "Stage";

    private                 EntityAIWorkFarmer.Stage    stage       = EntityAIWorkFarmer.Stage.WORKING;

    public JobFarmer(CitizenData entity)
    {
        super(entity);
    }

    @Override
    public String getName(){ return "com.minecolonies.job.Farmer"; }

    @Override
    public RenderBipedCitizen.Model getModel()
    {
        return RenderBipedCitizen.Model.FARMER;
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
        stage = EntityAIWorkFarmer.Stage.valueOf(compound.getString(TAG_STAGE));
    }

    @Override
    public void addTasks(EntityAITasks tasks)
    {
        tasks.addTask(3, new EntityAIWorkFarmer(this));
    }

    /**
     * Sets the stage of the worker
     *
     * @param stage     {@link com.minecolonies.entity.ai.EntityAIWorkFarmer.Stage} to set
     */
    public void setStage(EntityAIWorkFarmer.Stage stage)
    {
        this.stage = stage;
    }

    /**
     * Returns the stage of the worker
     *
     * @return  {@link com.minecolonies.entity.ai.EntityAIWorkFarmer.Stage}
     */
    public EntityAIWorkFarmer.Stage getStage()
    {
        return stage;
    }

    /**
     * Adds items if job requires items not in inventory
     *
     * @param stack Stack to check if it is a required item
     */
    public void addItemNeededIfNotAlready(ItemStack stack)
    {
        List<ItemStack> itemsNeeded = super.getItemsNeeded();

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
