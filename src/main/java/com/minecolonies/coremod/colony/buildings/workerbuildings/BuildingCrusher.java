package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.client.gui.WindowHutWorkerPlaceholder;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingFurnaceUser;
import com.minecolonies.coremod.colony.buildings.views.FilterableListView;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobCrusher;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;


/**
 * Class of the crusher building.
 */
public class BuildingCrusher extends AbstractBuildingFurnaceUser
{
    /**
     * The crusher string.
     */
    private static final String CRUSHER_DESC = "Crusher";

    /**
     * Max building level of the crusher.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * Instantiates a new crusher building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingCrusher(final Colony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return CRUSHER_DESC;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    @NotNull
    @Override
    public AbstractJob createJob(final CitizenData citizen)
    {
        return new JobCrusher(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return CRUSHER_DESC;
    }

    @Override
    public boolean canCraftComplexRecipes()
    {
        return true;
    }

    @Override
    public boolean canWorkDuringTheRain()
    {
        return true;
    }

    /**
     * BuildingCrusher View.
     */
    public static class View extends FilterableListView
    {
        /**
         * Instantiate the crusher view.
         *
         * @param c the colonyview to put it in
         * @param l the positon
         */
        public View(final ColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutWorkerPlaceholder<>(this, CRUSHER_DESC);
        }

        @NotNull
        @Override
        public Skill getPrimarySkill()
        {
            return Skill.STRENGTH;
        }

        @NotNull
        @Override
        public Skill getSecondarySkill()
        {
            return Skill.STRENGTH;
        }
    }
}
