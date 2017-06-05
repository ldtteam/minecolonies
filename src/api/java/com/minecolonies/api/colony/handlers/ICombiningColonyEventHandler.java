package com.minecolonies.api.colony.handlers;

import com.google.common.collect.ImmutableCollection;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Classes implementing this interface combine a set of given child event handlers.
 * When one of the method is invoked on this Handler the default implementation will iterate over all the children,
 * pulled by calling {@link ICombiningColonyEventHandler#getCombinedHandlers()} and call the respective methods on them.
 */
public interface ICombiningColonyEventHandler extends IColonyEventHandler
{

    @Override
    default void onServerTick(@NotNull TickEvent.ServerTickEvent event)
    {
        getCombinedHandlers().forEach(iColonyEventHandler -> iColonyEventHandler.onServerTick(event));
    }

    /**
     * Method used to get all the child handlers.
     *
     * @return An ImmutableCollection with all the child handlers.
     */
    @NotNull
    ImmutableCollection<IColonyEventHandler> getCombinedHandlers();

    @Override
    default void onClientTick(@NotNull TickEvent.ClientTickEvent event)
    {
        getCombinedHandlers().forEach(iColonyEventHandler -> iColonyEventHandler.onClientTick(event));
    }

    @Override
    default void onWorldTick(@NotNull TickEvent.WorldTickEvent event)
    {
        getCombinedHandlers().forEach(iColonyEventHandler -> iColonyEventHandler.onWorldTick(event));
    }

    @Override
    default void onWorldLoad(@NotNull WorldEvent.Load event)
    {
        getCombinedHandlers().forEach(iColonyEventHandler -> iColonyEventHandler.onWorldLoad(event));
    }

    @Override
    default void onWorldUnload(@NotNull WorldEvent.Unload event)
    {
        getCombinedHandlers().forEach(iColonyEventHandler -> iColonyEventHandler.onWorldUnload(event));
    }
}
