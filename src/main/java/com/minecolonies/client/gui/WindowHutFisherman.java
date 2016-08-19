package com.minecolonies.client.gui;

import com.blockout.controls.Button;
import com.blockout.views.SwitchView;
import com.minecolonies.colony.buildings.BuildingFisherman;
import com.minecolonies.lib.Constants;

/**
 * Window for the fisherman hut
 */
public class WindowHutFisherman extends AbstractWindowWorkerBuilding<BuildingFisherman.View>
{
    /**
     * Constructor for the window of the fisherman
     *
     * @param building      {@link com.minecolonies.colony.buildings.BuildingFisherman.View}
     */
    public WindowHutFisherman(BuildingFisherman.View building)
    {
        super(building, Constants.MOD_ID + ":gui/windowHutFisherman.xml");
    }

    /**
     * Returns the name of a building
     *
     * @return      Name of a building
     */
    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.gui.workerHuts.fisherman";
    }

}

