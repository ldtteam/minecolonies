package com.minecolonies.colony.buildings;

import com.minecolonies.client.gui.WindowTownhall;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import net.minecraft.util.ChunkCoordinates;

public class BuildingTownHall extends BuildingHut
{
    private static final String TOWNHALL = "Townhall";

    public BuildingTownHall(Colony c, ChunkCoordinates l)
    {
        super(c, l);
    }

    @Override
    public String getSchematicName()
    {
        return TOWNHALL;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return 4;
    }

    public static class View extends BuildingHut.View
    {
        public View(ColonyView c, ChunkCoordinates l)
        {
            super(c, l);
        }

        public com.blockout.views.Window getWindow()
        {
            return new WindowTownhall(this);
        }
    }
}
