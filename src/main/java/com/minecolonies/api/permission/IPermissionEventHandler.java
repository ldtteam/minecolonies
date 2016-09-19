package com.minecolonies.api.permission;

import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
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

    /**
     * Checks events when a player attacks an entity.
     *
     * @param event The attack event
     */
    void on(AttackEntityEvent event);

    /**
     * Checks events when an entity attacks an entity.
     *
     * @param event The attack event
     */
    void on(LivingAttackEvent event);

    /**
     * Checks events when a player drops items.
     *
     * @param event The item toss event
     */
    void on(ItemTossEvent event);

    /**
     * Checks events when a player picks up items.
     *
     * @param event
     */
    void on(EntityItemPickupEvent event);



}
