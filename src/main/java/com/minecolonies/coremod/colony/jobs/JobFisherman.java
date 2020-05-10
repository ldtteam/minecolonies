package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.coremod.entity.ai.citizen.fisherman.EntityAIWorkFisherman;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * The fisherman's job class.
 * implements some useful things for him.
 */
public class JobFisherman extends AbstractJob<EntityAIWorkFisherman, JobFisherman>
{
    /**
     * The water the fisherman is currently fishing at
     * Contains the location of the water so that the fisherman can path to the fishing spot.
     */
    private Tuple<BlockPos, BlockPos> water;

    /**
     * Contains all possible fishing spots.
     * This list is filled during the execution of the fisherman.
     * The fisherman will go from spot to spot and always store the location in this list.
     * After the fisherman has visited an fixed amount of ponds the fisherman will choose a random pond
     * from this list as the next fishing spot.
     * The first position is the water to fish in, the second is the land to stand on.
     */
    @NotNull
    private ArrayList<Tuple<BlockPos, BlockPos>> ponds = new ArrayList<>();

    /**
     * Initializes the job class.
     *
     * @param entity The entity which will use this job class.
     */
    public JobFisherman(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public JobEntry getJobRegistryEntry()
    {
        return ModJobs.fisherman;
    }

    /**
     * Return a Localization textContent for the Job.
     *
     * @return localization textContent String.
     */
    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.Fisherman";
    }

    /**
     * Get the RenderBipedCitizen.Model to use when the Citizen performs this job role.
     *
     * @return Model of the citizen.
     */
    @NotNull
    @Override
    public BipedModelType getModel()
    {
        return BipedModelType.FISHERMAN;
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();

        @NotNull final CompoundNBT waterTag = new CompoundNBT();
        if (water != null)
        {
            BlockPosUtil.write(waterTag, TAG_WATER_POND, water.getA());
            BlockPosUtil.write(waterTag, TAG_PARENT_POND, water.getB());
        }

        @NotNull final ListNBT lakes = new ListNBT();
        for (@NotNull final Tuple<BlockPos, BlockPos> pond : ponds)
        {
            final CompoundNBT compoundNBT = new CompoundNBT();
            BlockPosUtil.write(compoundNBT, TAG_WATER_POND, pond.getA());
            BlockPosUtil.write(compoundNBT, TAG_PARENT_POND, pond.getB());
            lakes.add(compoundNBT);
        }
        compound.put(TAG_PONDS, lakes);

        return compound;
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);

        if (compound.keySet().contains(TAG_WATER_POND))
        {
            water = new Tuple<>(BlockPosUtil.read(compound, TAG_WATER_POND), BlockPosUtil.read(compound, TAG_PARENT_POND));
        }

        ponds = new ArrayList<>();

        if (compound.keySet().contains(TAG_PONDS))
        {
            final ListNBT listOfPonds = compound.getList(TAG_PONDS, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < listOfPonds.size(); i++)
            {
                ponds.add(new Tuple<>(BlockPosUtil.read(listOfPonds.getCompound(i), TAG_WATER_POND), BlockPosUtil.read(listOfPonds.getCompound(i), TAG_PARENT_POND)));
            }
        }
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public EntityAIWorkFisherman generateAI()
    {
        return new EntityAIWorkFisherman(this);
    }

    @Override
    public void triggerDeathAchievement(final DamageSource source, final AbstractEntityCitizen citizen)
    {
        super.triggerDeathAchievement(source, citizen);
    }

    /**
     * Getter for current water.
     *
     * @return Location of the current water block.
     */
    public Tuple<BlockPos, BlockPos> getWater()
    {
        return water;
    }

    /**
     * Setter for current water.
     *
     * @param water New location for the current water block.
     */
    public void setWater(final Tuple<BlockPos, BlockPos> water)
    {
        this.water = water;
    }

    /**
     * Returns a safe copy of all current ponds.
     *
     * @return a list of coordinates.
     */
    @NotNull
    public List<Tuple<BlockPos, BlockPos>> getPonds()
    {
        return new ArrayList<>(ponds);
    }

    /**
     * Add one pond to our list of ponds.
     *
     * @param pond the pond to add.
     */
    public void addToPonds(final BlockPos pond, final BlockPos parent)
    {
        this.ponds.add(new Tuple<>(pond, parent));
    }

    /**
     * remove one pond from the ponds list.
     *
     * @param pond the coordinates matching one pond.
     */
    public void removeFromPonds(final Tuple<BlockPos, BlockPos> pond)
    {
        this.ponds.remove(pond);
    }
}

