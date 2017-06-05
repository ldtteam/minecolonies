package com.minecolonies.api.colony.handlers;

import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Classes implementing this interface are used to process Ticks for Colonies.
 * The default implementation does nothing on each of the event handlers.
 */
public interface IColonyEventHandler
{

    /**
     * On server tick, tick every Colony.
     * NOTE: Review this for performance.
     *
     * @param event {@link net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent}
     */
    default void onServerTick(@NotNull TickEvent.ServerTickEvent event) { /*NOOP*/ }

    /**
     * On Client tick, clears views when player left.
     *
     * @param event {@link TickEvent.ClientTickEvent}.
     */
    default void onClientTick(@NotNull TickEvent.ClientTickEvent event) { /*NOOP*/ }

    /**
     * On world tick, tick every Colony in that world.
     * NOTE: Review this for performance.
     *
     * @param event {@link TickEvent.WorldTickEvent}.
     */
    default void onWorldTick(@NotNull TickEvent.WorldTickEvent event) { /*NOOP*/ }

    /**
     * Called when a World is Loaded by MC.
     *
     * @param event The event indicating that a world is being loaded.
     */
    default void onWorldLoad(@NotNull WorldEvent.Load event) { /*NOOP*/ }

    /**
     * Called when a World is unloaded by MC.
     *
     * @param event The event that indicates a world is being unloaded-
     */
    default void onWorldUnload(@NotNull WorldEvent.Unload event) { /*NOOP*/ }
}
