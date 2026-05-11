package com.minecolonies.api.compatibility;

import com.minecolonies.api.compatibility.dynamictrees.DynamicTreeProxy;
import com.minecolonies.api.compatibility.resourcefulbees.IBeehiveCompat;
import com.minecolonies.api.compatibility.tinkers.SlimeTreeProxy;
import com.minecolonies.api.compatibility.tinkers.TinkersToolProxy;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * This class is to store the methods that call the methods to check for miscellaneous compatibility problems.
 */
public final class Compatibility
{

    private Compatibility()
    {
        throw new IllegalAccessError("Utility class");
    }

    private record TierEntry(@NotNull Tier tier, int level) {}

    private static final Map<ItemStorage, TierEntry> itemTierRegistry = new HashMap<>();
    private static final List<Predicate<ItemStack>> customWeaponRecognizers = new ArrayList<>();

    // Ordered by level 0–5. Level 5 maps to Netherite (the highest vanilla Tier).
    private static final Tiers[] LEVEL_TO_TIER = {
        Tiers.WOOD, Tiers.STONE, Tiers.IRON, Tiers.DIAMOND, Tiers.NETHERITE, Tiers.NETHERITE
    };

    private static Tiers tierForLevel(final int level)
    {
        return LEVEL_TO_TIER[Math.min(Math.max(level, 0), LEVEL_TO_TIER.length - 1)];
    }

    /**
     * Register an item with an explicit Tier object and pre-computed level.
     * Always overwrites any existing entry — intended for mod compat hooks.
     *
     * @param item  the item to register.
     * @param tier  the Tier object to associate.
     * @param level the equipment level integer.
     */
    public static void registerItemTier(@NotNull final Item item, @NotNull final Tier tier, final int level)
    {
        itemTierRegistry.put(new ItemStorage(new ItemStack(item), true, true), new TierEntry(tier, level));
    }

    /**
     * Register an item by level only.
     * The closest matching vanilla {@link Tiers} instance is stored so that
     * {@link #getItemTier} never returns null for a registered item.
     * Always overwrites any existing entry — intended for mod compat hooks.
     *
     * @param item  the item to register.
     * @param level the equipment level integer.
     */
    public static void registerItemTier(@NotNull final Item item, final int level)
    {
        itemTierRegistry.put(new ItemStorage(new ItemStack(item), true, true), new TierEntry(tierForLevel(level), level));
    }

    /**
     * Register an item with an explicit Tier and level only if not already registered.
     * Used by auto-population so explicit mod registrations are never overwritten.
     *
     * @param item  the item to register.
     * @param tier  the Tier object to associate.
     * @param level the equipment level integer.
     */
    public static void registerItemTierIfAbsent(@NotNull final Item item, @NotNull final Tier tier, final int level)
    {
        itemTierRegistry.putIfAbsent(new ItemStorage(new ItemStack(item), true, true), new TierEntry(tier, level));
    }

    /**
     * Register an item by level only if not already registered.
     * The closest matching vanilla {@link Tiers} instance is stored.
     * Used by auto-population so explicit mod registrations are never overwritten.
     *
     * @param item  the item to register.
     * @param level the equipment level integer.
     */
    public static void registerItemTierIfAbsent(@NotNull final Item item, final int level)
    {
        itemTierRegistry.putIfAbsent(new ItemStorage(new ItemStack(item), true, true), new TierEntry(tierForLevel(level), level));
    }

    /**
     * Return the Tier associated with this stack.
     * Returns {@code null} only when the item has not been registered.
     * Items registered without a real Tier (armor, bow, etc.) return the closest
     * matching vanilla {@link Tiers} instance.
     *
     * @param stack the item stack.
     * @return the registered Tier, or null if not registered.
     */
    @Nullable
    public static Tier getItemTier(final ItemStack stack)
    {
        final TierEntry entry = itemTierRegistry.get(new ItemStorage(stack, true));
        return entry != null ? entry.tier() : null;
    }

    /**
     * Return the pre-computed equipment level for this stack, or -1 if not registered.
     *
     * @param stack the item stack.
     * @return the equipment level, or -1.
     */
    public static int getItemLevel(final ItemStack stack)
    {
        final TierEntry entry = itemTierRegistry.get(new ItemStorage(stack, true));
        return entry != null ? entry.level() : -1;
    }

    /**
     * Register a custom weapon recognizer. The predicate should return true if the stack
     * is a weapon that should be treated as a sword by colonists (e.g. maces, javelins).
     *
     * @param recognizer the predicate to register.
     */
    public static void registerWeaponRecognizer(final Predicate<ItemStack> recognizer)
    {
        customWeaponRecognizers.add(recognizer);
    }

    /**
     * Query all registered weapon recognizers.
     *
     * @param stack the item stack.
     * @return true if any recognizer claims the stack as a weapon.
     */
    public static boolean isCustomWeapon(final ItemStack stack)
    {
        for (final Predicate<ItemStack> recognizer : customWeaponRecognizers)
        {
            if (recognizer.test(stack))
            {
                return true;
            }
        }
        return false;
    }

    public static IJeiProxy jeiProxy = new IJeiProxy() {};
    public static IBeehiveCompat beeHiveCompat = new IBeehiveCompat() {};
    public static SlimeTreeProxy   tinkersSlimeCompat = new SlimeTreeProxy();
    public static TinkersToolProxy tinkersCompat      = new TinkersToolProxy();
    public static DynamicTreeProxy dynamicTreesCompat = new DynamicTreeProxy();

    /**
     * This method checks to see if STACK is able to mine anything. It goes through all compatibility checks.
     *
     * @param stack the item in question.
     * @param tool  the name of the tool.
     * @return boolean whether the stack can mine or not.
     */
    public static boolean getMiningLevelCompatibility(@Nullable final ItemStack stack, @Nullable final String tool)
    {
        return !tinkersCompat.checkTinkersBroken(stack);
    }

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
     * @param stack the stack to check for.
     * @param toolType the tool type.
     * @return true if so.
     */
    public static boolean isTinkersTool(@Nullable final ItemStack stack, final EquipmentTypeEntry toolType) { return tinkersCompat.isTinkersTool(stack, toolType); }

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
     * Check whether the Itemstack is a dynamic Sapling
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
}