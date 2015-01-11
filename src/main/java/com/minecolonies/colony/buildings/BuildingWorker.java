package com.minecolonies.colony.buildings;

import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.colony.jobs.Job;
import cpw.mods.fml.common.gameevent.TickEvent;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;

public abstract class BuildingWorker extends BuildingHut
{
    private CitizenData worker;

    private static final String TAG_WORKER = "worker";

    public BuildingWorker(Colony c, ChunkCoordinates l)
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

    public abstract String getJobName(); //TODO remove this?

    public abstract Job createJob(CitizenData citizen);

    public CitizenData getWorker() { return worker; }
    public boolean hasWorker() { return worker != null; }

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

    public boolean isWorker(EntityCitizen citizen)
    {
        return isWorker(citizen.getCitizenData());
    }

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
        if (!hasWorker())
        {
            CitizenData joblessCitizen = getColony().getJoblessCitizen();
            if (joblessCitizen != null)
            {
                setWorker(joblessCitizen);
            }
        }
    }

    /**
     * BuildingWorker View for clients
     */
    public static class View extends BuildingHut.View
    {
        private int workerId;

        public View(ColonyView c, ChunkCoordinates l)
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
