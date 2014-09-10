package com.minecolonies.colony.buildings;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.entity.EntityCitizen;
import net.minecraft.util.ChunkCoordinates;

public class BuildingStonemason extends BuildingWorker
{
    public BuildingStonemason(Colony c, ChunkCoordinates l)
    {
        super(c, l);
    }

    public String getJobName() { return "Stonemason"; }

    public EntityCitizen createWorker()
    {
        return new EntityCitizen(getColony().getWorld()); //TODO Implement Later
    }

    public static class View extends BuildingWorker.View
    {
        public View(ColonyView c, ChunkCoordinates l)
        {
            super(c, l);
        }
    }
}
