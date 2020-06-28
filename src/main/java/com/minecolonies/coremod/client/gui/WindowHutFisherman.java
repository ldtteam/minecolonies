package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingFisherman;
import org.jetbrains.annotations.NotNull;

/**
 * Window for the fisherman hut.
 */
public class WindowHutFisherman extends AbstractWindowWorkerBuilding<BuildingFisherman.View>
{
    /**
     * Constructor for the window of the fisherman.
     *
     * @param building {@link BuildingFisherman.View}.
     */
    public WindowHutFisherman(final BuildingFisherman.View building)
    {
        super(building, Constants.MOD_ID + ":gui/windowhutfisherman.xml");
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
        return "com.minecolonies.coremod.gui.workerhuts.fisherman";
    }
}

