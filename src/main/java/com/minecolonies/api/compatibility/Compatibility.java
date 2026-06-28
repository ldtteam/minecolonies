package com.minecolonies.api.compatibility;

import com.minecolonies.api.compatibility.dynamictrees.DynamicTreeProxy;
import com.minecolonies.api.compatibility.resourcefulbees.IBeehiveCompat;
import com.minecolonies.api.compatibility.tinkers.SlimeTreeProxy;
import com.minecolonies.api.compatibility.tinkers.TinkersToolProxy;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * This class is to store the methods that call the methods to check for miscellaneous compatibility problems.
 */
public final class Compatibility
{

    private Compatibility()
    {
        throw new IllegalAccessError("Utility class");
    }

    public static IJeiProxy        jeiProxy           = new IJeiProxy() {};
    public static IBeehiveCompat   beeHiveCompat      = new IBeehiveCompat() {};
    public static SlimeTreeProxy   tinkersSlimeCompat = new SlimeTreeProxy();
    public static TinkersToolProxy tinkersCompat      = new TinkersToolProxy();
    public static DynamicTreeProxy dynamicTreesCompat = new DynamicTreeProxy();

    private static final Map<ResourceLocation, Map<ItemStorage, Integer>>         customEquipmentTypeLevels    = new HashMap<>();
    private static final Map<ResourceLocation, List<CustomEquipmentTypeFunction>> customEquipmentTypeFunctions = new HashMap<>();

    /**
     * This method checks if block is slime block.
     *
     * @param block the block.
     * @return if the block is a slime block.
     */
    public static boolean isSlimeBlock(@NotNull final Block block)
    {
        return tinkersSlimeCompat.checkForTinkersSlimeBlock(block);
    }

    /**
     * This method checks if block is slime leaf.
     *
     * @param block the block.
     * @return if the block is a slime leaf.
     */
    public static boolean isSlimeLeaf(@NotNull final Block block)
    {
        return tinkersSlimeCompat.checkForTinkersSlimeLeaves(block);
    }

    /**
     * This method checks if block is slime sapling.
     *
     * @param block the block.
     * @return if the block is a slime sapling.
     */
    public static boolean isSlimeSapling(@NotNull final Block block)
    {
        return tinkersSlimeCompat.checkForTinkersSlimeSapling(block);
    }

    /**
     * This method checks if block is slime dirt.
     *
     * @param block the block.
     * @return if the block is slime dirt.
     */
    public static boolean isSlimeDirtOrGrass(@NotNull final Block block)
    {
        return tinkersSlimeCompat.checkForTinkersSlimeDirtOrGrass(block);
    }

    /**
     * Get the Slime leaf variant.
     *
     * @param leaf the leaf.
     * @return the variant.
     */
    public static int getLeafVariant(@NotNull final BlockState leaf)
    {
        return tinkersSlimeCompat.getTinkersLeafVariant(leaf);
    }

    /**
     * Check if a certain itemstack is a tinkers weapon.
     *
     * @param stack the stack to check for.
     * @return true if so.
     */
    public static boolean isTinkersWeapon(@NotNull final ItemStack stack)
    {
        return tinkersCompat.isTinkersWeapon(stack);
    }

    /**
     * Check if a certain item stack is a tinkers tool of the given tool type.
     *
     * @param stack    the stack to check for.
     * @param toolType the tool type.
     * @return true if so.
     */
    public static boolean isTinkersTool(@Nullable final ItemStack stack, final EquipmentTypeEntry toolType) {return tinkersCompat.isTinkersTool(stack, toolType);}

    /**
     * Calculate the actual attack damage of the tinkers weapon.
     *
     * @param stack the stack.
     * @return the attack damage.
     */
    public static double getAttackDamage(@NotNull final ItemStack stack)
    {
        return tinkersCompat.getAttackDamage(stack);
    }

    /**
     * Calculate the tool level of the stack.
     *
     * @param stack the stack.
     * @return the tool level
     */
    public static int getToolLevel(@NotNull final ItemStack stack)
    {
        return tinkersCompat.getToolLevel(stack);
    }

    /**
     * Check if dynamic tree's is present
     *
     * @return the dynamic trees.
     */
    public static boolean isDynTreePresent()
    {
        return dynamicTreesCompat.isDynamicTreePresent();
    }

    /**
     * Returns the damageType string falling dynamic Tree's use
     *
     * @return damageType
     */
    public static ResourceKey<DamageType> getDynamicTreeDamage()
    {
        return dynamicTreesCompat.getDynamicTreeDamage();
    }

    /**
     * Check if block is a Dynamic tree
     *
     * @param block the block to check.
     * @return true if so.
     */
    public static boolean isDynamicBlock(final Block block)
    {
        return dynamicTreesCompat.checkForDynamicTreeBlock(block);
    }

    /**
     * Check if block is a Dynamic Leaf
     *
     * @param block the block to check.
     * @return true if so.
     */
    public static boolean isDynamicLeaf(final Block block)
    {
        return dynamicTreesCompat.checkForDynamicLeavesBlock(block);
    }

    /**
     * Check whether the block is a shell block.
     *
     * @param block the block to check
     * @return true if it is a shell block.
     */
    public static boolean isDynamicTrunkShell(final Block block)
    {
        return dynamicTreesCompat.checkForDynamicTrunkShellBlock(block);
    }

    /**
     * Returns drops of a dynamic seed as List
     *
     * @param world      world the Leaf is in
     * @param pos        position of the Leaf
     * @param blockState Blockstate of the Leaf
     * @param fortune    amount of fortune to use
     * @param leaf       The leaf to check
     * @return the list of drops
     */
    public static NonNullList<ItemStack> getDropsForDynamicLeaf(final LevelAccessor world, final BlockPos pos, final BlockState blockState, final int fortune, final Block leaf)
    {
        return dynamicTreesCompat.getDropsForLeaf(world, pos, blockState, fortune, leaf);
    }

