package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.BipedModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.sounds.FishermanSounds;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.citizen.fisherman.EntityAIWorkFisherman;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * The fisherman's job class.
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
     * Initializes the job class.
     *
     * @param entity The entity which will use this job class.
     */
    public JobFisherman(final ICitizenData entity)
    {
        super(entity);
    }

    /**
     * Restore the Job from an CompoundNBT.
     *
     * @param compound CompoundNBT containing saved Job data.
     */
    @Override
    public void read(@NotNull final CompoundNBT compound)
    {
        super.read(compound);

        if (compound.keySet().contains(TAG_WATER))
        {
            water = BlockPosUtil.read(compound, TAG_WATER);
        }

        ponds = new ArrayList<>();
        final ListNBT listOfPonds = compound.getList(TAG_PONDS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < listOfPonds.size(); i++)
        {
            ponds.add(BlockPosUtil.readFromListNBT(listOfPonds, i));
        }
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

    /**
     * Save the Job to an CompoundNBT.
     *
     * @param compound CompoundNBT to save the Job to.
     */
    @Override
    public void write(@NotNull final CompoundNBT compound)
    {

    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();

        @NotNull final CompoundNBT waterTag = new CompoundNBT();
        if (water != null)
        {
            BlockPosUtil.write(waterTag, TAG_WATER, water);
        }

        @NotNull final ListNBT lakes = new ListNBT();
        for (@NotNull final BlockPos pond : ponds)
        {
            BlockPosUtil.writeToListNBT(lakes, pond);
        }
        compound.put(TAG_PONDS, lakes);

        return compound;
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.read(compound);

        if (compound.keySet().contains(TAG_WATER))
        {
            water = BlockPosUtil.read(compound, TAG_WATER);
        }

        ponds = new ArrayList<>();
        final ListNBT listOfPonds = compound.getList(TAG_PONDS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < listOfPonds.size(); i++)
        {
            ponds.add(BlockPosUtil.readFromListNBT(listOfPonds, i));
        }
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public AbstractAISkeleton<JobFisherman> generateAI()
    {
        return new EntityAIWorkFisherman(this);
    }

    /**
     * Override this to let the worker return a bedTimeSound.
     *
     * @return soundEvent to be played.
     */
    @Override
    public SoundEvent getBedTimeSound()
    {
        if (getCitizen() != null)
        {
            return getCitizen().isFemale() ? FishermanSounds.Female.offToBed : FishermanSounds.Male.offToBed;
        }
        return null;
    }

    /**
     * Override this to let the worker return a badWeatherSound.
     *
     * @return soundEvent to be played.
     */
    @Override
    public SoundEvent getBadWeatherSound()
    {
        if (getCitizen() != null)
        {
            return getCitizen().isFemale() ? FishermanSounds.Female.badWeather : FishermanSounds.Male.badWeather;
        }
        return null;
    }

    @Override
    public void triggerDeathAchievement(final DamageSource source, final AbstractEntityCitizen citizen)
    {
        super.triggerDeathAchievement(source, citizen);
        if (source.getTrueSource() instanceof EntityGuardian)
        {
            this.getColony().getStatsManager().triggerAchievement(ModAchievements.achievementFisherDeathGuardian);
        }
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
    public void setWater(final BlockPos water)
    {
        this.water = water;
    }

    /**
     * Returns a safe copy of all current ponds.
     *
     * @return a list of coordinates.
     */
    @NotNull
    public List<BlockPos> getPonds()
    {
        return new ArrayList<>(ponds);
    }

    /**
     * Add one pond to our list of ponds.
     *
     * @param pond the pond to add.
     */
    public void addToPonds(final BlockPos pond)
    {
        this.ponds.add(pond);
    }

    /**
     * remove one pond from the ponds list.
     *
     * @param pond the coordinates matching one pond.
     */
    public void removeFromPonds(final BlockPos pond)
    {
        this.ponds.remove(pond);
    }
}

