package com.minecolonies.api.compatibility;

import com.google.common.collect.ImmutableSet;
import com.minecolonies.api.crafting.CompostRecipe;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.Disease;
import com.minecolonies.api.util.Tuple;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface for all compatabilityManagers. The compatability manager retrieves certain blocks from oreData and stores them.
 */
public interface ICompatibilityManager
{
    /**
     * Method called to instantiate internal data.
     *
     * @param recipeManager The vanilla recipe manager.
     */
    void discover(@NotNull final RecipeManager recipeManager, final Level level);

    /**
     * Transfer server-discovered item lists to client, to avoid double-handling (and
     * potentially getting different answers).
     *
     * @param buf serialization buffer
     */
    void serialize(@NotNull final FriendlyByteBuf buf);

    /**
     * Receive and update lists based on incoming server discovery data.
     *
     * Note: anything based purely on the registries and configs can be safely recalculated here.
     *       But anything based on tags or recipes must be updated purely via the packet,
     *       because this can be called before the client has the latest tags/recipes.
     *
     * @param buf deserialization buffer
     */
    void deserialize(@NotNull final FriendlyByteBuf buf, final ClientLevel level);

    /**
     * Gets the sapling matching a leaf.
     *
     * @param block the leaves.
     * @return the sapling stack or null.
     */
    @Nullable ItemStack getSaplingForLeaf(final Block block);

    /**
     * Get a copy of the list of saplings.
     *
     * @return the list of saplings.
     */
    Set<ItemStorage> getCopyOfSaplings();

    /**
     * Get a set of all fuel items.
     *
     * @return an immutable set.
     */
    Set<ItemStorage> getFuel();

    /**
     * Get a set of all food items.
     *
     * @return an immutable set.
     */
    Set<ItemStorage> getFood();

    /**
     * Get a set of all edibles for citizens.
     * @param minNutrition the min nutrition of the food.
     * @return list of edible food.
     */
    Set<ItemStorage> getEdibles(final int minNutrition);

    /**
     * Get a set of all smeltable ores.
     *
     * @return an immutable set.
     */
    Set<ItemStorage> getSmeltableOres();

    /**
     * Check if a stack belongs to a minable ore.
     *
     * @param stack the stack to test.
     * @return true if so.
     */
    boolean isMineableOre(@NotNull ItemStack stack);

    /**
     * Get a copy of the list of compost recipes.
     *
     * @return the list of compost recipes, indexed by input item.
     */
    Map<Item, CompostRecipe> getCopyOfCompostRecipes();

    /**
     * Just the possible composting inputs, for item filters.
     *
     * @return the set of compost input items.
     */
    Set<ItemStorage> getCompostInputs();

    /**
     * Get a copy of the list of plantables.
     *
     * @return the list of plantables.
     */
    Set<ItemStorage> getCopyOfPlantables();

    /**
     * Get a copy of the list of flowers.
     *
     * @return the list of flowers.
     */
    Set<ItemStorage> getImmutableFlowers();

    /**
     * Get the set of all monsters.
     * @return the set.
     */
    ImmutableSet<ResourceLocation> getAllMonsters();

    /**
     * Get a random disease of the compat manager.
     *
     * @return a randomly chosen disease.
     */
    String getRandomDisease();

    /**
     * Get a disease by the ID.
     *
     * @param disease the id.
     * @return the disease.
     */
    Disease getDisease(String disease);

    /**
     * Get the list of diseases.
     *
     * @return a copy of the list.
     */
    List<Disease> getDiseases();

    /**
     * Gets the list of recruitment costs with weights
     *
     * @return list of costs
     */
    List<Tuple<Item, Integer>> getRecruitmentCostsWeights();

    /**
     * Checks if a certain Block is an ore.
     *
     * @param block the block to check.
     * @return boolean if so.
     */
    boolean isOre(final BlockState block);

    /**
     * Test if an itemStack is an ore.
     *
     * @param stack the stack to test.
     * @return true if so.
     */
    boolean isOre(ItemStack stack);

    /**
     * Get a list of all blocks.
     *
     * @return the immutable list.
     */
    List<ItemStack> getListOfAllItems();

    /**
     * Get a set of all items (marked to ignore damage but not NBT).
     *
     * @return the immutable set.
     */
    Set<ItemStorage> getSetOfAllItems();

    /**
     * Write colonies to NBT data for saving.
     *
     * @param compound NBT-Tag.
     */
    void write(@NotNull final CompoundTag compound);

    /**
     * Read Colonies from saved NBT data.
     *
     * @param compound NBT Tag.
     */
    void read(@NotNull final CompoundTag compound);

    /**
     * Connect a certain block as leave to an ItemStack as sapling.
     *
     * @param block the block to connect the sapling to.
     * @param stack the sapling.
     */
    void connectLeafToSapling(Block block, ItemStack stack);

    /**
     * Test if an itemStack is plantable for the florist.
     *
     * @param itemStack the stack to check.
     * @return true if so.
     */
    boolean isPlantable(ItemStack itemStack);

    /**
     * If a block is a lucky block which can result in an extra ore drop.
     *
     * @param block the block to check.
     * @return true if so.
     */
    boolean isLuckyBlock(final Block block);

    /**
     * Get a random lucky ore from a luckyblock.
     * Loot may change depending on the mine level
     *
     * @param chanceBonus the chance bonus.
     * @param buildingLevel level of the mine
     * @return the lucky ore.
     */
    ItemStack getRandomLuckyOre(final double chanceBonus, final int buildingLevel);

    /**
     * Get the creative tab for a stack.
     * @param checkItem the storage wrapper.
     */
    CreativeModeTab getCreativeTab(ItemStorage checkItem);

    /**
     * Get the creative tab key as int associated.
     * @param checkItem the item to check.
     * @return the number or default.
     */
    int getCreativeTabKey(ItemStorage checkItem);
}
