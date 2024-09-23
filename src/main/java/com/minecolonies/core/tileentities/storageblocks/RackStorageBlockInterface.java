package com.minecolonies.core.tileentities.storageblocks;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.tileentities.storageblocks.AbstractStorageBlockInterface;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.core.tileentities.TileEntityRack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

/**
 * A StorageBlockInterface that works specifically for TileEntityRacks (not AbstractTileEntityRacks)
 */
public class RackStorageBlockInterface extends AbstractStorageBlockInterface
{
    public RackStorageBlockInterface(BlockPos pos, Level world)
    {
        super(pos, world);

        BlockEntity targetBlockEntity = world.getBlockEntity(pos);

        if (!(targetBlockEntity instanceof TileEntityRack))
        {
            throw new IllegalArgumentException("The block at the target position must be an instance of TileEntityRack");
        }
    }

    @Override
    public boolean isStillValid()
    {
        return getRack().isPresent();
    }

    @Override
    public boolean shouldAutomaticallyAdd(final IBuilding building)
    {
        return true;
    }

    @Override
    public void setInWarehouse(final boolean inWarehouse)
    {
        getRack().ifPresent(rack -> rack.setInWarehouse(inWarehouse));
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
    public int getCount(final ItemStorage storage)
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
    public int getCount(final ItemStack stack, final boolean ignoreDamageValue, final boolean ignoreNBT)
    {
        return getRack().map(rack -> rack.getCount(stack, ignoreDamageValue, ignoreNBT)).orElse(0);
    }

    /**
     * Whether there are any items in the target storageblock
     *
     * @return Whether the storageblock is empty
     */
    @Override
    public boolean isEmpty() {
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
    public boolean hasItemStack(final ItemStack stack, final int count, final boolean ignoreDamage) {
        return getRack().map(rack -> rack.hasItemStack(stack, count, ignoreDamage)).orElse(false);
    }

    /**
     * Return whether the storageblock contains any items matching the predicate
     *
     * @param predicate The predicate to check against
     * @return Whether the storageblock has any matches
     */
    @Override
    public boolean hasItemStack(Predicate<ItemStack> predicate) {
        return getRack().map(rack -> rack.hasItemStack(predicate)).orElse(false);
    }

    /**
     * Sets the block position of the building this storage belongs to
     *
     * @param pos The position of the building
     */
    @Override
    public void setBuildingPos(BlockPos pos) {
        getRack().ifPresent(rack -> rack.setBuildingPos(pos));
    }

    /**
     * Add an item stack to this storage block.
     *
     * @param stack The stack to add
     * @return Whether the addition was successful
     */
    @Override
    public boolean storeItemStack(final @NotNull ItemStack stack)
    {
        return getRack().map(rack -> InventoryUtils.addItemStackToItemHandler(rack.getInventory(), stack)).orElse(false);
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
     * Attempt to add an itemstack to the storage and return the remaining stack
     *
     * @param itemStack The stack to attempt to add
     * @return The remaining stack after adding whatever can be added
     */
    @Override
    public ItemStack addItemStackWithResult(@Nullable final ItemStack itemStack)
    {
        return getRack().map(rack -> InventoryUtils.addItemStackToItemHandlerWithResult(rack.getInventory(), itemStack)).orElse(itemStack);
    }

    /**
     * Force stack to the storage block.
     *
     * @param itemStack                ItemStack to add.
     * @param itemStackToKeepPredicate The {@link Predicate} that determines which ItemStacks to keep in the inventory. Return false to replace.
     * @return itemStack which has been replaced, null if none has been replaced.
     */
    @Override
    public @Nullable ItemStack forceAddItemStack(final @NotNull ItemStack itemStack, final @NotNull Predicate<ItemStack> itemStackToKeepPredicate)
    {
        return getRack().map(rack -> InventoryUtils.forceItemStackToItemHandler(rack.getInventory(), itemStack, itemStackToKeepPredicate)).orElse(null);
    }

    /**
     * Method to transfer an ItemStacks from the given source {@link IItemHandler} to the Storage Block.
     *
     * @param sourceHandler The {@link IItemHandler} that works as Source.
     * @param predicate     the predicate for the stack.
     * @return true when the swap was successful, false when not.
     */
    @Override
    public boolean transferItemStackToStorageIntoNextBestSlot(final @NotNull IItemHandler sourceHandler, final Predicate<ItemStack> predicate)
    {
        return getRack().map(rack -> InventoryUtils.transferItemStackIntoNextBestSlotInItemHandler(sourceHandler, predicate, rack.getInventory())).orElse(false);
    }

    /**
     * Method to transfer an ItemStacks from the given source {@link IItemHandler} to the Storage Block.
     *
     * @param targetHandler The {@link IItemHandler} that works as receiver.
     * @param predicate     the predicate for the stack.
     * @return true when the swap was successful, false when not.
     */
    @Override
    public boolean transferItemStackFromStorageIntoNextBestSlot(final @NotNull IItemHandler targetHandler, final Predicate<ItemStack> predicate)
    {
        return getRack().map(rack -> InventoryUtils.transferItemStackIntoNextBestSlotInItemHandler(rack.getInventory(), predicate, targetHandler)).orElse(false);
    }

    /**
     * Method to swap the ItemStacks from storage to the given target {@link IItemHandler}.
     *
     * @param targetHandler  The {@link IItemHandler} that works as Target.
     * @param stackPredicate The type of stack to pickup.
     * @param count          how much to pick up.
     * @return True when the swap was successful, false when not.
     */
    @Override
    public boolean transferItemStackFromStorageIntoNextFreeSlot(final @NotNull IItemHandler targetHandler, final @NotNull Predicate<ItemStack> stackPredicate, final int count)
    {
        return getRack().map(rack -> InventoryUtils.transferItemStackIntoNextFreeSlotFromItemHandler(rack.getInventory(), stackPredicate, count, targetHandler)).orElse(false);
    }

    /**
     * Get the target tile entity as the TileEntityRack if it doesn't exist
     *
     * @return The target TileEntityRack if it exists
     */
    private Optional<TileEntityRack> getRack() {
        BlockEntity targetBlockEntity = level.getBlockEntity(targetPos);
        if (!(targetBlockEntity instanceof TileEntityRack)) {
            return Optional.empty();
        }
        return Optional.of((TileEntityRack) targetBlockEntity);
    }
}
