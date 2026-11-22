package com.minecolonies.api.colony.buildings;

import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.workers.util.GuardGear;
import com.minecolonies.api.inventory.InventoryCitizen;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.core.tileentities.TileEntityRack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import com.minecolonies.api.colony.IColony;


/**
 * Common building interface for both client & server.
 */
public interface ICommonBuilding
{
    /**
     * Get the current level of the building.
     *
     * @return AbstractBuilding current level.
     */
    int getBuildingLevel();

    /**
     * Gets the location of this building.
     *
     * @return A BlockPos, where this building is.
     */
    @NotNull
    BlockPos getPosition();

    /**
     * Get the Building type
     *
     * @return building type
     */
    BuildingEntry getBuildingType();

    /**
     * Get the equivalent building level for equipment, etc.
     * Normally it's just the building level, but for buildings with fewer levels it can be 1,3,5 for example.
     * @return the adjusted level.
     */
    default int getBuildingLevelEquivalent()
    {
        return getBuildingLevel();
    }

    /**
     * Get the BlockPos of the Containers.
     *
     * @return containerList.
     */
    List<BlockPos> getContainers();

    /**
     * Get the colony from a building.
     * @return the colony it belongs to.
     */
    IColony getColony();

    /**
     * Get the prestige value of the building.
     * @return the prestige value.
     */
    int getPrestige();

    /**
     * Calculate the number of empty slots in a given building.
     *
     * @return the number of empty slots.
     */
    default boolean isInventoryFull()
    {
        final Level world = this.getColony().getWorld();
        for (final BlockPos pos : this.getContainers())
        {
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof TileEntityRack rack && rack.getFreeSlots() > 0)
                {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Calculate the number of empty slots in a given building.
     * @return the number of empty slots.
     */
    default int countEmptySlots()
    {
        int totalCount = 0;
        final Level world = this.getColony().getWorld();
        for (final BlockPos pos : this.getContainers())
        {
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof TileEntityRack rackEntity)
                {
                    totalCount += rackEntity.getFreeSlots();
                }
            }
        }

        return totalCount;
    }

    /**
     * Count the number of items of different types a building has.
     *
     * @param stacks   the stacks to check for.
     * @return Amount of occurrences of stacks that match the given stacks.
     */
    default int getItemCount(@NotNull final List<ItemStorage> stacks)
    {
        int totalCount = 0;
        for (ItemStorage stack : stacks)
        {
            totalCount += this.getItemCount(stack);
        }

        return totalCount;
    }

