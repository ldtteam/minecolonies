package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.BuildingBaker;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_BAKER;

/**
 * Window for the fisherman hut.
 */
public class WindowHutBaker extends AbstractWindowWorkerBuilding<BuildingBaker.View>
{
    /**
     * Constructor for the window of the fisherman.
     *
     * @param building {@link com.minecolonies.coremod.colony.buildings.BuildingBaker.View}.
     */
    public WindowHutBaker(final BuildingBaker.View building)
    {
        super(building, Constants.MOD_ID + ":gui/windowHutBaker.xml");
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
        return COM_MINECOLONIES_COREMOD_GUI_BAKER;
    }
}

