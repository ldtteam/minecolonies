package com.minecolonies.permissions;

import com.minecolonies.api.permission.IPermissionEventHandler;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 *
 */
public class PermissionEventHandler implements IPermissionEventHandler
{

    public PermissionEventHandler()
    {

    }

    @Override
    @SubscribeEvent
    public void on(final BlockEvent.PlaceEvent event)
    {
        System.out.println(event.placedBlock.getBlock().getRegistryName());
    }

    @Override
    @SubscribeEvent
    public void on(final BlockEvent.BreakEvent event)
    {
        System.out.println(event.state.getBlock().getRegistryName());
    }

    @Override
    @SubscribeEvent
    public void on(final PlayerInteractEvent event)
    {

    }

    @Override
    @SubscribeEvent
    public void on(final AttackEntityEvent event)
    {

    }

    @Override
    @SubscribeEvent
    public void on(final LivingAttackEvent event)
    {

    }

    @Override
    @SubscribeEvent
    public void on(final ItemTossEvent event)
    {

    }

    @Override
    @SubscribeEvent
    public void on(final EntityItemPickupEvent event)
    {

    }

}