    /**
     * Tries to plant a sapling at the given location
     *
     * @param world    World to plant the sapling in
     * @param location location to plant the sapling
     * @param sapling  Itemstack of the sapling
     * @return true if successful
     */
    public static boolean plantDynamicSapling(final Level world, final BlockPos location, final ItemStack sapling)
    {
        return dynamicTreesCompat.plantDynamicSaplingCompat(world, location, sapling);
    }

    /**
     * Creates a runnable to harvest/break a dynamic tree
     *
     * @param world        The world the tree is in
     * @param blockToBreak The block of the dynamic tree
     * @param toolToUse    The tool to break the tree with, optional
     * @param workerPos    The position the fakeplayer breaks the tree from, optional
     * @return Runnable to break the Tree
     */
    public static Runnable getDynamicTreeBreakAction(final Level world, final BlockPos blockToBreak, final ItemStack toolToUse, final BlockPos workerPos)
    {
        return dynamicTreesCompat.getTreeBreakActionCompat(world, blockToBreak, toolToUse, workerPos);
    }

    /**
     * Check wether the item is a dynamic Sapling
     *
     * @param item Item to check
     * @return true if so.
     */
    public static boolean isDynamicTreeSapling(final Item item)
    {
        return dynamicTreesCompat.checkForDynamicSapling(item);
    }

    /**
     * Check wether the Itemstack is a dynamic Sapling
     *
     * @param stack Itemstack to check
     * @return true if it is a dynamic Sapling
     */
    public static boolean isDynamicTreeSapling(final ItemStack stack)
    {
        return dynamicTreesCompat.checkForDynamicSapling(stack.getItem());
    }

    /**
     * Method to check if two given blocks have the same Tree family
     *
     * @param block1 First blockpos to compare
     * @param block2 Second blockpos to compare
     * @param world  the world to check.
     * @return true when same family
     */
    public static boolean isDynamicFamilyFitting(final BlockPos block1, final BlockPos block2, final LevelAccessor world)
    {
        return dynamicTreesCompat.hasFittingTreeFamilyCompat(block1, block2, world);
    }

    /**
     * Get comps from a hive at the given position
     *
     * @param pos    TE pos
     * @param world  world
     * @param amount comb amount
     * @return list of drops
     */
    public static List<ItemStack> getCombsFromHive(BlockPos pos, Level world, int amount)
    {
        return beeHiveCompat.getCombsFromHive(pos, world, amount);
    }

    /**
     * Get a custom equipment level for the given equipment type and item stack.
     *
     * @param equipmentType the equipment type to look up.
     * @param stack         the item stack to check.
     * @return the registered level, or null if no custom level exists for this combination.
     */
    @Nullable
    public static Integer getCustomEquipmentLevel(final ResourceLocation equipmentType, final ItemStack stack)
    {
        final Integer customLevel = customEquipmentTypeLevels.getOrDefault(equipmentType, Collections.emptyMap()).get(new ItemStorage(stack));
        if (customLevel != null)
        {
            return customLevel;
        }

        for (final CustomEquipmentTypeFunction function : customEquipmentTypeFunctions.getOrDefault(equipmentType, Collections.emptyList()))
        {
            final Integer functionCustomLevel = function.getLevel(stack);
            if (functionCustomLevel != null)
            {
                return functionCustomLevel;
            }
        }

        return null;
    }

    /**
     * Register a specific item with a fixed equipment level for the given equipment type.
     * Always overwrites any existing entry — intended for mod compat hooks.
     *
     * @param equipmentType the equipment type to register the item under.
     * @param item          the item to register.
     * @param level         the equipment level to assign.
     */
    @SuppressWarnings("unused") // Mod compat API
    public static void registerCustomEquipmentLevel(@NotNull final ResourceLocation equipmentType, @NotNull final Item item, final int level)
    {
        customEquipmentTypeLevels.compute(equipmentType, (key, value) -> {
            if (value == null)
            {
                value = new HashMap<>();
            }
            value.put(new ItemStorage(new ItemStack(item)), level);
            return value;
        });
    }

    /**
     * Register a function that dynamically determines the equipment level for items of the given equipment type.
     * The function receives an item stack and returns its level, or null if not applicable.
     * Always appends — intended for mod compat hooks.
     *
     * @param equipmentType the equipment type to register the function under.
     * @param function      a function mapping an item stack to its equipment level.
     */
    @SuppressWarnings("unused") // Mod compat API
    public static void registerCustomEquipmentLevel(@NotNull final ResourceLocation equipmentType, @NotNull final CustomEquipmentTypeFunction function)
    {
        customEquipmentTypeFunctions.compute(equipmentType, (key, value) -> {
            if (value == null)
            {
                value = new ArrayList<>();
            }
            value.add(function);
            return value;
        });
    }

    /**
     * A function that determines the equipment level of an item stack for a specific equipment type.
     *
     * <p>Implementations are responsible for verifying that the given item stack is actually
     * applicable — i.e., that it is the correct item and is capable of performing the relevant
     * action. If the item stack is not applicable, return {@code null}.
     *
     * <p>The returned level must be in the range {@code [0, 5]}, where {@code 0} is the lowest
     * and {@code 5} is the highest equipment tier.
     */
    @FunctionalInterface
    public interface CustomEquipmentTypeFunction
    {
        /**
         * @param item the item stack to evaluate.
         * @return the equipment level in the range [0, 5], or null if the item stack is not applicable.
         */
        @Nullable
        Integer getLevel(ItemStack item);
    }
}
