package com.minecolonies.coremod.generation.defaults.workers;

import com.google.common.collect.ImmutableMap;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.enchants.ModEnchants;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.research.util.ResearchConstants;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.crafting.LootTableAnalyzer;
import com.minecolonies.coremod.generation.CustomRecipeAndLootTableProvider;
import com.minecolonies.coremod.generation.CustomRecipeProvider;
import com.minecolonies.coremod.generation.SimpleLootTableProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.BuildingConstants.MODULE_CUSTOM;
import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/**
 * Datagen for Enchanter
 */
public class DefaultEnchanterCraftingProvider extends CustomRecipeAndLootTableProvider
{
    private final String ENCHANTER = ModJobs.ENCHANTER_ID.getPath();
    private static final int MAX_BUILDING_LEVEL = 5;
    private static final int NEVER = 99;

    private static final int[] LEVEL_WEIGHTS = new int[] { 0, 50, 25, 15, 5, 1 };

    /**
     * Rules for a specific enchantment.
     * @param firstLevel  the first enchanter level to produce this enchantment, or {@link #NEVER} if none should.
     * @param bonusLevels the number of extra levels above vanilla that the enchanter is allowed to produce.
     * @param rare        if true, this gets a weight of 1 instead of the typical weight for its level.
     */
    private record EnchantmentRule(int firstLevel, int bonusLevels, boolean rare) {}

    /** The list of supported enchantments and rules. */
    private static final Map<Enchantment, EnchantmentRule> RULES = ImmutableMap.<Enchantment, EnchantmentRule>builder()
            .put(Enchantments.AQUA_AFFINITY, new EnchantmentRule(1, 0, false))
            .put(Enchantments.BANE_OF_ARTHROPODS, new EnchantmentRule(1, 0, false))
            .put(Enchantments.BLAST_PROTECTION, new EnchantmentRule(1, 1, false))
            .put(Enchantments.DEPTH_STRIDER, new EnchantmentRule(1, 0, false))
            .put(Enchantments.BLOCK_EFFICIENCY, new EnchantmentRule(1, 0, false))
            .put(Enchantments.FALL_PROTECTION, new EnchantmentRule(1, 1, false))
            .put(Enchantments.FIRE_ASPECT, new EnchantmentRule(1, 1, false))
            .put(Enchantments.FIRE_PROTECTION, new EnchantmentRule(1, 1, false))
            .put(Enchantments.FLAMING_ARROWS, new EnchantmentRule(1, 0, false))
            .put(Enchantments.KNOCKBACK, new EnchantmentRule(1, 1, false))
            .put(Enchantments.MOB_LOOTING, new EnchantmentRule(1, 1, false))
            .put(Enchantments.POWER_ARROWS, new EnchantmentRule(1, 0, false))
            .put(Enchantments.PROJECTILE_PROTECTION, new EnchantmentRule(1, 1, false))
            .put(Enchantments.ALL_DAMAGE_PROTECTION, new EnchantmentRule(1, 1, false))
            .put(Enchantments.PIERCING, new EnchantmentRule(1, 1, false))
            .put(Enchantments.PUNCH_ARROWS, new EnchantmentRule(1, 1, false))
            .put(Enchantments.QUICK_CHARGE, new EnchantmentRule(1, 1, false))
            .put(Enchantments.RESPIRATION, new EnchantmentRule(1, 1, false))
            .put(Enchantments.SHARPNESS, new EnchantmentRule(1, 0, false))
            .put(Enchantments.SMITE, new EnchantmentRule(1, 0, false))
            .put(Enchantments.SWEEPING_EDGE, new EnchantmentRule(1, 1, false))
            .put(Enchantments.UNBREAKING, new EnchantmentRule(1, 1, false))

            .put(Enchantments.IMPALING, new EnchantmentRule(1, 0, true))
            .put(Enchantments.RIPTIDE, new EnchantmentRule(1, 1, true))
            .put(Enchantments.FROST_WALKER, new EnchantmentRule(2, 1, true))
            .put(Enchantments.LOYALTY, new EnchantmentRule(2, 1, true))
            .put(Enchantments.THORNS, new EnchantmentRule(2, 1, true))
            .put(Enchantments.BLOCK_FORTUNE, new EnchantmentRule(3, 0, true))
            .put(Enchantments.CHANNELING, new EnchantmentRule(3, 0, true))
            .put(Enchantments.SWIFT_SNEAK, new EnchantmentRule(3, 0, true))
            .put(Enchantments.SOUL_SPEED, new EnchantmentRule(3, 0, true))
            .put(Enchantments.INFINITY_ARROWS, new EnchantmentRule(4, 0, true))
            .put(Enchantments.SILK_TOUCH, new EnchantmentRule(5, 0, true))
            .put(Enchantments.MENDING, new EnchantmentRule(5, 0, true))
            .put(Enchantments.MULTISHOT, new EnchantmentRule(5, 0, false))

