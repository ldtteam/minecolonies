package com.minecolonies.colony.buildings;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import net.minecraft.util.ChunkCoordinates;

public class BuildingHut extends Building
{
    public BuildingHut(Colony c, ChunkCoordinates l)
    {
        super(c, l);
    }

    public static class View extends Building.View
    {
        protected View(ColonyView c, ChunkCoordinates l)
        {
            super(c, l);
        }
    }
}
