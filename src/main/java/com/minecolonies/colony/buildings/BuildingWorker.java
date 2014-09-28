package com.minecolonies.colony.buildings;

import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.jobs.ColonyJob;
import com.minecolonies.util.Utils;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.UUID;

public abstract class BuildingWorker extends BuildingHut
{
    private UUID workerId;
    //private WeakReference<EntityCitizen> worker;

    private final String TAG_WORKER_ID = "workerId";

    public BuildingWorker(Colony c, ChunkCoordinates l)
    {
        super(c, l);
    }

    @Override
    public void onDestroyed()
    {
        //  TODO REFACTOR - Ideally we will have a reference to the CitizenData
        if (hasWorker())
        {
            CitizenData citizen = getColony().getCitizen(workerId);
            if (citizen != null)
            {
                EntityCitizen entity = citizen.getCitizenEntity();
                if (entity != null)
                {
                    //  Possibly not necessary, workers now detect they need to remove themselves
                    entity.removeFromWorkBuilding();
                }
                else
                {
                    citizen.setWorkBuilding(null);
                }
            }

            workerId = null;
        }

        super.onDestroyed();
    }

    public abstract String getJobName();

    //  Classic Style of Jobs
    public /*abstract*/ EntityCitizen createWorker(World world)
    {
        return new EntityCitizen(world); //TODO Implement Later
    }

    //  Future Style of Jobs
    public abstract Class<? extends ColonyJob> getJobClass();
    public ColonyJob createJob(EntityCitizen citizen) { return null; }

    public UUID getWorkerId() { return workerId; }
    public boolean hasWorker() { return workerId != null; }
    //public EntityCitizen getWorker() { return worker != null ? worker.get() : null; }
    //public boolean hasWorker() { return worker != null && worker.get(); }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        
        if (compound.hasKey(TAG_WORKER_ID))
        {
            workerId = UUID.fromString(compound.getString(TAG_WORKER_ID));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        if (workerId != null)
        {
            compound.setString(TAG_WORKER_ID, workerId.toString());
        }
    }

    public void setWorker(EntityCitizen citizen)
    {
        workerId = citizen.getUniqueID();
        ////worker = new WeakReference<EntityCitizen>(citizen);
        citizen.setWorkBuilding(this);
    }

    @Override
    public void removeCitizen(UUID citizenId)
    {
        if (isWorker(citizenId))
        {
            workerId = null;
        }
    }

    public boolean isWorker(EntityCitizen citizen)
    {
        return isWorker(citizen.getUniqueID());
    }

    public boolean isWorker(UUID citizenId)
    {
        return citizenId.equals(workerId);
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
                //ColonyJob job = createJob(idleCitizen);
                //if (job != null)
                {
                    idleCitizen.addToWorkBuilding(this);
                    //idleCitizen.setWorkBuilding(this);
                    //idleCitizen.setColonyJob(job);
                }
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
