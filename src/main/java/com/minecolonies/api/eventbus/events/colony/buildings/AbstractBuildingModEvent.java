package com.minecolonies.api.eventbus.events.colony.buildings;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.eventbus.events.colony.AbstractColonyModEvent;

/**
 * Abstract event for building related things.
 */
public abstract class AbstractBuildingModEvent extends AbstractColonyModEvent
{
    /**
     * The building related to the event.
     */
    protected final IBuilding building;

    /**
     * Constructs a building-based event.
     *
     * @param building the building related to the event.
     */
    protected AbstractBuildingModEvent(final IBuilding building)
    {
        super(building.getColony());
        this.building = building;
    }

    /**
     * Get the building related to the event.
     *
     * @return the building instance.
     */
    public IBuilding getBuilding()
    {
        return building;
    }
}
