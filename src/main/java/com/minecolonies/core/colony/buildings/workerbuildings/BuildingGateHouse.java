package com.minecolonies.core.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.core.colony.buildings.modules.GuardBuildingModule;
import com.minecolonies.core.colony.buildings.modules.settings.GuardTaskSetting;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.EquipmentLevelConstants.BASIC_TOOL_LEVEL;
import static com.minecolonies.api.util.constant.EquipmentLevelConstants.TOOL_LEVEL_MAXIMUM;
import static com.minecolonies.api.util.constant.SchematicTagConstants.TAG_ARCHER;
import static com.minecolonies.api.util.constant.SchematicTagConstants.TAG_KNIGHT;
import static com.minecolonies.core.colony.buildings.modules.BuildingModules.*;

/**
 * Gate house building.
 *
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class BuildingGateHouse extends AbstractBuildingGuards
{

    /**
     * Our constants. The Schematic names, Defence bonus, and Offence bonus.
     */
    private static final String SCHEMATIC_NAME        = "gatehouse";
    private static final int    MAX_LEVEL             = 3;

    /**
     * The abstract constructor of the building.
     *
     * @param c the colony
     * @param l the position
     */
    public BuildingGateHouse(@NotNull final IColony c, final BlockPos l)
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
    public int getMaxEquipmentLevel()
    {
        if (getBuildingLevel() >= getMaxBuildingLevel())
        {
            return TOOL_LEVEL_MAXIMUM;
        }
        else if (getBuildingLevelEquivalent() <= WOOD_HUT_LEVEL)
        {
            return BASIC_TOOL_LEVEL;
        }
        return getBuildingLevelEquivalent() - WOOD_HUT_LEVEL;
    }

    @Override
    public int getClaimRadius(final int newLevel)
    {
        return switch (newLevel)
        {
            case 1, 2 -> 1;
            case 3 -> 2;
            default -> 0;
        };
    }

    @Override
    public int getBonusVision()
    {
        return BASE_VISION_RANGE + getBuildingLevelEquivalent() * VISION_RANGE_PER_LEVEL;
    }

    @Override
    public int getBonusHealth()
    {
        return getBuildingLevelEquivalent() * BONUS_HEALTH_PER_LEVEL;
    }

    @Override
    public int getBuildingLevelEquivalent()
    {
        return switch (getBuildingLevel())
        {
            case 1 -> 1;
            case 2 -> 3;
            case 3 -> 5;
            default -> 0;
        };
    }

    @Override
    public int getBuildingLevel()
    {
        return super.getBuildingLevel();
    }

    @Override
    public BlockPos getGuardPos(final @NotNull AbstractEntityCitizen worker)
    {
        if (getLocationsFromTag(TAG_KNIGHT).size() < 2 || getLocationsFromTag(TAG_ARCHER).size() < 2)
        {
            Log.getLogger().error("GateHouse at " + getID().toShortString() + " missing 'work' tag for guards of: " + getStructurePack() + " : " + getBlueprintPath());
            return getID();
        }

        final GuardBuildingModule knightWorkModule = this.getModule(KNIGHT_GATE_WORK);
        final GuardBuildingModule archerWorkModule = this.getModule(RANGER_GATE_WORK);

        int firstIndex = knightWorkModule.getAssignedCitizen().indexOf(worker.getCitizenData());
        if (firstIndex >= 0)
        {
            return getLocationsFromTag(TAG_KNIGHT).get(firstIndex);
        }
        int secondIndex = archerWorkModule.getAssignedCitizen().indexOf(worker.getCitizenData());
        if (secondIndex >= 0)
        {
            return getLocationsFromTag(TAG_ARCHER).get(secondIndex);
        }
        return getID();
    }

    // Always guard only mode.
    @Override
    public String getTask()
    {
        return GuardTaskSetting.GUARD;
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
    public void setBuildingLevel(final int level)
    {
        super.setBuildingLevel(level);
        if (level >= 1)
        {
            colony.getConnectionManager().addNewGateHouse(getPosition());
        }
    }

    @Override
    public void destroy()
    {
        colony.getConnectionManager().removeGateHouse(getPosition());
        super.destroy();
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
