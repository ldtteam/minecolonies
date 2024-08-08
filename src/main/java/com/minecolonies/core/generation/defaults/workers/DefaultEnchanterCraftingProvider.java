package com.minecolonies.core.generation.defaults.workers;

import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.enchants.ModEnchants;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.research.util.ResearchConstants;
import com.minecolonies.core.generation.CustomRecipeAndLootTableProvider;
import com.minecolonies.core.generation.CustomRecipeProvider.CustomRecipeBuilder;
import com.minecolonies.core.generation.SimpleLootTableProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.minecolonies.api.util.constant.BuildingConstants.MODULE_CUSTOM;
import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/**
 * Datagen for Enchanter
 */
public class DefaultEnchanterCraftingProvider extends CustomRecipeAndLootTableProvider
{
    private final String ENCHANTER = ModJobs.ENCHANTER_ID.getPath();
    private static final int MAX_BUILDING_LEVEL = 5;

    private final List<LootTable.Builder> levels = new ArrayList<>();
    private HolderLookup.Provider provider;

    public DefaultEnchanterCraftingProvider(@NotNull final PackOutput packOutput, final CompletableFuture<HolderLookup.Provider> lookupProvider)
    {
        super(packOutput, lookupProvider);
    }

    @Override
    protected CompletableFuture<HolderLookup.Provider> generate(@NotNull final HolderLookup.Provider provider)
    {
        this.provider = provider;

        // building level 1
        levels.add(LootTable.lootTable().withPool(LootPool.lootPool()
                .add(enchantedBook(Enchantments.AQUA_AFFINITY, 1).setWeight(50))
                .add(enchantedBook(Enchantments.BANE_OF_ARTHROPODS, 1).setWeight(50))
                .add(enchantedBook(Enchantments.BLAST_PROTECTION, 1).setWeight(50))
                .add(enchantedBook(Enchantments.DEPTH_STRIDER, 1).setWeight(50))
                .add(enchantedBook(Enchantments.EFFICIENCY, 1).setWeight(50))
                .add(enchantedBook(Enchantments.FEATHER_FALLING, 1).setWeight(50))
                .add(enchantedBook(Enchantments.FIRE_ASPECT, 1).setWeight(50))
                .add(enchantedBook(Enchantments.FIRE_PROTECTION, 1).setWeight(50))
                .add(enchantedBook(Enchantments.FLAME, 1).setWeight(50))
                .add(enchantedBook(Enchantments.FROST_WALKER, 1).setWeight(50))
                .add(enchantedBook(Enchantments.KNOCKBACK, 1).setWeight(50))
                .add(enchantedBook(Enchantments.LOOTING, 1).setWeight(50))
                .add(enchantedBook(Enchantments.POWER, 1).setWeight(50))
                .add(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 1).setWeight(50))
                .add(enchantedBook(Enchantments.PROTECTION, 1).setWeight(50))
                .add(enchantedBook(Enchantments.PUNCH, 1).setWeight(50))
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
                .add(enchantedBook(Enchantments.EFFICIENCY, 1).setWeight(50))
                .add(enchantedBook(Enchantments.PROTECTION, 1).setWeight(50))
                .add(enchantedBook(Enchantments.FIRE_ASPECT, 1).setWeight(50))
                .add(enchantedBook(Enchantments.FIRE_PROTECTION, 1).setWeight(50))
                .add(enchantedBook(Enchantments.FLAME, 1).setWeight(50))
                .add(enchantedBook(Enchantments.FROST_WALKER, 1).setWeight(50))
                .add(enchantedBook(Enchantments.KNOCKBACK, 1).setWeight(50))
                .add(enchantedBook(Enchantments.LOOTING, 1).setWeight(50))
                .add(enchantedBook(Enchantments.POWER, 1).setWeight(50))
                .add(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 1).setWeight(50))
                .add(enchantedBook(Enchantments.PROTECTION, 1).setWeight(50))
                .add(enchantedBook(Enchantments.PUNCH, 1).setWeight(50))
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
                .add(enchantedBook(Enchantments.EFFICIENCY, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FEATHER_FALLING, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FIRE_ASPECT, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FIRE_PROTECTION, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FLAME, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FROST_WALKER, 2).setWeight(25))
                .add(enchantedBook(Enchantments.KNOCKBACK, 2).setWeight(25))
                .add(enchantedBook(Enchantments.LOOTING, 2).setWeight(25))
                .add(enchantedBook(Enchantments.POWER, 2).setWeight(25))
                .add(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 2).setWeight(25))
                .add(enchantedBook(Enchantments.PROTECTION, 2).setWeight(25))
                .add(enchantedBook(Enchantments.PUNCH, 2).setWeight(25))
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
                .add(enchantedBook(Enchantments.EFFICIENCY, 1).setWeight(50))
                .add(enchantedBook(Enchantments.FEATHER_FALLING, 1).setWeight(50))
                .add(enchantedBook(Enchantments.FIRE_ASPECT, 1).setWeight(50))
                .add(enchantedBook(Enchantments.FIRE_PROTECTION, 1).setWeight(50))
                .add(enchantedBook(Enchantments.FLAME, 1).setWeight(50))
                .add(enchantedBook(Enchantments.FROST_WALKER, 1).setWeight(50))
                .add(enchantedBook(Enchantments.KNOCKBACK, 1).setWeight(50))
                .add(enchantedBook(Enchantments.LOOTING, 1).setWeight(50))
                .add(enchantedBook(Enchantments.POWER, 1).setWeight(50))
                .add(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 1).setWeight(50))
                .add(enchantedBook(Enchantments.PROTECTION, 1).setWeight(50))
                .add(enchantedBook(Enchantments.PUNCH, 1).setWeight(50))
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
                .add(enchantedBook(Enchantments.EFFICIENCY, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FEATHER_FALLING, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FIRE_ASPECT, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FIRE_PROTECTION, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FLAME, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FROST_WALKER, 2).setWeight(25))
                .add(enchantedBook(Enchantments.KNOCKBACK, 2).setWeight(25))
                .add(enchantedBook(Enchantments.LOOTING, 2).setWeight(25))
                .add(enchantedBook(Enchantments.POWER, 2).setWeight(25))
                .add(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 2).setWeight(25))
                .add(enchantedBook(Enchantments.PROTECTION, 2).setWeight(25))
                .add(enchantedBook(Enchantments.PUNCH, 2).setWeight(25))
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
                .add(enchantedBook(Enchantments.EFFICIENCY, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FEATHER_FALLING, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FIRE_ASPECT, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FIRE_PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FLAME, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FROST_WALKER, 3).setWeight(15))
                .add(enchantedBook(Enchantments.KNOCKBACK, 3).setWeight(15))
                .add(enchantedBook(Enchantments.LOOTING, 3).setWeight(15))
                .add(enchantedBook(Enchantments.POWER, 3).setWeight(15))
                .add(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.PUNCH, 3).setWeight(15))
                .add(enchantedBook(Enchantments.QUICK_CHARGE, 3).setWeight(15))
                .add(enchantedBook(Enchantments.RESPIRATION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.SHARPNESS, 3).setWeight(15))
                .add(enchantedBook(Enchantments.SMITE, 3).setWeight(15))
                .add(enchantedBook(Enchantments.SWEEPING_EDGE, 3).setWeight(15))
                .add(enchantedBook(Enchantments.UNBREAKING, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FORTUNE, 1).setWeight(1))
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
                .add(enchantedBook(Enchantments.EFFICIENCY, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FEATHER_FALLING, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FIRE_ASPECT, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FIRE_PROTECTION, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FLAME, 2).setWeight(25))
                .add(enchantedBook(Enchantments.FROST_WALKER, 2).setWeight(25))
                .add(enchantedBook(Enchantments.KNOCKBACK, 2).setWeight(25))
                .add(enchantedBook(Enchantments.LOOTING, 2).setWeight(25))
                .add(enchantedBook(Enchantments.POWER, 2).setWeight(25))
                .add(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 2).setWeight(25))
                .add(enchantedBook(Enchantments.PROTECTION, 2).setWeight(25))
                .add(enchantedBook(Enchantments.PUNCH, 2).setWeight(25))
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
                .add(enchantedBook(Enchantments.EFFICIENCY, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FEATHER_FALLING, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FIRE_ASPECT, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FIRE_PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FLAME, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FROST_WALKER, 3).setWeight(15))
                .add(enchantedBook(Enchantments.KNOCKBACK, 3).setWeight(15))
                .add(enchantedBook(Enchantments.LOOTING, 3).setWeight(15))
                .add(enchantedBook(Enchantments.POWER, 3).setWeight(15))
                .add(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.PUNCH, 3).setWeight(15))
                .add(enchantedBook(Enchantments.RESPIRATION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.QUICK_CHARGE, 3).setWeight(15))
                .add(enchantedBook(Enchantments.SHARPNESS, 3).setWeight(15))
                .add(enchantedBook(Enchantments.SMITE, 3).setWeight(15))
                .add(enchantedBook(Enchantments.SWEEPING_EDGE, 3).setWeight(15))
                .add(enchantedBook(Enchantments.UNBREAKING, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FORTUNE, 1).setWeight(1))
                .add(enchantedBook(ModEnchants.raiderDamage, 1).setWeight(15))
                // plus new level 4 enchants
                .add(enchantedBook(Enchantments.AQUA_AFFINITY, 4).setWeight(5))
                .add(enchantedBook(Enchantments.BANE_OF_ARTHROPODS, 4).setWeight(5))
                .add(enchantedBook(Enchantments.BLAST_PROTECTION, 4).setWeight(5))
                .add(enchantedBook(Enchantments.DEPTH_STRIDER, 4).setWeight(5))
                .add(enchantedBook(Enchantments.EFFICIENCY, 4).setWeight(5))
                .add(enchantedBook(Enchantments.FEATHER_FALLING, 4).setWeight(5))
                .add(enchantedBook(Enchantments.FIRE_ASPECT, 4).setWeight(5))
                .add(enchantedBook(Enchantments.FIRE_PROTECTION, 4).setWeight(5))
                .add(enchantedBook(Enchantments.FLAME, 4).setWeight(5))
                .add(enchantedBook(Enchantments.FROST_WALKER, 4).setWeight(5))
                .add(enchantedBook(Enchantments.INFINITY, 4).setWeight(5))
                .add(enchantedBook(Enchantments.KNOCKBACK, 4).setWeight(5))
                .add(enchantedBook(Enchantments.LOOTING, 4).setWeight(5))
                .add(enchantedBook(Enchantments.POWER, 4).setWeight(5))
                .add(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 4).setWeight(5))
                .add(enchantedBook(Enchantments.PUNCH, 4).setWeight(5))
                .add(enchantedBook(Enchantments.QUICK_CHARGE, 4).setWeight(5))
                .add(enchantedBook(Enchantments.RESPIRATION, 4).setWeight(5))
                .add(enchantedBook(Enchantments.SHARPNESS, 4).setWeight(5))
                .add(enchantedBook(Enchantments.SMITE, 4).setWeight(5))
                .add(enchantedBook(Enchantments.SWEEPING_EDGE, 4).setWeight(5))
                .add(enchantedBook(Enchantments.UNBREAKING, 4).setWeight(5))
                .add(enchantedBook(Enchantments.FORTUNE, 2).setWeight(1))
        ));

        // building level 5
        levels.add(LootTable.lootTable().withPool(LootPool.lootPool()
                // no more level 1 or 2 enchants
                // but still the level 3 enchants
                .add(enchantedBook(Enchantments.AQUA_AFFINITY, 3).setWeight(15))
                .add(enchantedBook(Enchantments.BANE_OF_ARTHROPODS, 3).setWeight(15))
                .add(enchantedBook(Enchantments.BLAST_PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.DEPTH_STRIDER, 3).setWeight(15))
                .add(enchantedBook(Enchantments.EFFICIENCY, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FEATHER_FALLING, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FIRE_ASPECT, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FIRE_PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FLAME, 3).setWeight(15))
                .add(enchantedBook(Enchantments.FROST_WALKER, 3).setWeight(15))
                .add(enchantedBook(Enchantments.KNOCKBACK, 3).setWeight(15))
                .add(enchantedBook(Enchantments.LOOTING, 3).setWeight(15))
                .add(enchantedBook(Enchantments.POWER, 3).setWeight(15))
                .add(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.PROTECTION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.PUNCH, 3).setWeight(15))
                .add(enchantedBook(Enchantments.QUICK_CHARGE, 3).setWeight(15))
                .add(enchantedBook(Enchantments.RESPIRATION, 3).setWeight(15))
                .add(enchantedBook(Enchantments.SHARPNESS, 3).setWeight(15))
                .add(enchantedBook(Enchantments.SMITE, 3).setWeight(15))
                .add(enchantedBook(Enchantments.SWEEPING_EDGE, 3).setWeight(15))
                .add(enchantedBook(Enchantments.UNBREAKING, 3).setWeight(15))
                .add(enchantedBook(ModEnchants.raiderDamage, 1).setWeight(15))
                .add(enchantedBook(Enchantments.FORTUNE, 1).setWeight(1))
                // plus level 4 enchants
                .add(enchantedBook(Enchantments.AQUA_AFFINITY, 4).setWeight(5))
                .add(enchantedBook(Enchantments.BANE_OF_ARTHROPODS, 4).setWeight(5))
                .add(enchantedBook(Enchantments.BLAST_PROTECTION, 4).setWeight(5))
                .add(enchantedBook(Enchantments.DEPTH_STRIDER, 4).setWeight(5))
                .add(enchantedBook(Enchantments.EFFICIENCY, 4).setWeight(5))
                .add(enchantedBook(Enchantments.FEATHER_FALLING, 4).setWeight(5))
                .add(enchantedBook(Enchantments.FIRE_ASPECT, 4).setWeight(5))
                .add(enchantedBook(Enchantments.FIRE_PROTECTION, 4).setWeight(5))
                .add(enchantedBook(Enchantments.FLAME, 4).setWeight(5))
                .add(enchantedBook(Enchantments.FROST_WALKER, 4).setWeight(5))
                .add(enchantedBook(Enchantments.INFINITY, 1).setWeight(5))
                .add(enchantedBook(Enchantments.KNOCKBACK, 4).setWeight(5))
                .add(enchantedBook(Enchantments.LOOTING, 4).setWeight(5))
                .add(enchantedBook(Enchantments.POWER, 4).setWeight(5))
                .add(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 4).setWeight(5))
                .add(enchantedBook(Enchantments.PUNCH, 4).setWeight(5))
                .add(enchantedBook(Enchantments.QUICK_CHARGE, 4).setWeight(5))
                .add(enchantedBook(Enchantments.RESPIRATION, 4).setWeight(5))
                .add(enchantedBook(Enchantments.SHARPNESS, 4).setWeight(5))
                .add(enchantedBook(Enchantments.SMITE, 4).setWeight(5))
                .add(enchantedBook(Enchantments.SWEEPING_EDGE, 4).setWeight(5))
                .add(enchantedBook(Enchantments.UNBREAKING, 4).setWeight(5))
                .add(enchantedBook(Enchantments.FORTUNE, 2).setWeight(1))
                // plus new level 5 enchants
                .add(enchantedBook(Enchantments.AQUA_AFFINITY, 5).setWeight(1))
                .add(enchantedBook(Enchantments.BANE_OF_ARTHROPODS, 5).setWeight(1))
                .add(enchantedBook(Enchantments.BLAST_PROTECTION, 5).setWeight(1))
                .add(enchantedBook(Enchantments.DEPTH_STRIDER, 5).setWeight(1))
                .add(enchantedBook(Enchantments.EFFICIENCY, 5).setWeight(1))
                .add(enchantedBook(Enchantments.FEATHER_FALLING, 5).setWeight(1))
                .add(enchantedBook(Enchantments.FIRE_ASPECT, 5).setWeight(1))
                .add(enchantedBook(Enchantments.FIRE_PROTECTION, 5).setWeight(1))
                .add(enchantedBook(Enchantments.FLAME, 5).setWeight(1))
                .add(enchantedBook(Enchantments.FROST_WALKER, 5).setWeight(1))
                .add(enchantedBook(Enchantments.INFINITY, 1).setWeight(1))
                .add(enchantedBook(Enchantments.KNOCKBACK, 5).setWeight(1))
                .add(enchantedBook(Enchantments.LOOTING, 5).setWeight(1))
                .add(enchantedBook(Enchantments.MENDING, 1).setWeight(1))
                .add(enchantedBook(Enchantments.MULTISHOT, 1).setWeight(1))
                .add(enchantedBook(Enchantments.POWER, 5).setWeight(1))
                .add(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 5).setWeight(1))
                .add(enchantedBook(Enchantments.PROTECTION, 5).setWeight(1))
                .add(enchantedBook(Enchantments.PUNCH, 5).setWeight(1))
                .add(enchantedBook(Enchantments.QUICK_CHARGE, 5).setWeight(1))
                .add(enchantedBook(Enchantments.RESPIRATION, 5).setWeight(1))
                .add(enchantedBook(Enchantments.SHARPNESS, 5).setWeight(1))
                .add(enchantedBook(Enchantments.SILK_TOUCH, 1).setWeight(1))
                .add(enchantedBook(Enchantments.SMITE, 5).setWeight(1))
                .add(enchantedBook(Enchantments.SWEEPING_EDGE, 5).setWeight(1))
                .add(enchantedBook(Enchantments.UNBREAKING, 5).setWeight(1))
                .add(enchantedBook(Enchantments.FORTUNE, 3).setWeight(1))
                .add(enchantedBook(ModEnchants.raiderDamage, 2).setWeight(1))
        ));

        return CompletableFuture.completedFuture(provider);
    }

    @NotNull
    private LootPoolSingletonContainer.Builder<?> enchantedBook(final ResourceKey<Enchantment> key, final int level)
    {
        final Holder<Enchantment> enchantment = provider.holderOrThrow(key);
        final ItemStack stack = new ItemStack(Items.ENCHANTED_BOOK);
        stack.enchant(enchantment, level);
        return SimpleLootTableProvider.itemStack(stack);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "EnchanterCraftingProvider";
    }

    @Override
    protected void registerRecipes(@NotNull final Consumer<CustomRecipeBuilder> consumer)
    {
        final List<ItemStorage> tome = Collections.singletonList(new ItemStorage(
                new ItemStack(ModItems.ancientTome), true, true));

        for (int buildingLevel = 1; buildingLevel <= MAX_BUILDING_LEVEL; ++buildingLevel)
        {
            recipe(ENCHANTER, MODULE_CUSTOM, "tome" + buildingLevel)
                    .minBuildingLevel(buildingLevel)
                    .maxBuildingLevel(buildingLevel)
                    .inputs(tome)
                    .secondaryOutputs(Collections.singletonList(new ItemStack(Items.ENCHANTED_BOOK)))
                    .lootTable(new ResourceLocation(MOD_ID, "recipes/" + ENCHANTER + buildingLevel))
                    .build(consumer);
        }

        recipe(ENCHANTER, MODULE_CUSTOM, "scroll_tp")
                .inputs(List.of(new ItemStorage(new ItemStack(Items.PAPER, 3)),
                        new ItemStorage(new ItemStack(Items.COMPASS)),
                        new ItemStorage(new ItemStack(com.ldtteam.structurize.items.ModItems.buildTool.get()))))
                .result(new ItemStack(ModItems.scrollColonyTP, 3))
                .showTooltip(true)
                .build(consumer);

        recipe(ENCHANTER, MODULE_CUSTOM, "scroll_area_tp")
                .inputs(List.of(new ItemStorage(new ItemStack(ModItems.scrollColonyTP, 3))))
                .result(new ItemStack(ModItems.scrollColonyAreaTP))
                .minBuildingLevel(2)
                .showTooltip(true)
                .build(consumer);

        recipe(ENCHANTER, MODULE_CUSTOM, "scroll_guard_help")
                .inputs(List.of(new ItemStorage(new ItemStack(ModItems.scrollColonyTP)),
                        new ItemStorage(new ItemStack(Items.LAPIS_LAZULI, 5)),
                        new ItemStorage(new ItemStack(Items.ENDER_PEARL)),
                        new ItemStorage(new ItemStack(Items.PAPER))))
                .result(new ItemStack(ModItems.scrollGuardHelp, 2))
                .minBuildingLevel(3)
                .minResearchId(ResearchConstants.MORE_SCROLLS)
                .showTooltip(true)
                .build(consumer);

        recipe(ENCHANTER, MODULE_CUSTOM, "scroll_highlight")
                .inputs(List.of(new ItemStorage(new ItemStack(ModItems.scrollColonyTP, 3)),
                        new ItemStorage(new ItemStack(Items.GLOWSTONE_DUST, 6)),
                        new ItemStorage(new ItemStack(Items.PAPER, 2))))
                .result(new ItemStack(ModItems.scrollHighLight, 5))
                .minBuildingLevel(3)
                .minResearchId(ResearchConstants.MORE_SCROLLS)
                .showTooltip(true)
                .build(consumer);
    }

    @NotNull
    @Override
    protected List<LootTableProvider.SubProviderEntry> registerTables()
    {
        return List.of(new LootTableProvider.SubProviderEntry(provider -> builder ->
        {
            for (int i = 0; i < levels.size(); i++)
            {
                final int buildingLevel = i + 1;
                builder.accept(table(new ResourceLocation(MOD_ID, "recipes/" + ENCHANTER + buildingLevel)), levels.get(i));
            }
        }, LootContextParamSets.ALL_PARAMS));
    }
}
