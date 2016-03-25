package com.minecolonies.colony.jobs;
import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.entity.ai.EntityAIWorkFisherman;
import com.minecolonies.entity.ai.EntityAIWorkMiner;
import com.minecolonies.entity.ai.Tree;
import com.minecolonies.entity.ai.Water;
import com.minecolonies.util.ChunkCoordUtils;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Ray on 17.03.2016.
 */

public class JobFisherman extends Job
{
    private static final String TAG_STAGE = "Stage";
    private static final String TAG_WATER = "Water";
    private static final String TAG_PONDS = "Ponds";

    private EntityAIWorkFisherman.Stage stage     = EntityAIWorkFisherman.Stage.START_WORKING;
    public Water water;
    public ArrayList<ChunkCoordinates> ponds = new ArrayList<>();
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
        compound.setString(TAG_STAGE, stage.name());

        NBTTagCompound waterTag = new NBTTagCompound();
        if(water != null)
        {
            water.writeToNBT(waterTag);
        }

        NBTTagList lakes = new NBTTagList();
        for(ChunkCoordinates pond : ponds)
        {
            ChunkCoordUtils.writeToNBTTagList(lakes, pond);
        }
        compound.setTag(TAG_PONDS, lakes);

    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        stage = EntityAIWorkFisherman.Stage.valueOf(compound.getString(TAG_STAGE));

        if(compound.hasKey(TAG_WATER))
        {
            water = Water.readFromNBT(compound.getCompoundTag(TAG_WATER));
        }

        ponds = new ArrayList<ChunkCoordinates>();
        NBTTagList listofponds = compound.getTagList(TAG_PONDS, Constants.NBT.TAG_COMPOUND);
        for(int i = 0; i < listofponds.tagCount(); i++)
        {
            ponds.add(ChunkCoordUtils.readFromNBTTagList(listofponds, i));
        }
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

