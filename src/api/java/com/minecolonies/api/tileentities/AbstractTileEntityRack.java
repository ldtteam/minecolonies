package com.minecolonies.api.tileentities;

import com.minecolonies.api.blocks.AbstractBlockMinecoloniesRack;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.Constants.DEFAULT_SIZE;

public abstract class AbstractTileEntityRack extends TileEntity
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
     * whether this rack is in a warehouse or not.
     * defaults to not
     * set by the warehouse building upon being built
     */
    protected boolean inWarehouse = false;
    /**
     * The inventory of the tileEntity.
     */
    protected RackInventory inventory = new RackInventory(DEFAULT_SIZE);

    public AbstractTileEntityRack(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

    /**
     * Rack inventory type.
     */
    public class RackInventory extends ItemStackHandler implements NonNullSupplier
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
            super.setStackInSlot(slot, stack);

            if (!ItemStackUtils.isEmpty(stack) && world != null && !world.isRemote && inWarehouse && IColonyManager.getInstance().isCoordinateInAnyColony(world, pos))
            {
                final IColony colony = IColonyManager.getInstance().getClosestColony(world, pos);

                if (colony != null && colony.getRequestManager() != null)
                {
                    colony.getRequestManager()
                      .onColonyUpdate(request -> request.getRequest() instanceof IDeliverable && ((IDeliverable) request.getRequest()).matches(stack));
                }
            }
        }

        @NotNull
        @Override
        public ItemStack extractItem(final int slot, final int amount, final boolean simulate)
        {
            final ItemStack result = super.extractItem(slot, amount, simulate);
            updateItemStorage();
            return result;
        }

        @Nonnull
        @Override
        public Object get()
        {
            return this;
        }
    }

    public abstract boolean hasItemStack(ItemStack stack);

    public abstract void setInWarehouse(Boolean isInWarehouse);

    public abstract boolean freeStacks();

    public abstract int getFreeSlots();

    public abstract boolean hasItemStack(ItemStack stack, boolean ignoreDamageValue);

    public abstract boolean hasItemStack(@NotNull Predicate<ItemStack> itemStackSelectionPredicate);

    public abstract void upgradeItemStorage();

    /* Get the amount of items matching a predicate in the inventory.
     * @param predicate the predicate.
     * @return the total count.
     */
    public abstract int getItemCount(Predicate<ItemStack> predicate);

    public abstract void updateItemStorage();

    protected abstract void updateBlockState();

    public abstract AbstractTileEntityRack getOtherChest();

    public abstract boolean isEmpty();

    /**
     * Method to change the main attribute of the rack.
     *
     * @param main the boolean value defining it.
     */
    public void setMain(final boolean main)
    {
        this.main = main;
        markDirty();
    }

    /**
     * On neighbor changed this will be called from the block.
     *
     * @param newNeighbor the blockPos which has changed.
     */
    public void neighborChanged(final BlockPos newNeighbor)
    {
        final TileEntity entity = world.getTileEntity(newNeighbor);

        if (relativeNeighbor == null && world.getBlockState(newNeighbor).getBlock() instanceof AbstractBlockMinecoloniesRack
              && !(entity instanceof AbstractTileEntityRack && ((AbstractTileEntityRack) entity).getOtherChest() != null))
        {
            this.relativeNeighbor = this.pos.subtract(newNeighbor);
            single = false;
            if (entity instanceof AbstractTileEntityRack)
            {
                if (!((AbstractTileEntityRack) entity).isMain())
                {
                    this.main = true;
                    ((AbstractTileEntityRack) entity).setMain(false);
                }
                ((AbstractTileEntityRack) entity).setNeighbor(this.getPos());
                ((AbstractTileEntityRack) entity).setMain(false);
                entity.markDirty();
            }

            updateItemStorage();
            this.markDirty();
        }
        else if (relativeNeighbor != null && this.pos.subtract(relativeNeighbor).equals(newNeighbor) && !(world.getBlockState(newNeighbor).getBlock() instanceof AbstractBlockMinecoloniesRack))
        {
            this.relativeNeighbor = null;
            single = true;
            this.main = false;
            updateItemStorage();
        }
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

    public abstract void setNeighbor(BlockPos neighbor);
}
