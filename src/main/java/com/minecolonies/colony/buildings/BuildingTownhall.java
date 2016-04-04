package com.minecolonies.colony.buildings;

import com.minecolonies.client.gui.WindowTownhall;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import net.minecraft.util.ChunkCoordinates;

public class BuildingTownhall extends BuildingHut
{
    private static final String TOWNHALL = "Townhall";

    public BuildingTownhall(Colony c, ChunkCoordinates l)
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
