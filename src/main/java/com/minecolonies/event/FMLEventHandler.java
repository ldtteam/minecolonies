package com.minecolonies.event;

import com.minecolonies.MineColonies;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.network.messages.ColonyStylesMessage;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayerMP;

public class FMLEventHandler
{
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event)
    {
        ColonyManager.onServerTick(event);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        ColonyManager.onClientTick(event);
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event)
    {
        ColonyManager.onWorldTick(event);
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        if(event.player instanceof EntityPlayerMP)
        {
            MineColonies.getNetwork().sendTo(new ColonyStylesMessage(), (EntityPlayerMP) event.player);
        }
    }
}
