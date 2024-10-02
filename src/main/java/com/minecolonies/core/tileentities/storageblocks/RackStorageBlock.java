package com.minecolonies.core.tileentities.storageblocks;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.tileentities.storageblocks.AbstractStorageBlock;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.tileentities.TileEntityRack;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

/**
 * A StorageBlockInterface that works specifically for TileEntityRacks (not AbstractTileEntityRacks)
 */
public class RackStorageBlock extends AbstractStorageBlock
{
    public RackStorageBlock(BlockPos pos, ResourceKey<Level> dimension)
    {
        super(pos, dimension);

        Level level = getLevel();
        // Check we're on the server side, otherwise we can't get the block entity
        if (level != null)
        {
            BlockEntity targetBlockEntity = getLevel().getBlockEntity(pos);
            if (!(targetBlockEntity instanceof TileEntityRack))
            {
                Log.getLogger().error("The block at {} must be an instance of TileEntityRack, is {}", pos, targetBlockEntity);
            }
        }
    }

    @Override
    public boolean isStillValid(final IBuilding building)
    {
        return getRack().isPresent();
    }

    @Override
    public boolean isStillValid(final IBuildingView building)
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

    @Override
    public boolean isFull()
    {
        return getRack().map(TileEntityRack::getFreeSlots).orElse(0) == 0;
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
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        Level level = server.levels.get(dimension);
        BlockEntity targetBlockEntity = level.getBlockEntity(targetPos);
        if (!(targetBlockEntity instanceof TileEntityRack)) {
            return Optional.empty();
        }
        return Optional.of((TileEntityRack) targetBlockEntity);
    }

    @Override
    public boolean supportsItemInsertNotification()
    {
        return true;
    }

    @Override
    public ItemStack extractItem(Predicate<ItemStack> predicate, boolean simulate)
    {
        return getRack().map(rack -> {
            Optional<IItemHandler> itemHandler = rack.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
            if (itemHandler.isEmpty()) {
                return ItemStack.EMPTY;
            }

            for (int i = 0; i < itemHandler.get().getSlots(); ++i)
            {
                ItemStack stack = itemHandler.get().getStackInSlot(i);
                if (stack.isEmpty())
                {
                    continue;
                }

                if (predicate.test(stack))
                {
                    return itemHandler.get().extractItem(i, stack.getCount(), simulate);
                }
            }

            return ItemStack.EMPTY;
        }).orElse(ItemStack.EMPTY);
    }

    @Override
    protected boolean insertFullStackImpl(ItemStack stack)
    {
        return getRack().map(rack -> {
            Optional<IItemHandler> itemHandler = rack.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
            if (itemHandler.isEmpty()) {
                return false;
            }

            return InventoryUtils.addItemStackToItemHandler(itemHandler.get(), stack);
        }).orElse(false);
        
    }

    @Override
    public ItemStack extractItem(ItemStack itemStack, int count, boolean simulate)
    {
        return getRack().map(rack -> {
            Optional<IItemHandler> itemHandler = rack.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
            if (itemHandler.isEmpty()) {
                return ItemStack.EMPTY;
            }

            final ItemStack workingStack = itemStack.copy();
            int localCount = count;
            int tries = 0;
            while (tries < count)
            {
                final int slot = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(itemHandler.get(), stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(workingStack, stack));
                if (slot == -1)
                {
                    return ItemStack.EMPTY;
                }

                final int removedSize = ItemStackUtils.getSize(itemHandler.get().extractItem(slot, localCount, false));

                if (removedSize == count)
                {
                    return itemStack;
                }
                else
                {
                    localCount -= removedSize;
                }
                tries++;
            }

            ItemStack result = itemStack.copy();
            result.setCount(count - localCount);
            return result;
        }).orElse(ItemStack.EMPTY);
    }

    @Override
    public ItemStack findFirstMatch(Predicate<ItemStack> predicate)
    {
        return getRack().map(rack -> {
            Optional<IItemHandler> itemHandler = rack.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
            if (itemHandler.isEmpty()) {
                return null;
            }

            int slot = InventoryUtils.findFirstSlotInItemHandlerWith(itemHandler.get(), predicate);
            if (slot == -1)
            {
                return null;
            }

            return itemHandler.get().getStackInSlot(slot);
        }).orElse(null);
    }

    @Override
    public Class<? extends AbstractStorageBlock> getStorageBlockClass() {
        return getClass();
    }
}
