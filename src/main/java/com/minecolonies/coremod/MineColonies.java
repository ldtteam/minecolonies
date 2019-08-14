package com.minecolonies.coremod;

import com.ldtteam.structurize.util.LanguageHandler;
import com.ldtteam.structurize.util.StructureLoadingUtils;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.configuration.Configuration;
import com.minecolonies.api.colony.IChunkmanagerCapability;
import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.apiimp.MinecoloniesAPIImpl;
import com.minecolonies.coremod.client.render.EmptyTileEntitySpecialRenderer;
import com.minecolonies.coremod.client.render.RenderBipedCitizen;
import com.minecolonies.coremod.client.render.RenderFishHook;
import com.minecolonies.coremod.client.render.TileEntityScarecrowRenderer;
import com.minecolonies.coremod.client.render.mobs.RenderMercenary;
import com.minecolonies.coremod.client.render.mobs.barbarians.RendererBarbarian;
import com.minecolonies.coremod.client.render.mobs.barbarians.RendererChiefBarbarian;
import com.minecolonies.coremod.client.render.mobs.pirates.RendererArcherPirate;
import com.minecolonies.coremod.client.render.mobs.pirates.RendererChiefPirate;
import com.minecolonies.coremod.client.render.mobs.pirates.RendererPirate;
import com.minecolonies.coremod.colony.IColonyManagerCapability;
import com.minecolonies.coremod.colony.requestsystem.init.RequestSystemInitializer;
import com.minecolonies.coremod.colony.requestsystem.init.StandardFactoryControllerInitializer;
import com.minecolonies.coremod.entity.NewBobberEntity;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.entity.mobs.EntityMercenary;
import com.minecolonies.coremod.entity.mobs.barbarians.EntityArcherBarbarian;
import com.minecolonies.coremod.entity.mobs.barbarians.EntityBarbarian;
import com.minecolonies.coremod.entity.mobs.barbarians.EntityChiefBarbarian;
import com.minecolonies.coremod.entity.mobs.pirates.EntityArcherPirate;
import com.minecolonies.coremod.entity.mobs.pirates.EntityCaptainPirate;
import com.minecolonies.coremod.entity.mobs.pirates.EntityPirate;
import com.minecolonies.coremod.event.*;
import com.minecolonies.coremod.placementhandlers.MinecoloniesPlacementHandlers;
import com.minecolonies.coremod.proxy.ClientProxy;
import com.minecolonies.coremod.proxy.CommonProxy;
import com.minecolonies.coremod.proxy.IProxy;
import com.minecolonies.coremod.proxy.ServerProxy;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import org.jetbrains.annotations.NotNull;

@Mod(Constants.MOD_ID)
public class MineColonies
{
    @CapabilityInject(IColonyTagCapability.class)
    public static Capability<IColonyTagCapability> CLOSE_COLONY_CAP;

    @CapabilityInject(IChunkmanagerCapability.class)
    public static Capability<IChunkmanagerCapability> CHUNK_STORAGE_UPDATE_CAP;

    @CapabilityInject(IColonyManagerCapability.class)
    public static Capability<IColonyManagerCapability> COLONY_MANAGER_CAP;

    /**
     * The config instance.
     */
    private static Configuration config;

    /**
     * The proxy.
     */
    public static final IProxy proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public MineColonies()
    {
        MinecoloniesAPIProxy.getInstance().setApiInstance(MinecoloniesAPIImpl.getInstance());
        config = new Configuration(ModLoadingContext.get().getActiveContainer());

        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(BarbarianSpawnEventHandler.class);
        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(EventHandler.class);
        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(FMLEventHandler.class);
        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(ClientEventHandler.class);
        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(DebugRendererChunkBorder.class);
        Mod.EventBusSubscriber.Bus.MOD.bus().get().register(CommonProxy.class);

        Mod.EventBusSubscriber.Bus.MOD.bus().get().register(this.getClass());
    }

    /**
     * Event handler for forge pre init event.
     *
     * @param event the forge pre init event.
     */
    @SubscribeEvent
    public static void preInit(@NotNull final FMLCommonSetupEvent event)
    {

        StructureLoadingUtils.originFolders.add(Constants.MOD_ID);
        CapabilityManager.INSTANCE.register(IColonyTagCapability.class, new IColonyTagCapability.Storage(), IColonyTagCapability.Impl::new);
        CapabilityManager.INSTANCE.register(IChunkmanagerCapability.class, new IChunkmanagerCapability.Storage(), IChunkmanagerCapability.Impl::new);
        CapabilityManager.INSTANCE.register(IColonyManagerCapability.class, new IColonyManagerCapability.Storage(), IColonyManagerCapability.Impl::new);

        Network.getNetwork().registerCommonMessages();

        StandardFactoryControllerInitializer.onPreInit();
    }

    /**
     * Called when MC loading is about to finish.
     *
     * @param event event
     */
    @SubscribeEvent
    public static void onLoadComplete(final FMLLoadCompleteEvent event)
    {
        Log.getLogger().warn("FMLLoadCompleteEvent");
        LanguageHandler.setMClanguageLoaded();
        MinecoloniesPlacementHandlers.initHandlers();
        //RecipeHandler.init(MineColonies.getConfig().getCommon().enableInDevelopmentFeatures.get(), MineColonies.getConfig().getCommon().supplyChests.get());
        RequestSystemInitializer.onPostInit();
    }

    @SubscribeEvent
    public static void doClientStuff(final FMLClientSetupEvent event)
    {
        RenderingRegistry.registerEntityRenderingHandler(EntityCitizen.class, RenderBipedCitizen::new);
        RenderingRegistry.registerEntityRenderingHandler(NewBobberEntity.class, RenderFishHook::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityBarbarian.class, RendererBarbarian::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityArcherBarbarian.class, RendererBarbarian::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityChiefBarbarian.class, RendererChiefBarbarian::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityPirate.class, RendererPirate::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityArcherPirate.class, RendererArcherPirate::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityCaptainPirate.class, RendererChiefPirate::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityMercenary.class, RenderMercenary::new);

        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityColonyBuilding.class, new EmptyTileEntitySpecialRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(ScarecrowTileEntity.class, new TileEntityScarecrowRenderer());
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
