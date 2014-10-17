package com.minecolonies.colony.buildings;

import com.minecolonies.client.gui.GuiTownHall;
import com.minecolonies.client.gui.GuiTypable;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.lib.EnumGUI;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

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

        public GuiScreen getGui(int guiId)
        {
            if (guiId == EnumGUI.TOWNHALL.getID())
            {
                return new GuiTownHall(this);
            }
            else if (guiId == EnumGUI.TOWNHALL_RENAME.getID())
            {
                return new GuiTypable(getColony());
            }

            return null;
        }
    }
}
