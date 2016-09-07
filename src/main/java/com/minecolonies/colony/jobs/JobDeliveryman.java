package com.minecolonies.colony.jobs;

import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.entity.ai.citizen.deliveryman.EntityAIWorkDeliveryman;
import com.minecolonies.util.BlockPosUtil;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import org.jetbrains.annotations.NotNull;

public class JobDeliveryman extends AbstractJob
{
    private static final String TAG_DESTINATION = "destination";
    private BlockPos destination;

    public JobDeliveryman(CitizenData entity)
    {
        super(entity);
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        if (compound.hasKey(TAG_DESTINATION))
        {
            destination = BlockPosUtil.readFromNBT(compound, TAG_DESTINATION);
        }
    }

    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.job.Deliveryman";
    }

    @NotNull
    @Override
    public RenderBipedCitizen.Model getModel()
    {
        return RenderBipedCitizen.Model.DELIVERYMAN;
    }

    @Override
    public void writeToNBT(@NotNull NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        if (hasDestination())
        {
            BlockPosUtil.writeToNBT(compound, TAG_DESTINATION, destination);
        }
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
        return new EntityAIWorkDeliveryman(this);
    }

    /**
     * Returns whether or not the job has a destination
     *
     * @return true if has destination, otherwise false
     */
    public boolean hasDestination()
    {
        return destination != null;
    }

    public boolean isNeeded()
    {
        Colony colony = getCitizen().getColony();
        return colony != null && !colony.getDeliverymanRequired().isEmpty();
    }

    /**
     * Returns the {@link BlockPos} of the destination
     *
     * @return {@link BlockPos} of the destination
     */
    public BlockPos getDestination()
    {
        return destination;
    }

    /**
     * Sets the destination of the job
     *
     * @param destination {@link BlockPos} of the destination
     */
    public void setDestination(BlockPos destination)
    {
        this.destination = destination;
    }
}
