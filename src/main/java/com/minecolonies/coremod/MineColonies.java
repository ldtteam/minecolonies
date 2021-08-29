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
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.sounds.ModSoundEvents;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.apiimp.initializer.InteractionValidatorInitializer;
import com.minecolonies.coremod.client.render.*;
import com.minecolonies.coremod.client.render.mobs.RenderMercenary;
import com.minecolonies.coremod.client.render.mobs.amazon.RendererAmazon;
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
import com.minecolonies.coremod.colony.IColonyManagerCapability;
import com.minecolonies.coremod.colony.requestsystem.init.RequestSystemInitializer;
import com.minecolonies.coremod.colony.requestsystem.init.StandardFactoryControllerInitializer;
import com.minecolonies.coremod.entity.mobs.EntityMercenary;
import com.minecolonies.coremod.event.*;
import com.minecolonies.coremod.placementhandlers.PlacementHandlerInitializer;
import com.minecolonies.coremod.proxy.ClientProxy;
import com.minecolonies.coremod.proxy.CommonProxy;
import com.minecolonies.coremod.proxy.IProxy;
import com.minecolonies.coremod.proxy.ServerProxy;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.client.renderer.entity.TippableArrowRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
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
        if (!event.getMap().location().equals(TextureAtlas.LOCATION_BLOCKS))
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

        Network.getNetwork().registerCommonMessages();

        AdvancementTriggers.preInit();

        StandardFactoryControllerInitializer.onPreInit();
    }

    @SubscribeEvent
    public static void registerCaps(final RegisterCapabilitiesEvent event)
    {
        event.register(IColonyTagCapability.class);
        event.register(IChunkmanagerCapability.class);
        event.register(IColonyManagerCapability.class);
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
     * @param event event
     */
    @SubscribeEvent
    public static void onConfigReload(final ModConfigEvent.Reloading event)
    {
        if(event.getConfig().getType() == ModConfig.Type.SERVER)
        {
            // ModConfig fires for each of server, client, and common.
            // Request Systems logging only really needs to be changed on the server, and this reduced log spam.
            RequestSystemInitializer.reconfigureLogging();
        }
    }
    
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void doClientStuff(final EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerEntityRenderer(ModEntities.CITIZEN, RenderBipedCitizen::new);
        event.registerEntityRenderer(ModEntities.VISITOR, RenderBipedCitizen::new);
        event.registerEntityRenderer(ModEntities.FISHHOOK, RenderFishHook::new);
        event.registerEntityRenderer(ModEntities.FIREARROW, FireArrowRenderer::new);
        event.registerEntityRenderer(ModEntities.MC_NORMAL_ARROW, TippableArrowRenderer::new);

        event.registerEntityRenderer(ModEntities.BARBARIAN, RendererBarbarian::new);
        event.registerEntityRenderer(ModEntities.ARCHERBARBARIAN, RendererBarbarian::new);
        event.registerEntityRenderer(ModEntities.CHIEFBARBARIAN, RendererChiefBarbarian::new);
        event.registerEntityRenderer(ModEntities.PIRATE, RendererPirate::new);
        event.registerEntityRenderer(ModEntities.ARCHERPIRATE, RendererArcherPirate::new);
        event.registerEntityRenderer(ModEntities.CHIEFPIRATE, RendererChiefPirate::new);

        event.registerEntityRenderer(ModEntities.MUMMY, RendererMummy::new);
        event.registerEntityRenderer(ModEntities.ARCHERMUMMY, RendererArcherMummy::new);
        event.registerEntityRenderer(ModEntities.PHARAO, RendererPharao::new);

        event.registerEntityRenderer(ModEntities.SHIELDMAIDEN, RendererShieldmaidenNorsemen::new);
        event.registerEntityRenderer(ModEntities.NORSEMEN_ARCHER, RendererArcherNorsemen::new);
        event.registerEntityRenderer(ModEntities.NORSEMEN_CHIEF, RendererChiefNorsemen::new);

        event.registerEntityRenderer(ModEntities.AMAZON, RendererAmazon::new);
        event.registerEntityRenderer(ModEntities.AMAZONCHIEF, RendererChiefAmazon::new);

        event.registerEntityRenderer(ModEntities.MERCENARY, RenderMercenary::new);
        event.registerEntityRenderer(ModEntities.SITTINGENTITY, RenderSitting::new);
        event.registerEntityRenderer(ModEntities.MINECART, (context) -> new MinecartRenderer<>(context, ModelLayers.MINECART));

        event.registerBlockEntityRenderer(MinecoloniesTileEntities.BUILDING, EmptyTileEntitySpecialRenderer::new);
        event.registerBlockEntityRenderer(MinecoloniesTileEntities.SCARECROW, TileEntityScarecrowRenderer::new);
        event.registerBlockEntityRenderer(MinecoloniesTileEntities.ENCHANTER, TileEntityEnchanterRenderer::new);
        event.registerBlockEntityRenderer(MinecoloniesTileEntities.COLONY_FLAG, TileEntityColonyFlagRenderer::new);
        event.registerBlockEntityRenderer(MinecoloniesTileEntities.NAMED_GRAVE, TileEntityNamedGraveRenderer::new);

        Arrays.stream(ModBlocks.getHuts())
          .forEach(hut -> ItemBlockRenderTypes.setRenderLayer(hut, renderType -> renderType.equals(RenderType.cutout()) || renderType.equals(RenderType.solid())));
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.blockScarecrow, RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.blockRack, RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.blockDecorationPlaceholder, RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.blockCompostedDirt, RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.blockBarrel, RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.blockWayPoint, RenderType.cutout());
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
