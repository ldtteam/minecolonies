package com.minecolonies.coremod.generation.defaults.workers;

import com.google.common.collect.ImmutableSet;
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
    private static final Set<Enchantment> DELIBERATELY_IGNORED = ImmutableSet.of(
            Enchantments.BINDING_CURSE, Enchantments.VANISHING_CURSE
    );

    private final List<LootTable.Builder> levels;
    private final LootTables lootTableManager;

    public DefaultEnchanterCraftingProvider(@NotNull final DataGenerator generatorIn,
                                            @NotNull final LootTables lootTableManager)
    {
        super(generatorIn);
        this.lootTableManager = lootTableManager;

        levels = new ArrayList<>();

        // building level 1
        levels.add(LootTable.lootTable().withPool(LootPool.lootPool()
                .add(enchantedBook(Enchantments.AQUA_AFFINITY, 1).setWeight(50))
                .add(enchantedBook(Enchantments.BANE_OF_ARTHROPODS, 1).setWeight(50))
                .add(enchantedBook(Enchantments.BLAST_PROTECTION, 1).setWeight(50))
                .add(enchantedBook(Enchantments.DEPTH_STRIDER, 1).setWeight(50))
                .add(enchantedBook(Enchantments.BLOCK_EFFICIENCY, 1).setWeight(50))
                .add(enchantedBook(Enchantments.FALL_PROTECTION, 1).setWeight(50))
                .add(enchantedBook(Enchantments.FIRE_ASPECT, 1).setWeight(50))
                .add(enchantedBook(Enchantments.FIRE_PROTECTION, 1).setWeight(50))
                .add(enchantedBook(Enchantments.FLAMING_ARROWS, 1).setWeight(50))
                .add(enchantedBook(Enchantments.FROST_WALKER, 1).setWeight(50))
                .add(enchantedBook(Enchantments.KNOCKBACK, 1).setWeight(50))
                .add(enchantedBook(Enchantments.MOB_LOOTING, 1).setWeight(50))
                .add(enchantedBook(Enchantments.POWER_ARROWS, 1).setWeight(50))
                .add(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 1).setWeight(50))
                .add(enchantedBook(Enchantments.ALL_DAMAGE_PROTECTION, 1).setWeight(50))
                .add(enchantedBook(Enchantments.PUNCH_ARROWS, 1).setWeight(50))
                .add(enchantedBook(Enchantments.QUICK_CHARGE, 1).setWeight(50))
                .add(enchantedBook(Enchantments.RESPIRATION, 1).setWeight(50))
                .add(enchantedBook(Enchantments.SHARPNESS, 1).setWeight(50))
                .add(enchantedBook(Enchantments.SMITE, 1).setWeight(50))
                .add(enchantedBook(Enchantments.SWEEPING_EDGE, 1).setWeight(50))
                .add(enchantedBook(Enchantments.UNBREAKING, 1).setWeight(50))
        ));

        // building level 2
        levels.add(LootTable.lootTable().withPool(LootPool.lootPool()
                // also the level 1 enchants
                .add(enchantedBook(Enchantments.AQUA_AFFINITY, 1).setWeight(50))
                .add(enchantedBook(Enchantments.BANE_OF_ARTHROPODS, 1).setWeight(50))
                .add(enchantedBook(Enchantments.BLAST_PROTECTION, 1).setWeight(50))
                .add(enchantedBook(Enchantments.DEPTH_STRIDER, 1).setWeight(50))
                .add(enchantedBook(Enchantments.BLOCK_EFFICIENCY, 1).setWeight(50))
                .add(enchantedBook(Enchantments.FALL_PROTECTION, 1).setWeight(50))
                .add(enchantedBook(Enchantments.FIRE_ASPECT, 1).setWeight(50))
                .add(enchantedBook(Enchantments.FIRE_PROTECTION, 1).setWeight(50))
                .add(enchantedBook(Enchantments.FLAMING_ARROWS, 1).setWeight(50))
                .add(enchantedBook(Enchantments.FROST_WALKER, 1).setWeight(50))
                .add(enchantedBook(Enchantments.KNOCKBACK, 1).setWeight(50))
                .add(enchantedBook(Enchantments.MOB_LOOTING, 1).setWeight(50))
                .add(enchantedBook(Enchantments.POWER_ARROWS, 1).setWeight(50))
                .add(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 1).setWeight(50))
                .add(enchantedBook(Enchantments.ALL_DAMAGE_PROTECTION, 1).setWeight(50))
                .add(enchantedBook(Enchantments.PUNCH_ARROWS, 1).setWeight(50))
                .add(enchantedBook(Enchantments.QUICK_CHARGE, 1).setWeight(50))
                .add(enchantedBook(Enchantments.RESPIRATION, 1).setWeight(50))
                .add(enchantedBook(Enchantments.SHARPNESS, 1).setWeight(50))
                .add(enchantedBook(Enchantments.SMITE, 1).setWeight(50))
                .add(enchantedBook(Enchantments.SWEEPING_EDGE, 1).setWeight(50))
                .add(enchantedBook(Enchantments.UNBREAKING, 1).setWeight(50))
                // plus new level 2 enchants
                .add(enchantedBook(Enchantments.BANE_OF_ARTHROPODS, 2).setWeight(25))
                .add(enchantedBook(Enchantments.BLAST_PROTECTION, 2).setWeight(25))
                .add(enchantedBook(Enchantments.DEPTH_STRIDER, 2).setWeight(25))
                .add(enchantedBook(Enchantments.BLOCK_EFFICIENCY, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FALL_PROTECTION, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FIRE_ASPECT, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FIRE_PROTECTION, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FROST_WALKER, 2).setWeight(25))
                .add(enchantedBook(Enchantments.KNOCKBACK, 2).setWeight(25))
                .add(enchantedBook(Enchantments.MOB_LOOTING, 2).setWeight(25))
                .add(enchantedBook(Enchantments.POWER_ARROWS, 2).setWeight(25))
                .add(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 2).setWeight(25))
                .add(enchantedBook(Enchantments.ALL_DAMAGE_PROTECTION, 2).setWeight(25))
                .add(enchantedBook(Enchantments.PUNCH_ARROWS, 2).setWeight(25))
                .add(enchantedBook(Enchantments.QUICK_CHARGE, 2).setWeight(25))
                .add(enchantedBook(Enchantments.RESPIRATION, 2).setWeight(25))
                .add(enchantedBook(Enchantments.SHARPNESS, 2).setWeight(25))
                .add(enchantedBook(Enchantments.SMITE, 2).setWeight(25))
                .add(enchantedBook(Enchantments.SWEEPING_EDGE, 2).setWeight(25))
                .add(enchantedBook(Enchantments.UNBREAKING, 2).setWeight(25))
        ));

        // building level 3
        levels.add(LootTable.lootTable().withPool(LootPool.lootPool()
                // also the level 1 enchants
                .add(enchantedBook(Enchantments.AQUA_AFFINITY, 1).setWeight(50))
                .add(enchantedBook(Enchantments.BANE_OF_ARTHROPODS, 1).setWeight(50))
                .add(enchantedBook(Enchantments.BLAST_PROTECTION, 1).setWeight(50))
                .add(enchantedBook(Enchantments.DEPTH_STRIDER, 1).setWeight(50))
                .add(enchantedBook(Enchantments.BLOCK_EFFICIENCY, 1).setWeight(50))
                .add(enchantedBook(Enchantments.FALL_PROTECTION, 1).setWeight(50))
                .add(enchantedBook(Enchantments.FIRE_ASPECT, 1).setWeight(50))
                .add(enchantedBook(Enchantments.FIRE_PROTECTION, 1).setWeight(50))
                .add(enchantedBook(Enchantments.FLAMING_ARROWS, 1).setWeight(50))
                .add(enchantedBook(Enchantments.FROST_WALKER, 1).setWeight(50))
                .add(enchantedBook(Enchantments.KNOCKBACK, 1).setWeight(50))
                .add(enchantedBook(Enchantments.MOB_LOOTING, 1).setWeight(50))
                .add(enchantedBook(Enchantments.POWER_ARROWS, 1).setWeight(50))
                .add(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 1).setWeight(50))
                .add(enchantedBook(Enchantments.ALL_DAMAGE_PROTECTION, 1).setWeight(50))
                .add(enchantedBook(Enchantments.PUNCH_ARROWS, 1).setWeight(50))
                .add(enchantedBook(Enchantments.QUICK_CHARGE, 1).setWeight(50))
                .add(enchantedBook(Enchantments.RESPIRATION, 1).setWeight(50))
                .add(enchantedBook(Enchantments.SHARPNESS, 1).setWeight(50))
                .add(enchantedBook(Enchantments.SMITE, 1).setWeight(50))
                .add(enchantedBook(Enchantments.SWEEPING_EDGE, 1).setWeight(50))
                .add(enchantedBook(Enchantments.UNBREAKING, 1).setWeight(50))
                // also the level 2 enchants
                .add(enchantedBook(Enchantments.BANE_OF_ARTHROPODS, 2).setWeight(25))
                .add(enchantedBook(Enchantments.BLAST_PROTECTION, 2).setWeight(25))
                .add(enchantedBook(Enchantments.DEPTH_STRIDER, 2).setWeight(25))
                .add(enchantedBook(Enchantments.BLOCK_EFFICIENCY, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FALL_PROTECTION, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FIRE_ASPECT, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FIRE_PROTECTION, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FROST_WALKER, 2).setWeight(25))
                .add(enchantedBook(Enchantments.KNOCKBACK, 2).setWeight(25))
                .add(enchantedBook(Enchantments.MOB_LOOTING, 2).setWeight(25))
                .add(enchantedBook(Enchantments.POWER_ARROWS, 2).setWeight(25))
                .add(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 2).setWeight(25))
                .add(enchantedBook(Enchantments.ALL_DAMAGE_PROTECTION, 2).setWeight(25))
                .add(enchantedBook(Enchantments.PUNCH_ARROWS, 2).setWeight(25))
                .add(enchantedBook(Enchantments.QUICK_CHARGE, 2).setWeight(25))
                .add(enchantedBook(Enchantments.RESPIRATION, 2).setWeight(25))
                .add(enchantedBook(Enchantments.SHARPNESS, 2).setWeight(25))
                .add(enchantedBook(Enchantments.SMITE, 2).setWeight(25))
                .add(enchantedBook(Enchantments.SWEEPING_EDGE, 2).setWeight(25))
                .add(enchantedBook(Enchantments.UNBREAKING, 2).setWeight(25))
                // plus new level 3 enchants
                .add(enchantedBook(Enchantments.BANE_OF_ARTHROPODS, 3).setWeight(15))
                .add(enchantedBook(Enchantments.BLAST_PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.DEPTH_STRIDER, 3).setWeight(15))
                .add(enchantedBook(Enchantments.BLOCK_EFFICIENCY, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FALL_PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FIRE_PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.MOB_LOOTING, 3).setWeight(15))
                .add(enchantedBook(Enchantments.POWER_ARROWS, 3).setWeight(15))
                .add(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.ALL_DAMAGE_PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.QUICK_CHARGE, 3).setWeight(15))
                .add(enchantedBook(Enchantments.RESPIRATION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.SHARPNESS, 3).setWeight(15))
                .add(enchantedBook(Enchantments.SMITE, 3).setWeight(15))
                .add(enchantedBook(Enchantments.SWEEPING_EDGE, 3).setWeight(15))
                .add(enchantedBook(Enchantments.UNBREAKING, 3).setWeight(15))
                .add(enchantedBook(Enchantments.BLOCK_FORTUNE, 1).setWeight(1))
                .add(enchantedBook(ModEnchants.raiderDamage.get(), 1).setWeight(15))
        ));

        // building level 4
        levels.add(LootTable.lootTable().withPool(LootPool.lootPool()
                // no more level 1 enchants
                // but still the level 2 enchants
                .add(enchantedBook(Enchantments.AQUA_AFFINITY, 1).setWeight(25))
                .add(enchantedBook(Enchantments.BANE_OF_ARTHROPODS, 2).setWeight(25))
                .add(enchantedBook(Enchantments.BLAST_PROTECTION, 2).setWeight(25))
                .add(enchantedBook(Enchantments.DEPTH_STRIDER, 2).setWeight(25))
                .add(enchantedBook(Enchantments.BLOCK_EFFICIENCY, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FALL_PROTECTION, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FIRE_ASPECT, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FIRE_PROTECTION, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FLAMING_ARROWS, 1).setWeight(25))
                .add(enchantedBook(Enchantments.FROST_WALKER, 2).setWeight(25))
                .add(enchantedBook(Enchantments.KNOCKBACK, 2).setWeight(25))
                .add(enchantedBook(Enchantments.MOB_LOOTING, 2).setWeight(25))
                .add(enchantedBook(Enchantments.POWER_ARROWS, 2).setWeight(25))
                .add(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 2).setWeight(25))
                .add(enchantedBook(Enchantments.ALL_DAMAGE_PROTECTION, 2).setWeight(25))
                .add(enchantedBook(Enchantments.PUNCH_ARROWS, 2).setWeight(25))
                .add(enchantedBook(Enchantments.QUICK_CHARGE, 2).setWeight(25))
                .add(enchantedBook(Enchantments.RESPIRATION, 2).setWeight(25))
                .add(enchantedBook(Enchantments.SHARPNESS, 2).setWeight(25))
                .add(enchantedBook(Enchantments.SMITE, 2).setWeight(25))
                .add(enchantedBook(Enchantments.SWEEPING_EDGE, 2).setWeight(25))
                .add(enchantedBook(Enchantments.UNBREAKING, 2).setWeight(25))
                // plus level 3 enchants
                .add(enchantedBook(Enchantments.BANE_OF_ARTHROPODS, 3).setWeight(15))
                .add(enchantedBook(Enchantments.BLAST_PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.DEPTH_STRIDER, 3).setWeight(15))
                .add(enchantedBook(Enchantments.BLOCK_EFFICIENCY, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FALL_PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FIRE_PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.MOB_LOOTING, 3).setWeight(15))
                .add(enchantedBook(Enchantments.POWER_ARROWS, 3).setWeight(15))
                .add(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.ALL_DAMAGE_PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.RESPIRATION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.QUICK_CHARGE, 3).setWeight(15))
                .add(enchantedBook(Enchantments.SHARPNESS, 3).setWeight(15))
                .add(enchantedBook(Enchantments.SMITE, 3).setWeight(15))
                .add(enchantedBook(Enchantments.SWEEPING_EDGE, 3).setWeight(15))
                .add(enchantedBook(Enchantments.UNBREAKING, 3).setWeight(15))
                .add(enchantedBook(Enchantments.BLOCK_FORTUNE, 1).setWeight(1))
                .add(enchantedBook(ModEnchants.raiderDamage.get(), 1).setWeight(15))
                // plus new level 4 enchants
                .add(enchantedBook(Enchantments.BANE_OF_ARTHROPODS, 4).setWeight(5))
                .add(enchantedBook(Enchantments.BLAST_PROTECTION, 4).setWeight(5))
                .add(enchantedBook(Enchantments.BLOCK_EFFICIENCY, 4).setWeight(5))
                .add(enchantedBook(Enchantments.FALL_PROTECTION, 4).setWeight(5))
                .add(enchantedBook(Enchantments.FIRE_PROTECTION, 4).setWeight(5))
                .add(enchantedBook(Enchantments.INFINITY_ARROWS, 1).setWeight(5))
                .add(enchantedBook(Enchantments.POWER_ARROWS, 4).setWeight(5))
                .add(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 4).setWeight(5))
                .add(enchantedBook(Enchantments.SHARPNESS, 4).setWeight(5))
                .add(enchantedBook(Enchantments.SMITE, 4).setWeight(5))
                .add(enchantedBook(Enchantments.BLOCK_FORTUNE, 2).setWeight(1))
        ));

        // building level 5
        levels.add(LootTable.lootTable().withPool(LootPool.lootPool()
                // no more level 1 or 2 enchants
                // but still the level 3 enchants
                .add(enchantedBook(Enchantments.AQUA_AFFINITY, 1).setWeight(15))
                .add(enchantedBook(Enchantments.BANE_OF_ARTHROPODS, 3).setWeight(15))
                .add(enchantedBook(Enchantments.BLAST_PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.DEPTH_STRIDER, 3).setWeight(15))
                .add(enchantedBook(Enchantments.BLOCK_EFFICIENCY, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FALL_PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FIRE_ASPECT, 2).setWeight(15))
                .add(enchantedBook(Enchantments.FIRE_PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FLAMING_ARROWS, 1).setWeight(15))
                .add(enchantedBook(Enchantments.FROST_WALKER, 2).setWeight(15))
                .add(enchantedBook(Enchantments.KNOCKBACK, 2).setWeight(15))
                .add(enchantedBook(Enchantments.MOB_LOOTING, 3).setWeight(15))
                .add(enchantedBook(Enchantments.POWER_ARROWS, 3).setWeight(15))
                .add(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.ALL_DAMAGE_PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.PUNCH_ARROWS, 2).setWeight(15))
                .add(enchantedBook(Enchantments.QUICK_CHARGE, 3).setWeight(15))
                .add(enchantedBook(Enchantments.RESPIRATION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.SHARPNESS, 3).setWeight(15))
                .add(enchantedBook(Enchantments.SMITE, 3).setWeight(15))
                .add(enchantedBook(Enchantments.SWEEPING_EDGE, 3).setWeight(15))
                .add(enchantedBook(Enchantments.UNBREAKING, 3).setWeight(15))
                .add(enchantedBook(ModEnchants.raiderDamage.get(), 1).setWeight(15))
                .add(enchantedBook(Enchantments.BLOCK_FORTUNE, 1).setWeight(1))
                // plus level 4 enchants
                .add(enchantedBook(Enchantments.BANE_OF_ARTHROPODS, 4).setWeight(5))
                .add(enchantedBook(Enchantments.BLAST_PROTECTION, 4).setWeight(5))
                .add(enchantedBook(Enchantments.BLOCK_EFFICIENCY, 4).setWeight(5))
                .add(enchantedBook(Enchantments.FALL_PROTECTION, 4).setWeight(5))
                .add(enchantedBook(Enchantments.FIRE_PROTECTION, 4).setWeight(5))
                .add(enchantedBook(Enchantments.INFINITY_ARROWS, 1).setWeight(5))
                .add(enchantedBook(Enchantments.POWER_ARROWS, 4).setWeight(5))
                .add(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 4).setWeight(5))
                .add(enchantedBook(Enchantments.ALL_DAMAGE_PROTECTION, 4).setWeight(5))
                .add(enchantedBook(Enchantments.SHARPNESS, 4).setWeight(5))
                .add(enchantedBook(Enchantments.SMITE, 4).setWeight(5))
                .add(enchantedBook(Enchantments.BLOCK_FORTUNE, 2).setWeight(1))
                // plus new level 5 enchants
                .add(enchantedBook(Enchantments.BANE_OF_ARTHROPODS, 5).setWeight(1))
                .add(enchantedBook(Enchantments.BLOCK_EFFICIENCY, 5).setWeight(1))
                .add(enchantedBook(Enchantments.MENDING, 1).setWeight(1))
                .add(enchantedBook(Enchantments.MULTISHOT, 1).setWeight(1))
                .add(enchantedBook(Enchantments.POWER_ARROWS, 5).setWeight(1))
                .add(enchantedBook(Enchantments.SHARPNESS, 5).setWeight(1))
                .add(enchantedBook(Enchantments.SILK_TOUCH, 1).setWeight(1))
                .add(enchantedBook(Enchantments.SMITE, 5).setWeight(1))
                .add(enchantedBook(Enchantments.BLOCK_FORTUNE, 3).setWeight(1))
                .add(enchantedBook(ModEnchants.raiderDamage.get(), 2).setWeight(1))
        ));
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

            validate(buildingLevel, lootTable);

            registrar.register(new ResourceLocation(MOD_ID, "recipes/" + ENCHANTER + buildingLevel),
                    LootContextParamSets.ALL_PARAMS, lootTable);
        }
    }

    private void validate(final int buildingLevel, @NotNull final LootTable.Builder lootTable)
    {
        final List<LootTableAnalyzer.LootDrop> drops = LootTableAnalyzer.toDrops(lootTableManager, lootTable.build());
        final Collection<Map.Entry<Enchantment, Integer>> enchantLevels = drops.stream()
                .flatMap(drop -> drop.getItemStacks().stream())
                .flatMap(stack -> EnchantmentHelper.getEnchantments(stack).entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Function.identity(), BinaryOperator.maxBy(Map.Entry.comparingByValue())))
                .values();
        for (final Map.Entry<Enchantment, Integer> entry : enchantLevels)
        {
            if (entry.getValue() > entry.getKey().getMaxLevel())
            {
                Log.getLogger().error("Enchanter level {} produces {} but max is {}", buildingLevel,
                        entry.getKey().getFullname(entry.getValue()).getString(),
                        entry.getKey().getFullname(entry.getKey().getMaxLevel()).getString());
            }
            else if (buildingLevel == levels.size() && entry.getValue() < entry.getKey().getMaxLevel())
            {
                Log.getLogger().warn("Enchanter max level produces {} but max is {}",
                        entry.getKey().getFullname(entry.getValue()).getString(),
                        entry.getKey().getFullname(entry.getKey().getMaxLevel()).getString());
            }
        }

        if (buildingLevel == levels.size())
        {
            final Set<Enchantment> enchantments = new HashSet<>(ForgeRegistries.ENCHANTMENTS.getValues());
            enchantLevels.stream().map(Map.Entry::getKey).forEach(enchantments::remove);
            DELIBERATELY_IGNORED.forEach(enchantments::remove);

            for (final Enchantment enchantment : enchantments)
            {
                Log.getLogger().info("Max enchanter does not produce {}",
                        Component.translatable(enchantment.getDescriptionId()).getString());
            }
        }
    }
}
