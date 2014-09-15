package com.minecolonies.colony.buildings;

import com.minecolonies.client.gui.GuiHutBuilder;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.entity.jobs.JobBuilder;
import com.minecolonies.lib.EnumGUI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

public class BuildingBuilder extends BuildingWorker
{
    public BuildingBuilder(Colony c, ChunkCoordinates l)
    {
        super(c, l);
    }

    public String getJobName() { return "Builder"; }

    public Class<JobBuilder> getJobClass()
    {
        return JobBuilder.class; //TODO Implement Later
    }

    public int getGuiId() { return EnumGUI.BUILDER.getID(); }

    public static class View extends BuildingWorker.View
    {
        public View(ColonyView c, ChunkCoordinates l)
        {
            super(c, l);
        }

        public Object getGui(EntityPlayer player, World world, int guiId, int x, int y, int z)
        {
            if (guiId == EnumGUI.BUILDER.getID())
            {
                return new GuiHutBuilder(this, player, world, x, y, z);
            }

            return null;
        }
    }
}
