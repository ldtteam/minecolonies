package com.minecolonies.api.colony.colonyEvents.descriptions;

/**
 * Event description for citizen spawn/death events.
 */
public interface ICitizenEventDescription extends IColonyEventDescription
{
    /**
     * Gets the name of the citizen causing this event.
     * 
     * @return the name of the citizen causing this event.
     */
    String getCitizenName();

    /**
     * Sets the name of the citizen causing this event.
     * 
     * @param name the name of the citizen causing this event.
     */
    void setCitizenName(String name);

    @Override
    default String toDisplayString()
    {
        return String.format("%s %s at %d %d %d.%n", getCitizenName(), getName(), getEventPos().getX(), getEventPos().getY(), getEventPos().getZ());
    }
}
