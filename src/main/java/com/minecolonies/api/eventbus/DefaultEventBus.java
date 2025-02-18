package com.minecolonies.api.eventbus;

import com.minecolonies.api.util.Log;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of the mod event bus.
 */
public class DefaultEventBus implements EventBus
{
    /**
     * The map of event handlers.
     */
    private final Map<Class<? extends IModEvent>, List<EventHandler<? extends IModEvent>>> eventHandlersPerType = new HashMap<>();

    @Override
    public <T extends IModEvent> void subscribe(final @NotNull Class<T> eventType, final @NotNull EventHandler<T> handler)
    {
        Log.getLogger().debug("Registering event handler for id {}.", eventType.getSimpleName());

        eventHandlersPerType.computeIfAbsent(eventType, (f) -> new ArrayList<>()).add(handler);
    }

    @Override
    public void post(final @NotNull IModEvent event)
    {
        final List<EventHandler<? extends IModEvent>> eventHandlers = eventHandlersPerType.get(event.getClass());
        if (eventHandlers == null)
        {
            return;
        }

        Log.getLogger().debug("Sending event '{}' for type '{}'. Sending to {} handlers.", event.getEventId(), event.getClass().getSimpleName(), eventHandlers.size());

        for (final EventHandler<? extends IModEvent> handler : eventHandlers)
        {
            try
            {
                ((EventHandler<IModEvent>) handler).apply(event);
            }
            catch (Exception ex)
            {
                Log.getLogger().warn("Sending event '{}' for type '{}'. Error occurred in handler:", event.getEventId(), event.getClass().getSimpleName(), ex);
            }
        }
    }
}
