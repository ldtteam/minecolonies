package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingUniversity;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Window for the lumberjack hut.
 */
public class WindowHutUniversity extends AbstractWindowBuilding<BuildingUniversity.View>
{
    /**
     * The building of the lumberjack (Client side representation).
     */
    private final BuildingUniversity.View ownBuilding;

    /**
     * The resource string.
     */
    private static final String RESOURCE_STRING = ":gui/windowhutuniversity.xml";

    /**
     * Constructor for the window of the lumberjack.
     *
     * @param building {@link BuildingUniversity.View}.
     */
    public WindowHutUniversity(final BuildingUniversity.View building)
    {
        super(building, Constants.MOD_ID + RESOURCE_STRING);
        this.ownBuilding = building;
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
        return COM_MINECOLONIES_COREMOD_GUI_UNIVERSITY;
    }
}

