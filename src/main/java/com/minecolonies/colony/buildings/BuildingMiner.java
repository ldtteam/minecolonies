package com.minecolonies.colony.buildings;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.jobs.ColonyJob;
import com.minecolonies.entity.jobs.ColonyJobPlaceholder;
import net.minecraft.util.ChunkCoordinates;

public class BuildingMiner extends BuildingWorker
{
    public BuildingMiner(Colony c, ChunkCoordinates l)
    {
        super(c, l);
    }

    @Override
    public String getSchematicName() { return "Miner"; }

    @Override
    public String getJobName() { return "Miner"; }

    @Override
    public ColonyJob createJob(EntityCitizen citizen)
    {
        return new ColonyJobPlaceholder(citizen); //TODO Implement Later
    }

    public static class View extends BuildingWorker.View
    {
        public View(ColonyView c, ChunkCoordinates l)
        {
            super(c, l);
        }
    }
}
