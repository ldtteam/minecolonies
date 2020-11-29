package com.minecolonies.api.compatibility;

import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.Disease;
import com.minecolonies.api.util.Tuple;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface for all compatabilityManagers. The compatability manager retrieves certain blocks from oreData and stores them.
 */
public interface ICompatibilityManager
{
    /**
     * Getter for the different meshes the sifter is allowed to use.
     *
     * @return a copy of the list of tuples containing the itemStorage and the chance of it breaking.
     */
    List<Tuple<ItemStorage, Double>> getMeshes();

    /**
     * Getter for the blocks which can be sieved.
     *
     * @return a copy of the list of itemStorages.
     */
    ArrayList<ItemStorage> getSievableBlock();

    /**
     * Get a random item return for a certain mesh and certain block which is in the sieve.
     *
     * @param mesh  the used mesh.
     * @param block the used block.
     * @param attempts attempts until an item is returned that is not air.
     * @return the ItemStack.
     */
    ItemStack getRandomSieveResultForMeshAndBlock(ItemStorage mesh, ItemStorage block, final int attempts);

    /**
     * Method called to instantiate the requirements.
     *
     * @param serverSide if server side (true) or client side (false).
     */
    void discover(final boolean serverSide);

    /**
     * Gets the sapling matching a leave.
     *
     * @param block the leave.
     * @return the sapling stack.
     */
    ItemStack getSaplingForLeaf(final BlockState block);

    /**
     * Get a copy of the list of saplings.
     *
     * @return the list of saplings.
     */
    List<ItemStorage> getCopyOfSaplings();

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
     * Get a copy of the list of compostable items.
     *
     * @return the list of compostable items.
     */
    List<ItemStorage> getCopyOfCompostableItems();

    /**
     * Get a copy of the list of plantables.
     *
     * @return the list of plantables.
     */
    List<ItemStorage> getCopyOfPlantables();

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
     * Test if an itemStack is compostable
     *
     * @param stack the stack to test
     * @return true if so
     */
    boolean isCompost(ItemStack stack);

    /**
     * Get a map of all the crusher modes.
     *
     * @return the modes.
     */
    Map<ItemStorage, ItemStorage> getCrusherModes();

    /**
     * Write colonies to NBT data for saving.
     *
     * @param compound NBT-Tag.
     */
    void write(@NotNull final CompoundNBT compound);

    /**
     * Read Colonies from saved NBT data.
     *
     * @param compound NBT Tag.
     */
    void read(@NotNull final CompoundNBT compound);

    /**
     * Connect a certain block as leave to an ItemStack as sapling.
     *
     * @param block the block to connect the sapling to.
     * @param stack the sapling.
     */
    void connectLeafToSapling(BlockState block, ItemStack stack);

    /**
     * If discovery process ran already.
     *
     * @return true if so.
     */
    boolean isDiscoveredAlready();

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
     *
     * @param chanceBonus the chance bonus.
     * @return the lucky ore.
     */
    ItemStack getRandomLuckyOre(final double chanceBonus);

    /**
     * Get a random enchantment book for a certain building level.
     *
     * @param buildingLevel the building level.
     * @return a tuple containing the stack and the level applied to it.
     */
    Tuple<ItemStack, Integer> getRandomEnchantmentBook(final int buildingLevel);

    /**
     * Check if the block is configured to bypass the colony restrictions.
     * @param block the block to check.
     * @return true if so.
     */
    boolean isFreeBlock(Block block);

    /**
     * Check if the position is configured to bypass the colony restrictions.
     * @param block the position to check.
     * @return true if so.
     */
    boolean isFreePos(BlockPos block);
}
