package com.minecolonies.client.gui;

import com.minecolonies.colony.buildings.BuildingBuilder;
import com.minecolonies.lib.Constants;
import javax.annotation.Nonnull;

/**
 * Window for the builder hut
 */
public class WindowHutBuilder extends AbstractWindowWorkerBuilding<BuildingBuilder.View>
{
    /**
     * The builders gui file
     */
    private static final String HUT_BUILDER_RESOURCE_SUFFIX = ":gui/windowHutBuilder.xml";

    /**
     * Constructor for window builder hut
     *
     * @param building {@link com.minecolonies.colony.buildings.BuildingBuilder.View}
     */
    public WindowHutBuilder(BuildingBuilder.View building)
    {
        super(building, Constants.MOD_ID + HUT_BUILDER_RESOURCE_SUFFIX);
    }

    /**
     * Returns the name of a building
     *
     * @return Name of a building
     */
    @Nonnull
    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.gui.workerHuts.buildersHut";
    }
}
