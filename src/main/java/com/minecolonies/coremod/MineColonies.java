package com.minecolonies.coremod;

import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.util.LanguageHandler;
import com.ldtteam.structurize.util.StructureLoadingUtils;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.configuration.Configuration;
import com.minecolonies.api.colony.IChunkmanagerCapability;
import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.apiimp.MinecoloniesAPIImpl;
import com.minecolonies.coremod.client.gui.WindowGuiFurnaceCrafting;
import com.minecolonies.coremod.colony.IColonyManagerCapability;
import com.minecolonies.coremod.colony.requestsystem.init.RequestSystemInitializer;
import com.minecolonies.coremod.colony.requestsystem.init.StandardFactoryControllerInitializer;
import com.minecolonies.coremod.commands.CommandEntryPoint;
import com.minecolonies.coremod.commands.CommandEntryPointNew;
import com.minecolonies.coremod.event.*;
import com.minecolonies.coremod.inventory.MinecoloniesContainers;
import com.minecolonies.coremod.placementhandlers.MinecoloniesPlacementHandlers;
import com.minecolonies.coremod.proxy.ClientProxy;
import com.minecolonies.coremod.proxy.IProxy;
import com.minecolonies.coremod.proxy.ServerProxy;
import com.minecolonies.coremod.util.RecipeHandler;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.entity.EntityType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

@Mod(com.ldtteam.structurize.api.util.constant.Constants.MOD_ID)
public class MineColonies
{
    @CapabilityInject(IColonyTagCapability.class)
    public static Capability<IColonyTagCapability> CLOSE_COLONY_CAP;

    @CapabilityInject(IChunkmanagerCapability.class)
    public static Capability<IChunkmanagerCapability> CHUNK_STORAGE_UPDATE_CAP;

    @CapabilityInject(IColonyManagerCapability.class)
    public static Capability<IColonyManagerCapability> COLONY_MANAGER_CAP;

    /**
     * Our mod logger.
     */
    private static final Logger logger = LogManager.getLogger(com.ldtteam.structurize.api.util.constant.Constants.MOD_ID);

    /**
     * The config instance.
     */
    private static final Configuration config = new Configuration(ModLoadingContext.get().getActiveContainer());

    /**
     * The proxy.
     */
    public static final IProxy proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public MineColonies()
    {
        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(BarbarianSpawnEventHandler.class);
        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(EventHandler.class);
        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(FMLEventHandler.class);
        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(ClientEventHandler.class);
        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(DebugRendererChunkBorder.class);

        Mod.EventBusSubscriber.Bus.MOD.bus().get().register(this.getClass());
    }

    /**
     * Event handler for forge pre init event.
     *
     * @param event the forge pre init event.
     */
    @SubscribeEvent
    public void preInit(@NotNull final FMLCommonSetupEvent event)
    {
        MinecoloniesAPIProxy.getInstance().setApiInstance(MinecoloniesAPIImpl.getInstance());

        StructureLoadingUtils.originFolders.add(Constants.MOD_ID);
        CapabilityManager.INSTANCE.register(IColonyTagCapability.class, new IColonyTagCapability.Storage(), IColonyTagCapability.Impl::new);
        CapabilityManager.INSTANCE.register(IChunkmanagerCapability.class, new IChunkmanagerCapability.Storage(), IChunkmanagerCapability.Impl::new);
        CapabilityManager.INSTANCE.register(IColonyManagerCapability.class, new IColonyManagerCapability.Storage(), IColonyManagerCapability.Impl::new);

        Network.getNetwork().registerCommonMessages();

        StandardFactoryControllerInitializer.onPreInit();
        proxy.registerEntityRendering();
    }

    /**
     * Called when MC loading is about to finish.
     *
     * @param event event
     */
    @SubscribeEvent
    public static void onLoadComplete(final FMLLoadCompleteEvent event)
    {
        Structurize.getLogger().warn("FMLLoadCompleteEvent");
        LanguageHandler.setMClanguageLoaded();
    }

