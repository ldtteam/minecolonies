package com.minecolonies.colony.buildings;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.jobs.ColonyJob;
import com.minecolonies.util.ChunkCoordUtils;
import com.minecolonies.util.Utils;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.UUID;

public abstract class BuildingWorker extends BuildingHut
{
    private UUID workerId;
    //private WeakReference<EntityCitizen> worker;

    public BuildingWorker(Colony c, ChunkCoordinates l)
    {
        super(c, l);
    }

    public abstract String getJobName();
    //public abstract EntityCitizen createWorker();
    public abstract Class<? extends ColonyJob> getJobClass();

    public UUID getWorkerId() { return workerId; }
    public boolean hasWorker() { return workerId != null; }
    //public EntityCitizen getWorker() { return worker != null ? worker.get() : null; }
    //public boolean hasWorker() { return worker != null && worker.get(); }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        String workerIdStr = compound.getString("workerId");
        if (workerIdStr != null)
        {
            workerId = UUID.fromString(workerIdStr);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        if (workerId != null)
        {
            compound.setString("workerId", workerId.toString());
        }
    }

    public void bindWorker(EntityCitizen citizen)
    {
        workerId = citizen.getUniqueID();
        ////worker = new WeakReference<EntityCitizen>(citizen);
        //citizen.setBuilding(this);
    }

    public void unbindWorker(EntityCitizen citizen)
    {
        workerId = null;
        ////if (worker != null)
        ////{
        ////    EntityCitizen citizen = worker.get();
        ////    if (citizen != null) citizen.setBuilding(null);
        ////    worker = null;
        ////}
        //citizen.setBuilding(null);
    }

    @Override
    public void onWorldTick(TickEvent.WorldTickEvent event)
    {
        super.onWorldTick(event);

        if (event.phase != TickEvent.Phase.END)
        {
            return;
        }

        //  If we have no active worker, grab one from the Colony -- TODO Maybe the Colony should assign jobs out, instead?
        if (!hasWorker())
        {
            EntityCitizen idleCitizen = getColony().getIdleCitizen();
            if (idleCitizen != null)
            {
                idleCitizen.setWorkBuilding(this);
            }
        }
//        else if (worker != null && worker.get() == null)
//        {
//            //  Our worker died... (or was unloaded?)
//            workerId = null;
//            worker = null;
//        }
    }

    /**
     * BuildingWorker View for clients
     */
    public static class View extends BuildingHut.View
    {
        //private int workerId = 0; //  Client uses int Entity IDs

        public View(ColonyView c, ChunkCoordinates l)
        {
            super(c, l);
        }
    }
}
