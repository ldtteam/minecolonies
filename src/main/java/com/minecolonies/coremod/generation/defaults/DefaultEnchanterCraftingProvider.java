package com.minecolonies.coremod.generation.defaults;

import com.ldtteam.datagenerators.loot_table.LootTableJson;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.enchants.ModEnchants;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.coremod.generation.CustomRecipeProvider;
import com.minecolonies.coremod.generation.LootTableBuilder;
import com.minecolonies.coremod.generation.LootTableJsonProvider;
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
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

public class DefaultEnchanterCraftingProvider implements IDataProvider
{
    private static final int MAX_BUILDING_LEVEL = 5;

    private final EnchanterRecipeProvider recipeProvider;
    private final EnchanterLootTableProvider lootTableProvider;
    private final List<LootTableJson> levels;

    public DefaultEnchanterCraftingProvider(@NotNull final DataGenerator generatorIn)
    {
        levels = new ArrayList<>();

        // building level 1
        levels.add(new LootTableBuilder()
                .item(enchantedBook(Enchantments.AQUA_AFFINITY, 1), 50)
                .item(enchantedBook(Enchantments.BANE_OF_ARTHROPODS, 1), 50)
                .item(enchantedBook(Enchantments.BLAST_PROTECTION, 1), 50)
                .item(enchantedBook(Enchantments.DEPTH_STRIDER, 1), 50)
                .item(enchantedBook(Enchantments.EFFICIENCY, 1), 50)
                .item(enchantedBook(Enchantments.FEATHER_FALLING, 1), 50)
                .item(enchantedBook(Enchantments.FIRE_ASPECT, 1), 50)
                .item(enchantedBook(Enchantments.FIRE_PROTECTION, 1), 50)
                .item(enchantedBook(Enchantments.FLAME, 1), 50)
                .item(enchantedBook(Enchantments.FROST_WALKER, 1), 50)
                .item(enchantedBook(Enchantments.KNOCKBACK, 1), 50)
                .item(enchantedBook(Enchantments.LOOTING, 1), 50)
                .item(enchantedBook(Enchantments.POWER, 1), 50)
                .item(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 1), 50)
                .item(enchantedBook(Enchantments.PROTECTION, 1), 50)
                .item(enchantedBook(Enchantments.PUNCH, 1), 50)
                .item(enchantedBook(Enchantments.QUICK_CHARGE, 1), 50)
                .item(enchantedBook(Enchantments.RESPIRATION, 1), 50)
                .item(enchantedBook(Enchantments.SHARPNESS, 1), 50)
                .item(enchantedBook(Enchantments.SMITE, 1), 50)
                .item(enchantedBook(Enchantments.SWEEPING, 1), 50)
                .item(enchantedBook(Enchantments.UNBREAKING, 1), 50)
                .build());

