package com.minecolonies.colony.buildings;

import com.minecolonies.client.gui.GuiTownHall;
import com.minecolonies.client.gui.GuiTypable;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.lib.EnumGUI;
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

        public Object getGui(EntityPlayer player, World world, int guiId, int x, int y, int z)
        {
            if (guiId == EnumGUI.TOWNHALL.getID())
            {
                return new GuiTownHall(this, player, world, x, y, z);
            }
            else if (guiId == EnumGUI.TOWNHALL_RENAME.getID())
            {
                return new GuiTypable(getColony(), player, world, x, y, z);
            }

            return null;
        }
    }
}
