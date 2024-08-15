package com.minecolonies.core.generation.defaults;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.expeditions.colony.types.ColonyExpeditionTypeDifficulty;
import com.minecolonies.core.generation.SimpleLootTableProvider;
import com.minecolonies.core.loot.ExpeditionDifficultyCondition;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;

import static com.minecolonies.api.loot.ModLootConditions.EXPEDITION_PARAMS;
import static com.minecolonies.core.generation.ExpeditionResourceManager.createStructureLootReference;
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
     * Expedition difficulties
     */
    private static final ColonyExpeditionTypeDifficulty DIFF_2 = ColonyExpeditionTypeDifficulty.MEDIUM;
    private static final ColonyExpeditionTypeDifficulty DIFF_3 = ColonyExpeditionTypeDifficulty.HARD;
    private static final ColonyExpeditionTypeDifficulty DIFF_4 = ColonyExpeditionTypeDifficulty.NIGHTMARE;

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
            .setRolls(UniformGenerator.between(8, 16))
            .add(LootItem.lootTableItem(Items.OAK_LOG).setWeight(100))
            .add(LootItem.lootTableItem(Items.BIRCH_LOG).setWeight(100))
            .add(LootItem.lootTableItem(Items.SPRUCE_LOG).setWeight(100))
            .add(LootItem.lootTableItem(Items.JUNGLE_LOG).setWeight(100))
            .add(LootItem.lootTableItem(Items.DARK_OAK_LOG)
                   .setWeight(100)
                   .when(ExpeditionDifficultyCondition.forDifficulty(DIFF_2, DIFF_3)))
            .add(LootItem.lootTableItem(Items.ACACIA_LOG)
                   .setWeight(100)
                   .when(ExpeditionDifficultyCondition.forDifficulty(DIFF_2, DIFF_3)))
            .add(LootItem.lootTableItem(Items.CHERRY_LOG)
                   .setWeight(100)
                   .when(ExpeditionDifficultyCondition.forDifficulty(DIFF_2, DIFF_3)))
            .add(LootItem.lootTableItem(Items.MANGROVE_LOG)
                   .setWeight(100)
                   .when(ExpeditionDifficultyCondition.forDifficulty(DIFF_2, DIFF_3)))
            .add(LootItem.lootTableItem(Items.COAL_ORE).setWeight(100))
            .add(LootItem.lootTableItem(Items.COPPER_ORE).setWeight(100))
            .add(LootItem.lootTableItem(Items.IRON_ORE).setWeight(100))
            .add(LootItem.lootTableItem(Items.GOLD_ORE)
                   .setWeight(50)
                   .when(ExpeditionDifficultyCondition.forDifficulty(DIFF_2, DIFF_3)))
            .add(LootItem.lootTableItem(Items.REDSTONE_ORE)
                   .setWeight(50)
                   .when(ExpeditionDifficultyCondition.forDifficulty(DIFF_2, DIFF_3)))
            .add(LootItem.lootTableItem(Items.LAPIS_ORE)
                   .setWeight(50)
                   .when(ExpeditionDifficultyCondition.forDifficulty(DIFF_2, DIFF_3)))
            .add(LootItem.lootTableItem(Items.POPPY).setWeight(100))
            .add(LootItem.lootTableItem(Items.ROSE_BUSH).setWeight(100))
            .add(LootItem.lootTableItem(Items.DANDELION).setWeight(100))
            .add(LootItem.lootTableItem(Items.CORNFLOWER).setWeight(100))
            .add(LootItem.lootTableItem(Items.AZURE_BLUET).setWeight(100))
            .add(LootItem.lootTableItem(Items.OXEYE_DAISY).setWeight(100))
            .add(LootItem.lootTableItem(Items.LILY_OF_THE_VALLEY).setWeight(100))
            .add(LootItem.lootTableItem(Items.RED_TULIP)
                   .setWeight(80)
                   .when(ExpeditionDifficultyCondition.forDifficulty(DIFF_2, DIFF_3)))
            .add(LootItem.lootTableItem(Items.PINK_TULIP)
                   .setWeight(80)
                   .when(ExpeditionDifficultyCondition.forDifficulty(DIFF_2, DIFF_3)))
            .add(LootItem.lootTableItem(Items.ORANGE_TULIP)
                   .setWeight(80)
                   .when(ExpeditionDifficultyCondition.forDifficulty(DIFF_2, DIFF_3)))
            .add(LootItem.lootTableItem(Items.WHITE_TULIP)
                   .setWeight(80)
                   .when(ExpeditionDifficultyCondition.forDifficulty(DIFF_2, DIFF_3)))
            .add(LootItem.lootTableItem(Items.SUNFLOWER)
                   .setWeight(80)
                   .when(ExpeditionDifficultyCondition.forDifficulty(DIFF_2, DIFF_3)))
            .add(LootItem.lootTableItem(Items.TORCHFLOWER)
                   .setWeight(50)
                   .when(ExpeditionDifficultyCondition.forDifficulty(DIFF_3)))
            .add(LootItem.lootTableItem(Items.ALLIUM)
                   .setWeight(50)
                   .when(ExpeditionDifficultyCondition.forDifficulty(DIFF_3)))
            .add(LootItem.lootTableItem(Items.BLUE_ORCHID)
                   .setWeight(50)
                   .when(ExpeditionDifficultyCondition.forDifficulty(DIFF_3)))
            .add(LootItem.lootTableItem(Items.PITCHER_PLANT)
                   .setWeight(50)
                   .when(ExpeditionDifficultyCondition.forDifficulty(DIFF_3)))
            .add(LootItem.lootTableItem(Items.BROWN_MUSHROOM)
                   .setWeight(50)
                   .when(ExpeditionDifficultyCondition.forDifficulty(DIFF_2, DIFF_3)))
            .add(LootItem.lootTableItem(Items.RED_MUSHROOM)
                   .setWeight(50)
                   .when(ExpeditionDifficultyCondition.forDifficulty(DIFF_2, DIFF_3)))
            .add(createStructureLootReference(VILLAGE_DESERT_ID).setWeight(50))
            .add(createStructureLootReference(VILLAGE_PLAINS_ID).setWeight(50))
            .add(createStructureLootReference(VILLAGE_SAVANNA_ID).setWeight(50))
            .add(createStructureLootReference(VILLAGE_SNOWY_ID).setWeight(50))
            .add(createStructureLootReference(VILLAGE_TAIGA_ID).setWeight(50))
            .add(createStructureLootReference(RUINED_PORTAL_ID).setWeight(30))
            .add(createStructureLootReference(PILLAGER_OUTPOST_ID).setWeight(20))
            .add(createStructureLootReference(DESERT_PYRAMID_ID).setWeight(20))
            .add(createStructureLootReference(JUNGLE_TEMPLE_ID).setWeight(20))
            .add(createStructureLootReference(IGLOO_ID).setWeight(20))
            .add(createStructureLootReference(SWAMP_HUT_ID).setWeight(20))
            .add(createStructureLootReference(MINESHAFT_ID).setWeight(20))
            .add(createStructureLootReference(SHIPWRECK_ID).setWeight(15))
            .add(createStructureLootReference(BURIED_TREASURE_ID).setWeight(15))
            .add(createStructureLootReference(MONUMENT_ID).setWeight(10))
            .add(createStructureLootReference(MANSION_ID).setWeight(10))
            .add(createStructureLootReference(STRONGHOLD_ID).setWeight(5))
            .add(createStructureLootReference(ANCIENT_CITY_ID).setWeight(5))
        ));

        createExpeditionLootTable(EXPEDITION_NETHER_LOOT, registrar, builder -> builder.withPool(new LootPool.Builder()));

        createExpeditionLootTable(EXPEDITION_END_LOOT, registrar, builder -> builder.withPool(new LootPool.Builder()));
    }

    @Override
    protected void validate(final @NotNull Map<ResourceLocation, LootTable> map, final @NotNull ValidationContext validationtracker)
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
