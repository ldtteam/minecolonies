package com.minecolonies.colony.buildings;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.EntityWorker;
import com.minecolonies.entity.EntityWorkerPlaceholder;
import com.minecolonies.entity.jobs.ColonyJob;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

public class BuildingBaker extends BuildingWorker
{
    public BuildingBaker(Colony c, ChunkCoordinates l)
    {
        super(c, l);
    }

    @Override
    public String getSchematicName() { return "Baker"; }

    @Override
    public String getJobName() { return "Baker"; }

    @Override
    public EntityWorker createWorker(World world)
    {
        return new EntityWorkerPlaceholder(world); //TODO Implement Later
    }

    public Class<ColonyJob> getJobClass()
    {
        return ColonyJob.class; //TODO Implement Later
    }

    public static class View extends BuildingWorker.View
    {
        public View(ColonyView c, ChunkCoordinates l)
        {
            super(c, l);
        }
    }
}
