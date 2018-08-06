package com.minecolonies.api.compatibility;

import com.minecolonies.api.crafting.ItemStorage;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Interface for all compatabilityManagers.
 * The compatability manager retrieves certain blocks from oreData and stores them.
 */
public interface ICompatibilityManager
{
    /**
     * Method called to instantiate the requirements.
     */
    void discover();

    /**
     * Gets the leave matching a sapling.
     * @param stack the sapling.
     * @return the leave block.
     */
    IBlockState getLeaveForSapling(final ItemStack stack);

    /**
     * Gets the sapling matching a leave.
     * @param block the leave.
     * @return the sapling stack.
     */
    ItemStack getSaplingForLeave(final IBlockState block);

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
     * Test if an itemStack is compostable
     * @param stack the stack to test
     * @return true if so
     */
    boolean isCompost(ItemStack stack);

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
    void connectLeaveToSapling(IBlockState block, ItemStack stack);

    /**
     * If discovery process ran already.
     * @return true if so.
     */
    boolean isDiscoveredAlready();
}
