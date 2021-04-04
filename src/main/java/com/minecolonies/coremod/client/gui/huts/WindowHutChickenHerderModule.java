package com.minecolonies.coremod.client.gui.huts;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.gui.AbstractWindowHerderModuleBuilding;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingChickenHerder;
import org.jetbrains.annotations.NotNull;

/**
 * Window for the chicken herder hut.
 */
public class WindowHutChickenHerderModule extends AbstractWindowHerderModuleBuilding<BuildingChickenHerder.View>
{
    /**
     * Constructor for the window of the chicken herder.
     *
     * @param building {@link BuildingChickenHerder.View}.
     */
    public WindowHutChickenHerderModule(final BuildingChickenHerder.View building)
    {
        super(building, Constants.MOD_ID + ":gui/windowhutchickenherder.xml");
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
        return "com.minecolonies.coremod.gui.workerhuts.chickenHerderHut";
    }
}

