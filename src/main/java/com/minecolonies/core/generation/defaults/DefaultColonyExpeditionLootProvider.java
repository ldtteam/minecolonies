package com.minecolonies.core.generation.defaults;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.generation.SimpleLootTableProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;

import static com.minecolonies.api.loot.ModLootConditions.EXPEDITION_PARAMS;
import static com.minecolonies.core.generation.ExpeditionResourceManager.*;
import static com.minecolonies.core.generation.defaults.DefaultExpeditionEncountersProvider.*;
import static com.minecolonies.core.generation.defaults.DefaultExpeditionStructureLootProvider.*;

/**
 * Loot table generator for expeditions.
 */
public class DefaultColonyExpeditionLootProvider extends SimpleLootTableProvider
{
    /**
     * Expedition constants.
     */
    public static final ResourceLocation EXPEDITION_OVERWORLD_LOOT = new ResourceLocation(Constants.MOD_ID, "expeditions/expedition_overworld");
    public static final ResourceLocation EXPEDITION_NETHER_LOOT    = new ResourceLocation(Constants.MOD_ID, "expeditions/expedition_nether");
    public static final ResourceLocation EXPEDITION_END_LOOT       = new ResourceLocation(Constants.MOD_ID, "expeditions/expedition_end");

    /**
     * Default constructor.
     */
    public DefaultColonyExpeditionLootProvider(final PackOutput output)
    {
        super(output);
    }

    @Override
    @NotNull
    public String getName()
    {
        return "Expedition Loot";
    }

