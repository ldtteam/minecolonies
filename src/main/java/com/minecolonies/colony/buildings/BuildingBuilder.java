package com.minecolonies.colony.buildings;

import com.blockout.views.Window;
import com.minecolonies.client.gui.WindowHutBuilder;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.jobs.Job;
import com.minecolonies.colony.jobs.JobBuilder;
import com.minecolonies.lib.EnumGUI;
import net.minecraft.util.ChunkCoordinates;

public class BuildingBuilder extends BuildingWorker
{
    public BuildingBuilder(Colony c, ChunkCoordinates l)
    {
        super(c, l);
    }

    @Override
    public String getSchematicName(){ return "Builder"; }

    @Override
    public int getMaxBuildingLevel(){ return 2; }

    @Override
    public String getJobName(){ return "Builder"; }

    @Override
    public Job createJob(CitizenData citizen)
    {
        return new JobBuilder(citizen);
    }

    @Override
    public int getGuiId(){ return EnumGUI.BUILDER.getID(); }

    public static class View extends BuildingWorker.View
    {
        public View(ColonyView c, ChunkCoordinates l)
        {
            super(c, l);
        }

        public Window getWindow(int guiId)
        {
            if (guiId == EnumGUI.BUILDER.getID())
            {
                return new WindowHutBuilder(this);
            }

            return null;
        }
    }
}
