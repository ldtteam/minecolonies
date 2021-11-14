package com.minecolonies.coremod.generation.defaults;

import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.enchants.ModEnchants;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.coremod.generation.CustomRecipeProvider;
import com.minecolonies.coremod.generation.SimpleLootTableProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.StandaloneLootEntry;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

public class DefaultEnchanterCraftingProvider implements IDataProvider
{
    private static final int MAX_BUILDING_LEVEL = 5;

    private final EnchanterRecipeProvider recipeProvider;
    private final EnchanterLootTableProvider lootTableProvider;
    private final List<LootTable.Builder> levels;

    public DefaultEnchanterCraftingProvider(@NotNull final DataGenerator generatorIn)
    {
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
                .add(enchantedBook(Enchantments.AQUA_AFFINITY, 2).setWeight(25))
                .add(enchantedBook(Enchantments.BANE_OF_ARTHROPODS, 2).setWeight(25))
                .add(enchantedBook(Enchantments.BLAST_PROTECTION, 2).setWeight(25))
                .add(enchantedBook(Enchantments.DEPTH_STRIDER, 2).setWeight(25))
                .add(enchantedBook(Enchantments.BLOCK_EFFICIENCY, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FALL_PROTECTION, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FIRE_ASPECT, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FIRE_PROTECTION, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FLAMING_ARROWS, 2).setWeight(25))
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
                .add(enchantedBook(Enchantments.AQUA_AFFINITY, 2).setWeight(25))
                .add(enchantedBook(Enchantments.BANE_OF_ARTHROPODS, 2).setWeight(25))
                .add(enchantedBook(Enchantments.BLAST_PROTECTION, 2).setWeight(25))
                .add(enchantedBook(Enchantments.DEPTH_STRIDER, 2).setWeight(25))
                .add(enchantedBook(Enchantments.BLOCK_EFFICIENCY, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FALL_PROTECTION, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FIRE_ASPECT, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FIRE_PROTECTION, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FLAMING_ARROWS, 2).setWeight(25))
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
                .add(enchantedBook(Enchantments.AQUA_AFFINITY, 3).setWeight(15))
                .add(enchantedBook(Enchantments.BANE_OF_ARTHROPODS, 3).setWeight(15))
                .add(enchantedBook(Enchantments.BLAST_PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.DEPTH_STRIDER, 3).setWeight(15))
                .add(enchantedBook(Enchantments.BLOCK_EFFICIENCY, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FALL_PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FIRE_ASPECT, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FIRE_PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FLAMING_ARROWS, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FROST_WALKER, 3).setWeight(15))
                .add(enchantedBook(Enchantments.KNOCKBACK, 3).setWeight(15))
                .add(enchantedBook(Enchantments.MOB_LOOTING, 3).setWeight(15))
                .add(enchantedBook(Enchantments.POWER_ARROWS, 3).setWeight(15))
                .add(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.ALL_DAMAGE_PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.PUNCH_ARROWS, 3).setWeight(15))
                .add(enchantedBook(Enchantments.QUICK_CHARGE, 3).setWeight(15))
                .add(enchantedBook(Enchantments.RESPIRATION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.SHARPNESS, 3).setWeight(15))
                .add(enchantedBook(Enchantments.SMITE, 3).setWeight(15))
                .add(enchantedBook(Enchantments.SWEEPING_EDGE, 3).setWeight(15))
                .add(enchantedBook(Enchantments.UNBREAKING, 3).setWeight(15))
                .add(enchantedBook(Enchantments.BLOCK_FORTUNE, 1).setWeight(1))
                .add(enchantedBook(ModEnchants.raiderDamage, 1).setWeight(15))
        ));

        // building level 4
        levels.add(LootTable.lootTable().withPool(LootPool.lootPool()
                // no more level 1 enchants
                // but still the level 2 enchants
                .add(enchantedBook(Enchantments.AQUA_AFFINITY, 2).setWeight(25))
                .add(enchantedBook(Enchantments.BANE_OF_ARTHROPODS, 2).setWeight(25))
                .add(enchantedBook(Enchantments.BLAST_PROTECTION, 2).setWeight(25))
                .add(enchantedBook(Enchantments.DEPTH_STRIDER, 2).setWeight(25))
                .add(enchantedBook(Enchantments.BLOCK_EFFICIENCY, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FALL_PROTECTION, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FIRE_ASPECT, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FIRE_PROTECTION, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FLAMING_ARROWS, 2).setWeight(25))
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
                .add(enchantedBook(Enchantments.AQUA_AFFINITY, 3).setWeight(15))
                .add(enchantedBook(Enchantments.BANE_OF_ARTHROPODS, 3).setWeight(15))
                .add(enchantedBook(Enchantments.BLAST_PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.DEPTH_STRIDER, 3).setWeight(15))
                .add(enchantedBook(Enchantments.BLOCK_EFFICIENCY, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FALL_PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FIRE_ASPECT, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FIRE_PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FLAMING_ARROWS, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FROST_WALKER, 3).setWeight(15))
                .add(enchantedBook(Enchantments.KNOCKBACK, 3).setWeight(15))
                .add(enchantedBook(Enchantments.MOB_LOOTING, 3).setWeight(15))
                .add(enchantedBook(Enchantments.POWER_ARROWS, 3).setWeight(15))
                .add(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.ALL_DAMAGE_PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.PUNCH_ARROWS, 3).setWeight(15))
                .add(enchantedBook(Enchantments.RESPIRATION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.QUICK_CHARGE, 3).setWeight(15))
                .add(enchantedBook(Enchantments.SHARPNESS, 3).setWeight(15))
                .add(enchantedBook(Enchantments.SMITE, 3).setWeight(15))
                .add(enchantedBook(Enchantments.SWEEPING_EDGE, 3).setWeight(15))
                .add(enchantedBook(Enchantments.UNBREAKING, 3).setWeight(15))
                .add(enchantedBook(Enchantments.BLOCK_FORTUNE, 1).setWeight(1))
                .add(enchantedBook(ModEnchants.raiderDamage, 1).setWeight(15))
                // plus new level 4 enchants
                .add(enchantedBook(Enchantments.AQUA_AFFINITY, 4).setWeight(5))
                .add(enchantedBook(Enchantments.BANE_OF_ARTHROPODS, 4).setWeight(5))
                .add(enchantedBook(Enchantments.BLAST_PROTECTION, 4).setWeight(5))
                .add(enchantedBook(Enchantments.DEPTH_STRIDER, 4).setWeight(5))
                .add(enchantedBook(Enchantments.BLOCK_EFFICIENCY, 4).setWeight(5))
                .add(enchantedBook(Enchantments.FALL_PROTECTION, 4).setWeight(5))
                .add(enchantedBook(Enchantments.FIRE_ASPECT, 4).setWeight(5))
                .add(enchantedBook(Enchantments.FIRE_PROTECTION, 4).setWeight(5))
                .add(enchantedBook(Enchantments.FLAMING_ARROWS, 4).setWeight(5))
                .add(enchantedBook(Enchantments.FROST_WALKER, 4).setWeight(5))
                .add(enchantedBook(Enchantments.INFINITY_ARROWS, 4).setWeight(5))
                .add(enchantedBook(Enchantments.KNOCKBACK, 4).setWeight(5))
                .add(enchantedBook(Enchantments.MOB_LOOTING, 4).setWeight(5))
                .add(enchantedBook(Enchantments.POWER_ARROWS, 4).setWeight(5))
                .add(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 4).setWeight(5))
                .add(enchantedBook(Enchantments.PUNCH_ARROWS, 4).setWeight(5))
                .add(enchantedBook(Enchantments.QUICK_CHARGE, 4).setWeight(5))
                .add(enchantedBook(Enchantments.RESPIRATION, 4).setWeight(5))
                .add(enchantedBook(Enchantments.SHARPNESS, 4).setWeight(5))
                .add(enchantedBook(Enchantments.SMITE, 4).setWeight(5))
                .add(enchantedBook(Enchantments.SWEEPING_EDGE, 4).setWeight(5))
                .add(enchantedBook(Enchantments.UNBREAKING, 4).setWeight(5))
                .add(enchantedBook(Enchantments.BLOCK_FORTUNE, 2).setWeight(1))
        ));

        // building level 5
        levels.add(LootTable.lootTable().withPool(LootPool.lootPool()
                // no more level 1 or 2 enchants
                // but still the level 3 enchants
                .add(enchantedBook(Enchantments.AQUA_AFFINITY, 3).setWeight(15))
                .add(enchantedBook(Enchantments.BANE_OF_ARTHROPODS, 3).setWeight(15))
                .add(enchantedBook(Enchantments.BLAST_PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.DEPTH_STRIDER, 3).setWeight(15))
                .add(enchantedBook(Enchantments.BLOCK_EFFICIENCY, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FALL_PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FIRE_ASPECT, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FIRE_PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FLAMING_ARROWS, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FROST_WALKER, 3).setWeight(15))
                .add(enchantedBook(Enchantments.KNOCKBACK, 3).setWeight(15))
                .add(enchantedBook(Enchantments.MOB_LOOTING, 3).setWeight(15))
                .add(enchantedBook(Enchantments.POWER_ARROWS, 3).setWeight(15))
                .add(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.ALL_DAMAGE_PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.PUNCH_ARROWS, 3).setWeight(15))
                .add(enchantedBook(Enchantments.QUICK_CHARGE, 3).setWeight(15))
                .add(enchantedBook(Enchantments.RESPIRATION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.SHARPNESS, 3).setWeight(15))
                .add(enchantedBook(Enchantments.SMITE, 3).setWeight(15))
                .add(enchantedBook(Enchantments.SWEEPING_EDGE, 3).setWeight(15))
                .add(enchantedBook(Enchantments.UNBREAKING, 3).setWeight(15))
                .add(enchantedBook(ModEnchants.raiderDamage, 1).setWeight(15))
                .add(enchantedBook(Enchantments.BLOCK_FORTUNE, 1).setWeight(1))
                // plus level 4 enchants
                .add(enchantedBook(Enchantments.AQUA_AFFINITY, 4).setWeight(5))
                .add(enchantedBook(Enchantments.BANE_OF_ARTHROPODS, 4).setWeight(5))
                .add(enchantedBook(Enchantments.BLAST_PROTECTION, 4).setWeight(5))
                .add(enchantedBook(Enchantments.DEPTH_STRIDER, 4).setWeight(5))
                .add(enchantedBook(Enchantments.BLOCK_EFFICIENCY, 4).setWeight(5))
                .add(enchantedBook(Enchantments.FALL_PROTECTION, 4).setWeight(5))
                .add(enchantedBook(Enchantments.FIRE_ASPECT, 4).setWeight(5))
                .add(enchantedBook(Enchantments.FIRE_PROTECTION, 4).setWeight(5))
                .add(enchantedBook(Enchantments.FLAMING_ARROWS, 4).setWeight(5))
                .add(enchantedBook(Enchantments.FROST_WALKER, 4).setWeight(5))
                .add(enchantedBook(Enchantments.INFINITY_ARROWS, 1).setWeight(5))
                .add(enchantedBook(Enchantments.KNOCKBACK, 4).setWeight(5))
                .add(enchantedBook(Enchantments.MOB_LOOTING, 4).setWeight(5))
                .add(enchantedBook(Enchantments.POWER_ARROWS, 4).setWeight(5))
                .add(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 4).setWeight(5))
                .add(enchantedBook(Enchantments.PUNCH_ARROWS, 4).setWeight(5))
                .add(enchantedBook(Enchantments.QUICK_CHARGE, 4).setWeight(5))
                .add(enchantedBook(Enchantments.RESPIRATION, 4).setWeight(5))
                .add(enchantedBook(Enchantments.SHARPNESS, 4).setWeight(5))
                .add(enchantedBook(Enchantments.SMITE, 4).setWeight(5))
                .add(enchantedBook(Enchantments.SWEEPING_EDGE, 4).setWeight(5))
                .add(enchantedBook(Enchantments.UNBREAKING, 4).setWeight(5))
                .add(enchantedBook(Enchantments.BLOCK_FORTUNE, 2).setWeight(1))
                // plus new level 5 enchants
                .add(enchantedBook(Enchantments.AQUA_AFFINITY, 5).setWeight(1))
                .add(enchantedBook(Enchantments.BANE_OF_ARTHROPODS, 5).setWeight(1))
                .add(enchantedBook(Enchantments.BLAST_PROTECTION, 5).setWeight(1))
                .add(enchantedBook(Enchantments.DEPTH_STRIDER, 5).setWeight(1))
                .add(enchantedBook(Enchantments.BLOCK_EFFICIENCY, 5).setWeight(1))
                .add(enchantedBook(Enchantments.FALL_PROTECTION, 5).setWeight(1))
                .add(enchantedBook(Enchantments.FIRE_ASPECT, 5).setWeight(1))
                .add(enchantedBook(Enchantments.FIRE_PROTECTION, 5).setWeight(1))
                .add(enchantedBook(Enchantments.FLAMING_ARROWS, 5).setWeight(1))
                .add(enchantedBook(Enchantments.FROST_WALKER, 5).setWeight(1))
                .add(enchantedBook(Enchantments.INFINITY_ARROWS, 1).setWeight(1))
                .add(enchantedBook(Enchantments.KNOCKBACK, 5).setWeight(1))
                .add(enchantedBook(Enchantments.MOB_LOOTING, 5).setWeight(1))
                .add(enchantedBook(Enchantments.MENDING, 1).setWeight(1))
                .add(enchantedBook(Enchantments.MULTISHOT, 1).setWeight(1))
                .add(enchantedBook(Enchantments.POWER_ARROWS, 5).setWeight(1))
                .add(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 5).setWeight(1))
                .add(enchantedBook(Enchantments.ALL_DAMAGE_PROTECTION, 5).setWeight(1))
                .add(enchantedBook(Enchantments.PUNCH_ARROWS, 5).setWeight(1))
                .add(enchantedBook(Enchantments.QUICK_CHARGE, 5).setWeight(1))
                .add(enchantedBook(Enchantments.RESPIRATION, 5).setWeight(1))
                .add(enchantedBook(Enchantments.SHARPNESS, 5).setWeight(1))
                .add(enchantedBook(Enchantments.SILK_TOUCH, 1).setWeight(1))
                .add(enchantedBook(Enchantments.SMITE, 5).setWeight(1))
                .add(enchantedBook(Enchantments.SWEEPING_EDGE, 5).setWeight(1))
                .add(enchantedBook(Enchantments.UNBREAKING, 5).setWeight(1))
                .add(enchantedBook(Enchantments.BLOCK_FORTUNE, 3).setWeight(1))
                .add(enchantedBook(ModEnchants.raiderDamage, 2).setWeight(1))
        ));

        recipeProvider = new EnchanterRecipeProvider(generatorIn);
        lootTableProvider = new EnchanterLootTableProvider(generatorIn);
    }

    @NotNull
    private StandaloneLootEntry.Builder<?> enchantedBook(@NotNull final Enchantment enchantment, final int level)
    {
        final ItemStack stack = new ItemStack(Items.ENCHANTED_BOOK);
        EnchantedBookItem.addEnchantment(stack, new EnchantmentData(enchantment, level));
        return SimpleLootTableProvider.itemStack(stack);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "EnchanterCraftingProvider";
    }

    @Override
    public void run(@NotNull final DirectoryCache cache) throws IOException
    {
        recipeProvider.run(cache);
        lootTableProvider.run(cache);
    }

    private static class EnchanterRecipeProvider extends CustomRecipeProvider
    {
        public EnchanterRecipeProvider(@NotNull final DataGenerator generatorIn)
        {
            super(generatorIn);
        }

        @NotNull
        @Override
        public String getName()
        {
            return "EnchanterRecipeProvider";
        }

        @Override
        protected void registerRecipes(@NotNull final Consumer<IFinishedRecipe> consumer)
        {
            final List<ItemStorage> tome = Collections.singletonList(new ItemStorage(
                    new ItemStack(ModItems.ancientTome), true, true));

            for (int buildingLevel = 1; buildingLevel <= MAX_BUILDING_LEVEL; ++buildingLevel)
            {
                CustomRecipeBuilder.create(ModJobs.ENCHANTER_ID.getPath() + "_custom", "tome" + buildingLevel)
                        .minBuildingLevel(buildingLevel)
                        .maxBuildingLevel(buildingLevel)
                        .inputs(tome)
                        .secondaryOutputs(Collections.singletonList(new ItemStack(Items.ENCHANTED_BOOK)))
                        .lootTable(new ResourceLocation(MOD_ID, "recipes/enchanter" + buildingLevel))
                        .build(consumer);
            }
        }
    }

    private class EnchanterLootTableProvider extends SimpleLootTableProvider
    {
        public EnchanterLootTableProvider(@NotNull final DataGenerator dataGeneratorIn)
        {
            super(dataGeneratorIn);
        }

        @NotNull
        @Override
        public String getName()
        {
            return "EnchanterLootTableProvider";
        }

        @Override
        protected void registerTables(@NotNull final LootTableRegistrar registrar)
        {
            for (int i = 0; i < levels.size(); i++)
            {
                final int buildingLevel = i + 1;
                final LootTable.Builder lootTable = levels.get(i);

                registrar.register(new ResourceLocation(MOD_ID, "recipes/enchanter" + buildingLevel),
                        LootParameterSets.ALL_PARAMS, lootTable);
            }
        }
    }
}
