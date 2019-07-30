package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.coremod.colony.buildings.IBuilding;
import com.minecolonies.coremod.colony.buildings.IBuildingContainer;
import com.minecolonies.coremod.colony.buildings.ICitizenAssignable;
import com.minecolonies.coremod.colony.buildings.ISchematicProvider;
import com.minecolonies.coremod.colony.permissions.PermissionEvent;

public interface ITownHall extends ISchematicProvider, ICitizenAssignable, IBuildingContainer, IBuilding
{
    /**
     * Add a colony permission event to the colony.
     * Reduce the list by one if bigger than a treshhold.
     * @param event the event to add.
     */
    void addPermissionEvent(PermissionEvent event);
}
