package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.client.gui.WindowHutWorkerPlaceholder;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobShepherd;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Creates a new building for the Shepherd.
 */
public class BuildingShepherd extends AbstractBuildingWorker
{
    /**
     * Description of the job executed in the hut.
     */
    private static final String SHEPHERD          = "Shepherd";

    /**
     * Description of the block used to set this block.
     */
    private static final String SHEPHERD_HUT_NAME = "shepherdHut";

    /**
     * Max building level of the hut.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * Instantiates the building.
     * @param c the colony.
     * @param l the location.
     */
    public BuildingShepherd(final Colony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return SHEPHERD;
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
        return SHEPHERD;
    }

    @NotNull
    @Override
    public AbstractJob createJob(final CitizenData citizen)
    {
        return new JobShepherd(citizen);
    }

    /**
     * ClientSide representation of the building.
     */
    public static class View extends AbstractBuildingWorker.View
    {
        /**
         * Instantiates the view of the building.
         * @param c the colonyView.
         * @param l the location of the block.
         */
        public View(final ColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutWorkerPlaceholder<AbstractBuildingWorker.View>(this, SHEPHERD_HUT_NAME);
        }

        @NotNull
        @Override
        public Skill getPrimarySkill()
        {
            return Skill.DEXTERITY;
        }

        @NotNull
        @Override
        public Skill getSecondarySkill()
        {
            return Skill.STRENGTH;
        }
    }
}
