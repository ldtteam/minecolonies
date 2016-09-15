package com.minecolonies.api.permission;

import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;

/**
 * @author Isfirs
 * @since 0.3
 */
public interface IPermissionEventHandler
{

    /**
     * Checks events on block place.
     *
     * @param event The block place event
     */
    void on(BlockEvent.PlaceEvent event);

    /**
     * Checks events on block break.
     *
     * @param event The block break event
     */
    void on(BlockEvent.BreakEvent event);

    /**
     * Checks events when a player interacts with something.
     *
     * @param event The interact event
     */
    void on(PlayerInteractEvent event);

}
