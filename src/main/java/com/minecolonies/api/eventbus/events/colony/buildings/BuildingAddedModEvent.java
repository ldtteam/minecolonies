package com.minecolonies.api.eventbus.events.colony.buildings;

import com.minecolonies.api.colony.buildings.IBuilding;

/**
 * Event for when a building was added to the building manager.
 */
public final class BuildingAddedModEvent extends AbstractBuildingModEvent
{
    /**
     * Building added event.
     *
     * @param building the building the event was for.
     */
    public BuildingAddedModEvent(final IBuilding building)
    {
        super(building);
    }
}
