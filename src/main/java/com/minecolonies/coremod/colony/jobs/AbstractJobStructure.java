package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.util.StructureWrapper;

/**
 * Common job object for all structure AIs.
 */
public abstract class AbstractJobStructure extends AbstractJob
{
    /**
     * The structure the job should build.
     */
    protected StructureWrapper structure;

    /**
     * Initialize citizen data.
     *
     * @param entity the citizen data.
     */
    public AbstractJobStructure(final CitizenData entity)
    {
        super(entity);
    }

    /**
     * Does this job has a loaded StructureProxy?
     * <p>
     * if a structure is not null there exists a location for it
     *
     * @return true if there is a loaded structure for this Job
     */
    public boolean hasStructure()
    {
        return structure != null;
    }

    /**
     * Get the StructureProxy loaded by the Job.
     *
     * @return StructureProxy loaded by the Job
     */
    public StructureWrapper getStructure()
    {
        return structure;
    }

    /**
     * Set the structure of the structure job.
     *
     * @param structure {@link StructureWrapper} object
     */
    public void setStructure(final StructureWrapper structure)
    {
        this.structure = structure;
    }
}
