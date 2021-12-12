package com.minecolonies.coremod.structures;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.PlainVillagePools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;

public class MineColoniesConfiguredStructures
{
    /**
     * Static instance of the structure.
     */
    public static ConfiguredStructureFeature<?, ?> CONFIGURED_EMPTY_COLONY = MineColoniesStructures.EMPTY_COLONY.get().configured(new JigsawConfiguration(
      () -> PlainVillagePools.START,
      5
    ));

    public static void registerConfiguredStructures()
    {
        Registry<ConfiguredStructureFeature<?, ?>> registry = BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE;
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "configured_empty_colony"), CONFIGURED_EMPTY_COLONY);
    }
}
