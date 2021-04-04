package com.minecolonies.coremod.client.gui.huts;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.gui.AbstractWindowWorkerModuleBuilding;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingFisherman;
import org.jetbrains.annotations.NotNull;

/**
 * Window for the fisherman hut.
 */
public class WindowHutFishermanModule extends AbstractWindowWorkerModuleBuilding<BuildingFisherman.View>
{
    /**
     * Constructor for the window of the fisherman.
     *
     * @param building {@link BuildingFisherman.View}.
     */
    public WindowHutFishermanModule(final BuildingFisherman.View building)
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

