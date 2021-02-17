package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;

import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Window for the builder hut.
 */
public class WindowHutGuide extends AbstractWindowSkeleton
{
    /**
     * Color constants for builder list.
     */
    private final BuildingBuilder.View building;

    /**
     * Constructor for window builder hut.
     *
     * @param building {@link BuildingBuilder.View}.
     */
    public WindowHutGuide(final BuildingBuilder.View building)
    {
        super(Constants.MOD_ID + GUIDE_RESOURCE_SUFFIX);
        registerButton(GUIDE_CONFIRM, this::closeGuide);
        registerButton(GUIDE_CLOSE, this::closeGuide);

        this.building = building;
    }

    private void closeGuide()
    {
        close();
        new WindowHutBuilder(building, false).open();
    }
}
