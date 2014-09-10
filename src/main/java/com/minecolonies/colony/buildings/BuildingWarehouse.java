package com.minecolonies.colony.buildings;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.EntityDeliveryman;
import net.minecraft.util.ChunkCoordinates;

public class BuildingWarehouse extends BuildingWorker
{
    public BuildingWarehouse(Colony c, ChunkCoordinates l)
    {
        super(c, l);
    }

    public String getJobName() { return "Deliveryman"; }

    public EntityCitizen createWorker()
    {
        return new EntityDeliveryman(getColony().getWorld());
    }

    public static class View extends BuildingWorker.View
    {
        public View(ColonyView c, ChunkCoordinates l)
        {
            super(c, l);
        }
    }
}
