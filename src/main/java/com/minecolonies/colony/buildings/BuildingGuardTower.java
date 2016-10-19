package com.minecolonies.colony.buildings;

import com.minecolonies.client.gui.WindowHutWorkerPlaceholder;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.jobs.AbstractJob;
import com.minecolonies.colony.jobs.JobGuard;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Building class of the guard tower.
 */
public class BuildingGuardTower extends AbstractBuildingWorker
{
    /**
     * Name description of the guard hat.
     */
    private static final String GUARD_TOWER = "TowerGuard";

    /**
     * Max level of the guard hut.
     */
    private static final int GUARD_HUT_MAX_LEVEL = 5;

    /**
     * The max vision bonus multiplier
     */
    private static final int MAX_VISION_BONUS_MULTIPLIER = 3;

    /**
     * The health multiplier each level after level 4.
     */
    private static final int HEALTH_MULTIPLIER = 2;

    /**
     * Base max health of the guard.
     */
    private static final double BASE_MAX_HEALTH = 20D;

    /**
     * Vision bonus per level.
     */
    private static final int VISION_BONUS = 5;

    /**
     * Constructor for the guardTower building.
     *
     * @param c Colony the building is in.
     * @param l Location of the building.
     */
    public BuildingGuardTower(Colony c, BlockPos l)
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
        return GUARD_TOWER;
    }

    /**
     * Gets the max level of the baker's hut.
     *
     * @return The max level of the baker's hut.
     */
    @Override
    public int getMaxBuildingLevel()
    {
        return GUARD_HUT_MAX_LEVEL;
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
        return GUARD_TOWER;
    }

    /**
     * If no vision multiplier give health bonus.
     * @return the bonus health.
     */
    public int getBonusHealth()
    {
        if (getBuildingLevel() > MAX_VISION_BONUS_MULTIPLIER)
        {
            return (getBuildingLevel() - MAX_VISION_BONUS_MULTIPLIER) * HEALTH_MULTIPLIER;
        }
        return 0;
    }

    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        if (this.getWorkerEntity() != null && newLevel > MAX_VISION_BONUS_MULTIPLIER)
        {
            this.getWorkerEntity().getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(BASE_MAX_HEALTH + getBonusHealth());
        }

        super.onUpgradeComplete(newLevel);
    }

    /**
     * Getter for the bonus vision.
     * @return an integer for the additional range.
     */
    public int getBonusVision()
    {
        if(getBuildingLevel() <= MAX_VISION_BONUS_MULTIPLIER)
        {
            return getBuildingLevel() * VISION_BONUS;
        }
        return MAX_VISION_BONUS_MULTIPLIER * VISION_BONUS;
    }

    @Override
    public void setWorker(final CitizenData citizen)
    {
        if(citizen == null && this.getWorkerEntity() != null)
        {
            this.getWorkerEntity().getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(BASE_MAX_HEALTH);
        }
        else if(citizen != null && citizen.getCitizenEntity() != null)
        {
            citizen.getCitizenEntity().getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(BASE_MAX_HEALTH + getBonusHealth());
        }
        super.setWorker(citizen);
    }

    /**
     * Create a Guard job.
     *
     * @param citizen the citizen to take the job.
     * @return The new Guard job.
     */
    @NotNull
    @Override
    public AbstractJob createJob(CitizenData citizen)
    {
        return new JobGuard(citizen);
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
        public View(ColonyView c, BlockPos l)
        {
            super(c, l);
        }

        /**
         * Creates a new window for the building.
         *
         * @return A BlockOut window.
         */
        @NotNull
        public com.blockout.views.Window getWindow()
        {
            return new WindowHutWorkerPlaceholder<>(this, GUARD_TOWER);
        }
    }
}


