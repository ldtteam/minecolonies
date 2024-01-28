package com.minecolonies.api.tileentities;

import com.minecolonies.api.inventory.InventoryCitizen;
import com.minecolonies.api.util.Tuple;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

public abstract class AbstractTileEntityWareHouse extends TileEntityColonyBuilding
{
    public AbstractTileEntityWareHouse(final BlockEntityType<? extends AbstractTileEntityWareHouse> warehouse, final BlockPos pos, final BlockState state)
    {
        super(warehouse, pos, state);
    }

    /**
     * Method to get the first matching ItemStack in the Warehouse.
     *
     * @param itemStackSelectionPredicate The predicate to select the ItemStack with.
     * @return The first matching ItemStack.
     */
    public abstract boolean hasMatchingItemStackInWarehouse(@NotNull Predicate<ItemStack> itemStackSelectionPredicate, int count);

    /**
     * Method used to check if this warehouse holds any of the requested itemstacks.
     *
     * @param itemStack The stack to check with to check with.
     * @param count the min count.
     * @param ignoreNBT if the nbt value should be ignored.
     * @return True when the warehouse holds a stack, false when not.
     */
    public abstract boolean hasMatchingItemStackInWarehouse(@NotNull final ItemStack itemStack, final int count, final boolean ignoreNBT);

    /**
     * Method used to check if this warehouse holds any of the requested itemstacks.
     *
     * @param itemStack The stack to check with to check with.
     * @param count the min count.
     * @param ignoreNBT if the nbt value should be ignored.
     * @param leftOver the leftover to keep at the warehouse at all times.
     * @return True when the warehouse holds a stack, false when not.
     */
    public abstract boolean hasMatchingItemStackInWarehouse(@NotNull final ItemStack itemStack, final int count, final boolean ignoreNBT, final int leftOver);

    /**
     * Method used to check if this warehouse holds any of the requested itemstacks.
     *
     * @param itemStack The stack to check with to check with.
     * @param count the min count.
     * @param ignoreNBT if the nbt value should be ignored.
     * @param ignoreDamage the ignore damage.
     * @param leftOver the leftover to keep at the warehouse at all times.
     * @return True when the warehouse holds a stack, false when not.
     */
    public abstract boolean hasMatchingItemStackInWarehouse(@NotNull final ItemStack itemStack, final int count, final boolean ignoreNBT, final boolean ignoreDamage, final int leftOver);

    /**
     * Method used to check if this warehouse holds any of the requested itemstacks.
     *
     * @param itemStackSelectionPredicate The predicate to check with.
     * @return True when the warehouse holds a stack, false when not.
     */
    @NotNull
    public abstract List<Tuple<ItemStack, BlockPos>> getMatchingItemStacksInWarehouse(@NotNull Predicate<ItemStack> itemStackSelectionPredicate);

    /**
     * Dump the inventory of a citizen into the warehouse. Go through all items and search the right chest to dump it in.
     *
     * @param inventoryCitizen the inventory of the citizen
     */
    public abstract void dumpInventoryIntoWareHouse(@NotNull InventoryCitizen inventoryCitizen);
}
