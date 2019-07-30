package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.coremod.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.colony.permissions.PermissionEvent;

import java.util.List;

public interface ITownHallView extends IBuildingView
{
    /**
     * Get a list of permission events.
     * @return a copy of the list of events.
     */
    List<PermissionEvent> getPermissionEvents();
}
