package com.minecolonies.core.tileentities.storagecontainers;

import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.tileentities.AbstractTileEntityRack;
import com.minecolonies.api.tileentities.storageblocks.IStorageBlockInterface;
import com.minecolonies.core.tileentities.TileEntityRack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Map;
import java.util.function.Predicate;

/**
 * A StorageBlockInterface that works specifically for TileEntityRacks (not AbstractTileEntityRacks)
 */
public class RackStorageBlockInterface implements IStorageBlockInterface
{
    @Override
    public boolean automaticallyAddToBuilding()
    {
        return true;
    }

    @Override
    public void setInWarehouse(BlockEntity blockEntity, final boolean inWarehouse)
    {
        getAbstractRack(blockEntity).setInWarehouse(inWarehouse);
    }

    @Override
    public int getUpgradeLevel(BlockEntity blockEntity)
    {
        return getAbstractRack(blockEntity).getUpgradeSize();
    }

    @Override
    public void increaseUpgradeLevel(BlockEntity blockEntity)
    {
        getAbstractRack(blockEntity).upgradeRackSize();
    }

    @Override
    public int getCount(final BlockEntity blockEntity, final ItemStorage storage)
    {
        return getAbstractRack(blockEntity).getCount(storage);
    }

    @Override
    public int getItemCount(final BlockEntity blockEntity, final Predicate<ItemStack> predicate)
    {
        return getAbstractRack(blockEntity).getItemCount(predicate);
    }

    @Override
    public int getFreeSlots(final BlockEntity blockEntity)
    {
        return getAbstractRack(blockEntity).getFreeSlots();
    }

    /**
     * Gets all items and their count from the storage block.
     *
     * @param blockEntity the block entity to check
     * @return The items and their count
     */
    @Override
    public Map<ItemStorage, Integer> getAllContent(final BlockEntity blockEntity)
    {
        return getRack(blockEntity).getAllContent();
    }

    /**
     * Gets the matching count for a specific item stack and can ignore NBT and damage as well.
     *
     * @param blockEntity       the block entity to check
     * @param stack             The stack to check against
     * @param ignoreDamageValue Whether to ignore damage
     * @param ignoreNBT         Whether to ignore nbt data
     * @return The count of matching items in the storageblock
     */
    @Override
    public int getCount(final BlockEntity blockEntity, final ItemStack stack, final boolean ignoreDamageValue, final boolean ignoreNBT)
    {
        return getAbstractRack(blockEntity).getCount(stack, ignoreDamageValue, ignoreNBT);
    }

    private AbstractTileEntityRack getAbstractRack(BlockEntity blockEntity) {
        return (AbstractTileEntityRack) blockEntity;
    }

    private TileEntityRack getRack(BlockEntity blockEntity) {
        return (TileEntityRack) blockEntity;
    }
}
