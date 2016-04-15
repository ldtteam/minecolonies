package com.minecolonies.colony.buildings;

import com.minecolonies.client.gui.WindowTownhall;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import net.minecraft.util.BlockPos;

public class BuildingTownHall extends BuildingHut
{
    private static final String TOWN_HALL = "TownHall";

    public BuildingTownHall(Colony c, BlockPos l)
    {
        super(c, l);
    }

    @Override
    public String getSchematicName()
    {
        return TOWN_HALL;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return 4;
    }

    public static class View extends BuildingHut.View
    {
        public View(ColonyView c, BlockPos l)
        {
            super(c, l);
        }

        public com.blockout.views.Window getWindow()
        {
            return new WindowTownhall(this);
        }
    }
}
