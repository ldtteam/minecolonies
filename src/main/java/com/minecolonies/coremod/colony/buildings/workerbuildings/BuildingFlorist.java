package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobFlorist;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.coremod.colony.buildings.AbstractBuildingStructureBuilder.MAX_BUILDING_LEVEL;

/**
 * The florist building.
 */
public class BuildingFlorist extends AbstractBuildingWorker
{
    /**
     * Florist.
     */
    private static final String FLORIST = "florist";

    /**
     * The abstract constructor of the building.
     *
     * @param c the colony
     * @param l the position
     */
    public BuildingFlorist(@NotNull final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public AbstractJob createJob(final ICitizenData citizen)
    {
        return new JobFlorist(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return FLORIST;
    }

    @Override
    public String getSchematicName()
    {
        return FLORIST;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.florist;
    }
}
