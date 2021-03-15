package com.minecolonies.api.tileentities;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IGraveData;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

import static com.minecolonies.api.research.util.ResearchConstants.RESURRECT_CHANCE;
import static com.minecolonies.api.util.constant.Constants.*;

public abstract class AbstractTileEntityGrave extends TileEntity implements INamedContainerProvider, ITickableTileEntity
{
    /**
     * The inventory of the tileEntity.
     */
    protected ItemStackHandler inventory;

    /**
     * default duration of the countdown before the grave disapear, in ticks (20 ticks / seconds)
     */
    protected static final int DEFAULT_DECAY_TIMER = TICKS_FIVE_MIN;

    /**
     * Is this grave decayed or not
     */
    protected boolean decayed;

    /**
     * The decay timer counting down before the grave decay and then disapear
     */
    protected int decay_timer;

    /**
     * The GraveData of the citizen that spawned this grave.
     */
    @Nullable
    protected IGraveData graveData;

    public AbstractTileEntityGrave(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
        inventory = createInventory(DEFAULT_SIZE);
        decay_timer = DEFAULT_DECAY_TIMER;
        decayed = false;
    }

    /**
     * Delay the decay timer by minutes
     * @param minutes number of minutes to delay the time by
     */
    public void delayDecayTimer(final double minutes)
    {
        decay_timer += minutes * TICKS_SECOND * 60;
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

    /**
     * Get the graveData of the saved citizen
     */
    public IGraveData getGraveData()
    {
        return graveData;
    }

    /**
     * Set the graveData of the saved citizen
     * @param graveData
     */
    public void setGraveData(IGraveData graveData)
    {
        this.graveData = graveData;
        markDirty();
    }
}
