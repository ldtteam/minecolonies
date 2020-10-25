package com.minecolonies.api.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.permissions.PermissionEvent;

public interface ITownHall extends IBuilding
{
    /**
     * Add a colony permission event to the colony. Reduce the list by one if bigger than a treshhold.
     *
     * @param event the event to add.
     */
    void addPermissionEvent(PermissionEvent event);
}
