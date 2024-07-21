package com.minecolonies.core.generation.defaults;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.expeditions.colony.types.ColonyExpeditionTypeDifficulty;
import com.minecolonies.core.generation.SimpleLootTableProvider;
import com.minecolonies.core.loot.ExpeditionDifficultyCondition;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.NotNull;

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
    public static final ResourceLocation BURIED_TREASURE_ID  = new ResourceLocation("ancient_city");
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
     * Structure roll counts
     */
    private static final NumberProvider COMMON_STRUCTURE_ROLLS         = UniformGenerator.between(10, 20);
    private static final NumberProvider COMMON_STRUCTURE_BONUS_ROLLS   = ConstantValue.exactly(5);
    private static final NumberProvider UNCOMMON_STRUCTURE_ROLLS       = UniformGenerator.between(5, 10);
    private static final NumberProvider UNCOMMON_STRUCTURE_BONUS_ROLLS = ConstantValue.exactly(3);
    private static final NumberProvider SPECIAL_STRUCTURE_ROLLS        = UniformGenerator.between(2, 5);
    private static final NumberProvider SPECIAL_STRUCTURE_BONUS_ROLLS  = ConstantValue.exactly(2);

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

        registrar.register(id.withPrefix("expeditions/structures/"), paramSet, builder);
    }

    @Override
    protected void registerTables(final @NotNull LootTableRegistrar registrar)
    {
        createStructureLootTable(ANCIENT_CITY_ID, registrar, builder -> builder.withPool(
          new LootPool.Builder()
            .setRolls(SPECIAL_STRUCTURE_ROLLS)
            .setBonusRolls(SPECIAL_STRUCTURE_BONUS_ROLLS)
            .add(LootItem.lootTableItem(Items.COAL).setWeight(200))
            .add(LootItem.lootTableItem(Items.BOOK).setWeight(200))
            .add(LootItem.lootTableItem(Items.NAME_TAG).setWeight(50).setQuality(1))
            .add(LootItem.lootTableItem(Items.LEAD).setWeight(50).setQuality(1))
            .add(LootItem.lootTableItem(Items.EXPERIENCE_BOTTLE).setWeight(40).setQuality(1))
            .add(LootItem.lootTableItem(Items.SCULK_SENSOR).setWeight(30).setQuality(1))
            .add(LootItem.lootTableItem(Items.SCULK_CATALYST).setWeight(25).setQuality(1))
            .add(LootItem.lootTableItem(Items.IRON_LEGGINGS).setWeight(15).setQuality(2))
            .add(LootItem.lootTableItem(Items.DIAMOND_LEGGINGS).setWeight(5).setQuality(2))
            .add(LootItem.lootTableItem(Items.ENCHANTED_BOOK)
                   .apply(EnchantWithLevelsFunction.enchantWithLevels(ConstantValue.exactly(3)))
                   .setWeight(5)
                   .setQuality(2))
            .add(createEncounterLootItem(WARDEN).setWeight(10).when(ExpeditionDifficultyCondition.forDifficulty(ColonyExpeditionTypeDifficulty.HARD)))
            .add(createEncounterLootItem(WARDEN).setWeight(20).when(ExpeditionDifficultyCondition.forDifficulty(ColonyExpeditionTypeDifficulty.NIGHTMARE)))
        ));

        createStructureLootTable(BASTION_REMNANT_ID, registrar, builder -> builder.withPool(
          new LootPool.Builder()
            .setRolls(UNCOMMON_STRUCTURE_ROLLS)
            .setBonusRolls(UNCOMMON_STRUCTURE_BONUS_ROLLS)
            .add(LootItem.lootTableItem(Items.ARROW).setWeight(200))
            .add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(200))
            .add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(200))
            .add(LootItem.lootTableItem(Items.NAME_TAG).setWeight(50).setQuality(1))
            .add(LootItem.lootTableItem(Items.LEAD).setWeight(50).setQuality(1))
            .add(LootItem.lootTableItem(Items.EXPERIENCE_BOTTLE).setWeight(40).setQuality(1))
            .add(LootItem.lootTableItem(Items.SCULK_SENSOR).setWeight(30).setQuality(1))
            .add(LootItem.lootTableItem(Items.SCULK_CATALYST).setWeight(25).setQuality(1))
            .add(LootItem.lootTableItem(Items.IRON_LEGGINGS).setWeight(15).setQuality(2))
            .add(LootItem.lootTableItem(Items.DIAMOND_LEGGINGS).setWeight(5).setQuality(2))
            .add(LootItem.lootTableItem(Items.ENCHANTED_BOOK)
                   .apply(EnchantWithLevelsFunction.enchantWithLevels(ConstantValue.exactly(3)))
                   .setWeight(5)
                   .setQuality(2))
            .add(createEncounterLootItem(WARDEN).setWeight(3))));

        createStructureLootTable(STRONGHOLD_ID, registrar, builder -> builder.withPool(
          new LootPool.Builder()
            .setRolls(SPECIAL_STRUCTURE_ROLLS)
            .setBonusRolls(SPECIAL_STRUCTURE_BONUS_ROLLS)
            .add(createEncounterLootItem(ZOMBIE).setWeight(50).setQuality(-10))
            .add(createEncounterLootItem(SKELETON).setWeight(30).setQuality(-10))
            .add(createEncounterLootItem(CREEPER).setWeight(10).setQuality(-15))
            .add(createEncounterLootItem(ENDERMAN).setWeight(10).setQuality(-20))));
    }
}
