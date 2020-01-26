package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.coremod.client.gui.WindowHutUniversity;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.jobs.JobResearch;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Creates a new building for the university.
 */
public class BuildingUniversity extends AbstractBuildingWorker
{
    /**
     * Description of the job executed in the hut.
     */
    private static final String UNIVERSITY = "university";

    /**
     * Max building level of the hut.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * Instantiates the building.
     * @param c the colony.
     * @param l the location.
     */
    public BuildingUniversity(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return UNIVERSITY;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return UNIVERSITY;
    }

    /**
     * The abstract method which creates a job for the building.
     *
     * @param citizen the citizen to take the job.
     * @return the Job.
     */
    @NotNull
    @Override
    public IJob createJob(final ICitizenData citizen)
    {
        return new JobResearch(citizen);
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.university;
    }

    /**
     * ClientSide representation of the building.
     */
    public static class View extends AbstractBuildingWorker.View
    {
        /**
         * Milk Cows or not.
         */
        private boolean milkCows = true;

        /**
         * Instantiates the view of the building.
         * @param c the colonyView.
         * @param l the location of the block.
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutUniversity(this);
        }

        @NotNull
        @Override
        public Skill getPrimarySkill()
        {
            return Skill.INTELLIGENCE;
        }

        @NotNull
        @Override
        public Skill getSecondarySkill()
        {
            return Skill.CHARISMA;
        }

        @Override
        public void deserialize(@NotNull final PacketBuffer buf)
        {
            super.deserialize(buf);
        }
    }
}
