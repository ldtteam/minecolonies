package com.minecolonies.coremod;

import com.ldtteam.structurize.util.LanguageHandler;
import com.ldtteam.structurize.util.StructureLoadingUtils;
import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IChunkmanagerCapability;
import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.configuration.Configuration;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.api.entity.mobs.RaiderMobUtils;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.loot.ModLootConditions;
import com.minecolonies.api.sounds.ModSoundEvents;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.apiimp.initializer.InteractionValidatorInitializer;
import com.minecolonies.apiimp.initializer.ModModelTypeInitializer;
import com.minecolonies.coremod.client.render.*;
import com.minecolonies.coremod.client.render.mobs.RenderMercenary;
import com.minecolonies.coremod.client.render.mobs.amazon.RendererAmazon;
import com.minecolonies.coremod.client.render.mobs.amazon.RendererAmazonSpearman;
import com.minecolonies.coremod.client.render.mobs.amazon.RendererChiefAmazon;
import com.minecolonies.coremod.client.render.mobs.barbarians.RendererBarbarian;
import com.minecolonies.coremod.client.render.mobs.barbarians.RendererChiefBarbarian;
import com.minecolonies.coremod.client.render.mobs.egyptians.RendererArcherMummy;
import com.minecolonies.coremod.client.render.mobs.egyptians.RendererMummy;
import com.minecolonies.coremod.client.render.mobs.egyptians.RendererPharao;
import com.minecolonies.coremod.client.render.mobs.norsemen.RendererArcherNorsemen;
import com.minecolonies.coremod.client.render.mobs.norsemen.RendererChiefNorsemen;
import com.minecolonies.coremod.client.render.mobs.norsemen.RendererShieldmaidenNorsemen;
import com.minecolonies.coremod.client.render.mobs.pirates.RendererArcherPirate;
import com.minecolonies.coremod.client.render.mobs.pirates.RendererChiefPirate;
import com.minecolonies.coremod.client.render.mobs.pirates.RendererPirate;
import com.minecolonies.coremod.client.render.projectile.FireArrowRenderer;
import com.minecolonies.coremod.client.render.projectile.RendererSpear;
import com.minecolonies.coremod.colony.IColonyManagerCapability;
import com.minecolonies.coremod.colony.requestsystem.init.RequestSystemInitializer;
import com.minecolonies.coremod.colony.requestsystem.init.StandardFactoryControllerInitializer;
import com.minecolonies.coremod.entity.DruidPotionEntity;
import com.minecolonies.coremod.entity.mobs.EntityMercenary;
import com.minecolonies.coremod.event.*;
import com.minecolonies.coremod.placementhandlers.PlacementHandlerInitializer;
import com.minecolonies.coremod.proxy.ClientProxy;
import com.minecolonies.coremod.proxy.CommonProxy;
import com.minecolonies.coremod.proxy.IProxy;
import com.minecolonies.coremod.proxy.ServerProxy;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.Consumer;

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
    public static final IProxy proxy = DistExecutor.unsafeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public MineColonies()
    {
        LanguageHandler.loadLangPath("assets/minecolonies/lang/%s.json"); // hotfix config comments, it's ugly bcs it's gonna be replaced
        config = new Configuration();

        Consumer<TagsUpdatedEvent> onTagsLoaded = (event) -> ModTags.tagsLoaded = true;
        MinecraftForge.EVENT_BUS.addListener(onTagsLoaded);

        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(EventHandler.class);
        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(FMLEventHandler.class);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(ClientEventHandler.class));
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(HighlightManager.class));

        Mod.EventBusSubscriber.Bus.MOD.bus().get().register(CommonProxy.class);

        Mod.EventBusSubscriber.Bus.MOD.bus().get().addListener(GatherDataHandler::dataGeneratorSetup);

        Mod.EventBusSubscriber.Bus.MOD.bus().get().register(this.getClass());

        // Temporary additional
        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(TagWorkAroundEventHandler.TagEventHandler.class);
        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(TagWorkAroundEventHandler.TagFMLEventHandlers.class);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(TagWorkAroundEventHandler.TagClientEventHandler.class));

        InteractionValidatorInitializer.init();
        proxy.setupApi();
    }

    /**
     * Called when registering sounds, we have to register all our mod items here.
     *
     * @param event the registery event for items.
     */
    @SubscribeEvent
    public static void registerSounds(@NotNull final RegistryEvent.Register<SoundEvent> event)
    {
        ModSoundEvents.registerSounds(event.getRegistry());
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onStitch(final TextureStitchEvent.Pre event)
    {
        if (!event.getMap().location().equals(AtlasTexture.LOCATION_BLOCKS))
        {
            return;
        }
        event.addSprite(new ResourceLocation(Constants.MOD_ID, "blocks/enchanting_table_book"));
        event.addSprite(new ResourceLocation(Constants.MOD_ID, "blocks/blockscarecrownormal"));
        event.addSprite(new ResourceLocation(Constants.MOD_ID, "blocks/blockscarecrowpumpkin"));
    }

    /**
     * Event handler for forge pre init event.
     *
     * @param event the forge pre init event.
     */
    @SubscribeEvent
    public static void preInit(@NotNull final FMLCommonSetupEvent event)
    {
        StructureLoadingUtils.addOriginMod(Constants.MOD_ID);
        CapabilityManager.INSTANCE.register(IColonyTagCapability.class, new IColonyTagCapability.Storage(), IColonyTagCapability.Impl::new);
        CapabilityManager.INSTANCE.register(IChunkmanagerCapability.class, new IChunkmanagerCapability.Storage(), IChunkmanagerCapability.Impl::new);
        CapabilityManager.INSTANCE.register(IColonyManagerCapability.class, new IColonyManagerCapability.Storage(), IColonyManagerCapability.Impl::new);

        Network.getNetwork().registerCommonMessages();

        AdvancementTriggers.preInit();

        StandardFactoryControllerInitializer.onPreInit();

        event.enqueueWork(ModLootConditions::init);
    }

    @SubscribeEvent
    public static void createEntityAttribute(final EntityAttributeCreationEvent event)
    {
        event.put(ModEntities.CITIZEN, AbstractEntityCitizen.getDefaultAttributes().build());
        event.put(ModEntities.VISITOR, AbstractEntityCitizen.getDefaultAttributes().build());
        event.put(ModEntities.MERCENARY, EntityMercenary.getDefaultAttributes().build());
        event.put(ModEntities.BARBARIAN, AbstractEntityMinecoloniesMob.getDefaultAttributes().build());
        event.put(ModEntities.ARCHERBARBARIAN, AbstractEntityMinecoloniesMob.getDefaultAttributes().build());
        event.put(ModEntities.CHIEFBARBARIAN, AbstractEntityMinecoloniesMob.getDefaultAttributes().build());
        event.put(ModEntities.PHARAO, AbstractEntityMinecoloniesMob.getDefaultAttributes().build());
        event.put(ModEntities.MUMMY, AbstractEntityMinecoloniesMob.getDefaultAttributes().build());
        event.put(ModEntities.ARCHERMUMMY, AbstractEntityMinecoloniesMob.getDefaultAttributes().build());
        event.put(ModEntities.PIRATE, AbstractEntityMinecoloniesMob.getDefaultAttributes().build());
        event.put(ModEntities.ARCHERPIRATE, AbstractEntityMinecoloniesMob.getDefaultAttributes().build());
        event.put(ModEntities.CHIEFPIRATE, AbstractEntityMinecoloniesMob.getDefaultAttributes().build());
        event.put(ModEntities.AMAZON, AbstractEntityMinecoloniesMob.getDefaultAttributes().build());
        event.put(ModEntities.AMAZONSPEARMAN, AbstractEntityMinecoloniesMob.getDefaultAttributes().build());
        event.put(ModEntities.AMAZONCHIEF, AbstractEntityMinecoloniesMob.getDefaultAttributes().build());
        event.put(ModEntities.NORSEMEN_ARCHER, AbstractEntityMinecoloniesMob.getDefaultAttributes().build());
        event.put(ModEntities.NORSEMEN_CHIEF, AbstractEntityMinecoloniesMob.getDefaultAttributes().build());
        event.put(ModEntities.SHIELDMAIDEN, AbstractEntityMinecoloniesMob.getDefaultAttributes().build());
    }

    @SubscribeEvent
    public static void registerAttributes(RegistryEvent.Register<Attribute> event)
    {
        RaiderMobUtils.MOB_ATTACK_DAMAGE.setRegistryName(Constants.MOD_ID, RaiderMobUtils.MOB_ATTACK_DAMAGE.getDescriptionId());
        event.getRegistry().register(RaiderMobUtils.MOB_ATTACK_DAMAGE);
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
        PlacementHandlerInitializer.initHandlers();
        RequestSystemInitializer.onPostInit();
    }

    /**
     * Called when MineCraft reloads a configuration file.
     *
     * @param event event
     */
    @SubscribeEvent
    public static void onConfigReload(final ModConfig.Reloading event)
    {
        if (event.getConfig().getType() == ModConfig.Type.SERVER)
        {
            // ModConfig fires for each of server, client, and common.
            // Request Systems logging only really needs to be changed on the server, and this reduced log spam.
            RequestSystemInitializer.reconfigureLogging();
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void doClientStuff(final FMLClientSetupEvent event)
    {
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.CITIZEN, RenderBipedCitizen::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.VISITOR, RenderBipedCitizen::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.FISHHOOK, RenderFishHook::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.FIREARROW, FireArrowRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.MC_NORMAL_ARROW, TippedArrowRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.SPEAR, RendererSpear::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.DRUID_POTION, m -> new SpriteRenderer(m, event.getMinecraftSupplier().get().getItemRenderer(), 1.0f, true));

        RenderingRegistry.registerEntityRenderingHandler(ModEntities.BARBARIAN, RendererBarbarian::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.ARCHERBARBARIAN, RendererBarbarian::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.CHIEFBARBARIAN, RendererChiefBarbarian::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.PIRATE, RendererPirate::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.ARCHERPIRATE, RendererArcherPirate::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.CHIEFPIRATE, RendererChiefPirate::new);

        RenderingRegistry.registerEntityRenderingHandler(ModEntities.MUMMY, RendererMummy::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.ARCHERMUMMY, RendererArcherMummy::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.PHARAO, RendererPharao::new);

        RenderingRegistry.registerEntityRenderingHandler(ModEntities.SHIELDMAIDEN, RendererShieldmaidenNorsemen::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.NORSEMEN_ARCHER, RendererArcherNorsemen::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.NORSEMEN_CHIEF, RendererChiefNorsemen::new);

        RenderingRegistry.registerEntityRenderingHandler(ModEntities.AMAZON, RendererAmazon::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.AMAZONSPEARMAN, RendererAmazonSpearman::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.AMAZONCHIEF, RendererChiefAmazon::new);

        RenderingRegistry.registerEntityRenderingHandler(ModEntities.MERCENARY, RenderMercenary::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.SITTINGENTITY, RenderSitting::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.MINECART, MinecartRenderer::new);

        ClientRegistry.bindTileEntityRenderer(MinecoloniesTileEntities.BUILDING, EmptyTileEntitySpecialRenderer::new);
        ClientRegistry.bindTileEntityRenderer(MinecoloniesTileEntities.SCARECROW, TileEntityScarecrowRenderer::new);
        ClientRegistry.bindTileEntityRenderer(MinecoloniesTileEntities.ENCHANTER, TileEntityEnchanterRenderer::new);
        ClientRegistry.bindTileEntityRenderer(MinecoloniesTileEntities.COLONY_FLAG, TileEntityColonyFlagRenderer::new);
        ClientRegistry.bindTileEntityRenderer(MinecoloniesTileEntities.NAMED_GRAVE, TileEntityNamedGraveRenderer::new);

        ModModelTypeInitializer.init();

        Arrays.stream(ModBlocks.getHuts())
          .forEach(hut -> RenderTypeLookup.setRenderLayer(hut, renderType -> renderType.equals(RenderType.cutout()) || renderType.equals(RenderType.solid()) || renderType.equals(RenderType.translucent())));
        RenderTypeLookup.setRenderLayer(ModBlocks.blockScarecrow, RenderType.cutout());
        RenderTypeLookup.setRenderLayer(ModBlocks.blockRack, RenderType.cutout());
        RenderTypeLookup.setRenderLayer(ModBlocks.blockDecorationPlaceholder, RenderType.cutout());
        RenderTypeLookup.setRenderLayer(ModBlocks.blockCompostedDirt, RenderType.cutout());
        RenderTypeLookup.setRenderLayer(ModBlocks.blockBarrel, RenderType.cutout());
        RenderTypeLookup.setRenderLayer(ModBlocks.blockWayPoint, RenderType.cutout());

        ItemModelsProperties.register(ModItems.spear, new ResourceLocation("throwing"), (item, world, entity) ->
                                                                                          (entity != null && entity.isUsingItem() && entity.getUseItem() == item) ? 1.0F : 0.0F);
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
