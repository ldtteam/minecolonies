package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.jobs.JobAlchemist;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

//TODO

/**
 * Building for the alchemist.
 */
public class BuildingAlchemist extends AbstractBuildingWorker
{
    /**
     * The abstract constructor of the building.
     *
     * @param c the colony
     * @param l the position
     */
    public BuildingAlchemist(@NotNull final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    /**
     * The abstract method which creates a job for the building.
     *
     * @param citizen the citizen to take the job.
     * @return the Job.
     */
    @NotNull
    @Override
    public IJob<?> createJob(final ICitizenData citizen)
    {
        return new JobAlchemist(citizen);
    }

    /**
     * The abstract method which returns the name of the job.
     *
     * @return the job name.
     */
    @NotNull
    @Override
    public String getJobName()
    {
        return "alchemist";
    }

    /**
     * Primary skill getter.
     *
     * @return the primary skill.
     */
    @NotNull
    @Override
    public Skill getPrimarySkill()
    {
        return null; //TODO
    }

    /**
     * Secondary skill getter.
     *
     * @return the secondary skill.
     */
    @NotNull
    @Override
    public Skill getSecondarySkill()
    {
        return null;//TODO
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.alchemist;
    }

    /**
     * Children must return the name of their structure.
     *
     * @return StructureProxy name.
     */
    @Override
    public String getSchematicName()
    {
        return null;
    }

    /**
     * Children must return their max building level.
     *
     * @return Max building level.
     */
    @Override
    public int getMaxBuildingLevel()
    {
        return 5;
    }
}
