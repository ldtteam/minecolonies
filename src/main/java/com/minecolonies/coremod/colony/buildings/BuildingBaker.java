package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.client.gui.WindowHutBaker;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobBaker;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Building for the baker.
 */
public class BuildingBaker extends AbstractBuildingWorker
{
    private static final String BAKER          = "Baker";
    private static final String BAKER_HUT_NAME = "bakerHut";

    private static final int BAKER_HUT_MAX_LEVEL = 5;

    private final List<BlockPos> furnaces = new ArrayList<>();

    /**
     * Amounts of dough the Baker left in the oven.
     */
    private int breadsInOvens = 0;

    /**
     * Amount of dough the Baker prepared already.
     */
    private int preparedDough = 0;

    /**
     * Amounts of breads which are baked but need some final preparing.
     */
    private int bakedBreads = 0;

    /**
     * Constructor for the baker building.
     *
     * @param c Colony the building is in.
     * @param l Location of the building.
     */
    public BuildingBaker(final Colony c, final BlockPos l)
    {
        super(c, l);
    }

    /**
     * Gets the name of the schematic.
     *
     * @return Baker schematic name.
     */
    @NotNull
    @Override
    public String getSchematicName()
    {
        return BAKER;
    }

    /**
     * Gets the max level of the baker's hut.
     *
     * @return The max level of the baker's hut.
     */
    @Override
    public int getMaxBuildingLevel()
    {
        return BAKER_HUT_MAX_LEVEL;
    }

    /**
     * The name of the baker's job.
     *
     * @return The name of the baker's job.
     */
    @NotNull
    @Override
    public String getJobName()
    {
        return BAKER;
    }

    /**
     * Create a Baker job.
     *
     * @param citizen the citizen to take the job.
     * @return The new Baker job.
     */
    @NotNull
    @Override
    public AbstractJob createJob(final CitizenData citizen)
    {
        return new JobBaker(citizen);
    }

    /**
     * Clear the furnaces list.
     */
    public void clearFurnaces()
    {
        furnaces.clear();
    }

    /**
     * Add a furnace to the building.
     * @param pos the position of it.
     */
    public void addToFurnaces(final BlockPos pos)
    {
        furnaces.add(pos);
    }

    /**
     * Remove a furnace from the building.
     * @param pos the position of it.
     */
    public void removeFromFurnaces(final BlockPos pos)
    {
        furnaces.remove(pos);
    }

    /**
     * Return a list of furnaces assigned to this hut.
     * @return copy of the list
     */
    public List<BlockPos> getFurnaces()
    {
        return new ArrayList<>(furnaces);
    }

    /**
     * Getter for the breads in oven.
     * @return the amount.
     */
    public int getBreadsInOvens()
    {
        return breadsInOvens;
    }

    /**
     * Setter for the breads in oven.
     * @param breadsInOvens new amount.
     */
    public void setBreadsInOvens(final int breadsInOvens)
    {
        this.breadsInOvens = breadsInOvens;
    }

    /**
     * Getter for the prepared dough.
     * @return the amount.
     */
    public int getPreparedDough()
    {
        return preparedDough;
    }

    /**
     * Setter for the prepared dough.
     * @param preparedDough new amount.
     */
    public void setPreparedDough(final int preparedDough)
    {
        this.preparedDough = preparedDough;
    }

    /**
     * Getter for the baked breads.
     * @return the amount.
     */
    public int getBakedBreads()
    {
        return bakedBreads;
    }

    /**
     * Setter for the bked breads.
     * @param bakedBreads the new amount.
     */
    public void setBakedBreads(final int bakedBreads)
    {
        this.bakedBreads = bakedBreads;
    }

    /**
     * The client view for the baker building.
     */
    public static class View extends AbstractBuildingWorker.View
    {
        /**
         * The client view constructor for the baker building.
         *
         * @param c The ColonyView the building is in.
         * @param l The location of the building.
         */
        public View(final ColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        /**
         * Creates a new window for the building.
         *
         * @return A BlockOut window.
         */
        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutBaker(this);
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
            return Skill.CHARISMA;
        }
    }
}
