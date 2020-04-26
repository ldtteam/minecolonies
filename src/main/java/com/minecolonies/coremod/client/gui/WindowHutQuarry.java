package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingQuarry;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingWareHouse;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.WindowConstants.QUARRY_RESOURCE_SUFFIX;

/**
 * Window for the home building.
 */
public class WindowHutQuarry extends AbstractWindowWorkerBuilding<BuildingQuarry.View>
{
    /**
     * Constructor for window warehouse hut.
     *
     * @param building {@link BuildingWareHouse.View}.
     */
    public WindowHutQuarry(final BuildingQuarry.View building)
    {
        super(building, Constants.MOD_ID + QUARRY_RESOURCE_SUFFIX);
    }

    /**
     * Returns the name of a building.
     *
     * @return Name of a building.
     */
    @NotNull
    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerhuts.quarry";
    }
}
