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

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_WORK;
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
    private static final int    BONUS_HP_SINGLE_GUARD = 20;
    //todo adjust guard to behave like 1/3/5 guard depending on level.

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

    //todo  make visitors arrive here if it exists.
    //todo change tag to "TAG_KNIGHT and TAG_ARCHER"
    @Override
    public BlockPos getGuardPos(final @NotNull AbstractEntityCitizen worker)
    {
        if (getLocationsFromTag(TAG_WORK).size() < 2)
        {
            Log.getLogger().error("GateHouse at " + getID().toShortString() + " missing 'work' tag for guards");
            return getID();
        }

        final GuardBuildingModule knightWorkModule = this.getModule(KNIGHT_GATE_WORK);
        final GuardBuildingModule archerWorkModule = this.getModule(RANGER_GATE_WORK);

        int firstIndex = knightWorkModule.getAssignedCitizen().indexOf(worker.getCitizenData());
        if (firstIndex != -1)
        {
            return getLocationsFromTag(TAG_WORK).get(firstIndex);
        }
        int secondIndex = archerWorkModule.getAssignedCitizen().indexOf(worker.getCitizenData());
        if (secondIndex != -1)
        {
            return getLocationsFromTag(TAG_WORK).get(secondIndex + firstIndex == -1 ? 0 : firstIndex);
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
    public int getBonusHealth()
    {
        return BONUS_HP_SINGLE_GUARD + super.getBonusHealth();
    }

    @Override
    public void onPlacement()
    {
        super.onPlacement();
        colony.getConnectionManager().addNewGateHouse(getPosition());
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
