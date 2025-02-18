package com.minecolonies.core.event;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.Network;
import com.minecolonies.core.colony.crafting.CustomRecipeManager;
import com.minecolonies.core.compatibility.CraftingTagAuditor;
import com.minecolonies.core.datalistener.DiseasesListener;
import com.minecolonies.core.datalistener.QuestJsonListener;
import com.minecolonies.core.network.messages.client.UpdateClientWithCompatibilityMessage;
import com.minecolonies.core.util.FurnaceRecipes;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Handles synching of custom datapack and compatibility data from server to client (and initial population
 * for the server in both single-player and dedicated).
 *
 * As of Forge 36.2.4, at least, events happen in this order:
 *
 * For Single Player, on startup:
 *  -- JsonReloadListeners, TagsUpdatedEvent, FMLServerAboutToStart, FMLServerStarted, OnDatapackSyncEvent, RecipesUpdatedEvent
 * For Dedicated Server, on startup:
 *  -- JsonReloadListeners, TagsUpdatedEvent, FMLServerAboutToStart, FMLServerStarted
 * For Remote Client, on login:
 *  -- OnDatapackSyncEvent [server], PlayerLoggedInEvent [server], RecipesUpdatedEvent [client], TagsUpdatedEvent [client]
 * On /reload:
 *  -- JsonReloadListeners, TagsUpdatedEvent [server], OnDatapackSyncEvent [server], TagsUpdatedEvent [remote client], RecipesUpdatedEvent [client]
 */
public class DataPackSyncEventHandler
{
    /**
     * Events subscribed on both client and server (but mostly for server-side events).
     */
    public static class ServerEvents
    {
        /**
         * Updates internal caches of vanilla recipes and tags.
         * This is only called server-side, after JsonReloadListeners have finished.
         *
         * @param server The server.
         */
        private static void discoverCompatLists(@NotNull final MinecraftServer server)
        {
            FurnaceRecipes.getInstance().loadRecipes(server.getRecipeManager(), server.overworld());
            IMinecoloniesAPI.getInstance().getColonyManager().getCompatibilityManager().discover(server.getRecipeManager(), server.overworld());
            CustomRecipeManager.getInstance().resolveTemplates();
            CustomRecipeManager.getInstance().buildLootData(server.getLootData(), server.overworld());
        }

        /**
         * Send custom sync packets to the given player.
         *
         * @param player    the player to send the sync packets to.
         * @param compatMsg a cached copy of this message, to avoid rebuilding it for each player.
         */
        private static void sendPackets(@NotNull final ServerPlayer player,
                                        @NotNull final UpdateClientWithCompatibilityMessage compatMsg)
        {
            Network.getNetwork().sendToPlayer(compatMsg, player);
            CustomRecipeManager.getInstance().sendCustomRecipeManagerPackets(player);
            IGlobalResearchTree.getInstance().sendGlobalResearchTreePackets(player);
            QuestJsonListener.sendGlobalQuestPackets(player);
            DiseasesListener.sendGlobalDiseasesPackets(player);
        }

        /**
         * This event fires on server-side both at initial world load and whenever a new player
         * joins the server (with getPlayer() != null), and also on datapack reload (with null).
         * Note that at this point the client has not yet received the recipes/tags.
         *
         * @param event {@link net.minecraftforge.event.OnDatapackSyncEvent}
         */
        @SubscribeEvent
        public static void onDataPackSync(final OnDatapackSyncEvent event)
        {
            final CustomRecipeManager recipeManager = CustomRecipeManager.getInstance();
            final MinecraftServer server = event.getPlayerList().getServer();
            final GameProfile owner = server.getSingleplayerProfile();

            if (event.getPlayer() == null)
            {
                // for a reload event, we also want to rebuild various lists (mirroring FMLServerStartedEvent)
                discoverCompatLists(server);

                // and then finally update every player with the results
                final UpdateClientWithCompatibilityMessage compatMsg = new UpdateClientWithCompatibilityMessage(true);
                for (final ServerPlayer player : event.getPlayerList().getPlayers())
                {
                    if (player.getGameProfile() != owner)   // don't need to send them in SP, or LAN owner
                    {
                        sendPackets(player, compatMsg);
                    }
                }
            }
            else if (event.getPlayer().getGameProfile() != owner)
            {
                sendPackets(event.getPlayer(), new UpdateClientWithCompatibilityMessage(true));
            }

            if (MineColonies.getConfig().getServer().auditCraftingTags.get() &&
                    (event.getPlayer() == null || event.getPlayerList().getPlayers().isEmpty()))
            {
                CraftingTagAuditor.doRecipeAudit(server, recipeManager);
            }
        }

        /**
         * Fires on a server side only, when the server has started.
         * This event is the first reliable point for server-only parsing of available smelting recipes, which are
         * required for FurnaceRecipes and CompatibilityManager.discoverOres and .discoverFood.
         *
         * @param event {@link ServerStartedEvent}
         */
        @SubscribeEvent
        public static void onServerStarted(@NotNull final ServerStartedEvent event)
        {
            discoverCompatLists(event.getServer());
        }
    }

    /**
     * Events subscribed on client-side only.
     */
    public static class ClientEvents
    {
        /**
         * Fired when the recipes are synched to client.  This happens after the {@link OnDatapackSyncEvent}.
         *
         * @param event {@link RecipesUpdatedEvent}
         */
        @SubscribeEvent
        public static void onRecipesLoaded(@NotNull final RecipesUpdatedEvent event)
        {
            final IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
            final GameProfile owner = server == null ? null : server.getSingleplayerProfile();

            if (owner != null && owner == Minecraft.getInstance().player.getGameProfile())
            {
                // don't need to update on single player, this already happened "server-side".
                return;
            }

            FurnaceRecipes.getInstance().loadRecipes(event.getRecipeManager(), Minecraft.getInstance().level);
        }
    }
}
