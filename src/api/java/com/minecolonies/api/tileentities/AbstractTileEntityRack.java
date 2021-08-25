package com.minecolonies.api.tileentities;

import com.minecolonies.api.blocks.AbstractBlockMinecoloniesRack;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.Constants.DEFAULT_SIZE;

public abstract class AbstractTileEntityRack extends TileEntity implements INamedContainerProvider
{
    /**
     * Variable which determines if it is a single or doublechest.
     */
    protected boolean single = true;

    /**
     * Neighbor position of the rack (double chest).
     */
    protected BlockPos relativeNeighbor = null;

    /**
     * Is this the main chest of the doubleChest.
     */
    protected boolean main = false;

    /**
     * whether this rack is in a warehouse or not. defaults to not set by the warehouse building upon being built
     */
    protected boolean inWarehouse = false;

    /**
     * Pos of the owning building.
     */
    protected BlockPos buildingPos = BlockPos.ZERO;

    /**
     * The inventory of the tileEntity.
     */
    protected ItemStackHandler inventory;

    public AbstractTileEntityRack(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
        inventory = createInventory(DEFAULT_SIZE);
    }

    /**
     * Rack inventory type.
     */
    public class RackInventory extends ItemStackHandler
    {
        public RackInventory(final int defaultSize)
        {
            super(defaultSize);
        }

        @Override
        protected void onContentsChanged(final int slot)
        {
            updateItemStorage();
            super.onContentsChanged(slot);
        }

        @Override
        public void setStackInSlot(final int slot, final @Nonnull ItemStack stack)
        {
            validateSlotIndex(slot);
            final boolean changed = !ItemStack.matches(stack, this.stacks.get(slot));
            this.stacks.set(slot, stack);
            if (changed)
            {
                onContentsChanged(slot);
            }
            updateWarehouseIfAvailable(stack);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(final int slot, @Nonnull final ItemStack stack, final boolean simulate)
        {
            final ItemStack result = super.insertItem(slot, stack, simulate);
            if ((result.isEmpty() || result.getCount() < stack.getCount()) && !simulate)
            {
                updateWarehouseIfAvailable(stack);
            }
            return result;
        }
    }

    /**
     * Create the inventory that belongs to the rack.
     * @param slots the number of slots.
     * @return the created inventory,
     */
    public abstract ItemStackHandler createInventory(final int slots);

    /**
     * Update the warehouse if available with the updated stack.
     *
     * @param stack the incoming stack.
     */
    public void updateWarehouseIfAvailable(final ItemStack stack)
    {
        if (!ItemStackUtils.isEmpty(stack) && level != null && !level.isClientSide)
        {
            if (inWarehouse || !buildingPos.equals(BlockPos.ZERO))
            {
                if (IColonyManager.getInstance().isCoordinateInAnyColony(level, worldPosition))
                {
                    final IColony colony = IColonyManager.getInstance().getClosestColony(level, worldPosition);
                    if (inWarehouse && colony != null && colony.getRequestManager() != null)
                    {
                        colony.getRequestManager().onColonyUpdate(request ->
                                                                    request.getRequest() instanceof IDeliverable && ((IDeliverable) request.getRequest()).matches(stack));
                    }
                    else if (!buildingPos.equals(BlockPos.ZERO))
                    {
                        final IBuilding building = colony.getBuildingManager().getBuilding(buildingPos);
                        if (building != null)
                        {
                            building.overruleNextOpenRequestWithStack(stack);
                        }
                    }
                }
            }
        }
    }

    /**
     * Set the value for inWarehouse
     *
     * @param isInWarehouse is this rack in a warehouse?
     */
    public abstract void setInWarehouse(Boolean isInWarehouse);

    /**
     * Get the amount of free slots in the inventory. This method checks the content list, it is therefore extremely fast.
     *
     * @return the amount of free slots (an integer).
     */
    public abstract int getFreeSlots();

    /**
     * Check if a similar/same item as the stack is in the inventory. This method checks the content list, it is therefore extremely fast.
     *
     * @param stack             the stack to check.
     * @param count             the min count it should have.
     * @param ignoreDamageValue ignore the damage value.
     * @return true if so.
     */
    public abstract boolean hasItemStack(ItemStack stack, final int count, boolean ignoreDamageValue);

