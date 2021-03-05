package com.minecolonies.api.tileentities;

import com.minecolonies.api.blocks.types.GraveType;
import com.minecolonies.api.blocks.types.RackType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

import static com.minecolonies.api.util.constant.Constants.DEFAULT_SIZE;

public abstract class AbstractTileEntityGrave extends TileEntity implements INamedContainerProvider, ITickableTileEntity
{
    /**
     * The inventory of the tileEntity.
     */
    protected ItemStackHandler inventory;

    protected static final int TICK = 1;
    protected static final int SECOND = 20 * TICK;
    protected static final int MINUTE = 60 * SECOND;

    /**
     * default duration of the countdown before the grave disapear, in ticks (20 ticks / seconds)
     */
    //TODO TG have 2 variant for this block, one full prestine, one half broken. Go 5m Full Prestine -> half broken + 5m half Broken -> disapear
    protected static final int DEFAULT_DECAY_TIMER = 5 * MINUTE;

    /**
     * The decay timer counting down before the grave disapear
     */
    protected int decay_timer;

    public AbstractTileEntityGrave(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
        inventory = createInventory(DEFAULT_SIZE);
        setDecayTimer(DEFAULT_DECAY_TIMER);
    }

    /**
     * Grave inventory type.
     */
    public class GraveInventory extends ItemStackHandler
    {
        public GraveInventory(final int defaultSize)
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
            final boolean changed = !ItemStack.areItemStacksEqual(stack, this.stacks.get(slot));
            this.stacks.set(slot, stack);
            if (changed)
            {
                onContentsChanged(slot);
            }
        }

        @Nonnull
        @Override
        public ItemStack insertItem(final int slot, @Nonnull final ItemStack stack, final boolean simulate)
        {
            final ItemStack result = super.insertItem(slot, stack, simulate);
            return result;
        }
    }

    /**
     * Create the inventory that belongs to the grave.
     * @param slots the number of slots.
     * @return the created inventory,
     */
    public abstract ItemStackHandler createInventory(final int slots);

    /**
     * Scans through the whole storage and updates it.
     */
    public abstract void updateItemStorage();

    /**
     * Update the blockState of the rack. Switch between connected, single, full and empty texture.
     */
    protected abstract void updateBlockState();

    /**
     * Checks if the chest is empty. This method checks the content list, it is therefore extremely fast.
     *
     * @return true if so.
     */
    public abstract boolean isEmpty();

    public IItemHandlerModifiable getInventory()
    {
        return inventory;
    }

    public abstract void setDecayTimer(int decayCountdownTicks);
}
