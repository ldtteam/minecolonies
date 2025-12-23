package com.minecolonies.api.eventbus.events.colony.buildings;

import com.minecolonies.api.colony.buildings.IBuilding;

/**
 * Event for when a building was removed from the building manager.
 */
public final class BuildingRemovedModEvent extends AbstractBuildingModEvent
{
    /**
     * Building added event.
     *
     * @param building the building the event was for.
     */
    public BuildingRemovedModEvent(final IBuilding building)
    {
        super(building);
    }
}
