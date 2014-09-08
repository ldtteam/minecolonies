package com.minecolonies.colony.buildings;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import net.minecraft.util.ChunkCoordinates;

public class BuildingBlacksmith extends BuildingWorker
{
    public BuildingBlacksmith(Colony c, ChunkCoordinates l)
    {
        super(c, l);
    }

    public static class View extends BuildingWorker.View
    {
        public View(ColonyView c, ChunkCoordinates l)
        {
            super(c, l);
        }
    }
}
