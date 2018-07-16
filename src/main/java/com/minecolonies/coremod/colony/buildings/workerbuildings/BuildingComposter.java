package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class BuildingComposter extends AbstractBuildingWorker
{

    private static final String COMPOSTER         = "Composter";

    /**
     * The abstract constructor of the building.
     *
     * @param c the colony
     * @param l the position
     */
    public BuildingComposter(@NotNull final Colony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public AbstractJob createJob(final CitizenData citizen)
    {
        return null;
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return null;
    }

    @Override
    public String getSchematicName()
    {
        return COMPOSTER;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return 1;
    }

    public class View extends AbstractBuildingWorker.View
    {

        /**
         * Creates the view representation of the building.
         *
         * @param c the colony.
         * @param l the location.
         */
        public View(final ColonyView c, @NotNull final BlockPos l)
        {
            super(c, l);
        }
    }

}