            // we don't want to produce these enchantments
            .put(Enchantments.FISHING_LUCK, new EnchantmentRule(NEVER, 0, false))
            .put(Enchantments.FISHING_SPEED, new EnchantmentRule(NEVER, 0, false))
            .build();

    private final List<LootTable.Builder> levels;
    private final LootTables lootTableManager;

    public DefaultEnchanterCraftingProvider(@NotNull final DataGenerator generatorIn,
                                            @NotNull final LootTables lootTableManager)
    {
        super(generatorIn);
        this.lootTableManager = lootTableManager;

        this.levels = buildLevels();
    }

    @NotNull
    private List<LootTable.Builder> buildLevels()
    {
        final List<LootPool.Builder> levelPools = new ArrayList<>();
        for (int buildingLevel = 1; buildingLevel <= MAX_BUILDING_LEVEL; ++buildingLevel)
        {
            levelPools.add(LootPool.lootPool());
        }

        for (int enchantLevel = 1; enchantLevel <= MAX_BUILDING_LEVEL; ++enchantLevel)
        {
            for (final Map.Entry<Enchantment, EnchantmentRule> entry : RULES.entrySet())
            {
                final Enchantment enchantment = entry.getKey();
                final EnchantmentRule rule = entry.getValue();
                final int maxEnchantLevel = enchantment.getMaxLevel() + rule.bonusLevels();

                if (rule.firstLevel() > MAX_BUILDING_LEVEL)
                {
                    continue;
                }
                if (enchantLevel > maxEnchantLevel)
                {
                    continue;
                }

                final int firstBuildingLevel = rule.firstLevel() + enchantLevel - 1;
                final int lastBuildingLevel = Math.min(firstBuildingLevel + 2, MAX_BUILDING_LEVEL);
                final int weight = rule.rare() ? 1 : LEVEL_WEIGHTS[Math.min(enchantLevel + rule.firstLevel() - 1, MAX_BUILDING_LEVEL)];

                for (int buildingLevel = firstBuildingLevel; buildingLevel <= lastBuildingLevel; ++buildingLevel)
                {
                    levelPools.get(buildingLevel - 1).add(enchantedBook(enchantment, enchantLevel).setWeight(weight));
                }
                if (enchantLevel == maxEnchantLevel)
                {
                    // ensure the final enchant level is available at all building levels, but at reduced weight
                    for (int buildingLevel = lastBuildingLevel + 1; buildingLevel <= MAX_BUILDING_LEVEL; ++buildingLevel)
                    {
                        levelPools.get(buildingLevel - 1).add(enchantedBook(enchantment, enchantLevel)
                                .setWeight(Math.min(weight, 5)));
                    }
                }
            }
        }

        // special rules
        levelPools.get(2).add(enchantedBook(ModEnchants.raiderDamage.get(), 1).setWeight(15));  //L3 gets I
        levelPools.get(3).add(enchantedBook(ModEnchants.raiderDamage.get(), 1).setWeight(15));  //L4 gets I
        levelPools.get(4).add(enchantedBook(ModEnchants.raiderDamage.get(), 1).setWeight(15));  //L5 gets I
        levelPools.get(4).add(enchantedBook(ModEnchants.raiderDamage.get(), 2).setWeight(1));   //L5 gets II

        return levelPools.stream().map(pool -> LootTable.lootTable().withPool(pool)).toList();
    }

    @NotNull
    private LootPoolSingletonContainer.Builder<?> enchantedBook(final Enchantment enchantment, final int level)
    {
        final ItemStack stack = new ItemStack(Items.ENCHANTED_BOOK);
        EnchantedBookItem.addEnchantment(stack, new EnchantmentInstance(enchantment, level));
        return SimpleLootTableProvider.itemStack(stack);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "EnchanterCraftingProvider";
    }

