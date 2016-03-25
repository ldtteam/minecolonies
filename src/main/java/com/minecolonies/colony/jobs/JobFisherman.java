package com.minecolonies.colony.jobs;
import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.entity.ai.EntityAIWorkFisherman;
import com.minecolonies.entity.ai.EntityAIWorkMiner;
import com.minecolonies.entity.ai.Tree;
import com.minecolonies.entity.ai.Water;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import java.util.List;

/**
 * Created by Ray on 17.03.2016.
 */

public class JobFisherman extends Job
{
    private static final String                  TAG_STAGE = "Stage";
    private static final String TAG_WATER = "Water";

    private              EntityAIWorkFisherman.Stage stage     = EntityAIWorkFisherman.Stage.START_WORKING;
    public Water water;

    public JobFisherman(CitizenData entity)
    {
        super(entity);
    }

    @Override
    public String getName(){ return "com.minecolonies.job.Fisherman"; }

    /*@Override
    public RenderBipedCitizen.Model getModel()
    {
        return RenderBipedCitizen.Model.Fisherman;
    }*/

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        NBTTagCompound waterTag = new NBTTagCompound();
        if(water != null)
        {
            water.writeToNBT(waterTag);
        }

        compound.setString(TAG_STAGE, stage.name());
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        if(compound.hasKey(TAG_WATER))
        {
            water = Water.readFromNBT(compound.getCompoundTag(TAG_WATER));
        }

        stage = EntityAIWorkFisherman.Stage.valueOf(compound.getString(TAG_STAGE));
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
    public void addTasks(EntityAITasks tasks)
    {
        tasks.addTask(3, new EntityAIWorkFisherman(this));
    }

    public EntityAIWorkFisherman.Stage getStage()
    {
        return stage;
    }

    public void setStage(EntityAIWorkFisherman.Stage stage)
    {
        this.stage = stage;
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

