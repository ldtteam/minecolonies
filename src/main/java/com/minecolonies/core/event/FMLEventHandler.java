package com.minecolonies.core.event;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.core.datalistener.*;
import com.minecolonies.core.entity.pathfinding.Pathfinding;
import com.minecolonies.core.util.BackUpHelper;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Event handler used to catch various forge events.
 */
public class FMLEventHandler
{
    @SubscribeEvent
    public static void onServerTick(final ServerTickEvent.Pre event)
    {
        IColonyManager.getInstance().onServerTick(event);
        DataPackSyncEventHandler.ServerEvents.load(event.getServer());
    }

    @SubscribeEvent
    public static void onClientTick(final ClientTickEvent.Pre event)
    {
        IColonyManager.getInstance().onClientTick(event);
    }

    @SubscribeEvent
    public static void onPlayerLogin(@NotNull final PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer)
        {
            // This automatically reloads the owner of the colony if failed.
            IColonyManager.getInstance().getIColonyByOwner(event.getEntity().level(), event.getEntity());
            //ColonyManager.syncAllColoniesAchievements();
        }
    }

    @SubscribeEvent
    public static void onAddServerReloadListenerEvent(@NotNull final AddServerReloadListenersEvent event)
    {
        /*
         * Minecraft 26.2 applies default item components only after all reload
         * listeners have completed.  Minecolonies' JSON listeners decode
         * ItemStacks during that reload, so make the same pending component
         * snapshot available before registering those listeners.  The vanilla
         * reload completion still applies its snapshot afterwards (and emits
         * DefaultDataComponentsBoundEvent), keeping the normal lifecycle intact.
         */
        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS
            .build(event.getServerResources().getRegistryLookup())
            .forEach(DataComponentInitializers.PendingComponents::apply);

        event.addListener(Identifier.fromNamespaceAndPath("minecolonies", "crafter_recipes"), new CrafterRecipeListener());
        event.addListener(Identifier.fromNamespaceAndPath("minecolonies", "research"), new ResearchListener());
        event.addListener(Identifier.fromNamespaceAndPath("minecolonies", "custom_visitors"), new CustomVisitorListener());
        event.addListener(Identifier.fromNamespaceAndPath("minecolonies", "citizen_names"), new CitizenNameListener());
        event.addListener(Identifier.fromNamespaceAndPath("minecolonies", "quests"), new QuestJsonListener());
        event.addListener(Identifier.fromNamespaceAndPath("minecolonies", "item_nbt"), new ItemNbtListener());
        event.addListener(Identifier.fromNamespaceAndPath("minecolonies", "study_items"), StudyItemListener.INSTANCE);
        event.addListener(Identifier.fromNamespaceAndPath("minecolonies", "diseases"), new DiseasesListener());
        event.addListener(Identifier.fromNamespaceAndPath("minecolonies", "recruitment_items"), new RecruitmentItemsListener());
    }

    @SubscribeEvent
    public static void onServerStarted(@NotNull final ServerStartedEvent event)
    {
        BackUpHelper.loadMissingColonies();
    }

    @SubscribeEvent
    public static void onWorldTick(final LevelTickEvent.Pre event)
    {
        IColonyManager.getInstance().onWorldTick(event);
    }

    @SubscribeEvent
    public static void onServerAboutToStart(@NotNull final ServerAboutToStartEvent event)
    {
        IColonyManager.getInstance().getRecipeManager().reset();
    }

    @SubscribeEvent
    public static void onServerStopped(@NotNull final ServerStoppingEvent event)
    {
        Pathfinding.shutdown();
        DataPackSyncEventHandler.ServerEvents.reset();
    }
}