    @Override
    protected void registerRecipes(@NotNull final Consumer<FinishedRecipe> consumer)
    {
        final List<ItemStorage> tome = Collections.singletonList(new ItemStorage(
                new ItemStack(ModItems.ancientTome), true, true));

        for (int buildingLevel = 1; buildingLevel <= MAX_BUILDING_LEVEL; ++buildingLevel)
        {
            CustomRecipeProvider.CustomRecipeBuilder.create(ENCHANTER, MODULE_CUSTOM, "tome" + buildingLevel)
                    .minBuildingLevel(buildingLevel)
                    .maxBuildingLevel(buildingLevel)
                    .inputs(tome)
                    .secondaryOutputs(Collections.singletonList(new ItemStack(Items.ENCHANTED_BOOK)))
                    .lootTable(new ResourceLocation(MOD_ID, "recipes/" + ENCHANTER + buildingLevel))
                    .build(consumer);
        }

        CustomRecipeProvider.CustomRecipeBuilder.create(ENCHANTER, MODULE_CUSTOM, "scroll_tp")
                .inputs(List.of(new ItemStorage(new ItemStack(Items.PAPER, 3)),
                        new ItemStorage(new ItemStack(Items.COMPASS)),
                        new ItemStorage(new ItemStack(com.ldtteam.structurize.items.ModItems.buildTool.get()))))
                .result(new ItemStack(ModItems.scrollColonyTP, 3))
                .showTooltip(true)
                .build(consumer);

        CustomRecipeProvider.CustomRecipeBuilder.create(ENCHANTER, MODULE_CUSTOM, "scroll_area_tp")
                .inputs(List.of(new ItemStorage(new ItemStack(ModItems.scrollColonyTP, 3))))
                .result(new ItemStack(ModItems.scrollColonyAreaTP))
                .minBuildingLevel(2)
                .showTooltip(true)
                .build(consumer);

        CustomRecipeProvider.CustomRecipeBuilder.create(ENCHANTER, MODULE_CUSTOM, "scroll_guard_help")
                .inputs(List.of(new ItemStorage(new ItemStack(ModItems.scrollColonyTP)),
                        new ItemStorage(new ItemStack(Items.LAPIS_LAZULI, 5)),
                        new ItemStorage(new ItemStack(Items.ENDER_PEARL)),
                        new ItemStorage(new ItemStack(Items.PAPER))))
                .result(new ItemStack(ModItems.scrollGuardHelp, 2))
                .minBuildingLevel(3)
                .minResearchId(ResearchConstants.MORE_SCROLLS)
                .showTooltip(true)
                .build(consumer);

        CustomRecipeProvider.CustomRecipeBuilder.create(ENCHANTER, MODULE_CUSTOM, "scroll_highlight")
                .inputs(List.of(new ItemStorage(new ItemStack(ModItems.scrollColonyTP, 3)),
                        new ItemStorage(new ItemStack(Items.GLOWSTONE_DUST, 6)),
                        new ItemStorage(new ItemStack(Items.PAPER, 2))))
                .result(new ItemStack(ModItems.scrollHighLight, 5))
                .minBuildingLevel(3)
                .minResearchId(ResearchConstants.MORE_SCROLLS)
                .showTooltip(true)
                .build(consumer);
    }

    @Override
    protected void registerTables(@NotNull final SimpleLootTableProvider.LootTableRegistrar registrar)
    {
        for (int i = 0; i < levels.size(); i++)
        {
            final int buildingLevel = i + 1;
            final LootTable.Builder lootTable = levels.get(i);

            registrar.register(new ResourceLocation(MOD_ID, "recipes/" + ENCHANTER + buildingLevel),
                    LootContextParamSets.ALL_PARAMS, lootTable);
        }

        validate(levels.get(MAX_BUILDING_LEVEL - 1).build());
    }

    private void validate(@NotNull final LootTable lootTable)
    {
        final List<LootTableAnalyzer.LootDrop> drops = LootTableAnalyzer.toDrops(lootTableManager, lootTable);
        final Collection<Map.Entry<Enchantment, Integer>> enchantLevels = drops.stream()
                .flatMap(drop -> drop.getItemStacks().stream())
                .flatMap(stack -> EnchantmentHelper.getEnchantments(stack).entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Function.identity(), BinaryOperator.maxBy(Map.Entry.comparingByValue())))
                .values();
        for (final Map.Entry<Enchantment, Integer> entry : enchantLevels)
        {
            final EnchantmentRule rule = RULES.getOrDefault(entry.getKey(), new EnchantmentRule(NEVER, 0, false));
            if (entry.getValue() < entry.getKey().getMaxLevel() + rule.bonusLevels())
            {
                Log.getLogger().warn("Enchanter max level produces {} but max is {}{}",
                        entry.getKey().getFullname(entry.getValue()).getString(),
                        entry.getKey().getFullname(entry.getKey().getMaxLevel() + rule.bonusLevels()).getString(),
                        rule.bonusLevels() > 0 ? " (with bonus)" : "");
            }
            if (entry.getKey() != ModEnchants.raiderDamage.get())   // only check the vanilla ones
            {
                if (entry.getKey().isTreasureOnly() && !rule.rare())
                {
                    Log.getLogger().warn("{} is a treasure enchant but isn't marked as rare?",
                            Component.translatable(entry.getKey().getDescriptionId()).getString());
                }
                if (entry.getKey().getRarity().equals(Enchantment.Rarity.VERY_RARE) && !rule.rare())
                {
                    Log.getLogger().warn("{} is a very_rare enchant but isn't marked as rare?",
                            Component.translatable(entry.getKey().getDescriptionId()).getString());
                }
            }
        }

        final Set<Enchantment> enchantments = new HashSet<>(ForgeRegistries.ENCHANTMENTS.getValues());
        enchantLevels.stream().map(Map.Entry::getKey).forEach(enchantments::remove);
        RULES.entrySet().stream().filter(entry -> entry.getValue().firstLevel() > MAX_BUILDING_LEVEL)
                .map(Map.Entry::getKey).forEach(enchantments::remove);
        enchantments.removeIf(Enchantment::isCurse);

        for (final Enchantment enchantment : enchantments)
        {
            Log.getLogger().info("Enchanter does not produce {}",
                    Component.translatable(enchantment.getDescriptionId()).getString());
        }
    }
}
