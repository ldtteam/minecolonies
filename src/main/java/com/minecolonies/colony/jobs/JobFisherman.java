package com.minecolonies.colony.jobs;
import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.entity.ai.EntityAIWorkFisherman;
import com.minecolonies.entity.ai.Water;
import com.minecolonies.util.ChunkCoordUtils;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.common.util.Constants;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ray on 17.03.2016.
 */

public class JobFisherman extends Job
{
    private static final String TAG_STAGE = "Stage";
    private static final String TAG_WATER = "Water";
    private static final String TAG_PONDS = "Ponds";

    //The water the fisherman is currently at
    private Water water;
    //Contains all possible fishing spots
    private ArrayList<ChunkCoordinates> ponds = new ArrayList<>();

    //Initializes the job class
    public JobFisherman(CitizenData entity)
    {
        super(entity);
    }

    @Override
    public String getName(){ return "com.minecolonies.job.Fisherman"; }

    @Override
    public RenderBipedCitizen.Model getModel()
    {
        //TODO Add Fisherman
        return RenderBipedCitizen.Model.FARMER;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

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

        if(compound.hasKey(TAG_WATER))
        {
            water = Water.readFromNBT(compound.getCompoundTag(TAG_WATER));
        }

        ponds = new ArrayList<>();
        NBTTagList listOfPonds = compound.getTagList(TAG_PONDS, Constants.NBT.TAG_COMPOUND);
        for(int i = 0; i < listOfPonds.tagCount(); i++)
        {
            ponds.add(ChunkCoordUtils.readFromNBTTagList(listOfPonds, i));
        }
    }

    @Override
    public void addTasks(EntityAITasks tasks)
    {
        tasks.addTask(3, new EntityAIWorkFisherman(this));
    }

    public Water getWater() {
        return water;
    }

    public void setWater(Water water) {
        this.water = water;
    }

    public ArrayList<ChunkCoordinates> getPonds() {
        return ponds;
    }

    public void setPonds(ArrayList<ChunkCoordinates> ponds) {
        this.ponds = ponds;
    }

    public void addToPonds(ChunkCoordinates pond)
    {
        this.ponds.add(pond);
    }

    public void removeFromPonds(ChunkCoordinates pond)
    {
        this.ponds.remove(pond);
    }

    public void removeFromPonds(int index)
    {
        this.ponds.remove(index);
    }
}

