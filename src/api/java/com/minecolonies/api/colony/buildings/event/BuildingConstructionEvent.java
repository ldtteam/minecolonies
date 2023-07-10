package com.minecolonies.api.colony.buildings.event;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.workorders.WorkOrderType;

/**
 * Event for when a building was built/repaired/removed.
 */
public final class BuildingConstructionEvent extends AbstractBuildingEvent
{
    /**
     * What happened to the building.
     */
    private final EventType eventType;

    /**
     * Building construction event.
     *
     * @param building  the building the event was for.
     * @param eventType what happened to the building.
     */
    public BuildingConstructionEvent(final IBuilding building, final EventType eventType)
    {
        super(building);
        this.eventType = eventType;
    }

    /**
     * Get what happened to the building.
     *
     * @return the event type.
     */
    public EventType getEventType()
    {
        return eventType;
    }

    /**
     * What happened to the building.
     */
    public enum EventType
    {
        BUILT,
        UPGRADED,
        REPAIRED,
        REMOVED;

        /**
         * Obtain the construction event type from the work order type.
         *
         * @param workOrderType the work order type.
         * @return the construction event type.
         */
        public static EventType fromWorkOrderType(final WorkOrderType workOrderType)
        {
            return switch (workOrderType)
            {
                case BUILD -> BUILT;
                case UPGRADE -> UPGRADED;
                case REPAIR -> REPAIRED;
                case REMOVE -> REMOVED;
            };
        }
    }
}