    /**
     * Count the number of items a building has.
     *
     * @param stack    the stack to check.
     * @return Amount of occurrences of stacks that match the given stack.
     */
    default int getItemCount(@NotNull final ItemStorage stack)
    {
        int totalCount = 0;
        final Level world = this.getColony().getWorld();

        for (final BlockPos pos : this.getContainers())
        {
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof TileEntityRack rackEntity)
                {
                    totalCount += rackEntity.getCount(stack);
                }
            }
        }

        return totalCount;
    }

    /**
     * Count the number of items a building has.
     *
     * @param stack the stack to match.
     * @return Amount of occurrences of stacks that match the given stack.
     */
    default int getItemCount(@NotNull final ItemStack stack)
    {
        return getItemCount(st -> ItemStackUtils.compareItemStacksIgnoreStackSize(st, stack));
    }

    /**
     * Count the number of items a building has.
     *
     * @param predicate the predicate to match.
     * @return Amount of occurrences of stacks that match the given predicate .
     */
    default int getItemCount(@NotNull final Predicate<ItemStack> predicate)
    {
        int totalCount = 0;
        final Level world = this.getColony().getWorld();

        for (final BlockPos pos : this.getContainers())
        {
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof TileEntityRack rackEntity)
                {
                    totalCount += rackEntity.getItemCount(predicate);
                }
            }
        }

        return totalCount;
    }

    /**
     * Count the number of items a building has.
     * Only count up to "limit" of a particular item.
     *
     * @param predicate the predicate to match.
     * @return Amount of occurrences of stacks that match the given stack.
     */
    default int getItemCountWithLimit(@NotNull final Predicate<ItemStack> predicate, final Function<ItemStack, Integer> limit)
    {
        final Level world = this.getColony().getWorld();

        final Map<ItemStorage, Integer> allMatching = new HashMap<>();

        for (final BlockPos pos : this.getContainers())
        {
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof TileEntityRack rackEntity)
                {
                    for (final Map.Entry<ItemStorage, Integer> entry : rackEntity.getAllContent().entrySet())
                    {
                        if (predicate.test(entry.getKey().getItemStack()))
                        {
                            allMatching.put(entry.getKey(), allMatching.getOrDefault(entry.getKey(), 0) + entry.getValue());
                        }
                    }
                }
            }
        }

        int totalCount = 0;
        for (final Map.Entry<ItemStorage, Integer> entry : allMatching.entrySet())
        {
            totalCount += Math.min(limit.apply(entry.getKey().getItemStack()), entry.getValue());
        }

        return totalCount;
    }

    /**
     * Check if a building has more than a count in stack. Return the count it has if it has less.
     *
     * @param stack    the stack to check.
     * @return Amount of occurrences of stacks that match the given predicate.
     */
    default int hasEnoughElseCount(@NotNull final ItemStorage stack, final int count)
    {
        int totalCount = 0;
        final Level world = this.getColony().getWorld();

        for (final BlockPos pos : this.getContainers())
        {
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof TileEntityRack rackEntity)
                {
                    totalCount += rackEntity.getCount(stack);
                }

                if (totalCount >= count)
                {
                    return totalCount;
                }
            }
        }

        return totalCount;
    }

    /**
     * Check if a building has more than a count in stack. Return the count it has if it has less.
     *
     * @param stack    the stack to check.
     * @return Amount of occurrences of stacks that match the given predicate.
     */
    default int hasEnoughElseCount(@NotNull final Predicate<ItemStack> stack, final int count)
    {
        int totalCount = 0;
        final Level world = this.getColony().getWorld();

        for (final BlockPos pos : this.getContainers())
        {
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof TileEntityRack rackEntity)
                {
                    totalCount += rackEntity.getItemCount(stack);
                }

                if (totalCount >= count)
                {
                    return totalCount;
                }
            }
        }

        return totalCount;
    }

    /**
     * Check if the building has an item.
     * @param stack the predicate to match.
     * @return true if so.
     */
    default boolean hasItem(@NotNull final Predicate<ItemStack> stack)
    {
        return hasEnoughElseCount(stack, 1) >= 1;
    }

    /**
     * Check if the building has an item.
     * @param item the item to match.
     * @return true if so.
     */
    default boolean hasItem(@NotNull final Item item)
    {
        return hasEnoughElseCount(stack -> !stack.isEmpty() && stack.getItem() == item, 1) >= 1;
    }

    /**
     * Transfer from building into itemHandler.
     * @param itemHandler the inventory to transfer to.
     * @param stack the stack to match to.
     * @param count the count to transfer.
     * @return the transferred count.
     */
    default int transferFrom(@NotNull IItemHandler itemHandler, Predicate<ItemStack> stack, int count)
    {
        final Level world = this.getColony().getWorld();
        int transferedQty = 0;
        for (final BlockPos pos : this.getContainers())
        {
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof TileEntityRack rackEntity)
                {
                    if (rackEntity.hasItemStack(stack))
                    {
                        transferedQty += InventoryUtils.transfer(rackEntity.getInventory(), itemHandler, stack, count - transferedQty);
                    }
                }
            }
            if (transferedQty >= count)
            {
                return transferedQty;
            }
        }
        return transferedQty;
    }

    /**
     * Transfer to building from itemHandler.
     * @param itemHandler the inventory to transfer from.
     * @param stack the stack to match to.
     * @param count the count to transfer.
     * @return the transferred count.
     */
    default int transferTo(@NotNull IItemHandler itemHandler, ItemStack stack, int count) { return transferTo(itemHandler, st -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, st), count); }
    default int transferTo(@NotNull IItemHandler itemHandler, Predicate<ItemStack> stack, int count)
    {
        final Level world = this.getColony().getWorld();
        int transferedQty = 0;
        // First let's try inserting into an inventory that already has this stack.
        for (final BlockPos pos : this.getContainers())
        {
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof TileEntityRack rackEntity)
                {
                    if (rackEntity.hasItemStack(stack))
                    {
                        transferedQty += InventoryUtils.transfer(itemHandler, rackEntity.getInventory(), stack, count - transferedQty);
                    }
                }
            }
            if (transferedQty >= count)
            {
                return transferedQty;
            }
        }

        for (final BlockPos pos : this.getContainers())
        {
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof TileEntityRack rackEntity)
                {
                    transferedQty += InventoryUtils.transfer(itemHandler, rackEntity.getInventory(), stack, count - transferedQty);
                }
            }
            if (transferedQty >= count)
            {
                return transferedQty;
            }
        }

        return transferedQty;
    }

    /**
     * Extract items from the building.
     * @param itemStack the matching Stack.
     * @return the extracted amount.
     */
    default int extractItem(final ItemStack itemStack) { return extractItem(stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, itemStack), itemStack.getCount()); }
    default int extractItem(final Predicate<ItemStack> predicate, final int count)
    {
        final Level world = getColony().getWorld();
        int extractedQty = 0;
        for (final BlockPos pos : this.getContainers())
        {
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof TileEntityRack rackEntity)
                {
                    extractedQty += InventoryUtils.extract(rackEntity.getInventory(), predicate, count - extractedQty).getB();
                }
            }
            if (extractedQty >= count)
            {
                return extractedQty;
            }
        }
        return extractedQty;
    }

    /**
     * Insert items into the building.
     * @param stack the stack to insert.
     * @return the amount.
     */
    default ItemStack insert(final ItemStack stack)
    {
        final Level world = getColony().getWorld();
        ItemStack leftOver = stack;
        for (final BlockPos pos : this.getContainers())
        {
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof TileEntityRack rackEntity)
                {
                    leftOver = InventoryUtils.insert(rackEntity.getInventory(), leftOver);
                }
            }
            if (ItemStackUtils.isEmpty(leftOver))
            {
                return leftOver;
            }
        }
        return leftOver;
    }

    /**
     * Get all matching slots given some predicate.
     * @param predicate the predicate to test for.
     * @return map of racks to matching slots.
     */
    default Map<IItemHandler, List<Integer>> getMatchingSlots(final Predicate<ItemStack> predicate)
    {
        final Level world = getColony().getWorld();
        final Map<IItemHandler, List<Integer>> map = new HashMap<>();
        for (final BlockPos pos : this.getContainers())
        {
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof TileEntityRack rackEntity)
                {
                    final List<Integer> tempList = InventoryUtils.getAllMatchingSlot(rackEntity.getInventory(), predicate);
                    if (!tempList.isEmpty())
                    {
                        map.put(rackEntity.getInventory(), tempList);
                    }
                }
            }
        }

        return map;
    }
}
