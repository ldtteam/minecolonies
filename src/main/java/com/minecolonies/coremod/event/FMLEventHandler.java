package com.minecolonies.coremod.event;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.sounds.ModSoundEvents;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.commands.EntryPoint;
import com.minecolonies.coremod.entity.pathfinding.Pathfinding;
import com.minecolonies.coremod.network.messages.ColonyStylesMessage;
import com.minecolonies.coremod.network.messages.ServerUUIDMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
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
        if (event.getPlayer() instanceof ServerPlayerEntity)
        {
            Network.getNetwork().sendToPlayer(new ServerUUIDMessage(), (ServerPlayerEntity) event.getPlayer());
            Network.getNetwork().sendToPlayer(new ColonyStylesMessage(), (ServerPlayerEntity) event.getPlayer());

            // This automatically reloads the owner of the colony if failed.
            IColonyManager.getInstance().getIColonyByOwner(((ServerPlayerEntity) event.getPlayer()).getServerWorld(), event.getPlayer());
            //ColonyManager.syncAllColoniesAchievements();
        }
    }

    /**
     * Called when registering sounds,
     * we have to register all our mod items here.
     *
     * @param event the registery event for items.
     */
    @SubscribeEvent
    public static void registerSounds(@NotNull final RegistryEvent.Register<SoundEvent> event)
    {
        ModSoundEvents.registerSounds(event.getRegistry());
    }

    @SubscribeEvent
    public static void onServerStarting(final FMLServerStartingEvent event)
    {
        EntryPoint.register(event.getCommandDispatcher());
    }
    
    @SubscribeEvent
    public static void onServerStopped(final FMLServerStoppingEvent event)
    {
        Pathfinding.shutdown();
    }


}
