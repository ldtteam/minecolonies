package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import net.minecraft.util.math.BlockPos;
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
    private static final String SCHEMATIC_NAME = "guardtower";
    private static final int    DEFENCE_BONUS  = 5;
    private static final int    OFFENCE_BONUS  = 0;
    private static final int    MAX_LEVEL      = 5;

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

    @Override
    public int getDefenceBonus()
    {
        return DEFENCE_BONUS;
    }

    @Override
    public int getOffenceBonus()
    {
        return OFFENCE_BONUS;
    }

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
        return Math.max(0, newLevel - 1);
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.guardTower;
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
