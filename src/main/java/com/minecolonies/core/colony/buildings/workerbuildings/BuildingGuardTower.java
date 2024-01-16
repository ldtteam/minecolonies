package com.minecolonies.core.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.core.colony.buildings.AbstractBuildingGuards;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Guard Tower building.
 *
 * @author Asherslab
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class BuildingGuardTower extends AbstractBuildingGuards
{

    /**
     * Our constants. The Schematic names, Defence bonus, and Offence bonus.
     */
    private static final String SCHEMATIC_NAME        = "guardtower";
    private static final int    MAX_LEVEL             = 5;
    private static final int    BONUS_HP_SINGLE_GUARD = 20;

    /**
     * The abstract constructor of the building.
     *
     * @param c the colony
     * @param l the position
     */
    public BuildingGuardTower(@NotNull final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return SCHEMATIC_NAME;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_LEVEL;
    }

    @Override
    public int getClaimRadius(final int newLevel)
    {
        switch (newLevel)
        {
            case 1:
                return 2;
            case 2:
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
                return 5;
            default:
                return 0;
        }
    }

    @Override
    public void onDestroyed()
    {
        super.onDestroyed();
        colony.getBuildingManager().guardBuildingChangedAt(this, 0);
    }

    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        super.onUpgradeComplete(newLevel);
        colony.getBuildingManager().guardBuildingChangedAt(this, newLevel);
    }

    @Override
    public boolean requiresManualTarget()
    {
        return (patrolTargets == null || patrolTargets.isEmpty() || tempNextPatrolPoint != null || !shallPatrolManually()) && tempNextPatrolPoint == null;
    }

    @Override
    public int getBonusHealth()
    {
        return BONUS_HP_SINGLE_GUARD + super.getBonusHealth();
    }

    /**
     * The client view for the bakery building.
     */
    public static class View extends AbstractBuildingGuards.View
    {
        /**
         * The client view constructor for the AbstractGuardBuilding.
         *
         * @param c the colony.
         * @param l the location.
         */
        public View(final IColonyView c, @NotNull final BlockPos l)
        {
            super(c, l);
        }
    }
}
