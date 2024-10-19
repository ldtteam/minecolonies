package com.minecolonies.core.generation.defaults;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.core.blocks.MinecoloniesCropBlock;
import com.minecolonies.core.generation.SimpleLootTableProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static com.minecolonies.api.loot.ModLootConditions.EXPEDITION_DIFFICULTY_PARAM;
import static com.minecolonies.core.generation.ExpeditionResourceManager.*;
import static com.minecolonies.core.generation.defaults.DefaultExpeditionEncountersProvider.*;

/**
 * Loot table generator for expeditions.
 */
public class DefaultExpeditionStructureLootProvider extends SimpleLootTableProvider
{
    /**
     * Expedition structure constants.
     */
    public static final ResourceLocation ANCIENT_CITY_ID     = new ResourceLocation("ancient_city");
    public static final ResourceLocation BASTION_REMNANT_ID  = new ResourceLocation("bastion_remnant");
    public static final ResourceLocation BURIED_TREASURE_ID  = new ResourceLocation("buried_treasure");
    public static final ResourceLocation END_CITY_ID         = new ResourceLocation("end_city");
    public static final ResourceLocation FORTRESS_ID         = new ResourceLocation("fortress");
    public static final ResourceLocation MANSION_ID          = new ResourceLocation("mansion");
    public static final ResourceLocation MINESHAFT_ID        = new ResourceLocation("mineshaft");
    public static final ResourceLocation MONUMENT_ID         = new ResourceLocation("monument");
    public static final ResourceLocation NETHER_FOSSIL_ID    = new ResourceLocation("nether_fossil");
    public static final ResourceLocation PILLAGER_OUTPOST_ID = new ResourceLocation("pillager_outpost");
    public static final ResourceLocation RUINED_PORTAL_ID    = new ResourceLocation("ruined_portal");
    public static final ResourceLocation SHIPWRECK_ID        = new ResourceLocation("shipwreck");
    public static final ResourceLocation STRONGHOLD_ID       = new ResourceLocation("stronghold");
    public static final ResourceLocation DESERT_PYRAMID_ID   = new ResourceLocation("desert_pyramid");
    public static final ResourceLocation IGLOO_ID            = new ResourceLocation("igloo");
    public static final ResourceLocation JUNGLE_TEMPLE_ID    = new ResourceLocation("jungle_temple");
    public static final ResourceLocation SWAMP_HUT_ID        = new ResourceLocation("swamp_hut");
    public static final ResourceLocation VILLAGE_DESERT_ID   = new ResourceLocation("village_desert");
    public static final ResourceLocation VILLAGE_PLAINS_ID   = new ResourceLocation("village_plains");
    public static final ResourceLocation VILLAGE_SAVANNA_ID  = new ResourceLocation("village_savanna");
    public static final ResourceLocation VILLAGE_SNOWY_ID    = new ResourceLocation("village_snowy");
    public static final ResourceLocation VILLAGE_TAIGA_ID    = new ResourceLocation("village_taiga");

    /**
     * Number providers.
     */
    private static final NumberProvider COMMON_STRUCTURE_ROLLS   = UniformGenerator.between(5, 8);
    private static final NumberProvider UNCOMMON_STRUCTURE_ROLLS = UniformGenerator.between(3, 5);
    private static final NumberProvider SPECIAL_STRUCTURE_ROLLS  = UniformGenerator.between(1, 3);

    /**
     * Default constructor.
     */
    public DefaultExpeditionStructureLootProvider(final PackOutput output)
    {
        super(output);
    }

    @Override
    @NotNull
    public String getName()
    {
        return "Expedition Structure Loot";
    }

    /**
     * Simple builder to automatically build a structure loot table.
     *
     * @param id        the id of the structure.
     * @param registrar the loot table registrar.
     * @param configure the further configuration handler.
     */
    private void createStructureLootTable(final ResourceLocation id, final @NotNull LootTableRegistrar registrar, final Consumer<Builder> configure)
    {
        final NumberProvider singleRoll = ConstantValue.exactly(1);

        final Builder builder = new Builder();
        builder.withPool(new LootPool.Builder().setRolls(singleRoll).add(createStructureStartItem(id)));
        configure.accept(builder);
        builder.withPool(new LootPool.Builder().setRolls(singleRoll).add(createStructureEndItem(id)));

        final LootContextParamSet paramSet = LootContextParamSet.builder().required(EXPEDITION_DIFFICULTY_PARAM).build();

        registrar.register(getStructureId(id), paramSet, builder);
    }

