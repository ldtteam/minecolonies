package com.minecolonies.core.tileentities.storageblocks;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.tileentities.storageblocks.AbstractStorageBlock;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.core.tileentities.TileEntityRack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

/**
 * A StorageBlockInterface that works specifically for TileEntityRacks (not AbstractTileEntityRacks)
 */
public class RackStorageBlock extends AbstractStorageBlock
{
    public RackStorageBlock(BlockPos pos, Level world)
    {
        super(pos, world);

        BlockEntity targetBlockEntity = world.getBlockEntity(pos);

        if (!(targetBlockEntity instanceof TileEntityRack))
        {
            throw new IllegalArgumentException("The block at the target position must be an instance of TileEntityRack");
        }
    }

    @Override
    public boolean isStillValid(final IBuilding building)
    {
        return getRack().isPresent();
    }

    @Override
    public int getUpgradeLevel()
    {
        return getRack().map(TileEntityRack::getUpgradeSize).orElse(0);
    }

    @Override
    public void increaseUpgradeLevel()
    {
        getRack().ifPresent(TileEntityRack::upgradeRackSize);
    }

    @Override
    public int getItemCount(final ItemStorage storage)
    {
        return getRack().map(rack -> rack.getCount(storage)).orElse(0);
    }

    @Override
    public int getItemCount(final Predicate<ItemStack> predicate)
    {
        return getRack().map(rack -> rack.getItemCount(predicate)).orElse(0);
    }

    @Override
    public int getFreeSlots()
    {
        return getRack().map(TileEntityRack::getFreeSlots).orElse(0);
    }

    /**
     * Gets all items and their count from the storage block.
     *
     * @return The items and their count
     */
    @Override
    public Map<ItemStorage, Integer> getAllContent()
    {
        return getRack().map(TileEntityRack::getAllContent).orElse(new HashMap<>());
    }

    /**
     * Gets the matching count for a specific item stack and can ignore NBT and damage as well.
     *
     * @param stack             The stack to check against
     * @param ignoreDamageValue Whether to ignore damage
     * @param ignoreNBT         Whether to ignore nbt data
     * @return The count of matching items in the storageblock
     */
    @Override
    public int getItemCount(final ItemStack stack, final boolean ignoreDamageValue, final boolean ignoreNBT)
    {
        return getRack().map(rack -> rack.getCount(stack, ignoreDamageValue, ignoreNBT)).orElse(0);
    }

    /**
     * Whether there are any items in the target storageblock
     *
     * @return Whether the storageblock is empty
     */
    @Override
    public boolean isEmpty()
    {
        return getRack().map(TileEntityRack::isEmpty).orElse(true);
    }

    /**
     * Return whether the storageblock contains a matching item stack
     *
     * @param stack        The item type to compare
     * @param count        The amount that must be present
     * @param ignoreDamage Whether the items should have matching damage values
     * @return Whether the storageblock contains the match
     */
    @Override
    public boolean hasItemStack(final ItemStack stack, final int count, final boolean ignoreDamage)
    {
        return getRack().map(rack -> rack.hasItemStack(stack, count, ignoreDamage)).orElse(false);
    }

    /**
     * Return whether the storageblock contains any items matching the predicate
     *
     * @param predicate The predicate to check against
     * @return Whether the storageblock has any matches
     */
    @Override
    public boolean hasItemStack(Predicate<ItemStack> predicate)
    {
        return getRack().map(rack -> rack.hasItemStack(predicate)).orElse(false);
    }

    /**
     * Get any matching item stacks within the storage block.
     *
     * @param predicate The predicate to test against
     * @return The list of matching item stacks
     */
    @Override
    public List<ItemStack> getMatching(final @NotNull Predicate<ItemStack> predicate)
    {
        return getRack().map(rack -> InventoryUtils.filterItemHandler(rack.getInventory(), predicate)).orElse(new ArrayList<>());
    }

    /**
     * Get the target tile entity as the TileEntityRack if it doesn't exist
     *
     * @return The target TileEntityRack if it exists
     */
    private Optional<TileEntityRack> getRack()
    {
        BlockEntity targetBlockEntity = level.getBlockEntity(targetPos);
        if (!(targetBlockEntity instanceof TileEntityRack)) {
            return Optional.empty();
        }
        return Optional.of((TileEntityRack) targetBlockEntity);
    }
}
