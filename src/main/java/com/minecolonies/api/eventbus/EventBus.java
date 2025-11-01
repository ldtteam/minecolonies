package com.minecolonies.api.eventbus;

import org.jetbrains.annotations.NotNull;

/**
 * Interface for the mod event bus.
 */
public interface EventBus
{
    /**
     * Subscribe to the given event type, providing a handler function.
     *
     * @param eventType the event class type.
     * @param handler   the handler function handling the event logic.
     * @param <T>       the generic type of the event class.
     */
    <T extends IModEvent> void subscribe(final @NotNull Class<T> eventType, final @NotNull EventHandler<T> handler);

    /**
     * Posts a new event on the event bus for the given type.
     *
     * @param event the event to send.
     */
    void post(final @NotNull IModEvent event);

    /**
     * The event handler lambda definition.
     *
     * @param <T> the generic type of the event class.
     */
    @FunctionalInterface
    interface EventHandler<T extends IModEvent>
    {
        void apply(final @NotNull T event);
    }
}
