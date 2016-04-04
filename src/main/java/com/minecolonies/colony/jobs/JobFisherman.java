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
 * The fishermans job class,
 * implements some useful things for him.
 */
public class JobFisherman extends Job
{
    private static final String TAG_WATER = "Water";
    private static final String TAG_PONDS = "Ponds";

    /**
     * The water the fisherman is currently fishing at
     */
    private Water water;
    /**
     * Contains all possible fishing spots
     */
    private ArrayList<ChunkCoordinates> ponds = new ArrayList<>();

    /**
     * Initializes the job class
     */
    public JobFisherman(CitizenData entity)
    {
        super(entity);
    }

    /**
     * Return a Localization label for the Job
     *
     * @return          localization label String
     */
    @Override
    public String getName(){ return "com.minecolonies.job.Fisherman"; }

    /**
     * Get the RenderBipedCitizen.Model to use when the Citizen performs this job role.
     *
     * @return Model of the citizen
     */
    @Override
    public RenderBipedCitizen.Model getModel()
    {
        //todo: Add Fisherman
        return RenderBipedCitizen.Model.FARMER;
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

        NBTTagCompound waterTag = new NBTTagCompound();
        if (water != null)
        {
            water.writeToNBT(waterTag);
        }

        NBTTagList lakes = new NBTTagList();
        for (ChunkCoordinates pond : ponds)
        {
            ChunkCoordUtils.writeToNBTTagList(lakes, pond);
        }
        compound.setTag(TAG_PONDS, lakes);
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

        if (compound.hasKey(TAG_WATER))
        {
            water = Water.readFromNBT(compound.getCompoundTag(TAG_WATER));
        }

        ponds = new ArrayList<>();
        NBTTagList listOfPonds = compound.getTagList(TAG_PONDS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < listOfPonds.tagCount(); i++)
        {
            ponds.add(ChunkCoordUtils.readFromNBTTagList(listOfPonds, i));
        }
    }

    /**
     * Override to add Job-specific AI tasks to the given EntityAITask list
     *
     * @param tasks EntityAITasks list to add tasks to
     */
    @Override
    public void addTasks(EntityAITasks tasks)
    {
        tasks.addTask(3, new EntityAIWorkFisherman(this));
    }

    /**
     * getter for current water
     */
    public Water getWater()
    {
        return water;
    }

    /**
     * Setter for current water
     */
    public void setWater(Water water)
    {
        this.water = water;
    }

    /**
     * Returns a safe copy of all current ponds
     *
     * @return a list of coordinates
     */
    public List<ChunkCoordinates> getPonds()
    {
        return new ArrayList<>(ponds);
    }

    /**
     * Add one pond to our list of ponds.
     *
     * @param pond the pond to add
     */
    public void addToPonds(ChunkCoordinates pond)
    {
        this.ponds.add(pond);
    }

    /**
     * remove one pond from the ponds list.
     *
     * @param pond the coordinates matching one pond
     */
    public void removeFromPonds(ChunkCoordinates pond)
    {
        this.ponds.remove(pond);
    }

}

