package com.minecolonies.coremod.event;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.compatibility.ICompatibilityManager;
import com.minecolonies.apiimp.initializer.ModTagsInitializer;
import com.minecolonies.coremod.colony.crafting.CustomRecipeManager;
import com.minecolonies.coremod.util.FurnaceRecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
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
 * For Single Player, startup pattern is :
 *  -- onTagUpdate (populate ModTagsInitializer) to onServerStarted (populate CompatibilityManager and FurnaceRecipes) to onRecipesUpdate (repeat FurnaceRecipes).
 * For Dedicated Servers, startup pattern is :
 *  -- onTagUpdate (populate ModTagsInitializer) to onServerStarted (populate CompatibilityManager and FurnaceRecipes).
 * For Remote Clients either connecting to Open To Lan Single Player, or a Dedicated Server, startup pattern is :
 *  -- onRecipesUpdated fires (populates FurnaceRecipes) to onTagUpdate fires (populates CompatibilityManager and ModTagsInitializer).
 * Data Pack Reloads during live play will fire onTagUpdate in all cases.
 */
public class TagWorkAroundEventHandler
{
    public static class TagEventHandler
    {
        /**
         * This event fires on both client and server, immediately after reading tags data from disk (on server) or from network (on remote clients)..
         * It is also a guaranteed source for valid tag suppliers on a remote client that aren't (as of Forge 36.1.2) reliable when taken from TagCollectionManager or ItemTags.
         * VanillaTagTypes only guarantees block, item, fluid, and entity_type tags are completely filled and updated.
         * If we need support for enchantment, potion, or block_entity_type, use {@link net.minecraftforge.event.TagsUpdatedEvent.CustomTagTypes}.
         *
         * @param event {@link net.minecraftforge.event.TagsUpdatedEvent.VanillaTagTypes}
         */
        @SubscribeEvent
        public static void onTagUpdate(final TagsUpdatedEvent.VanillaTagTypes event)
        {
            // This Tag Supplier is guaranteed to have the output of a transmitted TagSupplier on remote clients.
            // _Only_ these events and ClientWorld.getTags() are guaranteed to be consistent on remote clients.
            ModTagsInitializer.init(event.getTagManager());
        }

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
                final ICompatibilityManager compatibilityManager = IMinecoloniesAPI.getInstance().getColonyManager().getCompatibilityManager();

                FurnaceRecipes.getInstance().loadRecipes(server.getRecipeManager());
                compatibilityManager.discover();
                compatibilityManager.invalidateRecipes(server.getRecipeManager());
                recipeManager.buildLootData(server.getLootTables());

                // and then finally update every player with the results
                for (final ServerPlayerEntity player : event.getPlayerList().getPlayers())
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

    public static class TagClientEventHandler
    {
        /**
         * Fires only on client-side, immediately after a client has received recipes
         * This event consistently fires before TagUpdatedEvent does on remote clients.
         * @param event  {@link net.minecraftforge.client.event.RecipesUpdatedEvent}
         */
        @SubscribeEvent
        public static void onRecipesUpdated(final RecipesUpdatedEvent event)
        {
            // skip on integrated server (already done)
            if (!Minecraft.getInstance().hasSingleplayerServer())
            {
                final ICompatibilityManager compatibilityManager = IMinecoloniesAPI.getInstance().getColonyManager().getCompatibilityManager();

                FurnaceRecipes.getInstance().loadRecipes(event.getRecipeManager());
                compatibilityManager.discover();
                compatibilityManager.invalidateRecipes(event.getRecipeManager());
            }
        }
    }

    public static class TagFMLEventHandlers
    {
        /**
         * Fires on a server side only, when the server has started.
         * This event is the first reliable point for server-only parsing of available smelting recipes, which are
         * required for FurnaceRecipes and CompatibilityManager.discoverOres and .discoverFood.
         * @param event  {@link net.minecraftforge.fml.event.server.FMLServerStartedEvent}
         */
        @SubscribeEvent
        public static void onServerStarted(@NotNull final FMLServerStartedEvent event)
        {
            final MinecraftServer server = event.getServer();
            final ICompatibilityManager compatibilityManager = IMinecoloniesAPI.getInstance().getColonyManager().getCompatibilityManager();

            FurnaceRecipes.getInstance().loadRecipes(server.getRecipeManager());
            compatibilityManager.discover();
            compatibilityManager.invalidateRecipes(server.getRecipeManager());
            CustomRecipeManager.getInstance().buildLootData(server.getLootTables());
        }
    }
}
