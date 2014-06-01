package com.minecolonies.tileentities;

import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.util.Utils;
import net.minecraft.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class TileEntityHutWorker extends TileEntityHut
{
    public abstract String getJobName();

    public abstract EntityCitizen createWorker();

    public void bindWorker(EntityCitizen worker)
    {
        this.setHasWorker(true);
        worker.setWorkHut(this);
    }

    public void unbindWorker(EntityCitizen worker)
    {
        this.setHasWorker(false);
        worker.setWorkHut(null);
    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();
        if(!hasWorker() && this.getTownHall() != null)
        {
            attemptToAddIdleCitizen(this.getTownHall());
        }
    }

    public void attemptToAddIdleCitizen(TileEntityTownHall tileEntityTownHall)
    {
        ArrayList<UUID> citizens = tileEntityTownHall.getCitizens();

        List<Entity> entityCitizens = Utils.getEntitiesFromUUID(worldObj, citizens);
        if(entityCitizens != null)
            for(Entity entity : entityCitizens)
            {
                if(entity instanceof EntityCitizen)
                {
                    EntityCitizen entityCitizen = (EntityCitizen) entity;
                    if(entityCitizen.getJob().equals("Citizen") && !hasWorker())
                    {
                        entityCitizen.addToWorkHut(this);
                        return;
                    }
                }
            }
    }

    public void removeWorker(TileEntityTownHall tileEntityTownHall)//TODO store Worker UUID in NBT for easy access and performance
    {
        ArrayList<UUID> citizens = tileEntityTownHall.getCitizens();

        List<Entity> entityCitizens = Utils.getEntitiesFromUUID(worldObj, citizens);
        if(entityCitizens == null) return;

        for(Entity entity : entityCitizens)
        {
            if(entity instanceof EntityCitizen)
            {
                EntityCitizen entityCitizen = (EntityCitizen) entity;
                if(entityCitizen.getWorkHut() != null && entityCitizen.getWorkHut() == this && hasWorker())
                {
                    entityCitizen.removeFromWorkHut(this);
                    return;
                }
            }
        }
    }
}