    @Override
    protected void registerTables(final @NotNull LootTableRegistrar registrar)
    {
        createStructureLootTable(ANCIENT_CITY_ID, registrar, builder -> builder.withPool(
          new LootPool.Builder().setRolls(SPECIAL_STRUCTURE_ROLLS)
            // Blocks
            .add(createSimpleItem(Items.DEEPSLATE_BRICK_SLAB, 100).common().build())
            // Items
            .add(createSimpleItem(Items.BOOK, 100).common().build())
            .add(createSimpleItem(Items.NAME_TAG, 75).build())
            .add(createSimpleItem(Items.LEAD, 75).build())
            .add(createSimpleItem(Items.EXPERIENCE_BOTTLE, 40).build())
            .add(createSimpleItem(Items.SCULK, 40).build())
            .add(createSimpleItem(Items.SCULK_SENSOR, 30).build())
            .add(createSimpleItem(Items.SCULK_CATALYST, 20).build())
            // Tools
            .add(createToolItem(Items.IRON_LEGGINGS, 40).damageLow().enchant().build())
            .add(createToolItem(Items.DIAMOND_LEGGINGS, 5).damageMid().enchant().diffAfter(DIFF_2).build())
            .add(createSimpleItem(Items.ENCHANTED_GOLDEN_APPLE, 5).diffAfter(DIFF_3).build())
            .add(createEnchantItem(Items.ENCHANTED_BOOK, 5).enchant(Enchantments.MENDING).diffAfter(DIFF_3).build())
            // Mob encounters
            .add(createEncounterLootItem(WARDEN, 10).diffAfter(DIFF_3).build())
        ));

        createStructureLootTable(BASTION_REMNANT_ID, registrar, builder -> builder.withPool(
          new LootPool.Builder().setRolls(UNCOMMON_STRUCTURE_ROLLS)
            // Blocks
            .add(createSimpleItem(Items.POLISHED_BLACKSTONE_BRICKS, 100).common().build())
            .add(createSimpleItem(Items.GOLD_BLOCK, 50).build())
            .add(createSimpleItem(Items.CRYING_OBSIDIAN, 25).build())
            .add(createSimpleItem(Items.GILDED_BLACKSTONE, 15).diffAfter(DIFF_3).build())
            // Items
            .add(createSimpleItem(Items.IRON_INGOT, 100).uncommon().build())
            .add(createSimpleItem(Items.GOLD_INGOT, 100).uncommon().build())
            .add(createSimpleItem(Items.SPECTRAL_ARROW, 50).build())
            .add(createSimpleItem(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 5).diffAfter(DIFF_3).build())
            // Tools
            .add(createToolItem(Items.CROSSBOW, 100).damageLow().build())
            // Mob encounters
            .add(createEncounterLootItem(PIGLIN, 150).build())
            .add(createEncounterLootItem(PIGLIN_BRUTE, 150).build())
            .add(createEncounterLootItem(HOGLIN, 100).diffAfter(DIFF_3).build())
        ));

        createStructureLootTable(BURIED_TREASURE_ID, registrar, builder -> builder.withPool(
          new LootPool.Builder().setRolls(UNCOMMON_STRUCTURE_ROLLS)
            .add(createSimpleItem(Items.COOKED_COD, 100).build())
            .add(createSimpleItem(Items.IRON_INGOT, 75).uncommon().build())
            .add(createSimpleItem(Items.GOLD_INGOT, 75).rare().build())
            .add(createSimpleItem(Items.DIAMOND, 25).diffAfter(DIFF_2).build())
            .add(createSimpleItem(Items.EMERALD, 25).diffAfter(DIFF_2).build())
            .add(createPotionItem(Potions.WATER_BREATHING, 15).diffAfter(DIFF_2).build())
            .add(createSimpleItem(Items.HEART_OF_THE_SEA, 5).diffAfter(DIFF_3).build())
            .add(createEncounterLootItem(DROWNED, 100).build())
        ));

        createStructureLootTable(END_CITY_ID, registrar, builder -> builder.withPool(
          new LootPool.Builder().setRolls(UNCOMMON_STRUCTURE_ROLLS)
            .add(createSimpleItem(Items.END_STONE_BRICKS, 100).common().build())
            .add(createSimpleItem(Items.PURPUR_BLOCK, 100).common().build())
            .add(createSimpleItem(Items.IRON_INGOT, 200).uncommon().build())
            .add(createSimpleItem(Items.GOLD_INGOT, 200).rare().build())
            .add(createSimpleItem(Items.SADDLE, 100).build())
            .add(createSimpleItem(Items.DIAMOND, 50).build())
            .add(createSimpleItem(Items.EMERALD, 50).build())
            .add(createToolItem(Items.IRON_HELMET, 25).enchant().damageLow().build())
            .add(createToolItem(Items.IRON_CHESTPLATE, 25).enchant().damageLow().build())
            .add(createToolItem(Items.IRON_LEGGINGS, 25).enchant().damageLow().build())
            .add(createToolItem(Items.IRON_BOOTS, 25).enchant().damageLow().build())
            .add(createToolItem(Items.IRON_SWORD, 25).enchant().damageLow().build())
            .add(createToolItem(Items.IRON_PICKAXE, 25).enchant().damageLow().build())
            .add(createToolItem(Items.IRON_SHOVEL, 25).enchant().damageLow().build())
            .add(createToolItem(Items.DIAMOND_HELMET, 5).enchant().damageMid().diffAfter(DIFF_3).build())
            .add(createToolItem(Items.DIAMOND_CHESTPLATE, 5).enchant().damageMid().diffAfter(DIFF_3).build())
            .add(createToolItem(Items.DIAMOND_LEGGINGS, 5).enchant().damageMid().diffAfter(DIFF_3).build())
            .add(createToolItem(Items.DIAMOND_BOOTS, 5).enchant().damageMid().diffAfter(DIFF_3).build())
            .add(createToolItem(Items.DIAMOND_SWORD, 5).enchant().damageMid().diffAfter(DIFF_3).build())
            .add(createToolItem(Items.DIAMOND_PICKAXE, 5).enchant().damageMid().diffAfter(DIFF_3).build())
            .add(createToolItem(Items.DIAMOND_SHOVEL, 5).enchant().damageMid().diffAfter(DIFF_3).build())
            .add(createSimpleItem(Items.ELYTRA, 1).diffAfter(DIFF_3).build())
            .add(createEncounterLootItem(ENDERMAN, 200).build())
            .add(createEncounterLootItem(SHULKER, 300).build())
        ));

        createStructureLootTable(FORTRESS_ID, registrar, builder -> builder.withPool(
          new LootPool.Builder().setRolls(COMMON_STRUCTURE_ROLLS)
            .add(createSimpleItem(Items.NETHER_BRICKS, 100).common().build())
            .add(createSimpleItem(Items.IRON_INGOT, 100).uncommon().build())
            .add(createSimpleItem(Items.GOLD_INGOT, 100).rare().build())
            .add(createSimpleItem(Items.NETHER_WART, 75).uncommon().build())
            .add(createSimpleItem(Items.OBSIDIAN, 60).build())
            .add(createSimpleItem(Items.DIAMOND, 50).build())
            .add(createSimpleItem(Items.SADDLE, 50).build())
            .add(createEncounterLootItem(ZOMBIFIED_PIGLIN, 20).build())
            .add(createEncounterLootItem(SKELETON, 20).build())
            .add(createEncounterLootItem(WITHER_SKELETON, 10).build())
            .add(createEncounterLootItem(BLAZE, 10).build())
            .add(createEncounterLootItem(MAGMA_CUBE_LARGE, 5).build())
            .add(createEncounterLootItem(MAGMA_CUBE_MEDIUM, 10).build())
            .add(createEncounterLootItem(MAGMA_CUBE_SMALL, 20).build())
        ));

        createStructureLootTable(MANSION_ID, registrar, builder -> builder.withPool(
          new LootPool.Builder().setRolls(UNCOMMON_STRUCTURE_ROLLS)
            .add(createSimpleItem(Items.IRON_INGOT, 100).uncommon().build())
            .add(createSimpleItem(Items.GOLD_INGOT, 100).rare().build())
            .add(createSimpleItem(Items.NAME_TAG, 75).build())
            .add(createSimpleItem(Items.LEAD, 75).build())
            .add(createSimpleItem(Items.REDSTONE, 50).build())
            .add(createSimpleItem(Items.GOLDEN_APPLE, 25).diffAfter(DIFF_2).build())
            .add(createToolItem(Items.DIAMOND_CHESTPLATE, 20).enchant().damageMid().diffAfter(DIFF_2).build())
            .add(createToolItem(Items.ENCHANTED_BOOK, 15).enchant().diffAfter(DIFF_3).build())
            .add(createSimpleItem(Items.ENCHANTED_GOLDEN_APPLE, 5).diffAfter(DIFF_3).build())
            .add(createEncounterLootItem(VINDICATOR, 100).build())
            .add(createEncounterLootItem(EVOKER, 50).diffAfter(DIFF_2).build())
            .add(createEncounterLootItem(VEX, 50).diffAfter(DIFF_2).build())
        ));

        createStructureLootTable(MINESHAFT_ID, registrar, builder -> builder.withPool(
          new LootPool.Builder().setRolls(COMMON_STRUCTURE_ROLLS)
            .add(createSimpleItem(Items.RAIL, 100).common().build())
            .add(createSimpleItem(Items.POWERED_RAIL, 75).rare().build())
            .add(createSimpleItem(Items.DETECTOR_RAIL, 50).rare().build())
            .add(createSimpleItem(Items.ACTIVATOR_RAIL, 50).rare().build())
            .add(createSimpleItem(Items.IRON_INGOT, 50).uncommon().build())
            .add(createSimpleItem(Items.GOLD_INGOT, 50).rare().build())
            .add(createSimpleItem(Items.DIAMOND, 25).build())
            .add(createSimpleItem(Items.NAME_TAG, 25).build())
            .add(createSimpleItem(Items.GOLDEN_APPLE, 25).diffAfter(DIFF_2).build())
            .add(createToolItem(Items.ENCHANTED_BOOK, 20).enchant().diffAfter(DIFF_3).build())
            .add(createSimpleItem(Items.ENCHANTED_GOLDEN_APPLE, 5).diffAfter(DIFF_3).build())
            .add(createEncounterLootItem(ZOMBIE, 100).build())
            .add(createEncounterLootItem(SKELETON, 50).build())
            .add(createEncounterLootItem(CREEPER, 30).build())
            .add(createEncounterLootItem(SPIDER, 30).build())
            .add(createEncounterLootItem(CAVE_SPIDER, 30).build())
            .add(createEncounterLootItem(ENDERMAN, 15).build())
        ));

        createStructureLootTable(MONUMENT_ID, registrar, builder -> builder.withPool(
          new LootPool.Builder().setRolls(UNCOMMON_STRUCTURE_ROLLS)
            .add(createSimpleItem(Items.PRISMARINE_BRICKS, 50).build())
            .add(createSimpleItem(Items.DARK_PRISMARINE, 25).build())
            .add(createSimpleItem(Items.SEA_LANTERN, 15).build())
            .add(createSimpleItem(Items.WET_SPONGE, 15).diffAfter(DIFF_2).build())
            .add(createEncounterLootItem(GUARDIAN, 50).build())
            .add(createEncounterLootItem(ELDER_GUARDIAN, 5).diffAfter(DIFF_3).build())
        ));

        createStructureLootTable(NETHER_FOSSIL_ID, registrar, builder -> builder.withPool(
          new LootPool.Builder().setRolls(COMMON_STRUCTURE_ROLLS)
            .add(createSimpleItem(Items.AIR, 25).build())
            .add(createSimpleItem(Items.BONE_BLOCK, 50).build())
            .add(createSimpleItem(Items.BONE_MEAL, 10).build())
            .add(createEncounterLootItem(ZOMBIFIED_PIGLIN, 25).build())
        ));

        createStructureLootTable(PILLAGER_OUTPOST_ID, registrar, builder -> builder.withPool(
          new LootPool.Builder().setRolls(COMMON_STRUCTURE_ROLLS)
            .add(createSimpleItem(Items.IRON_INGOT, 50).uncommon().build())
            .add(createSimpleItem(Items.EXPERIENCE_BOTTLE, 40).build())
            .add(createToolItem(Items.CROSSBOW, 40).damageLow().build())
            .add(createToolItem(Items.ENCHANTED_BOOK, 10).enchant().diffAfter(DIFF_3).build())
            .add(createEncounterLootItem(PILLAGER, 25).build())
            .add(createEncounterLootItem(PILLAGER_CAPTAIN, 5).build())
        ));

        createStructureLootTable(RUINED_PORTAL_ID, registrar, builder -> builder.withPool(
          new LootPool.Builder().setRolls(COMMON_STRUCTURE_ROLLS)
            .add(createSimpleItem(Items.NETHERRACK, 100).common().build())
            .add(createSimpleItem(Items.OBSIDIAN, 100).common().build())
            .add(createSimpleItem(Items.FIRE_CHARGE, 50).build())
            .add(createSimpleItem(Items.GOLDEN_APPLE, 25).build())
            .add(createToolItem(Items.GOLDEN_HELMET, 25).damageLow().enchant().build())
            .add(createToolItem(Items.GOLDEN_CHESTPLATE, 25).damageLow().enchant().build())
            .add(createToolItem(Items.GOLDEN_LEGGINGS, 25).damageLow().enchant().build())
            .add(createToolItem(Items.GOLDEN_BOOTS, 25).damageLow().enchant().build())
            .add(createToolItem(Items.GOLDEN_SWORD, 25).damageLow().enchant().build())
            .add(createToolItem(Items.GOLDEN_PICKAXE, 25).damageLow().enchant().build())
            .add(createToolItem(Items.GOLDEN_SHOVEL, 25).damageLow().enchant().build())
            .add(createSimpleItem(Items.GOLD_BLOCK, 10).build())
            .add(createEncounterLootItem(ZOMBIFIED_PIGLIN, 25).build())
        ));

        createStructureLootTable(SHIPWRECK_ID, registrar, builder -> {
            MinecoloniesCropBlock[] crops = ModBlocks.getCrops();
            final LootPool.Builder pool = new LootPool.Builder().setRolls(COMMON_STRUCTURE_ROLLS)
                                            .add(createSimpleItem(Items.OAK_PLANKS, crops.length * 50).common().build())
                                            .add(createSimpleItem(Items.SPRUCE_PLANKS, crops.length * 50).common().build())
                                            .add(createSimpleItem(Items.IRON_INGOT, crops.length * 15).build())
                                            .add(createSimpleItem(Items.GOLD_INGOT, crops.length * 10).build())
                                            .add(createSimpleItem(Items.DIAMOND, crops.length * 5).diffAfter(DIFF_2).build())
                                            .add(createEncounterLootItem(DROWNED, crops.length * 20).build())
                                            .add(createEncounterLootItem(DROWNED_TRIDENT, crops.length * 10).build());
            for (MinecoloniesCropBlock crop : crops)
            {
                pool.add(createSimpleItem(crop.asItem(), 75).common().build());
            }
            builder.withPool(pool);
        });

        createStructureLootTable(STRONGHOLD_ID, registrar, builder -> builder.withPool(
          new LootPool.Builder().setRolls(SPECIAL_STRUCTURE_ROLLS)
            .add(createSimpleItem(Items.STONE_BRICKS, 100).common().build())
            .add(createSimpleItem(Items.CRACKED_STONE_BRICKS, 100).common().build())
            .add(createSimpleItem(Items.ENDER_PEARL, 75).build())
            .add(createSimpleItem(Items.BOOK, 50).uncommon().build())
            .add(createSimpleItem(Items.BOOKSHELF, 25).build())
            .add(createSimpleItem(Items.DIAMOND, 25).diffAfter(DIFF_2).build())
            .add(createSimpleItem(Items.EMERALD, 25).diffAfter(DIFF_2).build())
            .add(createToolItem(Items.ENCHANTED_BOOK, 20).enchant().diffAfter(DIFF_3).build())
            .add(createEncounterLootItem(ZOMBIE, 50).build())
            .add(createEncounterLootItem(SKELETON, 30).build())
            .add(createEncounterLootItem(CREEPER, 10).build())
            .add(createEncounterLootItem(ENDERMAN, 10).build())
        ));

        createStructureLootTable(DESERT_PYRAMID_ID, registrar, builder -> builder.withPool(
          new LootPool.Builder().setRolls(UNCOMMON_STRUCTURE_ROLLS)
            .add(createSimpleItem(Items.SANDSTONE, 100).common().build())
            .add(createSimpleItem(Items.CUT_SANDSTONE, 100).common().build())
            .add(createSimpleItem(Items.IRON_INGOT, 75).uncommon().build())
            .add(createSimpleItem(Items.GOLD_INGOT, 50).uncommon().build())
            .add(createSimpleItem(Items.GOLDEN_APPLE, 25).build())
            .add(createSimpleItem(Items.DIAMOND, 25).build())
            .add(createSimpleItem(Items.EMERALD, 25).build())
            .add(createToolItem(Items.ENCHANTED_BOOK, 20).enchant().build())
            .add(createEncounterLootItem(ZOMBIE, 50).build())
            .add(createEncounterLootItem(SKELETON, 30).build())
            .add(createEncounterLootItem(CREEPER, 10).build())
        ));

        createStructureLootTable(IGLOO_ID, registrar, builder -> builder.withPool(
          new LootPool.Builder().setRolls(UNCOMMON_STRUCTURE_ROLLS)
            .add(createSimpleItem(Items.SNOW, 100).common().build())
            .add(createSimpleItem(Items.ICE, 100).common().build())
            .add(createSimpleItem(Items.SNOWBALL, 75).uncommon().build())
            .add(createSimpleItem(ModItems.lamb_stew, 50).uncommon().build())
            .add(createSimpleItem(Items.EMERALD, 25).build())
        ));

        createStructureLootTable(JUNGLE_TEMPLE_ID, registrar, builder -> builder.withPool(
          new LootPool.Builder().setRolls(UNCOMMON_STRUCTURE_ROLLS)
            .add(createSimpleItem(Items.COBBLESTONE, 100).common().build())
            .add(createSimpleItem(Items.MOSSY_COBBLESTONE, 100).common().build())
            .add(createSimpleItem(Items.COBWEB, 75).uncommon().build())
            .add(createSimpleItem(ModItems.pita_hummus, 50).rare().build())
            .add(createSimpleItem(ModItems.pepper_hummus, 50).rare().build())
            .add(createSimpleItem(Items.DIAMOND, 35).diffAfter(DIFF_2).build())
            .add(createSimpleItem(Items.EMERALD, 35).diffAfter(DIFF_2).build())
            .add(createToolItem(Items.ENCHANTED_BOOK, 20).enchant().diffAfter(DIFF_3).build())
            .add(createEncounterLootItem(ZOMBIE, 50).build())
            .add(createEncounterLootItem(SKELETON, 30).build())
            .add(createEncounterLootItem(CREEPER, 10).build())
        ));

        createStructureLootTable(SWAMP_HUT_ID, registrar, builder -> builder.withPool(
          new LootPool.Builder().setRolls(UNCOMMON_STRUCTURE_ROLLS)
            .add(createSimpleItem(Items.SPRUCE_PLANKS, 100).common().build())
            .add(createSimpleItem(Items.SPRUCE_LOG, 100).common().build())
            .add(createSimpleItem(Items.RED_MUSHROOM, 50).uncommon().build())
            .add(createSimpleItem(Items.BROWN_MUSHROOM, 50).uncommon().build())
            .add(createSimpleItem(Items.CAULDRON, 50).build())
            .add(createSimpleItem(Items.BREWING_STAND, 50).build())
            .add(createEncounterLootItem(WITCH, 100).build())
            .add(createEncounterLootItem(ZOMBIE, 50).build())
        ));

        createStructureLootTable(VILLAGE_DESERT_ID, registrar, builder -> {
            List<MinecoloniesCropBlock> crops =
              Arrays.stream(ModBlocks.getCrops()).filter(f -> f.getPreferredBiome() == null || f.getPreferredBiome().equals(ModTags.dryBiomes)).toList();
            final LootPool.Builder pool = new LootPool.Builder().setRolls(COMMON_STRUCTURE_ROLLS)
                                            .add(createSimpleItem(Items.SANDSTONE, crops.size() * 50).common().build())
                                            .add(createSimpleItem(Items.SMOOTH_SANDSTONE, crops.size() * 50).common().build())
                                            .add(createSimpleItem(Items.CACTUS, crops.size() * 30).uncommon().build())
                                            .add(createSimpleItem(Items.DEAD_BUSH, crops.size() * 30).uncommon().build())
                                            .add(createSimpleItem(Items.CLAY_BALL, crops.size() * 20).rare().build())
                                            .add(createSimpleItem(ModItems.flatbread, crops.size() * 30).uncommon().build())
                                            .add(createSimpleItem(ModItems.pottage, crops.size() * 20).rare().build())
                                            .add(createSimpleItem(ModItems.pepper_hummus, crops.size() * 30).rare().build())
                                            .add(createSimpleItem(ModItems.pita_hummus, crops.size() * 20).build());
            for (MinecoloniesCropBlock crop : crops)
            {
                pool.add(createSimpleItem(crop.asItem(), 75).common().build());
            }
            builder.withPool(pool);
        });

        createStructureLootTable(VILLAGE_PLAINS_ID, registrar, builder -> {
            List<MinecoloniesCropBlock> crops =
              Arrays.stream(ModBlocks.getCrops()).filter(f -> f.getPreferredBiome() == null || f.getPreferredBiome().equals(ModTags.temperateBiomes)).toList();
            final LootPool.Builder pool = new LootPool.Builder().setRolls(COMMON_STRUCTURE_ROLLS)
                                            .add(createSimpleItem(Items.COBBLESTONE, crops.size() * 50).common().build())
                                            .add(createSimpleItem(Items.OAK_PLANKS, crops.size() * 50).common().build())
                                            .add(createSimpleItem(Items.OAK_SAPLING, crops.size() * 30).uncommon().build())
                                            .add(createSimpleItem(Items.DANDELION, crops.size() * 30).uncommon().build())
                                            .add(createSimpleItem(Items.POPPY, crops.size() * 30).uncommon().build())
                                            .add(createSimpleItem(ModItems.cheddar_cheese, crops.size() * 30).uncommon().build())
                                            .add(createSimpleItem(ModItems.pasta_plain, crops.size() * 20).rare().build())
                                            .add(createSimpleItem(ModItems.pasta_tomato, crops.size() * 30).rare().build())
                                            .add(createSimpleItem(ModItems.stuffed_pita, crops.size() * 20).build());
            for (MinecoloniesCropBlock crop : crops)
            {
                pool.add(createSimpleItem(crop.asItem(), 75).common().build());
            }
            builder.withPool(pool);
        });

        createStructureLootTable(VILLAGE_SAVANNA_ID, registrar, builder -> {
            List<MinecoloniesCropBlock> crops =
              Arrays.stream(ModBlocks.getCrops()).filter(f -> f.getPreferredBiome() == null || f.getPreferredBiome().equals(ModTags.dryBiomes)).toList();
            final LootPool.Builder pool = new LootPool.Builder().setRolls(COMMON_STRUCTURE_ROLLS)
                                            .add(createSimpleItem(Items.YELLOW_TERRACOTTA, crops.size() * 50).common().build())
                                            .add(createSimpleItem(Items.ACACIA_PLANKS, crops.size() * 50).common().build())
                                            .add(createSimpleItem(Items.ACACIA_SAPLING, crops.size() * 30).uncommon().build())
                                            .add(createSimpleItem(Items.GRASS, crops.size() * 30).uncommon().build())
                                            .add(createSimpleItem(Items.TALL_GRASS, crops.size() * 30).uncommon().build())
                                            .add(createSimpleItem(ModItems.tofu, crops.size() * 30).uncommon().build())
                                            .add(createSimpleItem(ModItems.lembas_scone, crops.size() * 20).rare().build())
                                            .add(createSimpleItem(ModItems.pepper_hummus, crops.size() * 30).rare().build())
                                            .add(createSimpleItem(ModItems.pita_hummus, crops.size() * 20).build());
            for (MinecoloniesCropBlock crop : crops)
            {
                pool.add(createSimpleItem(crop.asItem(), 75).common().build());
            }
            builder.withPool(pool);
        });

        createStructureLootTable(VILLAGE_SNOWY_ID, registrar, builder -> {
            List<MinecoloniesCropBlock> crops =
              Arrays.stream(ModBlocks.getCrops()).filter(f -> f.getPreferredBiome() == null || f.getPreferredBiome().equals(ModTags.coldBiomes)).toList();
            final LootPool.Builder pool = new LootPool.Builder().setRolls(COMMON_STRUCTURE_ROLLS)
                                            .add(createSimpleItem(Items.SNOW_BLOCK, crops.size() * 50).common().build())
                                            .add(createSimpleItem(Items.ICE, crops.size() * 50).common().build())
                                            .add(createSimpleItem(Items.SNOWBALL, crops.size() * 30).uncommon().build())
                                            .add(createSimpleItem(Items.BEETROOT, crops.size() * 30).uncommon().build())
                                            .add(createSimpleItem(Items.BEETROOT_SOUP, crops.size() * 30).uncommon().build())
                                            .add(createSimpleItem(ModItems.feta_cheese, crops.size() * 30).uncommon().build())
                                            .add(createSimpleItem(ModItems.manchet_bread, crops.size() * 20).rare().build())
                                            .add(createSimpleItem(ModItems.cabochis, crops.size() * 30).rare().build())
                                            .add(createSimpleItem(ModItems.lamb_stew, crops.size() * 20).build());
            for (MinecoloniesCropBlock crop : crops)
            {
                pool.add(createSimpleItem(crop.asItem(), 75).common().build());
            }
            builder.withPool(pool);
        });

        createStructureLootTable(VILLAGE_TAIGA_ID, registrar, builder -> {
            List<MinecoloniesCropBlock> crops =
              Arrays.stream(ModBlocks.getCrops()).filter(f -> f.getPreferredBiome() == null || f.getPreferredBiome().equals(ModTags.coldBiomes)).toList();
            final LootPool.Builder pool = new LootPool.Builder().setRolls(COMMON_STRUCTURE_ROLLS)
                                            .add(createSimpleItem(Items.COBBLESTONE, crops.size() * 50).common().build())
                                            .add(createSimpleItem(Items.SPRUCE_PLANKS, crops.size() * 50).common().build())
                                            .add(createSimpleItem(Items.FERN, crops.size() * 30).uncommon().build())
                                            .add(createSimpleItem(Items.LARGE_FERN, crops.size() * 30).uncommon().build())
                                            .add(createSimpleItem(Items.SWEET_BERRIES, crops.size() * 30).uncommon().build())
                                            .add(createSimpleItem(ModItems.cooked_rice, crops.size() * 30).uncommon().build())
                                            .add(createSimpleItem(ModItems.muffin, crops.size() * 20).rare().build())
                                            .add(createSimpleItem(ModItems.cabochis, crops.size() * 30).rare().build())
                                            .add(createSimpleItem(ModItems.lamb_stew, crops.size() * 20).build());
            for (MinecoloniesCropBlock crop : crops)
            {
                pool.add(createSimpleItem(crop.asItem(), 75).common().build());
            }
            builder.withPool(pool);
        });
    }
}
