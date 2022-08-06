package com.minecolonies.coremod.event;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.crafting.CustomRecipeManager;
import com.minecolonies.coremod.compatibility.CraftingTagAuditor;
import com.minecolonies.coremod.network.messages.client.UpdateClientWithCompatibilityMessage;
import com.minecolonies.coremod.util.FurnaceRecipes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
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
     * Updates internal caches of vanilla recipes and tags.
     * This is only called server-side, after JsonReloadListeners have finished.
     *
     * @param server The server.
     */
    private static void loadRecipes(@NotNull final MinecraftServer server)
    {
        FurnaceRecipes.getInstance().loadRecipes(server.getRecipeManager());
        IMinecoloniesAPI.getInstance().getColonyManager().getCompatibilityManager().discover(server.getRecipeManager());
        CustomRecipeManager.getInstance().buildLootData(server.getLootTables());
    }

    /**
     * Send custom sync packets to the given player.
     *
     * @param player the player to send the sync packets to.
     * @param compatMsg a cached copy of this message, to avoid rebuilding it for each player.
     */
    private static void sendPackets(@NotNull final ServerPlayer player,
                                    @NotNull final UpdateClientWithCompatibilityMessage compatMsg)
    {
        Network.getNetwork().sendToPlayer(compatMsg, player);
        CustomRecipeManager.getInstance().sendCustomRecipeManagerPackets(player);
        IGlobalResearchTree.getInstance().sendGlobalResearchTreePackets(player);
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

        if (event.getPlayer() == null)
        {
            // for a reload event, we also want to rebuild various lists (mirroring FMLServerStartedEvent)
            loadRecipes(server);

            // and then finally update every player with the results (not needed for single player)
            if (!server.isSingleplayer())
            {
                final UpdateClientWithCompatibilityMessage compatMsg = new UpdateClientWithCompatibilityMessage(true);
                for (final ServerPlayer player : event.getPlayerList().getPlayers())
                {
                    sendPackets(player, compatMsg);
                }
            }
        }
        else if (!server.isSingleplayer())
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
     * @param event  {@link ServerStartedEvent}
     */
    @SubscribeEvent
    public static void onServerStarted(@NotNull final ServerStartedEvent event)
    {
        loadRecipes(event.getServer());
    }
}
