package com.minecolonies.coremod.event;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.coremod.colony.crafting.CustomRecipeManager;
import com.minecolonies.coremod.util.FurnaceRecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagContainer;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Handles forge events specifically focused around mitigating Minecraft Forge behavior for tags on remote clients.
 * Minecraft/Forge synchronizes some tag information to remote clients from servers automatically, but (as of 36.1.2)
 * does not update the TagCollectionManager and ItemTags tag suppliers to use these new data sources.
 * As a result, any remote use of these tag suppliers will return inconsistent results.
 * The data is important to support ModTagsInitializer and CompatibilityManager, as well as any use of crafter tag rules.
 * While we currently don't use tags often on the client, moving more and more information to the data pack system /will/ cause problems.
 *
 * The event handlers themselves are good uses for this case, but their interactions are hacky,
 * primarily because of timing problems between CompatibilityManager and smelting recipes.
 * If Forge changes its behavior for TagCollectionManager and ItemTags, some of the logic in here can be cleaned up and moved to normal FooEventHandlers.
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
public class TagWorkAroundEventHandler
{
    /**
     * Updates internal caches of vanilla recipes and tags.
     * This is mostly called server-side, but can be called on the client too.
     *
     * @param recipeManager The vanilla recipe manager.
     */
    private static void loadRecipes(@NotNull final RecipeManager recipeManager)
    {
        FurnaceRecipes.getInstance().loadRecipes(recipeManager);
        IMinecoloniesAPI.getInstance().getColonyManager().getCompatibilityManager().discover(recipeManager);
    }

    public static class TagEventHandler
    {
        /**
         * This event fires on server-side both at initial world load and whenever a new player
         * joins the server (with getPlayer() != null), and also on datapack reload (with null).
         *
         * @param event {@link net.minecraftforge.event.OnDatapackSyncEvent}
         */
        @SubscribeEvent
        public static void onDataPackSync(final OnDatapackSyncEvent event)
        {
            final CustomRecipeManager recipeManager = CustomRecipeManager.getInstance();

            if (event.getPlayer() == null)
            {
                // for a reload event, we also want to rebuild various lists (mirroring FMLServerStartedEvent)
                final MinecraftServer server = event.getPlayerList().getServer();

                loadRecipes(server.getRecipeManager());
                recipeManager.buildLootData(server.getLootTables());

                // and then finally update every player with the results
                for (final ServerPlayer player : event.getPlayerList().getPlayers())
                {
                    recipeManager.sendCustomRecipeManagerPackets(player);
                }
            }
            else
            {
                recipeManager.sendCustomRecipeManagerPackets(event.getPlayer());
            }
        }
    }

    /**
     * Events handled only on client side (single-player or multi-player).
     */
    public static class TagClientEventHandler
    {
        private static RecipeManager recipeManager;
        private static TagContainer tagManager;

        /**
         * The RecipesUpdatedEvent and TagsUpdatedEvent occur in unspecified order on the client side
         * (or rather, a different order for initial connect vs. /reload) so we have to handle them
         * happening either way around -- but we also don't want to trigger our recipe reload sequence
         * until we have both of them.
         */
        private static void maybeLoadRecipes()
        {
            if (recipeManager != null && tagManager != null)
            {
                // skip on integrated server (already done)
                if (!Minecraft.getInstance().hasSingleplayerServer())
                {
                    loadRecipes(recipeManager);
                }

                recipeManager = null;
                tagManager = null;
            }
        }

        /**
         * Fires immediately after a client has received recipes
         * @param event  {@link net.minecraftforge.client.event.RecipesUpdatedEvent}
         */
        @SubscribeEvent
        public static void onRecipesUpdated(final RecipesUpdatedEvent event)
        {
            recipeManager = event.getRecipeManager();
            maybeLoadRecipes();
        }

        /**
         * Fires after receiving tags.
         * @param event {@link net.minecraftforge.event.TagsUpdatedEvent}
         */
        @SubscribeEvent
        public static void onTagsUpdated(final TagsUpdatedEvent event)
        {
            tagManager = event.getTagManager();
            maybeLoadRecipes();
        }

        /**
         * Fires whenever unloading the client -- which is more often than you might first think, so be careful.
         * @param event {@link net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggedOutEvent}
         */
        @SubscribeEvent
        public static void onLoggedOut(final ClientPlayerNetworkEvent.LoggedOutEvent event)
        {
            recipeManager = null;
            tagManager = null;
        }
    }

    public static class TagFMLEventHandlers
    {
        /**
         * Fires on a server side only, when the server has started.
         * This event is the first reliable point for server-only parsing of available smelting recipes, which are
         * required for FurnaceRecipes and CompatibilityManager.discoverOres and .discoverFood.
         * @param event  {@link ServerStartedEvent}
         */
        @SubscribeEvent
        public static void onServerStarted(@NotNull final ServerStartedEvent event)
        {
            final MinecraftServer server = event.getServer();

            loadRecipes(server.getRecipeManager());
            CustomRecipeManager.getInstance().buildLootData(server.getLootTables());
        }
    }
}
