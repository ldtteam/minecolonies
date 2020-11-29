package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.entity.ai.citizen.enchanter.EntityAIWorkEnchanter;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_BUILDING_TO_DRAIN;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_WAITING_TICKS;

public class JobEnchanter extends AbstractJobCrafter<EntityAIWorkEnchanter, JobEnchanter>
{
    /**
     * Max waiting ticks.
     */
    private static final int MAX_WAITING_TICKS = 60;

    /**
     * Pos of the worker to drain from.
     */
    private BlockPos posToDrainFrom = null;

    /**
     * The waiting ticks of the enchanter at the current pos.
     */
    private int waitingTicks = 0;

    /**
     * Initialize citizen data.
     *
     * @param entity the citizen data.
     */
    public JobEnchanter(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public JobEntry getJobRegistryEntry()
    {
        return ModJobs.enchanter;
    }

    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.enchanter";
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
        return BipedModelType.STUDENT;
    }

    @Override
    public EntityAIWorkEnchanter generateAI()
    {
        return new EntityAIWorkEnchanter(this);
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);
        if (compound.keySet().contains(TAG_BUILDING_TO_DRAIN))
        {
            this.posToDrainFrom = BlockPosUtil.read(compound, TAG_BUILDING_TO_DRAIN);
        }
        this.waitingTicks = compound.getInt(TAG_WAITING_TICKS);
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();
        if (posToDrainFrom != null)
        {
            BlockPosUtil.write(compound, TAG_BUILDING_TO_DRAIN, posToDrainFrom);
        }
        compound.putInt(TAG_WAITING_TICKS, this.waitingTicks);
        return compound;
    }

    /**
     * Set the building the worker is currently draining.
     *
     * @param pos the pos of the building to drain.
     */
    public void setBuildingToDrainFrom(final BlockPos pos)
    {
        this.posToDrainFrom = pos;
    }

    /**
     * Get the current set pos to drain from.
     *
     * @return the pos.
     */
    public BlockPos getPosToDrainFrom()
    {
        return posToDrainFrom;
    }

    /**
     * Increment the waiting ticks.
     *
     * @return false if limit reached.
     */
    public boolean incrementWaitingTicks()
    {
        if (++waitingTicks > MAX_WAITING_TICKS)
        {
            waitingTicks = 0;
            return false;
        }
        return true;
    }
}
