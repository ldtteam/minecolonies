package com.minecolonies.colony.buildings;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import net.minecraft.util.ChunkCoordinates;

public class BuildingTownHall extends BuildingHut
{
    public BuildingTownHall(Colony c, ChunkCoordinates l)
    {
        super(c, l);
    }

    public class View extends BuildingHut.View
    {
        protected View(ColonyView c, ChunkCoordinates l)
        {
            super(c, l);
        }
    }
}
