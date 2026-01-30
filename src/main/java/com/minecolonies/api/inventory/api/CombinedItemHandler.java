package com.minecolonies.api.inventory.api;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

import static com.minecolonies.api.util.constant.Suppression.UNCHECKED;

/**
 * Abstract class wrapping around multiple IItemHandler.
 */
public class CombinedItemHandler implements IItemHandlerModifiable, INBTSerializable<CompoundTag>
{
    /// NBT Constants
    private static final String NBT_KEY_HANDLERS           = "Handlers";
    private static final String NBT_KEY_HANDLERS_INDEXLIST = "Index";

    private final List<IItemHandlerModifiable> handlers;

    /**
     * Total slots of the item handler.
     */
    private int totalSlots = 0;

    /**
     * Method to create a new {@link CombinedItemHandler}.
     *
     * @param handlers    The combining {@link IItemHandlerModifiable}.
     */
    public CombinedItemHandler(@NotNull final IItemHandlerModifiable... handlers)
    {
        this(Arrays.asList(handlers));
    }

    /**
     * Method to create a new {@link CombinedItemHandler}.
     *
     * @param handlers    The combining {@link IItemHandlerModifiable}.
     */
    public CombinedItemHandler(@NotNull final List<IItemHandlerModifiable> handlers)
    {
        this.handlers = handlers;
        for (final IItemHandler handler : handlers)
        {
            if (handler != null)
            {
                totalSlots += handler.getSlots();
            }
        }
    }

