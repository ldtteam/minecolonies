package com.minecolonies.api.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.colonyEvents.descriptions.IColonyEventDescription;
import com.minecolonies.api.colony.permissions.PermissionEvent;

import java.util.List;

public interface ITownHallView extends IBuildingView
{
    /**
     * Get a list of permission events.
     *
     * @return a copy of the list of events.
     */
    List<PermissionEvent> getPermissionEvents();

    /**
     * Gets a list if colony events.
     * 
     * @return a copy of the list of events.
     */
    List<IColonyEventDescription> getColonyEvents();

    /**
     * Check if the player can use the teleport command.
     *
     * @return true if so.
     */
    boolean canPlayerUseTP();
}
