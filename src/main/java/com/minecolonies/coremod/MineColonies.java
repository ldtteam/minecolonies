package com.minecolonies.coremod;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.ldtteam.structurize.util.LanguageHandler;
import com.ldtteam.structurize.util.StructureLoadingUtils;
import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.colony.IChunkmanagerCapability;
import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.configuration.Configuration;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.api.entity.mobs.RaiderMobUtils;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.loot.ModLootConditions;
import com.minecolonies.api.sounds.ModSoundEvents;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.apiimp.initializer.InteractionValidatorInitializer;
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
import com.minecolonies.coremod.structures.EmptyColonyStructure;
import com.minecolonies.coremod.structures.MineColoniesConfiguredStructures;
import com.minecolonies.coremod.structures.MineColoniesStructures;
import com.mojang.serialization.Codec;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
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
        LanguageHandler.loadLangPath("assets/minecolonies/lang/%s.json"); // hotfix config comments, it's ugly bcs it's gonna be replaced
        config = new Configuration();

        Consumer<TagsUpdatedEvent> onTagsLoaded = (event) -> ModTags.tagsLoaded = true;
        MinecraftForge.EVENT_BUS.addListener(onTagsLoaded);

        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(EventHandler.class);
        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(FMLEventHandler.class);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(ClientEventHandler.class));

        Mod.EventBusSubscriber.Bus.MOD.bus().get().register(CommonProxy.class);

        Mod.EventBusSubscriber.Bus.MOD.bus().get().addListener(GatherDataHandler::dataGeneratorSetup);

        Mod.EventBusSubscriber.Bus.MOD.bus().get().register(this.getClass());
        Mod.EventBusSubscriber.Bus.MOD.bus().get().register(ClientRegistryHandler.class);

        // Temporary additional
        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(TagWorkAroundEventHandler.TagEventHandler.class);
        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(TagWorkAroundEventHandler.TagFMLEventHandlers.class);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(TagWorkAroundEventHandler.TagClientEventHandler.class));

        InteractionValidatorInitializer.init();
        proxy.setupApi();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MineColoniesStructures.DEFERRED_REGISTRY_STRUCTURE.register(modEventBus);
        modEventBus.addListener(this::setup);

        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        forgeBus.addListener(EventPriority.NORMAL, this::addDimensionalSpacing);
        forgeBus.addListener(EventPriority.NORMAL, EmptyColonyStructure::setupStructureSpawns);
    }

    public void setup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(() -> {
            MineColoniesStructures.setupStructures();
            MineColoniesConfiguredStructures.registerConfiguredStructures();
        });
    }

    // todo, change the missing respective building thing.
    // todo: tick in the TEs and check for colony periodically
    // todo: when colony exists, check for building, if building doesn't exist, create building. (can use chunk claim for that as a first double check).
    // todo: actually add reactivation UI instead of automatic reactivation, use chunk claim to check if it needs reactivation.
    //also add some deco schems


    /**
     * Specifies structure spawning restrictions.
     */
    private static Method GETCODEC_METHOD;
    public void addDimensionalSpacing(final WorldEvent.Load event)
    {
        if (event.getWorld() instanceof ServerLevel serverLevel)
        {
            ChunkGenerator chunkGenerator = serverLevel.getChunkSource().getGenerator();
            // Avoid superflat world.
            if (chunkGenerator instanceof FlatLevelSource && serverLevel.dimension().equals(Level.OVERWORLD))
            {
                return;
            }

            StructureSettings worldStructureConfig = chunkGenerator.getSettings();

            HashMap<StructureFeature<?>, HashMultimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>>> map = new HashMap<>();

            for (Map.Entry<ResourceKey<Biome>, Biome> biomeEntry : serverLevel.registryAccess().ownedRegistryOrThrow(Registry.BIOME_REGISTRY).entrySet())
            {
                Biome.BiomeCategory biomeCategory = biomeEntry.getValue().getBiomeCategory();
                if (biomeCategory != Biome.BiomeCategory.OCEAN
                      && biomeCategory != Biome.BiomeCategory.THEEND
                      && biomeCategory != Biome.BiomeCategory.NETHER
                      && biomeCategory != Biome.BiomeCategory.NONE
                      && biomeCategory != Biome.BiomeCategory.DESERT)
                {
                    // This adds it to all biomes, if we want other biomes of this specific structure. We have to manually fill the map above easily.
                    associateBiomeToConfiguredStructure(map, MineColoniesConfiguredStructures.CONFIGURED_EMPTY_COLONY, biomeEntry.getKey());
                }
            }
            ImmutableMap.Builder<StructureFeature<?>, ImmutableMultimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>>> tempStructureToMultiMap = ImmutableMap.builder();
            worldStructureConfig.configuredStructures.entrySet().stream().filter(entry -> !map.containsKey(entry.getKey())).forEach(tempStructureToMultiMap::put);

            map.forEach((key, value) -> tempStructureToMultiMap.put(key, ImmutableMultimap.copyOf(value)));
            worldStructureConfig.configuredStructures = tempStructureToMultiMap.build();

            // Terraforge workaround.
            try
            {
                if (GETCODEC_METHOD == null)
                {
                    GETCODEC_METHOD = ObfuscationReflectionHelper.findMethod(ChunkGenerator.class, "func_230347_a_");
                }
                ResourceLocation cgRL = Registry.CHUNK_GENERATOR.getKey((Codec<? extends ChunkGenerator>) GETCODEC_METHOD.invoke(chunkGenerator));
                if (cgRL != null && cgRL.getNamespace().equals("terraforged"))
                {
                    return;
                }
            }
            catch (Exception e)
            {
                Log.getLogger().error("Was unable to check if " + serverLevel.dimension().location() + " is using Terraforged's ChunkGenerator.");
            }

            Map<StructureFeature<?>, StructureFeatureConfiguration> tempMap = new HashMap<>(worldStructureConfig.structureConfig());
            tempMap.putIfAbsent(MineColoniesStructures.EMPTY_COLONY.get(), StructureSettings.DEFAULTS.get(MineColoniesStructures.EMPTY_COLONY.get()));
            worldStructureConfig.structureConfig = tempMap;
        }
    }

    /**
     * Helper method that handles setting up the map to multimap relationship to help prevent issues.
     */
    private static void associateBiomeToConfiguredStructure(
      Map<StructureFeature<?>, HashMultimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>>> STStructureToMultiMap,
      ConfiguredStructureFeature<?, ?> configuredStructureFeature,
      ResourceKey<Biome> biomeRegistryKey)
    {
        STStructureToMultiMap.putIfAbsent(configuredStructureFeature.feature, HashMultimap.create());
        HashMultimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>> configuredStructureToBiomeMultiMap = STStructureToMultiMap.get(configuredStructureFeature.feature);
        if (configuredStructureToBiomeMultiMap.containsValue(biomeRegistryKey))
        {
            Log.getLogger().error("Detected 2 ConfiguredStructureFeatures that share the same base StructureFeature trying to be added to same biome. One will be prevented from spawning. "
                                    + "This issue happens with vanilla too and is why a Snowy Village and Plains Village cannot spawn in the same biome because they both use the Village base structure.  "
                                    + "The two conflicting ConfiguredStructures are: {}, {} The biome that is attempting to be shared: {}",
              BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE.getId(configuredStructureFeature),
              BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE.getId(configuredStructureToBiomeMultiMap.entries()
                                                                     .stream()
                                                                     .filter(e -> e.getValue() == biomeRegistryKey)
                                                                     .findFirst()
                                                                     .get()
                                                                     .getKey()),
              biomeRegistryKey
            );
        }
        else
        {
            configuredStructureToBiomeMultiMap.put(configuredStructureFeature, biomeRegistryKey);
        }
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
        StructureLoadingUtils.addOriginMod(Constants.MOD_ID);

        Network.getNetwork().registerCommonMessages();

        AdvancementTriggers.preInit();

        StandardFactoryControllerInitializer.onPreInit();

        event.enqueueWork(ModLootConditions::init);
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
        if (event.getConfig().getType() == ModConfig.Type.SERVER)
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
