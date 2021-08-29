package com.minecolonies.coremod.event;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.datalistener.CrafterRecipeListener;
import com.minecolonies.coremod.datalistener.CustomVisitorListener;
import com.minecolonies.coremod.datalistener.ResearchListener;
import com.minecolonies.coremod.entity.pathfinding.Pathfinding;
import com.minecolonies.coremod.network.messages.client.ColonyStylesMessage;
import com.minecolonies.coremod.network.messages.client.ServerUUIDMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fmlserverevents.FMLServerAboutToStartEvent;
import net.minecraftforge.fmlserverevents.FMLServerStoppingEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Event handler used to catch various forge events.
 */
public class FMLEventHandler
{
    @SubscribeEvent
    public static void onServerTick(final TickEvent.ServerTickEvent event)
    {
        IColonyManager.getInstance().onServerTick(event);
    }

    @SubscribeEvent
    public static void onClientTick(final TickEvent.ClientTickEvent event)
    {
        IColonyManager.getInstance().onClientTick(event);
    }

    @SubscribeEvent
    public static void onWorldTick(final TickEvent.WorldTickEvent event)
    {
        IColonyManager.getInstance().onWorldTick(event);
    }

    @SubscribeEvent
    public static void onPlayerLogin(@NotNull final PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.getPlayer() instanceof ServerPlayer)
        {
            Network.getNetwork().sendToPlayer(new ServerUUIDMessage(), (ServerPlayer) event.getPlayer());
            Network.getNetwork().sendToPlayer(new ColonyStylesMessage(), (ServerPlayer) event.getPlayer());

            // This automatically reloads the owner of the colony if failed.
            IColonyManager.getInstance().getIColonyByOwner(((ServerPlayer) event.getPlayer()).getLevel(), event.getPlayer());
            //ColonyManager.syncAllColoniesAchievements();
        }
    }

    @SubscribeEvent
    public static void onAddReloadListenerEvent(@NotNull final AddReloadListenerEvent event)
    {
        event.addListener(new CrafterRecipeListener(event.getDataPackRegistries()));
        event.addListener(new ResearchListener());
        event.addListener(new CustomVisitorListener());
    }

    @SubscribeEvent
    public static void onServerAboutToStart(@NotNull final FMLServerAboutToStartEvent event)
    {
        IColonyManager.getInstance().getRecipeManager().reset();
    }

    public static void onServerStopped(final FMLServerStoppingEvent event)
    {
        Pathfinding.shutdown();
    }
}
