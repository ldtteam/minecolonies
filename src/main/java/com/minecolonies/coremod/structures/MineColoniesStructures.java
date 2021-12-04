package com.minecolonies.coremod.structures;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

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

    /**
     * Setup new structure.
     */
    public static void setupStructures()
    {
        setupMapSpacingAndLand(
          EMPTY_COLONY.get(),
          new StructureFeatureConfiguration(40,
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
}