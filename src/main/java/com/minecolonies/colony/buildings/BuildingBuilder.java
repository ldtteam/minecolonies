package com.minecolonies.colony.buildings;

import com.minecolonies.client.gui.WindowHutBuilder;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.jobs.Job;
import com.minecolonies.colony.jobs.JobBuilder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;

public class BuildingBuilder extends BuildingWorker
{
    private static final String TAG_CLEARED = "cleared";
    private static final String BUILDER     = "Builder";
    /**
     * Has the building are been cleared
     */
    private boolean cleared;


    public BuildingBuilder(Colony c, BlockPos l)
    {
        super(c, l);
    }

    @Override
    public String getSchematicName()
    {
        return BUILDER;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return 2;
    }

    @Override
    public String getJobName()
    {
        return BUILDER;
    }

    @Override
    public Job createJob(CitizenData citizen)
    {
        return new JobBuilder(citizen);
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setBoolean(TAG_CLEARED, cleared);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        cleared = compound.getBoolean(TAG_CLEARED);
    }

    /**
     * If the builder has cleared the current area already
     *
     * @return true if so
     */
    public boolean isCleared()
    {
        return cleared;
    }

    /**
     * Sets if the building area has been cleared
     *
     * @param cleared true or false
     */
    public void setCleared(boolean cleared)
    {
        this.cleared = cleared;
    }

    public static class View extends BuildingWorker.View
    {
        public View(ColonyView c, BlockPos l)
        {
            super(c, l);
        }

        public com.blockout.views.Window getWindow()
        {
            return new WindowHutBuilder(this);
        }
    }
}