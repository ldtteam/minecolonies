package com.minecolonies.coremod.event;

import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.network.messages.ColonyStylesMessage;
import com.minecolonies.coremod.network.messages.ServerUUIDMessage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Event handler used to catch various forge events.
 */
public class FMLEventHandler
{
    /**
     * Called when the server ticks.
     * Calls {@link ColonyManager#onServerTick(TickEvent.ServerTickEvent)}.
     *
     * @param event {@link net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent}.
     */
    @SubscribeEvent
    public void onServerTick(final TickEvent.ServerTickEvent event)
    {
        ColonyManager.onServerTick(event);
    }

    /**
     * Called when the client ticks.
     * Calls {@link ColonyManager#onClientTick(TickEvent.ClientTickEvent)}.
     *
     * @param event {@link net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent}.
     */
    @SubscribeEvent
    public void onClientTick(final TickEvent.ClientTickEvent event)
    {
        ColonyManager.onClientTick(event);
    }

    /**
     * Called when the world ticks.
     * Calls {@link ColonyManager#onWorldTick(TickEvent.WorldTickEvent)}.
     *
     * @param event {@link net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent}.
     */
    @SubscribeEvent
    public void onWorldTick(final TickEvent.WorldTickEvent event)
    {
        ColonyManager.onWorldTick(event);
    }

    /**
     * Called when a player logs in.
     * If the joining player is a MP-Player, sends all possible styles in a message.
     *
     * @param event {@link net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent}
     */
    @SubscribeEvent
    public void onPlayerLogin(@NotNull final PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.player instanceof EntityPlayerMP)
        {
            MineColonies.getNetwork().sendTo(new ServerUUIDMessage(), (EntityPlayerMP) event.player);
            MineColonies.getNetwork().sendTo(new ColonyStylesMessage(), (EntityPlayerMP) event.player);
            ColonyManager.syncAllColoniesAchievements();
        }
    }
}
