package com.minecolonies.colony.buildings;

import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.jobs.AbstractJob;
import com.minecolonies.entity.EntityCitizen;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The abstract class for each worker building.
 */
public abstract class AbstractBuildingWorker extends AbstractBuildingHut
{
    private static final String TAG_WORKER = "worker";
    private CitizenData worker;

    /**
     * The abstract constructor of the building.
     *
     * @param c the colony
     * @param l the position
     */
    public AbstractBuildingWorker(@Nonnull Colony c, BlockPos l)
    {
        super(c, l);
    }

    /**
     * The abstract method which returns the name of the job.
     *
     * @return the job name.
     */
    @Nonnull
    public abstract String getJobName();

    /**
     * The abstract method which creates a job for the building.
     *
     * @param citizen the citizen to take the job.
     * @return the Job.
     */
    @Nonnull
    public abstract AbstractJob createJob(CitizenData citizen);

    /**
     * Returns the worker of the current building.
     *
     * @return {@link CitizenData} of the current building
     */
    public CitizenData getWorker()
    {
        return worker;
    }

    /**
     * Set the worker of the current building.
     *
     * @param citizen {@link CitizenData} of the worker
     */
    public void setWorker(CitizenData citizen)
    {
        if (worker == citizen)
        {
            return;
        }

        // If we have a worker, it no longer works here
        if (worker != null)
        {
            worker.setWorkBuilding(null);
        }

        worker = citizen;

        // If we set a worker, inform it of such
        if (worker != null)
        {
            worker.setWorkBuilding(this);
        }

        markDirty();
    }

    /**
     * Returns the {@link net.minecraft.entity.Entity} of the worker.
     *
     * @return {@link net.minecraft.entity.Entity} of the worker
     */
    @Nullable
    public EntityCitizen getWorkerEntity()
    {
        if (worker == null)
        {
            return null;
        }
        return worker.getCitizenEntity();
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        if (compound.hasKey(TAG_WORKER))
        {
            // Bypass setWorker, which marks dirty
            worker = getColony().getCitizen(compound.getInteger(TAG_WORKER));
            if (worker != null)
            {
                worker.setWorkBuilding(this);
            }
        }
    }

    @Override
    public void writeToNBT(@Nonnull NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        if (worker != null)
        {
            compound.setInteger(TAG_WORKER, worker.getId());
        }
    }

    @Override
    public void onDestroyed()
    {
        if (hasWorker())
        {
            // EntityCitizen will detect the workplace is gone and fix up it's
            // Entity properly
            removeCitizen(worker);
        }

        super.onDestroyed();
    }

    /**
     * Returns whether or not the building has a worker.
     *
     * @return true if building has worker, otherwise false.
     */
    public boolean hasWorker()
    {
        return worker != null;
    }

    /**
     * Returns if the {@link CitizenData} is the same as {@link #worker}.
     *
     * @param citizen {@link CitizenData} you want to compare
     * @return true if same citizen, otherwise false
     */
    public boolean isWorker(CitizenData citizen)
    {
        return citizen == worker;
    }

    @Override
    public void removeCitizen(CitizenData citizen)
    {
        if (isWorker(citizen))
        {
            setWorker(null);
        }
    }

    /**
     * @see AbstractBuilding#onUpgradeComplete(int)
     */
    @Override
    public void onWorldTick(@Nonnull TickEvent.WorldTickEvent event)
    {
        super.onWorldTick(event);

        if (event.phase != TickEvent.Phase.END)
        {
            return;
        }

        // If we have no active worker, grab one from the Colony
        // TODO Maybe the Colony should assign jobs out, instead?
        if (!hasWorker() && (getBuildingLevel() > 0 || this instanceof BuildingBuilder)
              && !this.getColony().isManualHiring())
        {
            final CitizenData joblessCitizen = getColony().getJoblessCitizen();
            if (joblessCitizen != null)
            {
                setWorker(joblessCitizen);
            }
        }
    }

    @Override
    public void serializeToView(@Nonnull ByteBuf buf)
    {
        super.serializeToView(buf);

        buf.writeInt(worker == null ? 0 : worker.getId());
    }

    /**
     * AbstractBuildingWorker View for clients.
     */
    public static class View extends AbstractBuildingHut.View
    {
        private int workerId;

        /**
         * Creates the view representation of the building.
         *
         * @param c the colony.
         * @param l the location.
         */
        public View(ColonyView c, @Nonnull BlockPos l)
        {
            super(c, l);
        }

        /**
         * Returns the id of the worker.
         *
         * @return 0 if there is no worker else the correct citizen id.
         */
        public int getWorkerId()
        {
            return workerId;
        }

        /**
         * Sets the id of the worker.
         *
         * @param workerId the id to set.
         */
        public void setWorkerId(int workerId)
        {
            this.workerId = workerId;
        }

        @Override
        public void deserialize(@Nonnull ByteBuf buf)
        {
            super.deserialize(buf);

            workerId = buf.readInt();
        }
    }
}
