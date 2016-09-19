package com.minecolonies.colony.jobs;

import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.entity.ai.citizen.fisherman.EntityAIWorkFisherman;
import com.minecolonies.util.BlockPosUtil;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * The fisherman's job class,
 * implements some useful things for him.
 */
public class JobFisherman extends AbstractJob
{
    /**
     * Final strings to save and retrieve the current water location and pond list.
     */
    private static final String TAG_WATER = "Pond";
    private static final String TAG_PONDS = "Ponds";

    /**
     * The water the fisherman is currently fishing at
     * Contains the location of the water so that the fisherman can path to the fishing spot.
     */
    private BlockPos water;
    /**
     * Contains all possible fishing spots.
     * This list is filled during the execution of the fisherman.
     * The fisherman will go from spot to spot and always store the location in this list.
     * After the fisherman has visited an fixed amount of ponds the fisherman will choose a random pond
     * from this list as the next fishing spot.
     */
    @NotNull
    private ArrayList<BlockPos> ponds = new ArrayList<>();

    /**
     * Initializes the job class
     *
     * @param entity The entity which will use this job class.
     */
    public JobFisherman(CitizenData entity)
    {
        super(entity);
    }

    /**
     * Restore the Job from an NBTTagCompound
     *
     * @param compound NBTTagCompound containing saved Job data
     */
    @Override
    public void readFromNBT(@NotNull NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        if (compound.hasKey(TAG_WATER))
        {
            water = BlockPosUtil.readFromNBT(compound, TAG_WATER);
        }

        ponds = new ArrayList<>();
        NBTTagList listOfPonds = compound.getTagList(TAG_PONDS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < listOfPonds.tagCount(); i++)
        {
            ponds.add(BlockPosUtil.readFromNBTTagList(listOfPonds, i));
        }
    }

    /**
     * Return a Localization textContent for the Job
     *
     * @return localization textContent String
     */
    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.job.Fisherman";
    }

    /**
     * Get the RenderBipedCitizen.Model to use when the Citizen performs this job role.
     *
     * @return Model of the citizen
     */
    @NotNull
    @Override
    public RenderBipedCitizen.Model getModel()
    {
        return RenderBipedCitizen.Model.FISHERMAN;
    }

    /**
     * Save the Job to an NBTTagCompound
     *
     * @param compound NBTTagCompound to save the Job to
     */
    @Override
    public void writeToNBT(@NotNull NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        @NotNull NBTTagCompound waterTag = new NBTTagCompound();
        if (water != null)
        {
            BlockPosUtil.writeToNBT(waterTag, TAG_WATER, water);
        }

        @NotNull NBTTagList lakes = new NBTTagList();
        for (@NotNull BlockPos pond : ponds)
        {
            BlockPosUtil.writeToNBTTagList(lakes, pond);
        }
        compound.setTag(TAG_PONDS, lakes);
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public AbstractAISkeleton generateAI()
    {
        return new EntityAIWorkFisherman(this);
    }

    /**
     * Getter for current water.
     *
     * @return Location of the current water block.
     */
    public BlockPos getWater()
    {
        return water;
    }

    /**
     * Setter for current water.
     *
     * @param water New location for the current water block.
     */
    public void setWater(BlockPos water)
    {
        this.water = water;
    }

    /**
     * Returns a safe copy of all current ponds.
     *
     * @return a list of coordinates
     */
    @NotNull
    public List<BlockPos> getPonds()
    {
        return new ArrayList<>(ponds);
    }

    /**
     * Add one pond to our list of ponds.
     *
     * @param pond the pond to add
     */
    public void addToPonds(BlockPos pond)
    {
        this.ponds.add(pond);
    }

    /**
     * remove one pond from the ponds list.
     *
     * @param pond the coordinates matching one pond
     */
    public void removeFromPonds(BlockPos pond)
    {
        this.ponds.remove(pond);
    }
}

