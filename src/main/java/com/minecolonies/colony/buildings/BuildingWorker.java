package com.minecolonies.colony.buildings;

import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.jobs.Job;
import com.minecolonies.entity.EntityCitizen;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public abstract class BuildingWorker extends BuildingHut
{
    private static final    String      TAG_WORKER = "worker";
    private                 CitizenData worker;

    public abstract         String      getJobName();
    public abstract         Job         createJob(CitizenData citizen);

    public BuildingWorker(Colony c, BlockPos l)
    {
        super(c, l);
    }

    @Override
    public void onDestroyed()
    {
        if (hasWorker())
        {
            //  EntityCitizen will detect the workplace is gone and fix up it's Entity properly
            removeCitizen(worker);
        }

        super.onDestroyed();
    }

    /**
     * Returns the worker of the current building
     *
     * @return          {@link CitizenData} of the current building
     */
    public CitizenData getWorker()
    {
        return worker;
    }

    /**
     * Returns whether or not the building has a worker
     *
     * @return          true if building has worker, otherwise false.
     */
    public boolean hasWorker()
    {
        return worker != null;
    }

    /**
     * Returns the {@link net.minecraft.entity.Entity} of the worker
     *
     * @return          {@link net.minecraft.entity.Entity} of the worker
     */
    public EntityCitizen getWorkerEntity()
    {
        return (worker != null) ? worker.getCitizenEntity() : null;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        
        if (compound.hasKey(TAG_WORKER))
        {
            //  Bypass setWorker, which marks dirty
            worker = getColony().getCitizen(compound.getInteger(TAG_WORKER));
            if (worker != null)
            {
                worker.setWorkBuilding(this);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        if (worker != null)
        {
            compound.setInteger(TAG_WORKER, worker.getId());
        }
    }

    /**
     * Set the worker of the current building
     *
     * @param citizen       {@link CitizenData} of the worker
     */
    public void setWorker(CitizenData citizen)
    {
        if (worker == citizen)
        {
            return;
        }

        //  If we have a worker, it no longer works here
        if (worker != null)
        {
            worker.setWorkBuilding(null);
        }

        worker = citizen;

        //  If we set a worker, inform it of such
        if (worker != null)
        {
            worker.setWorkBuilding(this);
        }

        markDirty();
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
     * Returns if the {@link CitizenData} is the same as {@link #worker}
     *
     * @param           citizen {@link CitizenData} you want to compare
     * @return          true if same citizen, otherwise false
     */
    public boolean isWorker(CitizenData citizen)
    {
        return citizen == worker;
    }

    @Override
    public void onWorldTick(TickEvent.WorldTickEvent event)
    {
        super.onWorldTick(event);

        if (event.phase != TickEvent.Phase.END)
        {
            return;
        }

        //  If we have no active worker, grab one from the Colony
        //  TODO Maybe the Colony should assign jobs out, instead?
        if (!hasWorker() &&
                (getBuildingLevel() > 0 || this instanceof BuildingBuilder))
        {
            if(!this.getColony().isManualHiring())
            {
                CitizenData joblessCitizen = getColony().getJoblessCitizen();
                if (joblessCitizen != null)
                {
                    setWorker(joblessCitizen);
                }
            }
        }
    }

    /**
     * BuildingWorker View for clients
     */
    public static class View extends BuildingHut.View
    {
        private int workerId;

        public View(ColonyView c, BlockPos l)
        {
            super(c, l);
        }

        public int getWorkerId() { return workerId; }

        @Override
        public void deserialize(ByteBuf buf)
        {
            super.deserialize(buf);

            workerId = buf.readInt();
        }
    }

    @Override
    public void serializeToView(ByteBuf buf)
    {
        super.serializeToView(buf);

        buf.writeInt(worker != null ? worker.getId() : 0);
    }
}
