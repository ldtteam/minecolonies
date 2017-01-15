package com.minecolonies.coremod.client.gui;

import com.minecolonies.blockout.controls.Button;
import com.minecolonies.coremod.colony.buildings.BuildingDeliveryman;
import com.minecolonies.coremod.lib.Constants;
import org.jetbrains.annotations.NotNull;

/**
 * Window for the farmer hut.
 */
public class WindowHutDeliveryman extends AbstractWindowWorkerBuilding<BuildingDeliveryman.View> implements Button.Handler
{
    private static final String HUT_WAREHOUSE_RESOURCE_SUFFIX = ":gui/windowHutWarehouse.xml";

    /**
     * Constructor for the window of the warehouse hut.
     *
     * @param building {@link BuildingDeliveryman.View}
     */
    public WindowHutDeliveryman(@NotNull final BuildingDeliveryman.View building)
    {
        super(building, Constants.MOD_ID + HUT_WAREHOUSE_RESOURCE_SUFFIX);
    }

    @NotNull
    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerHuts.warehouse";
    }

}
