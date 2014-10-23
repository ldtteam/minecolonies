package com.minecolonies.colony.buildings;

import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.EntityWorker;
import com.minecolonies.entity.EntityWorkerPlaceholder;
import com.minecolonies.entity.jobs.ColonyJob;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

import java.util.UUID;

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

    public abstract String getJobName();

    //  Classic Style of Jobs
    public abstract EntityWorker createWorker(World world);

    //  Future Style of Jobs
    public abstract Class<? extends ColonyJob> getJobClass();
    public ColonyJob createJob(EntityCitizen citizen) { return null; }

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
            UUID workerId = UUID.fromString(compound.getString(TAG_WORKER));

            //  Bypass setWorker, which marks dirty
            worker = getColony().getCitizen(workerId);
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
            compound.setString(TAG_WORKER, worker.getId().toString());
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
        private UUID workerId;

        public View(ColonyView c, ChunkCoordinates l)
        {
            super(c, l);
        }

        public UUID getWorkerId() { return workerId; }

        public void parseNetworkData(NBTTagCompound compound)
        {
            super.parseNetworkData(compound);

            workerId = compound.hasKey(TAG_WORKER) ? UUID.fromString(compound.getString(TAG_WORKER)) : null;
        }
    }


    public void createViewNetworkData(NBTTagCompound compound)
    {
        //  TODO - Use a PacketBuffer
        super.createViewNetworkData(compound);

        if (worker != null)
        {
            compound.setString(TAG_WORKER, worker.getId().toString());
        }
    }
}
