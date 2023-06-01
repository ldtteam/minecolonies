package com.minecolonies.coremod;

import com.ldtteam.structurize.storage.SurvivalBlueprintHandlers;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.colony.IChunkmanagerCapability;
import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.configuration.Configuration;
import com.minecolonies.api.enchants.ModEnchants;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.api.entity.mobs.RaiderMobUtils;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.loot.ModLootConditions;
import com.minecolonies.api.sounds.ModSoundEvents;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.apiimp.initializer.*;
import com.minecolonies.coremod.colony.IColonyManagerCapability;
import com.minecolonies.coremod.colony.requestsystem.init.RequestSystemInitializer;
import com.minecolonies.coremod.colony.requestsystem.init.StandardFactoryControllerInitializer;
import com.minecolonies.coremod.entity.mobs.EntityMercenary;
import com.minecolonies.coremod.event.*;
import com.minecolonies.coremod.placementhandlers.PlacementHandlerInitializer;
import com.minecolonies.coremod.placementhandlers.main.PlantationFieldPlacementHandler;
import com.minecolonies.coremod.placementhandlers.main.SuppliesHandler;
import com.minecolonies.coremod.placementhandlers.main.SurvivalHandler;
import com.minecolonies.coremod.proxy.ClientProxy;
import com.minecolonies.coremod.proxy.CommonProxy;
import com.minecolonies.coremod.proxy.IProxy;
import com.minecolonies.coremod.proxy.ServerProxy;
import com.minecolonies.coremod.structures.MineColoniesStructures;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@Mod(Constants.MOD_ID)
public class MineColonies
{
    public static final Capability<IChunkmanagerCapability> CHUNK_STORAGE_UPDATE_CAP = CapabilityManager.get(new CapabilityToken<>() {});

    public static final Capability<IColonyManagerCapability> COLONY_MANAGER_CAP = CapabilityManager.get(new CapabilityToken<>() {});

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
        TileEntityInitializer.BLOCK_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModEnchants.ENCHANTMENTS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModContainerInitializers.CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModBuildingsInitializer.DEFERRED_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModFieldsInitializer.DEFERRED_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModGuardTypesInitializer.DEFERRED_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModColonyEventDescriptionTypeInitializer.DEFERRED_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModResearchRequirementInitializer.DEFERRED_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModRecipeSerializerInitializer.RECIPE_SERIALIZER.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModRecipeSerializerInitializer.RECIPE_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModColonyEventTypeInitializer.DEFERRED_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModCraftingTypesInitializer.DEFERRED_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModJobsInitializer.DEFERRED_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModRecipeTypesInitializer.DEFERRED_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        RaiderMobUtils.ATTRIBUTES.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModSoundEvents.SOUND_EVENTS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModInteractionsInitializer.DEFERRED_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModResearchEffectInitializer.DEFERRED_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());

        ModQuestInitializer.DEFERRED_REGISTER_OBJECTIVE.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModQuestInitializer.DEFERRED_REGISTER_TRIGGER.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModQuestInitializer.DEFERRED_REGISTER_REWARD.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModQuestInitializer.DEFERRED_REGISTER_ANSWER_RESULT.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModHappinessFactorTypeInitializer.DEFERRED_REGISTER_HAPPINESS_FACTOR.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModHappinessFactorTypeInitializer.DEFERRED_REGISTER_HAPPINESS_FUNCTION.register(FMLJavaModLoadingContext.get().getModEventBus());


        ModEnchantInitializer.init();

        LanguageHandler.loadLangPath("assets/minecolonies/lang/%s.json"); // hotfix config comments, it's ugly bcs it's gonna be replaced
        config = new Configuration();

        Consumer<TagsUpdatedEvent> onTagsLoaded = (event) -> ModTags.tagsLoaded = true;
        MinecraftForge.EVENT_BUS.addListener(onTagsLoaded);

        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(EventHandler.class);
        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(FMLEventHandler.class);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(ClientEventHandler.class));
        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(DataPackSyncEventHandler.ServerEvents.class);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(DataPackSyncEventHandler.ClientEvents.class));

        Mod.EventBusSubscriber.Bus.MOD.bus().get().register(CommonProxy.class);

        Mod.EventBusSubscriber.Bus.MOD.bus().get().addListener(GatherDataHandler::dataGeneratorSetup);

        Mod.EventBusSubscriber.Bus.MOD.bus().get().register(this.getClass());
        Mod.EventBusSubscriber.Bus.MOD.bus().get().register(ClientRegistryHandler.class);

        InteractionValidatorInitializer.init();
        proxy.setupApi();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MineColoniesStructures.DEFERRED_REGISTRY_STRUCTURE.register(modEventBus);

        SurvivalBlueprintHandlers.registerHandler(new SurvivalHandler());
        SurvivalBlueprintHandlers.registerHandler(new SuppliesHandler());
        SurvivalBlueprintHandlers.registerHandler(new PlantationFieldPlacementHandler());
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onStitch(final TextureStitchEvent.Pre event)
    {
        if (!event.getAtlas().location().equals(TextureAtlas.LOCATION_BLOCKS))
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
        Network.getNetwork().registerCommonMessages();

        AdvancementTriggers.preInit();

        StandardFactoryControllerInitializer.onPreInit();

        event.enqueueWork(ModLootConditions::init);
        event.enqueueWork(ModTags::init);
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
        event.put(ModEntities.AMAZONSPEARMAN, AbstractEntityMinecoloniesMob.getDefaultAttributes().build());
        event.put(ModEntities.AMAZONCHIEF, AbstractEntityMinecoloniesMob.getDefaultAttributes().build());
        event.put(ModEntities.NORSEMEN_ARCHER, AbstractEntityMinecoloniesMob.getDefaultAttributes().build());
        event.put(ModEntities.NORSEMEN_CHIEF, AbstractEntityMinecoloniesMob.getDefaultAttributes().build());
        event.put(ModEntities.SHIELDMAIDEN, AbstractEntityMinecoloniesMob.getDefaultAttributes().build());
    }

    /**
     * Called when MC loading is about to finish.
     *
     * @param event event
     */
    @SubscribeEvent
    public static void onLoadComplete(final FMLLoadCompleteEvent event)
    {
        PlacementHandlerInitializer.initHandlers();
        RequestSystemInitializer.onPostInit();
    }

    /**
     * Called when MineCraft reloads a configuration file.
     *
     * @param event event
     */
    @SubscribeEvent
    public static void onConfigReload(final ModConfigEvent.Reloading event)
    {
        if (event.getConfig().getType() == ModConfig.Type.COMMON)
        {
            // ModConfig fires for each of server, client, and common.
            // Request Systems logging only really needs to be changed on the server, and this reduced log spam.
            RequestSystemInitializer.reconfigureLogging();
        }
    }

    @SubscribeEvent
    public static void onConfigLoaded(final ModConfigEvent.Loading event)
    {
        if (event.getConfig().getType() == ModConfig.Type.COMMON)
        {
            // ModConfig fires for each of server, client, and common.
            // Request Systems logging only really needs to be changed on the server, and this reduced log spam.
            RequestSystemInitializer.reconfigureLogging();
        }
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
