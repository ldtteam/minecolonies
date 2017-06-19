package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.client.gui.WindowHutFisherman;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobFisherman;
import com.minecolonies.coremod.entity.ai.item.handling.ItemStorage;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * The fishermans building.
 */
public class BuildingFisherman extends AbstractBuildingWorker
{
    /**
     * The maximum upgrade of the building.
     */
    private static final int    MAX_BUILDING_LEVEL = 5;
    /**
     * The job description.
     */
    private static final String FISHERMAN          = "Fisherman";

    private final Map<ItemStorage, Integer> keepX = new HashMap<>();

    /**
     * Public constructor of the building, creates an object of the building.
     *
     * @param c the colony.
     * @param l the position.
     */
    public BuildingFisherman(final Colony c, final BlockPos l)
    {
        super(c, l);

        keepX.put(new ItemStorage(new ItemStack(Items.FISHING_ROD), true), 1);
    }

    /**
     * Getter of the schematic name.
     *
     * @return the schematic name.
     */
    @NotNull
    @Override
    public String getSchematicName()
    {
        return FISHERMAN;
    }

    /**
     * Override this method if you want to keep an amount of items in inventory.
     * When the inventory is full, everything get's dumped into the building chest.
     * But you can use this method to hold some stacks back.
     *
     * @return a list of objects which should be kept.
     */
    @Override
    public Map<ItemStorage, Integer> getRequiredItemsAndAmount()
    {
        return keepX;
    }

    /**
     * Getter of the max building level.
     *
     * @return the integer.
     */
    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    /**
     * @see AbstractBuilding#onUpgradeComplete(int)
     */
    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        super.onUpgradeComplete(newLevel);

        if (newLevel == 1)
        {
            this.getColony().triggerAchievement(ModAchievements.achievementBuildingFisher);
        }
        if (newLevel >= this.getMaxBuildingLevel())
        {
            this.getColony().triggerAchievement(ModAchievements.achievementUpgradeFisherMax);
        }
    }

    /**
     * Getter of the job description.
     *
     * @return the description of the fisherman job.
     */
    @NotNull
    @Override
    public String getJobName()
    {
        return FISHERMAN;
    }

    /**
     * Create the job for the fisherman.
     *
     * @param citizen the citizen to take the job.
     * @return the new job.
     */
    @NotNull
    @Override
    public AbstractJob createJob(final CitizenData citizen)
    {
        return new JobFisherman(citizen);
    }

    /**
     * Provides a view of the fisherman building class.
     */
    public static class View extends AbstractBuildingWorker.View
    {
        /**
         * Public constructor of the view, creates an instance of it.
         *
         * @param c the colony.
         * @param l the position.
         */
        public View(final ColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        /**
         * Gets the blockOut Window.
         *
         * @return the window of the fisherman building.
         */
        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutFisherman(this);
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
            return Skill.DEXTERITY;
        }
    }
}
