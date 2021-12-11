package com.minecolonies.coremod.structures;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Thanks to: https://github.com/TelepathicGrunt/StructureTutorialMod/tree/1.18.x-Forge-Jigsaw
 */
public class MineColoniesStructures
{
    public static final DeferredRegister<StructureFeature<?>>                 DEFERRED_REGISTRY_STRUCTURE =
      DeferredRegister.create(ForgeRegistries.STRUCTURE_FEATURES, Constants.MOD_ID);
    public static final RegistryObject<StructureFeature<JigsawConfiguration>> EMPTY_COLONY                =
      DEFERRED_REGISTRY_STRUCTURE.register("empty_colony", () -> (new EmptyColonyStructure(JigsawConfiguration.CODEC)));

    public static void setup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(() -> {
            MineColoniesStructures.setupStructures();
            MineColoniesConfiguredStructures.registerConfiguredStructures();
        });
    }

    /**
     * Setup new structure.
     */
    public static void setupStructures()
    {
        setupMapSpacingAndLand(
          EMPTY_COLONY.get(),
          new StructureFeatureConfiguration(MineColonies.getConfig().getServer().averageEmptyColonyDistance.get(),
            20,
            435989457),
          true);
    }

    /**
     * Setup structure.
     * @param structure the structure to setup.
     * @param featureConfig the feature config.
     * @param transformSurroundingLand the surrounding land transform.
     * @param <F> generic type of the feature.
     */
    public static <F extends StructureFeature<?>> void setupMapSpacingAndLand(
      F structure,
      StructureFeatureConfiguration featureConfig,
      boolean transformSurroundingLand)
    {
        StructureFeature.STRUCTURES_REGISTRY.put(structure.getRegistryName().toString(), structure);

        if (transformSurroundingLand)
        {
            StructureFeature.NOISE_AFFECTING_FEATURES =
              ImmutableList.<StructureFeature<?>>builder()
                .addAll(StructureFeature.NOISE_AFFECTING_FEATURES)
                .add(structure)
                .build();
        }

        StructureSettings.DEFAULTS =
          ImmutableMap.<StructureFeature<?>, StructureFeatureConfiguration>builder()
            .putAll(StructureSettings.DEFAULTS)
            .put(structure, featureConfig)
            .build();

        BuiltinRegistries.NOISE_GENERATOR_SETTINGS.entrySet().forEach(settings -> {
            Map<StructureFeature<?>, StructureFeatureConfiguration> structureMap = settings.getValue().structureSettings().structureConfig();

            if (structureMap instanceof ImmutableMap)
            {
                Map<StructureFeature<?>, StructureFeatureConfiguration> tempMap = new HashMap<>(structureMap);
                tempMap.put(structure, featureConfig);
                settings.getValue().structureSettings().structureConfig = tempMap;
            }
            else
            {
                structureMap.put(structure, featureConfig);
            }
        });
    }

    /**
     * Specifies structure spawning restrictions.
     */
    private static Method GETCODEC_METHOD;
    public static void addDimensionalSpacing(final WorldEvent.Load event)
    {
        if (event.getWorld() instanceof ServerLevel serverLevel)
        {
            ChunkGenerator chunkGenerator = serverLevel.getChunkSource().getGenerator();
            // Avoid superflat world.
            if ((chunkGenerator instanceof FlatLevelSource && serverLevel.dimension().equals(Level.OVERWORLD)) || MineColonies.getConfig().getServer().disableEmptyColonies.get())
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
}