    @Override
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider)
    {
        final CompoundTag compound = new CompoundTag();

        int index = 0;
        final ListTag handlerList = new ListTag();
        final ListTag indexList = new ListTag();
        for (final IItemHandlerModifiable handlerModifiable : handlers)
        {
            if (handlerModifiable instanceof final INBTSerializable<?> serializable)
            {
                handlerList.add(serializable.serializeNBT(provider));
                indexList.add(IntTag.valueOf(index));
            }

            index++;
        }

        compound.put(NBT_KEY_HANDLERS, handlerList);
        compound.put(NBT_KEY_HANDLERS_INDEXLIST, indexList);

        return compound;
    }

    @SuppressWarnings(UNCHECKED)
    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, final CompoundTag nbt)
    {
        final ListTag handlerList = nbt.getList(NBT_KEY_HANDLERS, Tag.TAG_COMPOUND);
        final ListTag indexList = nbt.getList(NBT_KEY_HANDLERS_INDEXLIST, Tag.TAG_INT);

        if (handlerList.size() == handlers.size())
        {
            for (int i = 0; i < handlerList.size(); i++)
            {
                final CompoundTag handlerCompound = handlerList.getCompound(i);
                final IItemHandlerModifiable modifiable = handlers.get(indexList.getInt(i));
                if (modifiable instanceof INBTSerializable)
                {
                    final INBTSerializable<CompoundTag> serializable = (INBTSerializable<CompoundTag>) modifiable;
                    serializable.deserializeNBT(provider, handlerCompound);
                }
            }
        }
    }

    /**
     * Overrides the stack in the given slot. This method is used by the standard Forge helper methods and classes. It is not intended for general use by other mods, and the
     * handler may throw an error if it is called unexpectedly.
     *
     * @param slot  Slot to modify
     * @param stack ItemStack to set slot to (may be null)
     * @throws RuntimeException if the handler is called in a way that the handler was not expecting.
     **/
    @Override
    public void setStackInSlot(final int slot, final @NotNull ItemStack stack)
    {
        int activeSlot = slot;

        for (final IItemHandlerModifiable modifiable : handlers)
        {
            if (activeSlot < modifiable.getSlots())
            {
                modifiable.setStackInSlot(activeSlot, stack);
                return;
            }

            activeSlot -= modifiable.getSlots();
        }
    }

    /**
     * Get last index of the current itemHandler a slot belongs to.
     *
     * @param slot the slot of an itemHandler.
     * @return the last index.
     */
    public int getLastIndex(final int slot)
    {
        int slots = 0;
        int activeSlot = slot;

        for (final IItemHandlerModifiable modifiable : handlers)
        {
            if (activeSlot < modifiable.getSlots())
            {
                return modifiable.getSlots() + slots;
            }
            slots += modifiable.getSlots();
            activeSlot -= modifiable.getSlots();
        }
        return 0;
    }

    /**
     * Returns the number of slots available.
     *
     * @return The number of slots available
     **/
    @Override
    public int getSlots()
    {
        return totalSlots;
    }

    /**
     * Returns the ItemStack in a given slot.
     * <p>
     * The result's stack size may be greater than the itemstacks max size.
     * <p>
     * If the result is null, then the slot is empty. If the result is not null but the stack size is zero, then it represents an empty slot that will only accept* a specific
     * itemstack.
     * <p>
     * IMPORTANT: This ItemStack MUST NOT be modified. This method is not for altering an inventories contents. Any implementers who are able to detect modification through this
     * method should throw an exception.
     * <p>
     * SERIOUSLY: DO NOT MODIFY THE RETURNED ITEMSTACK
     *
     * @param slot Slot to query
     * @return ItemStack in given slot. May be null.
     **/
    @NotNull
    @Override
    public ItemStack getStackInSlot(final int slot)
    {
        int activeSlot = slot;

        for (final IItemHandlerModifiable modifiable : handlers)
        {
            if (activeSlot < modifiable.getSlots())
            {
                return modifiable.getStackInSlot(activeSlot);
            }

            activeSlot -= modifiable.getSlots();
        }

        return ItemStack.EMPTY;
    }

    /**
     * Inserts an ItemStack into the given slot and return the remainder. The ItemStack should not be modified in this function! Note: This behaviour is subtly different from
     * IFluidHandlers.fill()
     *
     * @param slot     Slot to insert into.
     * @param stack    ItemStack to insert.
     * @param simulate If true, the insertion is only simulated
     * @return The remaining ItemStack that was not inserted (if the entire stack is accepted, then return null). May be the same as the input ItemStack if unchanged, otherwise a
     * new ItemStack.
     **/
    @NotNull
    @Override
    public ItemStack insertItem(final int slot, @NotNull final ItemStack stack, final boolean simulate)
    {
        int activeSlot = slot;

        for (final IItemHandlerModifiable modifiable : handlers)
        {
            if (activeSlot < modifiable.getSlots())
            {
                return modifiable.insertItem(activeSlot, stack, simulate);
            }

            activeSlot -= modifiable.getSlots();
        }

        return stack;
    }

    /**
     * Extracts an ItemStack from the given slot. The returned value must be null if nothing is extracted, otherwise it's stack size must not be greater than amount or the
     * itemstacks getMaxStackSize().
     *
     * @param slot     Slot to extract from.
     * @param amount   Amount to extract (may be greater than the current stacks max limit)
     * @param simulate If true, the extraction is only simulated
     * @return ItemStack extracted from the slot, must be null, if nothing can be extracted
     **/
    @NotNull
    @Override
    public ItemStack extractItem(final int slot, final int amount, final boolean simulate)
    {
        int checkedSlots = 0;
        for (final IItemHandlerModifiable modifiable : handlers)
        {
            if (modifiable.getSlots() + checkedSlots <= slot)
            {
                checkedSlots += modifiable.getSlots();
                continue;
            }
            final int activeSlot = slot - checkedSlots;
            if (activeSlot < modifiable.getSlots())
            {
                return modifiable.extractItem(activeSlot, amount, simulate);
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(final int slot)
    {
        int slotIndex = slot;
        for (final IItemHandlerModifiable modifiable : handlers)
        {
            if (slotIndex >= modifiable.getSlots())
            {
                slotIndex -= modifiable.getSlots();
            }
            else
            {
                return modifiable.getSlotLimit(slotIndex);
            }
        }

        return 0;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack)
    {
        int slotIndex = slot;
        for (final IItemHandlerModifiable modifiable : handlers)
        {
            if (slotIndex >= modifiable.getSlots())
            {
                slotIndex -= modifiable.getSlots();
            }
            else
            {
                return modifiable.isItemValid(slotIndex, stack);
            }
        }

        return false;
    }

    @Override
    public final boolean equals(final Object o)
    {
        if (!(o instanceof final CombinedItemHandler that))
        {
            return false;
        }

        return totalSlots == that.totalSlots && handlers.equals(that.handlers);
    }

    @Override
    public int hashCode()
    {
        int result = handlers.hashCode();
        result = 31 * result + totalSlots;
        return result;
    }
}