    //todo this here stays!
    @SubscribeEvent
    private void doClientStuff(final FMLClientSetupEvent event)
    {
        ScreenManager.registerFactory(MinecoloniesContainers.craftingFurnace, WindowGuiFurnaceCrafting::new);
    }

    @SubscribeEvent
    public void registerEntities(final RegistryEvent.Register<EntityType<?>> event)
    {
        //Register Barbarian loot tables.
        LootTableList.register(EntityBarbarian.LOOT_TABLE);
        LootTableList.register(EntityArcherBarbarian.LOOT_TABLE);
        LootTableList.register(EntityChiefBarbarian.LOOT_TABLE);

        //Register Pirate loot tables.
        LootTableList.register(EntityPirate.LOOT_TABLE);
        LootTableList.register(EntityArcherPirate.LOOT_TABLE);
        LootTableList.register(EntityCaptainPirate.LOOT_TABLE);
    }

    /**
     * Event handler for forge init event.
     *
     * @param event the forge init event.
     */
    @Mod.EventHandler
    public void init(final FMLInitializationEvent event)
    {
        initializeNetwork();


        proxy.registerTileEntityRendering();

        proxy.registerRenderer();

        MinecoloniesPlacementHandlers.initHandlers();

        RecipeHandler.init(MineColonies.getConfig().getCommon().gameplay.enableInDevelopmentFeatures, MineColonies.getConfig().getCommon().gameplay.supplyChests);

        //Register Vanilla items with tags

        //FOOD
        OreDictionary.registerOre("food", Items.APPLE);
        OreDictionary.registerOre("food", Items.PORKCHOP);
        OreDictionary.registerOre("food", Items.BEEF);
        OreDictionary.registerOre("food", Items.BAKED_POTATO);
        OreDictionary.registerOre("food", Items.WHEAT);
        OreDictionary.registerOre("food", Items.BEETROOT);
        OreDictionary.registerOre("food", Items.BREAD);
        OreDictionary.registerOre("food", Items.CARROT);
        OreDictionary.registerOre("food", Items.CAKE);
        OreDictionary.registerOre("food", Items.CHICKEN);
        OreDictionary.registerOre("food", Items.COOKED_BEEF);
        OreDictionary.registerOre("food", Items.COOKED_CHICKEN);
        OreDictionary.registerOre("food", Items.COOKED_FISH);
        OreDictionary.registerOre("food", Items.COOKED_MUTTON);
        OreDictionary.registerOre("food", Items.COOKED_PORKCHOP);
        OreDictionary.registerOre("food", Items.COOKED_RABBIT);
        OreDictionary.registerOre("food", Items.COOKIE);
        OreDictionary.registerOre("food", Items.EGG);
        OreDictionary.registerOre("food", Items.FISH);
        OreDictionary.registerOre("food", Items.MELON);
        OreDictionary.registerOre("food", Items.MUTTON);
        OreDictionary.registerOre("food", Items.POTATO);
        OreDictionary.registerOre("food", Items.RABBIT);

        //SEEDS
        OreDictionary.registerOre("seed", Items.BEETROOT_SEEDS);
        OreDictionary.registerOre("seed", Items.MELON_SEEDS);
        OreDictionary.registerOre("seed", Items.PUMPKIN_SEEDS);
        OreDictionary.registerOre("seed", Items.WHEAT_SEEDS);
    }

    public static SimpleNetworkWrapper getNetwork()
    {
        return network;
    }

    @Mod.EventHandler
    public void postInit(final FMLPostInitializationEvent event)
    {
        RequestSystemInitializer.onPostInit();
    }

    @Mod.EventHandler
    public void serverLoad(final FMLServerStartingEvent event)
    {
        // register server commands
        event.registerServerCommand(new CommandEntryPoint());
        event.registerServerCommand(new CommandEntryPointNew());
    }

    /**
     * Getter for the minecolonies Logger.
     *
     * @return the logger.
     */
    public static Logger getLogger()
    {
        return logger;
    }

    /**
     * Get the config handler.
     *
     * @return the config handler.
     */
    public static Configuration getConfig()
    {
        return config;
    }
}