    @Override
    protected void registerTables(final @NotNull LootTableRegistrar registrar)
    {
        createExpeditionLootTable(EXPEDITION_OVERWORLD_LOOT, registrar, builder -> builder.withPool(
          new LootPool.Builder()
            .setRolls(UniformGenerator.between(10, 20))
            // Logs
            .add(createSimpleItem(Items.OAK_LOG, 100).common().build())
            .add(createSimpleItem(Items.BIRCH_LOG, 100).common().build())
            .add(createSimpleItem(Items.SPRUCE_LOG, 100).common().build())
            .add(createSimpleItem(Items.DARK_OAK_LOG, 100).common().diffAfter(DIFF_2).build())
            .add(createSimpleItem(Items.ACACIA_LOG, 100).common().diffAfter(DIFF_2).build())
            .add(createSimpleItem(Items.CHERRY_LOG, 100).common().diffAfter(DIFF_2).build())
            .add(createSimpleItem(Items.MANGROVE_LOG, 100).common().diffAfter(DIFF_2).build())
            // Ores and gems
            .add(createSimpleItem(Items.COAL, 100).common().build())
            .add(createSimpleItem(Items.RAW_COPPER, 100).common().build())
            .add(createSimpleItem(Items.RAW_IRON, 100).common().build())
            .add(createSimpleItem(Items.RAW_GOLD, 50).uncommon().diffAfter(DIFF_2).build())
            .add(createSimpleItem(Items.REDSTONE, 50).uncommon().diffAfter(DIFF_2).build())
            .add(createSimpleItem(Items.LAPIS_LAZULI, 50).uncommon().diffAfter(DIFF_2).build())
            // Flowers
            .add(createSimpleItem(Items.POPPY, 100).common().build())
            .add(createSimpleItem(Items.ROSE_BUSH, 100).common().build())
            .add(createSimpleItem(Items.DANDELION, 100).common().build())
            .add(createSimpleItem(Items.DANDELION, 100).common().build())
            .add(createSimpleItem(Items.CORNFLOWER, 100).common().build())
            .add(createSimpleItem(Items.AZURE_BLUET, 100).common().build())
            .add(createSimpleItem(Items.OXEYE_DAISY, 100).common().build())
            .add(createSimpleItem(Items.LILY_OF_THE_VALLEY, 100).common().build())
            .add(createSimpleItem(Items.RED_TULIP, 80).uncommon().diffAfter(DIFF_2).build())
            .add(createSimpleItem(Items.PINK_TULIP, 80).uncommon().diffAfter(DIFF_2).build())
            .add(createSimpleItem(Items.ORANGE_TULIP, 80).uncommon().diffAfter(DIFF_2).build())
            .add(createSimpleItem(Items.WHITE_TULIP, 80).uncommon().diffAfter(DIFF_2).build())
            .add(createSimpleItem(Items.SUNFLOWER, 80).uncommon().diffAfter(DIFF_2).build())
            .add(createSimpleItem(Items.TORCHFLOWER, 50).rare().diffAfter(DIFF_3).build())
            .add(createSimpleItem(Items.ALLIUM, 50).rare().diffAfter(DIFF_3).build())
            .add(createSimpleItem(Items.BLUE_ORCHID, 50).rare().diffAfter(DIFF_3).build())
            .add(createSimpleItem(Items.PITCHER_PLANT, 50).rare().diffAfter(DIFF_3).build())
            // Mushrooms
            .add(createSimpleItem(Items.BROWN_MUSHROOM, 50).uncommon().diffAfter(DIFF_2).build())
            .add(createSimpleItem(Items.RED_MUSHROOM, 50).uncommon().diffAfter(DIFF_2).build())
            // Structures - Friendly
            .add(createStructureRef(VILLAGE_DESERT_ID, 50).diffBefore(DIFF_2).build())
            .add(createStructureRef(VILLAGE_PLAINS_ID, 50).diffBefore(DIFF_2).build())
            .add(createStructureRef(VILLAGE_SAVANNA_ID, 50).diffBefore(DIFF_2).build())
            .add(createStructureRef(VILLAGE_SNOWY_ID, 50).diffBefore(DIFF_2).build())
            .add(createStructureRef(VILLAGE_TAIGA_ID, 50).diffBefore(DIFF_2).build())
            .add(createStructureRef(IGLOO_ID, 50).diffBefore(DIFF_2).build())
            // Structures - Enemy - Simple - Overground
            .add(createStructureRef(RUINED_PORTAL_ID, 25).diffBefore(DIFF_2).build())
            .add(createStructureRef(PILLAGER_OUTPOST_ID, 25).diffBefore(DIFF_2).build())
            .add(createStructureRef(DESERT_PYRAMID_ID, 25).diffBefore(DIFF_2).build())
            .add(createStructureRef(JUNGLE_TEMPLE_ID, 25).diffBefore(DIFF_2).build())
            .add(createStructureRef(SWAMP_HUT_ID, 25).diffBefore(DIFF_2).build())
            // Structures - Enemy - Simple - Underground/water
            .add(createStructureRef(MINESHAFT_ID, 25).diffAfter(DIFF_2).build())
            .add(createStructureRef(SHIPWRECK_ID, 25).diffAfter(DIFF_2).build())
            .add(createStructureRef(BURIED_TREASURE_ID, 25).diffAfter(DIFF_2).build())
            // Structures - Enemy - Difficult
            .add(createStructureRef(STRONGHOLD_ID, 25).diffAfter(DIFF_3).build())
            .add(createStructureRef(MONUMENT_ID, 25).diffAfter(DIFF_3).build())
            .add(createStructureRef(MANSION_ID, 25).diffAfter(DIFF_3).build())
            .add(createStructureRef(ANCIENT_CITY_ID, 25).diffAfter(DIFF_3).build())
            // Encounters
            .add(createEncounterLootItem(ZOMBIE, 100).build())
            .add(createEncounterLootItem(SKELETON, 50).build())
            .add(createEncounterLootItem(CREEPER, 30).build())
            .add(createEncounterLootItem(SPIDER, 30).build())
            .add(createEncounterLootItem(CAVE_SPIDER, 30).build())
            .add(createEncounterLootItem(ENDERMAN, 15).build())
        ));

        createExpeditionLootTable(EXPEDITION_NETHER_LOOT, registrar, builder -> builder.withPool(
          new LootPool.Builder()
            .setRolls(UniformGenerator.between(10, 20))
            // Blocks
            .add(createSimpleItem(Items.NETHERRACK, 100).common().build())
            .add(createSimpleItem(Items.BLACKSTONE, 100).common().build())
            .add(createSimpleItem(Items.BASALT, 100).common().build())
            .add(createSimpleItem(Items.SOUL_SAND, 100).common().build())
            .add(createSimpleItem(Items.SOUL_SOIL, 100).common().build())
            .add(createSimpleItem(Items.GRAVEL, 100).common().build())
            .add(createSimpleItem(Items.CRIMSON_NYLIUM, 100).common().build())
            .add(createSimpleItem(Items.WARPED_NYLIUM, 100).common().build())
            .add(createSimpleItem(Items.MAGMA_BLOCK, 50).uncommon().diffAfter(DIFF_2).build())
            .add(createSimpleItem(Items.OBSIDIAN, 50).uncommon().diffAfter(DIFF_2).build())
            // Ores and gems
            .add(createSimpleItem(Items.GOLD_NUGGET, 100).common().build())
            .add(createSimpleItem(Items.QUARTZ, 100).common().build())
            .add(createSimpleItem(Items.GLOWSTONE, 50).uncommon().diffAfter(DIFF_2).build())
            .add(createSimpleItem(Items.ANCIENT_DEBRIS, 25).rare().diffAfter(DIFF_3).build())
            // Flowers
            .add(createSimpleItem(Items.BROWN_MUSHROOM, 100).common().build())
            .add(createSimpleItem(Items.RED_MUSHROOM, 100).common().build())
            .add(createSimpleItem(Items.CRIMSON_FUNGUS, 100).common().build())
            .add(createSimpleItem(Items.WARPED_FUNGUS, 100).common().build())
            .add(createSimpleItem(Items.CRIMSON_STEM, 50).uncommon().diffAfter(DIFF_2).build())
            .add(createSimpleItem(Items.WARPED_STEM, 50).uncommon().diffAfter(DIFF_2).build())
            .add(createSimpleItem(Items.SHROOMLIGHT, 50).uncommon().diffAfter(DIFF_2).build())
            .add(createSimpleItem(Items.TWISTING_VINES, 50).uncommon().diffAfter(DIFF_2).build())
            .add(createSimpleItem(Items.WEEPING_VINES, 50).uncommon().diffAfter(DIFF_2).build())
            // Structures - Friendly
            .add(createStructureRef(NETHER_FOSSIL_ID, 50).diffBefore(DIFF_2).build())
            // Structures - Enemy
            .add(createStructureRef(FORTRESS_ID, 25).diffAfter(DIFF_2).build())
            .add(createStructureRef(BASTION_REMNANT_ID, 25).diffAfter(DIFF_2).build())
            // Encounters
            .add(createEncounterLootItem(ZOMBIFIED_PIGLIN, 100).build())
            .add(createEncounterLootItem(PIGLIN, 50).build())
            .add(createEncounterLootItem(HOGLIN, 30).build())
            .add(createEncounterLootItem(WITHER_SKELETON, 30).build())
            .add(createEncounterLootItem(BLAZE, 30).build())
        ));

        createExpeditionLootTable(EXPEDITION_END_LOOT, registrar, builder -> builder.withPool(
          new LootPool.Builder()
            .setRolls(UniformGenerator.between(10, 20))
            // Blocks
            .add(createSimpleItem(Items.END_STONE, 100).common().build())
            .add(createSimpleItem(Items.OBSIDIAN, 50).uncommon().build())
            // Plants
            .add(createSimpleItem(Items.CHORUS_PLANT, 100).common().build())
            .add(createSimpleItem(Items.CHORUS_FLOWER, 100).common().build())
            // Structures - Enemy
            .add(createStructureRef(END_CITY_ID, 25).diffAfter(DIFF_2).build())
            .add(createStructureRef(BASTION_REMNANT_ID, 25).diffAfter(DIFF_2).build())
            // Encounters
            .add(createEncounterLootItem(ENDERMAN, 100).build())
        ));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void validate(final @NotNull Map<ResourceLocation, LootTable> map, final @NotNull ValidationContext validationTracker)
    {
        ValidationContext newTracker = new ValidationContext(EXPEDITION_PARAMS, new LootDataResolver()
        {
            @Nullable
            public <T> T getElement(@NotNull LootDataId<T> id)
            {
                if (id.location().getPath().startsWith("expeditions/structures"))
                {
                    return (T) map.get(id.location());
                }
                return null;
            }
        });

        super.validate(map, newTracker);
    }

    /**
     * Simple builder to automatically build an expedition loot table.
     *
     * @param id        the id of the expedition.
     * @param registrar the loot table registrar.
     * @param configure the further configuration handler.
     */
    private void createExpeditionLootTable(final ResourceLocation id, final @NotNull LootTableRegistrar registrar, final Consumer<Builder> configure)
    {
        final Builder builder = new Builder();
        configure.accept(builder);

        registrar.register(id, EXPEDITION_PARAMS, builder);
    }
}