        // building level 2
        levels.add(new LootTableBuilder()
                // also the level 1 enchants
                .item(enchantedBook(Enchantments.AQUA_AFFINITY, 1), 50)
                .item(enchantedBook(Enchantments.BANE_OF_ARTHROPODS, 1), 50)
                .item(enchantedBook(Enchantments.BLAST_PROTECTION, 1), 50)
                .item(enchantedBook(Enchantments.DEPTH_STRIDER, 1), 50)
                .item(enchantedBook(Enchantments.EFFICIENCY, 1), 50)
                .item(enchantedBook(Enchantments.FEATHER_FALLING, 1), 50)
                .item(enchantedBook(Enchantments.FIRE_ASPECT, 1), 50)
                .item(enchantedBook(Enchantments.FIRE_PROTECTION, 1), 50)
                .item(enchantedBook(Enchantments.FLAME, 1), 50)
                .item(enchantedBook(Enchantments.FROST_WALKER, 1), 50)
                .item(enchantedBook(Enchantments.KNOCKBACK, 1), 50)
                .item(enchantedBook(Enchantments.LOOTING, 1), 50)
                .item(enchantedBook(Enchantments.POWER, 1), 50)
                .item(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 1), 50)
                .item(enchantedBook(Enchantments.PROTECTION, 1), 50)
                .item(enchantedBook(Enchantments.PUNCH, 1), 50)
                .item(enchantedBook(Enchantments.QUICK_CHARGE, 1), 50)
                .item(enchantedBook(Enchantments.RESPIRATION, 1), 50)
                .item(enchantedBook(Enchantments.SHARPNESS, 1), 50)
                .item(enchantedBook(Enchantments.SMITE, 1), 50)
                .item(enchantedBook(Enchantments.SWEEPING, 1), 50)
                .item(enchantedBook(Enchantments.UNBREAKING, 1), 50)
                // plus new level 2 enchants
                .item(enchantedBook(Enchantments.AQUA_AFFINITY, 2), 25)
                .item(enchantedBook(Enchantments.BANE_OF_ARTHROPODS, 2), 25)
                .item(enchantedBook(Enchantments.BLAST_PROTECTION, 2), 25)
                .item(enchantedBook(Enchantments.DEPTH_STRIDER, 2), 25)
                .item(enchantedBook(Enchantments.EFFICIENCY, 2), 25)
                .item(enchantedBook(Enchantments.FEATHER_FALLING, 2), 25)
                .item(enchantedBook(Enchantments.FIRE_ASPECT, 2), 25)
                .item(enchantedBook(Enchantments.FIRE_PROTECTION, 2), 25)
                .item(enchantedBook(Enchantments.FLAME, 2), 25)
                .item(enchantedBook(Enchantments.FROST_WALKER, 2), 25)
                .item(enchantedBook(Enchantments.KNOCKBACK, 2), 25)
                .item(enchantedBook(Enchantments.LOOTING, 2), 25)
                .item(enchantedBook(Enchantments.POWER, 2), 25)
                .item(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 2), 25)
                .item(enchantedBook(Enchantments.PROTECTION, 2), 25)
                .item(enchantedBook(Enchantments.PUNCH, 2), 25)
                .item(enchantedBook(Enchantments.QUICK_CHARGE, 2), 25)
                .item(enchantedBook(Enchantments.RESPIRATION, 2), 25)
                .item(enchantedBook(Enchantments.SHARPNESS, 2), 25)
                .item(enchantedBook(Enchantments.SMITE, 2), 25)
                .item(enchantedBook(Enchantments.SWEEPING, 2), 25)
                .item(enchantedBook(Enchantments.UNBREAKING, 2), 25)
                .build());

        // building level 3
        levels.add(new LootTableBuilder()
                // also the level 1 enchants
                .item(enchantedBook(Enchantments.AQUA_AFFINITY, 1), 50)
                .item(enchantedBook(Enchantments.BANE_OF_ARTHROPODS, 1), 50)
                .item(enchantedBook(Enchantments.BLAST_PROTECTION, 1), 50)
                .item(enchantedBook(Enchantments.DEPTH_STRIDER, 1), 50)
                .item(enchantedBook(Enchantments.EFFICIENCY, 1), 50)
                .item(enchantedBook(Enchantments.FEATHER_FALLING, 1), 50)
                .item(enchantedBook(Enchantments.FIRE_ASPECT, 1), 50)
                .item(enchantedBook(Enchantments.FIRE_PROTECTION, 1), 50)
                .item(enchantedBook(Enchantments.FLAME, 1), 50)
                .item(enchantedBook(Enchantments.FROST_WALKER, 1), 50)
                .item(enchantedBook(Enchantments.KNOCKBACK, 1), 50)
                .item(enchantedBook(Enchantments.LOOTING, 1), 50)
                .item(enchantedBook(Enchantments.POWER, 1), 50)
                .item(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 1), 50)
                .item(enchantedBook(Enchantments.PROTECTION, 1), 50)
                .item(enchantedBook(Enchantments.PUNCH, 1), 50)
                .item(enchantedBook(Enchantments.QUICK_CHARGE, 1), 50)
                .item(enchantedBook(Enchantments.RESPIRATION, 1), 50)
                .item(enchantedBook(Enchantments.SHARPNESS, 1), 50)
                .item(enchantedBook(Enchantments.SMITE, 1), 50)
                .item(enchantedBook(Enchantments.SWEEPING, 1), 50)
                .item(enchantedBook(Enchantments.UNBREAKING, 1), 50)
                // also the level 2 enchants
                .item(enchantedBook(Enchantments.AQUA_AFFINITY, 2), 25)
                .item(enchantedBook(Enchantments.BANE_OF_ARTHROPODS, 2), 25)
                .item(enchantedBook(Enchantments.BLAST_PROTECTION, 2), 25)
                .item(enchantedBook(Enchantments.DEPTH_STRIDER, 2), 25)
                .item(enchantedBook(Enchantments.EFFICIENCY, 2), 25)
                .item(enchantedBook(Enchantments.FEATHER_FALLING, 2), 25)
                .item(enchantedBook(Enchantments.FIRE_ASPECT, 2), 25)
                .item(enchantedBook(Enchantments.FIRE_PROTECTION, 2), 25)
                .item(enchantedBook(Enchantments.FLAME, 2), 25)
                .item(enchantedBook(Enchantments.FROST_WALKER, 2), 25)
                .item(enchantedBook(Enchantments.KNOCKBACK, 2), 25)
                .item(enchantedBook(Enchantments.LOOTING, 2), 25)
                .item(enchantedBook(Enchantments.POWER, 2), 25)
                .item(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 2), 25)
                .item(enchantedBook(Enchantments.PROTECTION, 2), 25)
                .item(enchantedBook(Enchantments.PUNCH, 2), 25)
                .item(enchantedBook(Enchantments.QUICK_CHARGE, 2), 25)
                .item(enchantedBook(Enchantments.RESPIRATION, 2), 25)
                .item(enchantedBook(Enchantments.SHARPNESS, 2), 25)
                .item(enchantedBook(Enchantments.SMITE, 2), 25)
                .item(enchantedBook(Enchantments.SWEEPING, 2), 25)
                .item(enchantedBook(Enchantments.UNBREAKING, 2), 25)
                // plus new level 3 enchants
                .item(enchantedBook(Enchantments.AQUA_AFFINITY, 3), 15)
                .item(enchantedBook(Enchantments.BANE_OF_ARTHROPODS, 3), 15)
                .item(enchantedBook(Enchantments.BLAST_PROTECTION, 3), 15)
                .item(enchantedBook(Enchantments.DEPTH_STRIDER, 3), 15)
                .item(enchantedBook(Enchantments.EFFICIENCY, 3), 15)
                .item(enchantedBook(Enchantments.FEATHER_FALLING, 3), 15)
                .item(enchantedBook(Enchantments.FIRE_ASPECT, 3), 15)
                .item(enchantedBook(Enchantments.FIRE_PROTECTION, 3), 15)
                .item(enchantedBook(Enchantments.FLAME, 3), 15)
                .item(enchantedBook(Enchantments.FROST_WALKER, 3), 15)
                .item(enchantedBook(Enchantments.KNOCKBACK, 3), 15)
                .item(enchantedBook(Enchantments.LOOTING, 3), 15)
                .item(enchantedBook(Enchantments.POWER, 3), 15)
                .item(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 3), 15)
                .item(enchantedBook(Enchantments.PROTECTION, 3), 15)
                .item(enchantedBook(Enchantments.PUNCH, 3), 15)
                .item(enchantedBook(Enchantments.QUICK_CHARGE, 3), 15)
                .item(enchantedBook(Enchantments.RESPIRATION, 3), 15)
                .item(enchantedBook(Enchantments.SHARPNESS, 3), 15)
                .item(enchantedBook(Enchantments.SMITE, 3), 15)
                .item(enchantedBook(Enchantments.SWEEPING, 3), 15)
                .item(enchantedBook(Enchantments.UNBREAKING, 3), 15)
                .item(enchantedBook(Enchantments.FORTUNE, 1), 1)
                .item(enchantedBook(ModEnchants.raiderDamage, 1), 15)
                .build());

        // building level 4
        levels.add(new LootTableBuilder()
                // no more level 1 enchants
                // but still the level 2 enchants
                .item(enchantedBook(Enchantments.AQUA_AFFINITY, 2), 25)
                .item(enchantedBook(Enchantments.BANE_OF_ARTHROPODS, 2), 25)
                .item(enchantedBook(Enchantments.BLAST_PROTECTION, 2), 25)
                .item(enchantedBook(Enchantments.DEPTH_STRIDER, 2), 25)
                .item(enchantedBook(Enchantments.EFFICIENCY, 2), 25)
                .item(enchantedBook(Enchantments.FEATHER_FALLING, 2), 25)
                .item(enchantedBook(Enchantments.FIRE_ASPECT, 2), 25)
                .item(enchantedBook(Enchantments.FIRE_PROTECTION, 2), 25)
                .item(enchantedBook(Enchantments.FLAME, 2), 25)
                .item(enchantedBook(Enchantments.FROST_WALKER, 2), 25)
                .item(enchantedBook(Enchantments.KNOCKBACK, 2), 25)
                .item(enchantedBook(Enchantments.LOOTING, 2), 25)
                .item(enchantedBook(Enchantments.POWER, 2), 25)
                .item(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 2), 25)
                .item(enchantedBook(Enchantments.PROTECTION, 2), 25)
                .item(enchantedBook(Enchantments.PUNCH, 2), 25)
                .item(enchantedBook(Enchantments.QUICK_CHARGE, 2), 25)
                .item(enchantedBook(Enchantments.RESPIRATION, 2), 25)
                .item(enchantedBook(Enchantments.SHARPNESS, 2), 25)
                .item(enchantedBook(Enchantments.SMITE, 2), 25)
                .item(enchantedBook(Enchantments.SWEEPING, 2), 25)
                .item(enchantedBook(Enchantments.UNBREAKING, 2), 25)
                // plus level 3 enchants
                .item(enchantedBook(Enchantments.AQUA_AFFINITY, 3), 15)
                .item(enchantedBook(Enchantments.BANE_OF_ARTHROPODS, 3), 15)
                .item(enchantedBook(Enchantments.BLAST_PROTECTION, 3), 15)
                .item(enchantedBook(Enchantments.DEPTH_STRIDER, 3), 15)
                .item(enchantedBook(Enchantments.EFFICIENCY, 3), 15)
                .item(enchantedBook(Enchantments.FEATHER_FALLING, 3), 15)
                .item(enchantedBook(Enchantments.FIRE_ASPECT, 3), 15)
                .item(enchantedBook(Enchantments.FIRE_PROTECTION, 3), 15)
                .item(enchantedBook(Enchantments.FLAME, 3), 15)
                .item(enchantedBook(Enchantments.FROST_WALKER, 3), 15)
                .item(enchantedBook(Enchantments.KNOCKBACK, 3), 15)
                .item(enchantedBook(Enchantments.LOOTING, 3), 15)
                .item(enchantedBook(Enchantments.POWER, 3), 15)
                .item(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 3), 15)
                .item(enchantedBook(Enchantments.PROTECTION, 3), 15)
                .item(enchantedBook(Enchantments.PUNCH, 3), 15)
                .item(enchantedBook(Enchantments.RESPIRATION, 3), 15)
                .item(enchantedBook(Enchantments.QUICK_CHARGE, 3), 15)
                .item(enchantedBook(Enchantments.SHARPNESS, 3), 15)
                .item(enchantedBook(Enchantments.SMITE, 3), 15)
                .item(enchantedBook(Enchantments.SWEEPING, 3), 15)
                .item(enchantedBook(Enchantments.UNBREAKING, 3), 15)
                .item(enchantedBook(Enchantments.FORTUNE, 1), 1)
                .item(enchantedBook(ModEnchants.raiderDamage, 1), 15)
                // plus new level 4 enchants
                .item(enchantedBook(Enchantments.AQUA_AFFINITY, 4), 5)
                .item(enchantedBook(Enchantments.BANE_OF_ARTHROPODS, 4), 5)
                .item(enchantedBook(Enchantments.BLAST_PROTECTION, 4), 5)
                .item(enchantedBook(Enchantments.DEPTH_STRIDER, 4), 5)
                .item(enchantedBook(Enchantments.EFFICIENCY, 4), 5)
                .item(enchantedBook(Enchantments.FEATHER_FALLING, 4), 5)
                .item(enchantedBook(Enchantments.FIRE_ASPECT, 4), 5)
                .item(enchantedBook(Enchantments.FIRE_PROTECTION, 4), 5)
                .item(enchantedBook(Enchantments.FLAME, 4), 5)
                .item(enchantedBook(Enchantments.FROST_WALKER, 4), 5)
                .item(enchantedBook(Enchantments.INFINITY, 4), 5)
                .item(enchantedBook(Enchantments.KNOCKBACK, 4), 5)
                .item(enchantedBook(Enchantments.LOOTING, 4), 5)
                .item(enchantedBook(Enchantments.POWER, 4), 5)
                .item(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 4), 5)
                .item(enchantedBook(Enchantments.PUNCH, 4), 5)
                .item(enchantedBook(Enchantments.QUICK_CHARGE, 4), 5)
                .item(enchantedBook(Enchantments.RESPIRATION, 4), 5)
                .item(enchantedBook(Enchantments.SHARPNESS, 4), 5)
                .item(enchantedBook(Enchantments.SMITE, 4), 5)
                .item(enchantedBook(Enchantments.SWEEPING, 4), 5)
                .item(enchantedBook(Enchantments.UNBREAKING, 4), 5)
                .item(enchantedBook(Enchantments.FORTUNE, 2), 1)
                .build());

        // building level 5
        levels.add(new LootTableBuilder()
                // no more level 1 or 2 enchants
                // but still the level 3 enchants
                .item(enchantedBook(Enchantments.AQUA_AFFINITY, 3), 15)
                .item(enchantedBook(Enchantments.BANE_OF_ARTHROPODS, 3), 15)
                .item(enchantedBook(Enchantments.BLAST_PROTECTION, 3), 15)
                .item(enchantedBook(Enchantments.DEPTH_STRIDER, 3), 15)
                .item(enchantedBook(Enchantments.EFFICIENCY, 3), 15)
                .item(enchantedBook(Enchantments.FEATHER_FALLING, 3), 15)
                .item(enchantedBook(Enchantments.FIRE_ASPECT, 3), 15)
                .item(enchantedBook(Enchantments.FIRE_PROTECTION, 3), 15)
                .item(enchantedBook(Enchantments.FLAME, 3), 15)
                .item(enchantedBook(Enchantments.FROST_WALKER, 3), 15)
                .item(enchantedBook(Enchantments.KNOCKBACK, 3), 15)
                .item(enchantedBook(Enchantments.LOOTING, 3), 15)
                .item(enchantedBook(Enchantments.POWER, 3), 15)
                .item(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 3), 15)
                .item(enchantedBook(Enchantments.PROTECTION, 3), 15)
                .item(enchantedBook(Enchantments.PUNCH, 3), 15)
                .item(enchantedBook(Enchantments.QUICK_CHARGE, 3), 15)
                .item(enchantedBook(Enchantments.RESPIRATION, 3), 15)
                .item(enchantedBook(Enchantments.SHARPNESS, 3), 15)
                .item(enchantedBook(Enchantments.SMITE, 3), 15)
                .item(enchantedBook(Enchantments.SWEEPING, 3), 15)
                .item(enchantedBook(Enchantments.UNBREAKING, 3), 15)
                .item(enchantedBook(ModEnchants.raiderDamage, 1), 15)
                .item(enchantedBook(Enchantments.FORTUNE, 1), 1)
                // plus level 4 enchants
                .item(enchantedBook(Enchantments.AQUA_AFFINITY, 4), 5)
                .item(enchantedBook(Enchantments.BANE_OF_ARTHROPODS, 4), 5)
                .item(enchantedBook(Enchantments.BLAST_PROTECTION, 4), 5)
                .item(enchantedBook(Enchantments.DEPTH_STRIDER, 4), 5)
                .item(enchantedBook(Enchantments.EFFICIENCY, 4), 5)
                .item(enchantedBook(Enchantments.FEATHER_FALLING, 4), 5)
                .item(enchantedBook(Enchantments.FIRE_ASPECT, 4), 5)
                .item(enchantedBook(Enchantments.FIRE_PROTECTION, 4), 5)
                .item(enchantedBook(Enchantments.FLAME, 4), 5)
                .item(enchantedBook(Enchantments.FROST_WALKER, 4), 5)
                .item(enchantedBook(Enchantments.INFINITY, 1), 5)
                .item(enchantedBook(Enchantments.KNOCKBACK, 4), 5)
                .item(enchantedBook(Enchantments.LOOTING, 4), 5)
                .item(enchantedBook(Enchantments.POWER, 4), 5)
                .item(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 4), 5)
                .item(enchantedBook(Enchantments.PUNCH, 4), 5)
                .item(enchantedBook(Enchantments.QUICK_CHARGE, 4), 5)
                .item(enchantedBook(Enchantments.RESPIRATION, 4), 5)
                .item(enchantedBook(Enchantments.SHARPNESS, 4), 5)
                .item(enchantedBook(Enchantments.SMITE, 4), 5)
                .item(enchantedBook(Enchantments.SWEEPING, 4), 5)
                .item(enchantedBook(Enchantments.UNBREAKING, 4), 5)
                .item(enchantedBook(Enchantments.FORTUNE, 2), 1)
                // plus new level 5 enchants
                .item(enchantedBook(Enchantments.AQUA_AFFINITY, 5), 1)
                .item(enchantedBook(Enchantments.BANE_OF_ARTHROPODS, 5), 1)
                .item(enchantedBook(Enchantments.BLAST_PROTECTION, 5), 1)
                .item(enchantedBook(Enchantments.DEPTH_STRIDER, 5), 1)
                .item(enchantedBook(Enchantments.EFFICIENCY, 5), 1)
                .item(enchantedBook(Enchantments.FEATHER_FALLING, 5), 1)
                .item(enchantedBook(Enchantments.FIRE_ASPECT, 5), 1)
                .item(enchantedBook(Enchantments.FIRE_PROTECTION, 5), 1)
                .item(enchantedBook(Enchantments.FLAME, 5), 1)
                .item(enchantedBook(Enchantments.FROST_WALKER, 5), 1)
                .item(enchantedBook(Enchantments.INFINITY, 1), 1)
                .item(enchantedBook(Enchantments.KNOCKBACK, 5), 1)
                .item(enchantedBook(Enchantments.LOOTING, 5), 1)
                .item(enchantedBook(Enchantments.MENDING, 1), 1)
                .item(enchantedBook(Enchantments.MULTISHOT, 1), 1)
                .item(enchantedBook(Enchantments.POWER, 5), 1)
                .item(enchantedBook(Enchantments.PROJECTILE_PROTECTION, 5), 1)
                .item(enchantedBook(Enchantments.PROTECTION, 5), 1)
                .item(enchantedBook(Enchantments.PUNCH, 5), 1)
                .item(enchantedBook(Enchantments.QUICK_CHARGE, 5), 1)
                .item(enchantedBook(Enchantments.RESPIRATION, 5), 1)
                .item(enchantedBook(Enchantments.SHARPNESS, 5), 1)
                .item(enchantedBook(Enchantments.SILK_TOUCH, 1), 1)
                .item(enchantedBook(Enchantments.SMITE, 5), 1)
                .item(enchantedBook(Enchantments.SWEEPING, 5), 1)
                .item(enchantedBook(Enchantments.UNBREAKING, 5), 1)
                .item(enchantedBook(Enchantments.FORTUNE, 3), 1)
                .item(enchantedBook(ModEnchants.raiderDamage, 2), 1)
                .build());

        recipeProvider = new EnchanterRecipeProvider(generatorIn);
        lootTableProvider = new EnchanterLootTableProvider(generatorIn);
    }

    @NotNull
    private ItemStack enchantedBook(@NotNull final Enchantment enchantment, final int level)
    {
        final ItemStack stack = new ItemStack(Items.ENCHANTED_BOOK);
        EnchantedBookItem.addEnchantment(stack, new EnchantmentData(enchantment, level));
        return stack;
    }

    @NotNull
    @Override
    public String getName()
    {
        return "EnchanterCraftingProvider";
    }

    @Override
    public void act(@NotNull final DirectoryCache cache) throws IOException
    {
        recipeProvider.act(cache);
        lootTableProvider.act(cache);
    }

    private static class EnchanterRecipeProvider extends CustomRecipeProvider
    {
        public EnchanterRecipeProvider(@NotNull final DataGenerator generatorIn)
        {
            super(generatorIn);
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

    private class EnchanterLootTableProvider extends LootTableJsonProvider
    {
        public EnchanterLootTableProvider(@NotNull final DataGenerator dataGeneratorIn)
        {
            super(dataGeneratorIn);
        }

        @Override
        protected Map<ResourceLocation, LootTableJson> getLootTables()
        {
            final Map<ResourceLocation, LootTableJson> tables = new HashMap<>();
            for (int i = 0; i < levels.size(); i++)
            {
                final int buildingLevel = i + 1;
                final LootTableJson lootTable = levels.get(i);

                tables.put(new ResourceLocation(MOD_ID, "recipes/enchanter" + buildingLevel), lootTable);
            }
            return tables;
        }
    }
}
