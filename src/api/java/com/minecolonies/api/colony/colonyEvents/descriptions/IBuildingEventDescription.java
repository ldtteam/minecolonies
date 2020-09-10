package com.minecolonies.api.colony.colonyEvents.descriptions;

/**
 * Event description for building events.
 */
public interface IBuildingEventDescription extends IColonyEventDescription
{
    /**
     * Gets the name of the building type involved in this event.
     * 
     * @return the name of the building type involved in this event.
     */
    String getBuilding();

    /**
     * Sets the building type for this event.
     * 
     * @param building the building type for this event.
     */
    void setBuilding(String building);

    /**
     * Gets the level of the building after the event.
     * 
     * @return the level of the building after the event.
     */
    int getLevel();

    /**
     * Sets the level of the building after the event.
     * 
     * @param lvl the level of the building after the event.
     */
    void setLevel(int lvl);

    @Override
    default String toDisplayString()
    {
        return String.format("%s: %s %d at %d %d %d.%n", getName(), getBuilding(), getLevel(), getEventPos().getX(), getEventPos().getY(), getEventPos().getZ());
    }
}
