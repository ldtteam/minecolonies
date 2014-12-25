package com.minecolonies.colony.buildings;

import com.blockout.views.Window;
import com.minecolonies.client.gui.WindowTownhall;
import com.minecolonies.client.gui.WindowTownhallNameEntry;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.lib.EnumGUI;
import net.minecraft.util.ChunkCoordinates;

public class BuildingTownHall extends BuildingHut
{
    public BuildingTownHall(Colony c, ChunkCoordinates l)
    {
        super(c, l);
    }

    @Override
    public String getSchematicName() { return "Townhall"; }

    public static class View extends BuildingHut.View
    {
        public View(ColonyView c, ChunkCoordinates l)
        {
            super(c, l);
        }

        public Window getWindow(int guiId)
        {
            if (guiId == EnumGUI.TOWNHALL.getID())
            {
                return new WindowTownhall(this);
            }
            else if (guiId == EnumGUI.TOWNHALL_RENAME.getID())
            {
                return new WindowTownhallNameEntry(getColony());
            }

            return null;
        }
    }
}
