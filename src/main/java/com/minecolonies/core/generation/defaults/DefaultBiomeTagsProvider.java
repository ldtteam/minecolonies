package com.minecolonies.core.generation.defaults;

import com.minecolonies.api.items.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

@SuppressWarnings({"ConstantConditions", "unchecked"})
public class DefaultBiomeTagsProvider extends BiomeTagsProvider
{

    public DefaultBiomeTagsProvider(
      final PackOutput output,
      final CompletableFuture<HolderLookup.Provider> lookupProvider,
      @Nullable final ExistingFileHelper existingFileHelper)
    {
        super(output, lookupProvider, MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(final HolderLookup.Provider holder)
    {
        tag(ModTags.coldBiomes)
          .addTags(BiomeTags.IS_TAIGA)
          .addTags(BiomeTags.SPAWNS_SNOW_FOXES)
          .addTags(BiomeTags.POLAR_BEARS_SPAWN_ON_ALTERNATE_BLOCKS)
          .addTags(Tags.Biomes.IS_COLD)
          .addTags(BiomeTags.IS_END)
          .addTags(Tags.Biomes.IS_SNOWY)
          .add(Biomes.COLD_OCEAN,
            Biomes.DEEP_COLD_OCEAN,
            Biomes.DEEP_FROZEN_OCEAN,
            Biomes.DEEP_DARK,
            Biomes.FROZEN_OCEAN,
            Biomes.DEEP_FROZEN_OCEAN,
            Biomes.FROZEN_RIVER,
            Biomes.FROZEN_PEAKS,
            Biomes.GROVE,
            Biomes.ICE_SPIKES,
            Biomes.JAGGED_PEAKS,
            Biomes.OLD_GROWTH_PINE_TAIGA,
            Biomes.OLD_GROWTH_SPRUCE_TAIGA,
            Biomes.SNOWY_BEACH,
            Biomes.SNOWY_PLAINS,
            Biomes.SNOWY_TAIGA,
            Biomes.SNOWY_SLOPES,
            Biomes.STONY_PEAKS,
            Biomes.STONY_SHORE,
            Biomes.TAIGA,
            Biomes.WINDSWEPT_FOREST,
            Biomes.WINDSWEPT_HILLS,
            Biomes.WINDSWEPT_GRAVELLY_HILLS);

        tag(ModTags.temperateBiomes)
          .addTags(BiomeTags.HAS_VILLAGE_PLAINS)
          .addTags(Tags.Biomes.IS_PLAINS)
          .addTags(Tags.Biomes.IS_SWAMP)
          .remove(Tags.Biomes.IS_COLD, Tags.Biomes.IS_DRY, Tags.Biomes.IS_DESERT)
          .add(Biomes.BIRCH_FOREST,
            Biomes.CHERRY_GROVE,
            Biomes.DARK_FOREST,
            Biomes.DEEP_LUKEWARM_OCEAN,
            Biomes.DEEP_OCEAN,
            Biomes.FLOWER_FOREST,
            Biomes.FOREST,
            Biomes.LUKEWARM_OCEAN,
            Biomes.MEADOW,
            Biomes.MUSHROOM_FIELDS,
            Biomes.OLD_GROWTH_BIRCH_FOREST,
            Biomes.PLAINS,
            Biomes.RIVER,
            Biomes.SUNFLOWER_PLAINS,
            Biomes.SWAMP);

        tag(ModTags.humidBiomes)
          .addTags(BiomeTags.IS_JUNGLE)
          .addTags(Tags.Biomes.IS_WET_OVERWORLD)
          .add(Biomes.BAMBOO_JUNGLE,
            Biomes.BEACH,
            Biomes.RIVER,
            Biomes.WARM_OCEAN,
            Biomes.DRIPSTONE_CAVES,
            Biomes.JUNGLE,
            Biomes.LUSH_CAVES,
            Biomes.MANGROVE_SWAMP,
            Biomes.SPARSE_JUNGLE);

        tag(ModTags.dryBiomes)
          .addTags(BiomeTags.HAS_DESERT_PYRAMID)
          .addTags(Tags.Biomes.IS_DESERT)
          .addTags(Tags.Biomes.IS_DRY)
          .addTags(BiomeTags.IS_SAVANNA)
          .addTags(BiomeTags.IS_NETHER)
          .add(Biomes.BADLANDS,
            Biomes.DESERT,
            Biomes.ERODED_BADLANDS,
            Biomes.SAVANNA,
            Biomes.SAVANNA_PLATEAU,
            Biomes.WINDSWEPT_SAVANNA);
    }
}