    /**
     * Check if a similar/same item as the stack is in the inventory. And return the count if so.
     *
     * @param stack             the stack to check.
     * @param ignoreDamageValue ignore the damage value.
     * @param ignoreNBT           if nbt should be ignored.
     * @return the quantity or 0.
     */
    public abstract int getCount(ItemStack stack, boolean ignoreDamageValue, final boolean ignoreNBT);

    /**
     * Check if a similar/same item as the stack is in the inventory. And return the count if so.
     *
     * @param storage the storage to match.
     * @return the quantity or 0.
     */
    public abstract int getCount(ItemStorage storage);

    /**
     * Check if a similar/same item as the stack is in the inventory. This method checks the content list, it is therefore extremely fast.
     *
     * @param itemStackSelectionPredicate the predicate to test the stack against.
     * @return true if so.
     */
    public abstract boolean hasItemStack(@NotNull Predicate<ItemStack> itemStackSelectionPredicate);

    /**
     * Check if a similar stack is in the rack.
     *
     * @param stack stack to check.
     * @return a set of different results depending on the similarity metric.
     */
    public abstract boolean hasSimilarStack(@NotNull ItemStack stack);

    /**
     * Upgrade the rack by 1. This adds 9 more slots and copies the inventory to the new one.
     */
    public abstract void upgradeItemStorage();

    /**
     * Set the building pos it belongs to.
     *
     * @param pos the pos of the building.
     */
    public void setBuildingPos(final BlockPos pos)
    {
        if (level != null && (buildingPos == null || !buildingPos.equals(pos)))
        {
            setChanged();
        }
        this.buildingPos = pos;
    }

    /* Get the amount of items matching a predicate in the inventory.
     * @param predicate the predicate.
     * @return the total count.
     */
    public abstract int getItemCount(Predicate<ItemStack> predicate);

    /**
     * Scans through the whole storage and updates it.
     */
    public abstract void updateItemStorage();

    /**
     * Update the blockState of the rack. Switch between connected, single, full and empty texture.
     */
    protected abstract void updateBlockState();

    /**
     * Get the other double chest or null.
     *
     * @return the tileEntity of the other half or null.
     */
    public abstract AbstractTileEntityRack getOtherChest();

    /**
     * Checks if the chest is empty. This method checks the content list, it is therefore extremely fast.
     *
     * @return true if so.
     */
    public abstract boolean isEmpty();

    /**
     * Method to change the main attribute of the rack.
     *
     * @param main the boolean value defining it.
     */
    public void setMain(final boolean main)
    {
        this.main = main;
        setChanged();
    }

    /**
     * On neighbor changed this will be called from the block.
     *
     * @param newNeighbor the blockPos which has changed.
     */
    public void neighborChanged(final BlockPos newNeighbor)
    {
        final TileEntity entity = level.getBlockEntity(newNeighbor);

        if (relativeNeighbor == null && level.getBlockState(newNeighbor).getBlock() instanceof AbstractBlockMinecoloniesRack
              && !(entity instanceof AbstractTileEntityRack && ((AbstractTileEntityRack) entity).getOtherChest() != null))
        {
            if (!setNeighbor(newNeighbor))
            {
                return;
            }

            setSingle(false);
            if (entity instanceof AbstractTileEntityRack)
            {
                if (!((AbstractTileEntityRack) entity).isMain())
                {
                    this.main = true;
                    ((AbstractTileEntityRack) entity).setMain(false);
                }
                ((AbstractTileEntityRack) entity).setNeighbor(this.getBlockPos());

                entity.setChanged();
            }

            updateItemStorage();
            this.setChanged();
            updateBlockState();
        }
        else if (relativeNeighbor != null && this.worldPosition.subtract(relativeNeighbor).equals(newNeighbor) && level.getBlockState(newNeighbor).getBlock() != ModBlocks.blockRack)
        {
            this.relativeNeighbor = null;
            setSingle(true);
            this.main = false;
            updateItemStorage();
            updateBlockState();
        }
    }

    /**
     * Set the rack as single (or unset).
     * @param single if so.
     */
    public void setSingle(final boolean single)
    {
        this.single = single;
    }

    /**
     * Check if this is the main chest of the double chest.
     *
     * @return true if so.
     */
    public boolean isMain()
    {
        return this.main;
    }

    public IItemHandlerModifiable getInventory()
    {
        return inventory;
    }

    public abstract BlockPos getNeighbor();

    public abstract boolean setNeighbor(BlockPos neighbor);
}
