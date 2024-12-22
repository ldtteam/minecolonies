package com.minecolonies.api.eventbus;

import java.util.UUID;

/**
 * Default event interface.
 */
public interface IModEvent
{
    /**
     * The unique id for this event.
     *
     * @return the event id.
     */
    UUID getEventId();
}
