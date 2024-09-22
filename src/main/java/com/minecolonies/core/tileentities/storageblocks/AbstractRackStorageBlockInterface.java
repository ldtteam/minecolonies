package com.minecolonies.core.tileentities.storageblocks;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.tileentities.AbstractTileEntityRack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.Map;
import java.util.function.Predicate;

/**
 * A StorageBlockInterface that works specifically for TileEntityRacks (not AbstractTileEntityRacks)
 */
public class AbstractRackStorageBlockInterface extends AbstractStorageBlockInterface
{
    public AbstractRackStorageBlockInterface(BlockEntity targetBlockEntity) {
        super(targetBlockEntity);

        if (!(targetBlockEntity instanceof AbstractTileEntityRack)) {
            throw new RuntimeException("Trying to create an AbstractRackStorageBlockInterface with not instance of AbstractTileEntityRack");
        }
    }

    @Override
    public boolean shouldAutomaticallyAdd(final IBuilding building)
    {
        return true;
    }

    @Override
    public void setInWarehouse(final boolean inWarehouse)
    {
        getRack().setInWarehouse(inWarehouse);
    }

    @Override
    public int getUpgradeLevel()
    {
        return getRack().getUpgradeSize();
    }

    @Override
    public void increaseUpgradeLevel()
    {
        getRack().upgradeRackSize();
    }

    @Override
    public int getCount(final ItemStorage storage)
    {
        return getRack().getCount(storage);
    }

    @Override
    public int getItemCount(final Predicate<ItemStack> predicate)
    {
        return getRack().getItemCount(predicate);
    }

    @Override
    public int getFreeSlots()
    {
        return getRack().getFreeSlots();
    }

    /**
     * Gets all items and their count from the storage block.
     *
     * @return The items and their count
     */
    @Override
    public Map<ItemStorage, Integer> getAllContent()
    {
        return null;
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
        return getRack().getCount(stack, ignoreDamageValue, ignoreNBT);
    }

    /**
     * Whether there are any items in the target storageblock
     *
     * @return Whether the storageblock is empty
     */
    @Override
    public boolean isEmpty() {
        return getRack().isEmpty();
    }

    /**
     * Get the modifiable ItemHandler for the given storageblock
     *
     * @return the itemhandler
     */
    @Override
    public IItemHandlerModifiable getInventory() {
        return getRack().getInventory();
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
        return getRack().hasItemStack(stack, count, ignoreDamage);
    }

    /**
     * Return whether the storageblock contains any items matching the predicate
     *
     * @param predicate The predicate to check against
     * @return Whether the storageblock has any matches
     */
    @Override
    public boolean hasItemStack(Predicate<ItemStack> predicate) {
        return getRack().hasItemStack(predicate);
    }

    /**
     * Sets the block position of the building this storage belongs to
     *
     * @param pos The position of the building
     */
    @Override
    public void setBuildingPos(BlockPos pos) {
        getRack().setBuildingPos(pos);
    }

    private AbstractTileEntityRack getRack() {
        return (AbstractTileEntityRack) targetBlockEntity;
    }
}
