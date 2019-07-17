package com.minecolonies.api.compatibility;

import com.minecolonies.api.crafting.ItemStorage;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Interface for all compatabilityManagers.
 * The compatability manager retrieves certain blocks from oreData and stores them.
 */
public interface ICompatibilityManager
{
    /**
     * Getter for the different meshes the sifter is allowed to use.
     * @return a copy of the list of tuples containing the itemStorage and the chance of it breaking.
     */
    List<Tuple<ItemStorage, Double>> getMeshes();

    /**
     * Getter for the blocks which can be sieved.
     * @return a copy of the list of itemStorages.
     */
    ArrayList<ItemStorage> getSievableBlock();

    /**
     * Get a random item return for a certain mesh and certain block which is in the sieve.
     * @param mesh the used mesh.
     * @param block the used block.
     * @return the ItemStack.
     */
    ItemStack getRandomSieveResultForMeshAndBlock(ItemStorage mesh, ItemStorage block);

    /**
     * Method called to instantiate the requirements.
     */
    void discover();

    /**
     * Gets the sapling matching a leave.
     * @param block the leave.
     * @return the sapling stack.
     */
    ItemStack getSaplingForLeaf(final IBlockState block);

    /**
     * Get a copy of the list of saplings.
     * @return the list of saplings.
     */
    List<ItemStorage> getCopyOfSaplings();

    /**
     * Get a copy of the list of compostable items.
     * @return the list of compostable items.
     */
    List<ItemStorage> getCopyOfCompostableItems();

    /**
     * Checks if a certain Block is an ore.
     * @param block the block to check.
     * @return boolean if so.
     */
    boolean isOre(final IBlockState block);

    /**
     * Test if an itemStack is an ore.
     * @param stack the stack to test.
     * @return true if so.
     */
    boolean isOre(ItemStack stack);

    /**
     * Get a list of all blocks.
     * @return the immutable list.
     */
    List<ItemStack> getBlockList();

    /**
     * Test if an itemStack is compostable
     * @param stack the stack to test
     * @return true if so
     */
    boolean isCompost(ItemStack stack);

    /**
     * Get a map of all the crusher modes.
     * @return the modes.
     */
    Map<ItemStorage, ItemStorage> getCrusherModes();

    /**
     * Write colonies to NBT data for saving.
     *
     * @param compound NBT-Tag.
     */
    void writeToNBT(@NotNull final NBTTagCompound compound);

    /**
     * Read Colonies from saved NBT data.
     *
     * @param compound NBT Tag.
     */
    void readFromNBT(@NotNull final NBTTagCompound compound);

    /**
     * Connect a certain block as leave to an ItemStack as sapling.
     * @param block the block to connect the sapling to.
     * @param stack the sapling.
     */
    void connectLeafToSapling(IBlockState block, ItemStack stack);

    /**
     * If discovery process ran already.
     * @return true if so.
     */
    boolean isDiscoveredAlready();

    /**
     * If an itemStack is a lucky block which can result in an extra ore drop.
     * @param itemStack the stack to check.
     * @return true if so.
     */
    boolean isLuckyBlock(final ItemStack itemStack);

    /**
     * Get a random lucky ore from a luckyblock.
     * @return the lucky ore.
     */
    ItemStack getRandomLuckyOre();
}
