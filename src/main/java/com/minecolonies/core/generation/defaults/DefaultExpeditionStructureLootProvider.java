package com.minecolonies.core.generation.defaults;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.blocks.MinecoloniesCropBlock;
import com.minecolonies.core.colony.expeditions.colony.types.ColonyExpeditionTypeDifficulty;
import com.minecolonies.core.generation.SimpleLootTableProvider;
import com.minecolonies.core.loot.ExpeditionDifficultyCondition;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemDamageFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

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
    public static final ResourceLocation OCEAN_RUIN_ID       = new ResourceLocation("ocean_ruin");
    public static final ResourceLocation PILLAGER_OUTPOST_ID = new ResourceLocation("pillager_outpost");
    public static final ResourceLocation RUINED_PORTAL_ID    = new ResourceLocation("ruined_portal");
    public static final ResourceLocation SHIPWRECK_ID        = new ResourceLocation("shipwreck");
    public static final ResourceLocation STRONGHOLD_ID       = new ResourceLocation("stronghold");
    public static final ResourceLocation DESERT_PYRAMID_ID   = new ResourceLocation("desert_pyramid");
    public static final ResourceLocation IGLOO_ID            = new ResourceLocation("igloo");
    public static final ResourceLocation JUNGLE_TEMPLE_ID    = new ResourceLocation("jungle_temple");
    public static final ResourceLocation SWAMP_HUT_ID        = new ResourceLocation("swamp_hut");
    public static final ResourceLocation TRAIL_RUINS_ID      = new ResourceLocation("trail_ruins");
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
    private static final NumberProvider LOW_TOOL_DAMAGE          = UniformGenerator.between(0.75f, 0.95f);
    private static final NumberProvider AVERAGE_TOOL_DAMAGE      = UniformGenerator.between(0.5f, 0.75f);
    private static final NumberProvider SUPER_COMMON_ITEM_COUNT  = UniformGenerator.between(3, 5);
    private static final NumberProvider COMMON_ITEM_COUNT        = UniformGenerator.between(1, 3);
    private static final NumberProvider UNCOMMON_ITEM_COUNT      = UniformGenerator.between(1, 2);

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
    public void createStructureLootTable(final ResourceLocation id, final @NotNull LootTableRegistrar registrar, final Consumer<Builder> configure)
    {
        final NumberProvider singleRoll = ConstantValue.exactly(1);

        final Builder builder = new Builder();
        builder.withPool(new LootPool.Builder().setRolls(singleRoll).add(createStructureStartItem(id)));
        configure.accept(builder);
        builder.withPool(new LootPool.Builder().setRolls(singleRoll).add(createStructureEndItem(id)));

        final LootContextParamSet paramSet = LootContextParamSet.builder()
                                               .required(new LootContextParam<>(new ResourceLocation(Constants.MOD_ID, "difficulty")))
                                               .build();

        registrar.register(new ResourceLocation(Constants.MOD_ID, id.withPrefix("expeditions/structures/").getPath()), paramSet, builder);
    }

    @Override
    protected void registerTables(final @NotNull LootTableRegistrar registrar)
    {
        createStructureLootTable(ANCIENT_CITY_ID, registrar, builder -> builder.withPool(
          new LootPool.Builder()
            .setRolls(SPECIAL_STRUCTURE_ROLLS)
            .add(LootItem.lootTableItem(Items.DEEPSLATE_BRICKS).setWeight(100).apply(SetItemCountFunction.setCount(SUPER_COMMON_ITEM_COUNT)))
            .add(LootItem.lootTableItem(Items.BOOK).setWeight(100))
            .add(LootItem.lootTableItem(Items.NAME_TAG).setWeight(75))
            .add(LootItem.lootTableItem(Items.LEAD).setWeight(75))
            .add(LootItem.lootTableItem(Items.IRON_LEGGINGS)
                   .setWeight(40)
                   .apply(EnchantRandomlyFunction.randomApplicableEnchantment())
                   .apply(SetItemDamageFunction.setDamage(LOW_TOOL_DAMAGE)))
            .add(LootItem.lootTableItem(Items.EXPERIENCE_BOTTLE).setWeight(40))
            .add(LootItem.lootTableItem(Items.SCULK).setWeight(40))
            .add(LootItem.lootTableItem(Items.SCULK_SENSOR).setWeight(30))
            .add(LootItem.lootTableItem(Items.SCULK_CATALYST).setWeight(20))
            .add(LootItem.lootTableItem(Items.DIAMOND_LEGGINGS)
                   .setWeight(5)
                   .apply(EnchantRandomlyFunction.randomApplicableEnchantment())
                   .apply(SetItemDamageFunction.setDamage(AVERAGE_TOOL_DAMAGE)))
            .add(LootItem.lootTableItem(Items.ENCHANTED_GOLDEN_APPLE).setWeight(5))
            .add(LootItem.lootTableItem(Items.ENCHANTED_BOOK)
                   .setWeight(5)
                   .apply(EnchantRandomlyFunction.randomEnchantment().withEnchantment(Enchantments.MENDING)))
            .add(createEncounterLootItem(WARDEN).setWeight(10).when(ExpeditionDifficultyCondition.forDifficulty(ColonyExpeditionTypeDifficulty.HARD)))
            .add(createEncounterLootItem(WARDEN).setWeight(20).when(ExpeditionDifficultyCondition.forDifficulty(ColonyExpeditionTypeDifficulty.NIGHTMARE)))
        ));

        createStructureLootTable(BASTION_REMNANT_ID, registrar, builder -> builder.withPool(
          new LootPool.Builder()
            .setRolls(UNCOMMON_STRUCTURE_ROLLS)
            .add(LootItem.lootTableItem(Items.POLISHED_BLACKSTONE_BRICKS).setWeight(100).apply(SetItemCountFunction.setCount(SUPER_COMMON_ITEM_COUNT)))
            .add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(100).apply(SetItemCountFunction.setCount(COMMON_ITEM_COUNT)))
            .add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(100).apply(SetItemCountFunction.setCount(UNCOMMON_ITEM_COUNT)))
            .add(LootItem.lootTableItem(Items.CROSSBOW).setWeight(100).apply(SetItemDamageFunction.setDamage(LOW_TOOL_DAMAGE)))
            .add(LootItem.lootTableItem(Items.GOLD_BLOCK).setWeight(50))
            .add(LootItem.lootTableItem(Items.SPECTRAL_ARROW).setWeight(50))
            .add(LootItem.lootTableItem(Items.CRYING_OBSIDIAN).setWeight(25))
            .add(LootItem.lootTableItem(Items.GILDED_BLACKSTONE).setWeight(15))
            .add(LootItem.lootTableItem(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE).setWeight(5))
            .add(createEncounterLootItem(PIGLIN).setWeight(150))
            .add(createEncounterLootItem(PIGLIN_BRUTE).setWeight(150))
            .add(createEncounterLootItem(HOGLIN).setWeight(100))
        ));

        createStructureLootTable(BURIED_TREASURE_ID, registrar, builder -> builder.withPool(
          new LootPool.Builder()
            .setRolls(UNCOMMON_STRUCTURE_ROLLS)
            .add(LootItem.lootTableItem(Items.COOKED_COD).setWeight(100))
            .add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(75).apply(SetItemCountFunction.setCount(COMMON_ITEM_COUNT)))
            .add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(75).apply(SetItemCountFunction.setCount(UNCOMMON_ITEM_COUNT)))
            .add(LootItem.lootTableItem(Items.DIAMOND).setWeight(25))
            .add(LootItem.lootTableItem(Items.EMERALD).setWeight(25))
            .add(createPotionItem(Potions.WATER_BREATHING).setWeight(15))
            .add(LootItem.lootTableItem(Items.HEART_OF_THE_SEA).setWeight(5))
            .add(createEncounterLootItem(DROWNED).setWeight(100))
        ));

        createStructureLootTable(END_CITY_ID, registrar, builder -> builder.withPool(
          new LootPool.Builder()
            .setRolls(UNCOMMON_STRUCTURE_ROLLS)
            .add(LootItem.lootTableItem(Items.END_STONE_BRICKS).setWeight(100).apply(SetItemCountFunction.setCount(SUPER_COMMON_ITEM_COUNT)))
            .add(LootItem.lootTableItem(Items.PURPUR_BLOCK).setWeight(100).apply(SetItemCountFunction.setCount(SUPER_COMMON_ITEM_COUNT)))
            .add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(200).apply(SetItemCountFunction.setCount(COMMON_ITEM_COUNT)))
            .add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(200).apply(SetItemCountFunction.setCount(UNCOMMON_ITEM_COUNT)))
            .add(LootItem.lootTableItem(Items.SADDLE).setWeight(100))
            .add(LootItem.lootTableItem(Items.DIAMOND).setWeight(50))
            .add(LootItem.lootTableItem(Items.EMERALD).setWeight(50))
            .add(LootItem.lootTableItem(Items.IRON_HELMET)
                   .setWeight(25)
                   .apply(EnchantRandomlyFunction.randomApplicableEnchantment())
                   .apply(SetItemDamageFunction.setDamage(LOW_TOOL_DAMAGE)))
            .add(LootItem.lootTableItem(Items.IRON_CHESTPLATE)
                   .setWeight(25)
                   .apply(EnchantRandomlyFunction.randomApplicableEnchantment())
                   .apply(SetItemDamageFunction.setDamage(LOW_TOOL_DAMAGE)))
            .add(LootItem.lootTableItem(Items.IRON_LEGGINGS)
                   .setWeight(25)
                   .apply(EnchantRandomlyFunction.randomApplicableEnchantment())
                   .apply(SetItemDamageFunction.setDamage(LOW_TOOL_DAMAGE)))
            .add(LootItem.lootTableItem(Items.IRON_BOOTS)
                   .setWeight(25)
                   .apply(EnchantRandomlyFunction.randomApplicableEnchantment())
                   .apply(SetItemDamageFunction.setDamage(LOW_TOOL_DAMAGE)))
            .add(LootItem.lootTableItem(Items.IRON_SWORD)
                   .setWeight(25)
                   .apply(EnchantRandomlyFunction.randomApplicableEnchantment())
                   .apply(SetItemDamageFunction.setDamage(LOW_TOOL_DAMAGE)))
            .add(LootItem.lootTableItem(Items.IRON_PICKAXE)
                   .setWeight(25)
                   .apply(EnchantRandomlyFunction.randomApplicableEnchantment())
                   .apply(SetItemDamageFunction.setDamage(LOW_TOOL_DAMAGE)))
            .add(LootItem.lootTableItem(Items.IRON_SHOVEL)
                   .setWeight(25)
                   .apply(EnchantRandomlyFunction.randomApplicableEnchantment())
                   .apply(SetItemDamageFunction.setDamage(LOW_TOOL_DAMAGE)))
            .add(LootItem.lootTableItem(Items.DIAMOND_HELMET)
                   .setWeight(5)
                   .apply(EnchantRandomlyFunction.randomApplicableEnchantment())
                   .apply(SetItemDamageFunction.setDamage(AVERAGE_TOOL_DAMAGE)))
            .add(LootItem.lootTableItem(Items.DIAMOND_CHESTPLATE)
                   .setWeight(5)
                   .apply(EnchantRandomlyFunction.randomApplicableEnchantment())
                   .apply(SetItemDamageFunction.setDamage(AVERAGE_TOOL_DAMAGE)))
            .add(LootItem.lootTableItem(Items.DIAMOND_LEGGINGS)
                   .setWeight(5)
                   .apply(EnchantRandomlyFunction.randomApplicableEnchantment())
                   .apply(SetItemDamageFunction.setDamage(AVERAGE_TOOL_DAMAGE)))
            .add(LootItem.lootTableItem(Items.DIAMOND_BOOTS)
                   .setWeight(5)
                   .apply(EnchantRandomlyFunction.randomApplicableEnchantment())
                   .apply(SetItemDamageFunction.setDamage(AVERAGE_TOOL_DAMAGE)))
            .add(LootItem.lootTableItem(Items.DIAMOND_SWORD)
                   .setWeight(5)
                   .apply(EnchantRandomlyFunction.randomApplicableEnchantment())
                   .apply(SetItemDamageFunction.setDamage(AVERAGE_TOOL_DAMAGE)))
            .add(LootItem.lootTableItem(Items.DIAMOND_PICKAXE)
                   .setWeight(5)
                   .apply(EnchantRandomlyFunction.randomApplicableEnchantment())
                   .apply(SetItemDamageFunction.setDamage(AVERAGE_TOOL_DAMAGE)))
            .add(LootItem.lootTableItem(Items.DIAMOND_SHOVEL)
                   .setWeight(5)
                   .apply(EnchantRandomlyFunction.randomApplicableEnchantment())
                   .apply(SetItemDamageFunction.setDamage(AVERAGE_TOOL_DAMAGE)))
            .add(LootItem.lootTableItem(Items.ELYTRA).setWeight(1))
            .add(createEncounterLootItem(ENDERMAN).setWeight(200))
            .add(createEncounterLootItem(SHULKER).setWeight(300))
        ));

        createStructureLootTable(FORTRESS_ID, registrar, builder -> builder.withPool(
          new LootPool.Builder()
            .setRolls(COMMON_STRUCTURE_ROLLS)
            .add(LootItem.lootTableItem(Items.NETHER_BRICKS).setWeight(100).apply(SetItemCountFunction.setCount(SUPER_COMMON_ITEM_COUNT)))
            .add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(100).apply(SetItemCountFunction.setCount(COMMON_ITEM_COUNT)))
            .add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(100).apply(SetItemCountFunction.setCount(UNCOMMON_ITEM_COUNT)))
            .add(LootItem.lootTableItem(Items.NETHER_WART).setWeight(75).apply(SetItemCountFunction.setCount(COMMON_ITEM_COUNT)))
            .add(LootItem.lootTableItem(Items.OBSIDIAN).setWeight(60))
            .add(LootItem.lootTableItem(Items.DIAMOND).setWeight(50))
            .add(LootItem.lootTableItem(Items.SADDLE).setWeight(50))
            .add(createEncounterLootItem(ZOMBIFIED_PIGLIN).setWeight(20))
            .add(createEncounterLootItem(SKELETON).setWeight(20))
            .add(createEncounterLootItem(WITHER_SKELETON).setWeight(10))
            .add(createEncounterLootItem(BLAZE).setWeight(10))
            .add(createEncounterLootItem(MAGMA_CUBE_LARGE).setWeight(5))
            .add(createEncounterLootItem(MAGMA_CUBE_MEDIUM).setWeight(10))
            .add(createEncounterLootItem(MAGMA_CUBE_SMALL).setWeight(20))
        ));

        createStructureLootTable(MANSION_ID, registrar, builder -> builder.withPool(
          new LootPool.Builder()
            .setRolls(UNCOMMON_STRUCTURE_ROLLS)
            .add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(100).apply(SetItemCountFunction.setCount(COMMON_ITEM_COUNT)))
            .add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(100).apply(SetItemCountFunction.setCount(UNCOMMON_ITEM_COUNT)))
            .add(LootItem.lootTableItem(Items.NAME_TAG).setWeight(75))
            .add(LootItem.lootTableItem(Items.LEAD).setWeight(75))
            .add(LootItem.lootTableItem(Items.REDSTONE).setWeight(50))
            .add(LootItem.lootTableItem(Items.GOLDEN_APPLE).setWeight(25))
            .add(LootItem.lootTableItem(Items.ENCHANTED_BOOK).setWeight(20).apply(EnchantRandomlyFunction.randomApplicableEnchantment()))
            .add(LootItem.lootTableItem(Items.DIAMOND_CHESTPLATE)
                   .setWeight(15)
                   .apply(EnchantRandomlyFunction.randomApplicableEnchantment())
                   .apply(SetItemDamageFunction.setDamage(AVERAGE_TOOL_DAMAGE)))
            .add(LootItem.lootTableItem(Items.ENCHANTED_GOLDEN_APPLE).setWeight(5))
            .add(createEncounterLootItem(VINDICATOR).setWeight(100))
            .add(createEncounterLootItem(EVOKER).setWeight(50))
            .add(createEncounterLootItem(VEX).setWeight(50))
        ));

        createStructureLootTable(MINESHAFT_ID, registrar, builder -> builder.withPool(
          new LootPool.Builder()
            .setRolls(COMMON_STRUCTURE_ROLLS)
            .add(LootItem.lootTableItem(Items.RAIL).setWeight(100).apply(SetItemCountFunction.setCount(SUPER_COMMON_ITEM_COUNT)))
            .add(LootItem.lootTableItem(Items.POWERED_RAIL).setWeight(75).apply(SetItemCountFunction.setCount(UNCOMMON_ITEM_COUNT)))
            .add(LootItem.lootTableItem(Items.DETECTOR_RAIL).setWeight(50).apply(SetItemCountFunction.setCount(UNCOMMON_ITEM_COUNT)))
            .add(LootItem.lootTableItem(Items.ACTIVATOR_RAIL).setWeight(50).apply(SetItemCountFunction.setCount(UNCOMMON_ITEM_COUNT)))
            .add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(50).apply(SetItemCountFunction.setCount(COMMON_ITEM_COUNT)))
            .add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(50).apply(SetItemCountFunction.setCount(UNCOMMON_ITEM_COUNT)))
            .add(LootItem.lootTableItem(Items.DIAMOND).setWeight(25))
            .add(LootItem.lootTableItem(Items.NAME_TAG).setWeight(25))
            .add(LootItem.lootTableItem(Items.GOLDEN_APPLE).setWeight(25))
            .add(LootItem.lootTableItem(Items.ENCHANTED_BOOK).setWeight(20).apply(EnchantRandomlyFunction.randomApplicableEnchantment()))
            .add(LootItem.lootTableItem(Items.ENCHANTED_GOLDEN_APPLE).setWeight(5))
            .add(createEncounterLootItem(ZOMBIE).setWeight(100))
            .add(createEncounterLootItem(SKELETON).setWeight(50))
            .add(createEncounterLootItem(CREEPER).setWeight(30))
            .add(createEncounterLootItem(SPIDER).setWeight(30))
            .add(createEncounterLootItem(CAVE_SPIDER).setWeight(30))
            .add(createEncounterLootItem(ENDERMAN).setWeight(15))
        ));

        createStructureLootTable(MONUMENT_ID, registrar, builder -> builder.withPool(
          new LootPool.Builder()
            .setRolls(UNCOMMON_STRUCTURE_ROLLS)
            .add(LootItem.lootTableItem(Items.PRISMARINE_BRICKS).setWeight(50))
            .add(LootItem.lootTableItem(Items.DARK_PRISMARINE).setWeight(25))
            .add(LootItem.lootTableItem(Items.SEA_LANTERN).setWeight(15))
            .add(LootItem.lootTableItem(Items.WET_SPONGE).setWeight(15))
            .add(createEncounterLootItem(GUARDIAN).setWeight(50))
            .add(createEncounterLootItem(ELDER_GUARDIAN).setWeight(5))
        ));

        createStructureLootTable(NETHER_FOSSIL_ID, registrar, builder -> builder.withPool(
          new LootPool.Builder()
            .setRolls(COMMON_STRUCTURE_ROLLS)
            .add(LootItem.lootTableItem(Items.AIR).setWeight(25))
            .add(LootItem.lootTableItem(Items.BONE_BLOCK).setWeight(50))
            .add(LootItem.lootTableItem(Items.BONE_MEAL).setWeight(10))
            .add(createEncounterLootItem(ZOMBIFIED_PIGLIN).setWeight(25))
        ));

        createStructureLootTable(OCEAN_RUIN_ID, registrar, builder -> builder.withPool(
          new LootPool.Builder()
            .setRolls(UNCOMMON_STRUCTURE_ROLLS)
            .add(LootItem.lootTableItem(Items.STONE_AXE).setWeight(35).apply(SetItemDamageFunction.setDamage(LOW_TOOL_DAMAGE)))
            .add(LootItem.lootTableItem(Items.BONE_MEAL).setWeight(10))
            .add(LootItem.lootTableItem(Items.EMERALD).setWeight(35))
            .add(LootItem.lootTableItem(Items.GOLDEN_APPLE).setWeight(25))
            .add(LootItem.lootTableItem(Items.FISHING_ROD)
                   .setWeight(25)
                   .apply(EnchantRandomlyFunction.randomApplicableEnchantment()))
            .add(LootItem.lootTableItem(Items.ENCHANTED_BOOK)
                   .setWeight(25)
                   .apply(EnchantRandomlyFunction.randomApplicableEnchantment()))
            .add(createEncounterLootItem(ZOMBIFIED_PIGLIN).setWeight(25))
        ));

        createStructureLootTable(PILLAGER_OUTPOST_ID, registrar, builder -> builder.withPool(
          new LootPool.Builder()
            .setRolls(COMMON_STRUCTURE_ROLLS)
            .add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(50).apply(SetItemCountFunction.setCount(COMMON_ITEM_COUNT)))
            .add(LootItem.lootTableItem(Items.EXPERIENCE_BOTTLE).setWeight(40))
            .add(LootItem.lootTableItem(Items.CROSSBOW).setWeight(40).apply(SetItemDamageFunction.setDamage(LOW_TOOL_DAMAGE)))
            .add(LootItem.lootTableItem(Items.ENCHANTED_BOOK)
                   .setWeight(10)
                   .apply(EnchantRandomlyFunction.randomApplicableEnchantment()))
            .add(createEncounterLootItem(PILLAGER).setWeight(25))
            .add(createEncounterLootItem(PILLAGER_CAPTAIN).setWeight(5))
        ));

        createStructureLootTable(RUINED_PORTAL_ID, registrar, builder -> builder.withPool(
          new LootPool.Builder()
            .setRolls(COMMON_STRUCTURE_ROLLS)
            .add(LootItem.lootTableItem(Items.NETHERRACK).setWeight(100).apply(SetItemCountFunction.setCount(COMMON_ITEM_COUNT)))
            .add(LootItem.lootTableItem(Items.OBSIDIAN).setWeight(100).apply(SetItemCountFunction.setCount(COMMON_ITEM_COUNT)))
            .add(LootItem.lootTableItem(Items.FIRE_CHARGE).setWeight(50))
            .add(LootItem.lootTableItem(Items.GOLDEN_APPLE).setWeight(25))
            .add(LootItem.lootTableItem(Items.GOLDEN_HELMET)
                   .setWeight(25)
                   .apply(EnchantRandomlyFunction.randomApplicableEnchantment())
                   .apply(SetItemDamageFunction.setDamage(LOW_TOOL_DAMAGE)))
            .add(LootItem.lootTableItem(Items.GOLDEN_CHESTPLATE)
                   .setWeight(25)
                   .apply(EnchantRandomlyFunction.randomApplicableEnchantment())
                   .apply(SetItemDamageFunction.setDamage(LOW_TOOL_DAMAGE)))
            .add(LootItem.lootTableItem(Items.GOLDEN_LEGGINGS)
                   .setWeight(25)
                   .apply(EnchantRandomlyFunction.randomApplicableEnchantment())
                   .apply(SetItemDamageFunction.setDamage(LOW_TOOL_DAMAGE)))
            .add(LootItem.lootTableItem(Items.GOLDEN_BOOTS)
                   .setWeight(25)
                   .apply(EnchantRandomlyFunction.randomApplicableEnchantment())
                   .apply(SetItemDamageFunction.setDamage(LOW_TOOL_DAMAGE)))
            .add(LootItem.lootTableItem(Items.GOLDEN_SWORD)
                   .setWeight(25)
                   .apply(EnchantRandomlyFunction.randomApplicableEnchantment())
                   .apply(SetItemDamageFunction.setDamage(LOW_TOOL_DAMAGE)))
            .add(LootItem.lootTableItem(Items.GOLDEN_PICKAXE)
                   .setWeight(25)
                   .apply(EnchantRandomlyFunction.randomApplicableEnchantment())
                   .apply(SetItemDamageFunction.setDamage(LOW_TOOL_DAMAGE)))
            .add(LootItem.lootTableItem(Items.GOLDEN_SHOVEL)
                   .setWeight(25)
                   .apply(EnchantRandomlyFunction.randomApplicableEnchantment())
                   .apply(SetItemDamageFunction.setDamage(LOW_TOOL_DAMAGE)))
            .add(LootItem.lootTableItem(Items.GOLD_BLOCK).setWeight(10))
            .add(createEncounterLootItem(ZOMBIFIED_PIGLIN).setWeight(25))
        ));

        createStructureLootTable(SHIPWRECK_ID, registrar, builder -> {
            MinecoloniesCropBlock[] crops = ModBlocks.getCrops();
            final LootPool.Builder pool = new LootPool.Builder()
                                            .setRolls(COMMON_STRUCTURE_ROLLS)
                                            .add(LootItem.lootTableItem(Items.OAK_PLANKS)
                                                   .setWeight(crops.length * 50)
                                                   .apply(SetItemCountFunction.setCount(SUPER_COMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(Items.SPRUCE_PLANKS)
                                                   .setWeight(crops.length * 50)
                                                   .apply(SetItemCountFunction.setCount(SUPER_COMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(crops.length * 15))
                                            .add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(crops.length * 10))
                                            .add(LootItem.lootTableItem(Items.DIAMOND).setWeight(crops.length * 5))
                                            .add(createEncounterLootItem(DROWNED).setWeight(crops.length * 20))
                                            .add(createEncounterLootItem(DROWNED_TRIDENT).setWeight(crops.length * 10));
            for (MinecoloniesCropBlock crop : crops)
            {
                pool.add(LootItem.lootTableItem(crop.asItem()).setWeight(75).apply(SetItemCountFunction.setCount(COMMON_ITEM_COUNT)));
            }
            builder.withPool(pool);
        });

        createStructureLootTable(STRONGHOLD_ID, registrar, builder -> builder.withPool(
          new LootPool.Builder()
            .setRolls(SPECIAL_STRUCTURE_ROLLS)
            .add(LootItem.lootTableItem(Items.STONE_BRICKS).setWeight(100).apply(SetItemCountFunction.setCount(SUPER_COMMON_ITEM_COUNT)))
            .add(LootItem.lootTableItem(Items.CRACKED_STONE_BRICKS).setWeight(100).apply(SetItemCountFunction.setCount(SUPER_COMMON_ITEM_COUNT)))
            .add(LootItem.lootTableItem(Items.ENDER_PEARL).setWeight(75))
            .add(LootItem.lootTableItem(Items.BOOK).setWeight(50).apply(SetItemCountFunction.setCount(COMMON_ITEM_COUNT)))
            .add(LootItem.lootTableItem(Items.BOOKSHELF).setWeight(25))
            .add(LootItem.lootTableItem(Items.DIAMOND).setWeight(25))
            .add(LootItem.lootTableItem(Items.EMERALD).setWeight(25))
            .add(LootItem.lootTableItem(Items.ENCHANTED_BOOK)
                   .setWeight(20)
                   .apply(EnchantRandomlyFunction.randomApplicableEnchantment()))
            .add(createEncounterLootItem(ZOMBIE).setWeight(50))
            .add(createEncounterLootItem(SKELETON).setWeight(30))
            .add(createEncounterLootItem(CREEPER).setWeight(10))
            .add(createEncounterLootItem(ENDERMAN).setWeight(10))
        ));

        createStructureLootTable(VILLAGE_DESERT_ID, registrar, builder -> {
            List<MinecoloniesCropBlock> crops =
              Arrays.stream(ModBlocks.getCrops()).filter(f -> f.getPreferredBiome() == null || f.getPreferredBiome().equals(ModTags.dryBiomes)).toList();
            final LootPool.Builder pool = new LootPool.Builder()
                                            .setRolls(COMMON_STRUCTURE_ROLLS)
                                            .add(LootItem.lootTableItem(Items.SANDSTONE)
                                                   .setWeight(crops.size() * 50)
                                                   .apply(SetItemCountFunction.setCount(SUPER_COMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(Items.SMOOTH_SANDSTONE)
                                                   .setWeight(crops.size() * 50)
                                                   .apply(SetItemCountFunction.setCount(SUPER_COMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(Items.CACTUS).setWeight(crops.size() * 30).apply(SetItemCountFunction.setCount(COMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(Items.DEAD_BUSH).setWeight(crops.size() * 30).apply(SetItemCountFunction.setCount(COMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(Items.CLAY_BALL).setWeight(crops.size() * 20).apply(SetItemCountFunction.setCount(UNCOMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(ModItems.flatbread)
                                                   .setWeight(crops.size() * 30)
                                                   .apply(SetItemCountFunction.setCount(COMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(ModItems.pottage)
                                                   .setWeight(crops.size() * 20)
                                                   .apply(SetItemCountFunction.setCount(UNCOMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(ModItems.pepper_hummus)
                                                   .setWeight(crops.size() * 30)
                                                   .apply(SetItemCountFunction.setCount(UNCOMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(ModItems.pita_hummus).setWeight(crops.size() * 20));
            for (MinecoloniesCropBlock crop : crops)
            {
                pool.add(LootItem.lootTableItem(crop.asItem()).setWeight(75).apply(SetItemCountFunction.setCount(COMMON_ITEM_COUNT)));
            }
            builder.withPool(pool);
        });

        createStructureLootTable(VILLAGE_PLAINS_ID, registrar, builder -> {
            List<MinecoloniesCropBlock> crops =
              Arrays.stream(ModBlocks.getCrops()).filter(f -> f.getPreferredBiome() == null || f.getPreferredBiome().equals(ModTags.temperateBiomes)).toList();
            final LootPool.Builder pool = new LootPool.Builder()
                                            .setRolls(COMMON_STRUCTURE_ROLLS)
                                            .add(LootItem.lootTableItem(Items.COBBLESTONE)
                                                   .setWeight(crops.size() * 50)
                                                   .apply(SetItemCountFunction.setCount(SUPER_COMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(Items.OAK_PLANKS)
                                                   .setWeight(crops.size() * 50)
                                                   .apply(SetItemCountFunction.setCount(SUPER_COMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(Items.OAK_SAPLING).setWeight(crops.size() * 30).apply(SetItemCountFunction.setCount(COMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(Items.DANDELION).setWeight(crops.size() * 30).apply(SetItemCountFunction.setCount(COMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(Items.POPPY).setWeight(crops.size() * 30).apply(SetItemCountFunction.setCount(COMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(ModItems.cheddar_cheese)
                                                   .setWeight(crops.size() * 30)
                                                   .apply(SetItemCountFunction.setCount(COMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(ModItems.pasta_plain)
                                                   .setWeight(crops.size() * 20)
                                                   .apply(SetItemCountFunction.setCount(UNCOMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(ModItems.pasta_tomato)
                                                   .setWeight(crops.size() * 30)
                                                   .apply(SetItemCountFunction.setCount(UNCOMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(ModItems.stuffed_pita).setWeight(crops.size() * 20));
            for (MinecoloniesCropBlock crop : crops)
            {
                pool.add(LootItem.lootTableItem(crop.asItem()).setWeight(75).apply(SetItemCountFunction.setCount(COMMON_ITEM_COUNT)));
            }
            builder.withPool(pool);
        });

        createStructureLootTable(VILLAGE_SAVANNA_ID, registrar, builder -> {
            List<MinecoloniesCropBlock> crops =
              Arrays.stream(ModBlocks.getCrops()).filter(f -> f.getPreferredBiome() == null || f.getPreferredBiome().equals(ModTags.dryBiomes)).toList();
            final LootPool.Builder pool = new LootPool.Builder()
                                            .setRolls(COMMON_STRUCTURE_ROLLS)
                                            .add(LootItem.lootTableItem(Items.YELLOW_TERRACOTTA)
                                                   .setWeight(crops.size() * 50)
                                                   .apply(SetItemCountFunction.setCount(SUPER_COMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(Items.ACACIA_PLANKS)
                                                   .setWeight(crops.size() * 50)
                                                   .apply(SetItemCountFunction.setCount(SUPER_COMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(Items.ACACIA_SAPLING).setWeight(crops.size() * 30).apply(SetItemCountFunction.setCount(COMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(Items.GRASS).setWeight(crops.size() * 30).apply(SetItemCountFunction.setCount(COMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(Items.TALL_GRASS).setWeight(crops.size() * 30).apply(SetItemCountFunction.setCount(COMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(ModItems.tofu)
                                                   .setWeight(crops.size() * 30)
                                                   .apply(SetItemCountFunction.setCount(COMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(ModItems.lembas_scone)
                                                   .setWeight(crops.size() * 20)
                                                   .apply(SetItemCountFunction.setCount(UNCOMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(ModItems.pepper_hummus)
                                                   .setWeight(crops.size() * 30)
                                                   .apply(SetItemCountFunction.setCount(UNCOMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(ModItems.pita_hummus).setWeight(crops.size() * 20));
            for (MinecoloniesCropBlock crop : crops)
            {
                pool.add(LootItem.lootTableItem(crop.asItem()).setWeight(75).apply(SetItemCountFunction.setCount(COMMON_ITEM_COUNT)));
            }
            builder.withPool(pool);
        });

        createStructureLootTable(VILLAGE_SNOWY_ID, registrar, builder -> {
            List<MinecoloniesCropBlock> crops =
              Arrays.stream(ModBlocks.getCrops()).filter(f -> f.getPreferredBiome() == null || f.getPreferredBiome().equals(ModTags.coldBiomes)).toList();
            final LootPool.Builder pool = new LootPool.Builder()
                                            .setRolls(COMMON_STRUCTURE_ROLLS)
                                            .add(LootItem.lootTableItem(Items.SNOW_BLOCK)
                                                   .setWeight(crops.size() * 50)
                                                   .apply(SetItemCountFunction.setCount(SUPER_COMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(Items.ICE)
                                                   .setWeight(crops.size() * 50)
                                                   .apply(SetItemCountFunction.setCount(SUPER_COMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(Items.SNOWBALL).setWeight(crops.size() * 30).apply(SetItemCountFunction.setCount(COMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(Items.BEETROOT).setWeight(crops.size() * 30).apply(SetItemCountFunction.setCount(COMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(Items.BEETROOT_SOUP).setWeight(crops.size() * 30).apply(SetItemCountFunction.setCount(COMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(ModItems.feta_cheese)
                                                   .setWeight(crops.size() * 30)
                                                   .apply(SetItemCountFunction.setCount(COMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(ModItems.manchet_bread)
                                                   .setWeight(crops.size() * 20)
                                                   .apply(SetItemCountFunction.setCount(UNCOMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(ModItems.cabochis)
                                                   .setWeight(crops.size() * 30)
                                                   .apply(SetItemCountFunction.setCount(UNCOMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(ModItems.lamb_stew).setWeight(crops.size() * 20));
            for (MinecoloniesCropBlock crop : crops)
            {
                pool.add(LootItem.lootTableItem(crop.asItem()).setWeight(75).apply(SetItemCountFunction.setCount(COMMON_ITEM_COUNT)));
            }
            builder.withPool(pool);
        });

        createStructureLootTable(VILLAGE_TAIGA_ID, registrar, builder -> {
            List<MinecoloniesCropBlock> crops =
              Arrays.stream(ModBlocks.getCrops()).filter(f -> f.getPreferredBiome() == null || f.getPreferredBiome().equals(ModTags.coldBiomes)).toList();
            final LootPool.Builder pool = new LootPool.Builder()
                                            .setRolls(COMMON_STRUCTURE_ROLLS)
                                            .add(LootItem.lootTableItem(Items.COBBLESTONE)
                                                   .setWeight(crops.size() * 50)
                                                   .apply(SetItemCountFunction.setCount(SUPER_COMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(Items.SPRUCE_PLANKS)
                                                   .setWeight(crops.size() * 50)
                                                   .apply(SetItemCountFunction.setCount(SUPER_COMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(Items.FERN).setWeight(crops.size() * 30).apply(SetItemCountFunction.setCount(COMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(Items.LARGE_FERN).setWeight(crops.size() * 30).apply(SetItemCountFunction.setCount(COMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(Items.SWEET_BERRIES).setWeight(crops.size() * 30).apply(SetItemCountFunction.setCount(COMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(ModItems.cooked_rice)
                                                   .setWeight(crops.size() * 30)
                                                   .apply(SetItemCountFunction.setCount(COMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(ModItems.muffin)
                                                   .setWeight(crops.size() * 20)
                                                   .apply(SetItemCountFunction.setCount(UNCOMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(ModItems.cabochis)
                                                   .setWeight(crops.size() * 30)
                                                   .apply(SetItemCountFunction.setCount(UNCOMMON_ITEM_COUNT)))
                                            .add(LootItem.lootTableItem(ModItems.lamb_stew).setWeight(crops.size() * 20));
            for (MinecoloniesCropBlock crop : crops)
            {
                pool.add(LootItem.lootTableItem(crop.asItem()).setWeight(75).apply(SetItemCountFunction.setCount(COMMON_ITEM_COUNT)));
            }
            builder.withPool(pool);
        });
    }
}
