package com.minecolonies.api.inventory.api;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

import static com.minecolonies.api.util.constant.Suppression.RAWTYPES;
import static com.minecolonies.api.util.constant.Suppression.UNCHECKED;

/**
 * Abstract class wrapping around multiple IItemHandler.
 */
public class CombinedItemHandler implements IItemHandlerModifiable, INBTSerializable<CompoundNBT>, IWorldNameableModifiable
{

    ///NBT Constants
    private static final String NBT_KEY_HANDLERS           = "Handlers";
    private static final String NBT_KEY_HANDLERS_INDEXLIST = "Index";
    private static final String NBT_KEY_NAME               = "Name";

    private final IItemHandlerModifiable[] handlers;

    @NotNull
    private String defaultName = "";

    @NotNull
    private String customName = "";

    /**
     * Method to create a new {@link CombinedItemHandler}.
     *
     * @param defaultName The default name of this {@link CombinedItemHandler}.
     * @param handlers    The combining {@link IItemHandlerModifiable}.
     */
    public CombinedItemHandler(@NotNull final String defaultName, @NotNull final IItemHandlerModifiable... handlers)
    {
        this.handlers = handlers;
        this.defaultName = defaultName;
    }

    /**
     * Method to create a new combined {@link CombinedItemHandler} with
     * a given custom name.
     *
     * @param defaultName The name of this {@link CombinedItemHandler}.
     * @param customName  The preset custom name of this {@link
     *                    CombinedItemHandler}.
     * @param handlers    The combinging {@link IItemHandlerModifiable}.
     */
    public CombinedItemHandler(@NotNull final String defaultName, @NotNull final String customName, @NotNull final IItemHandlerModifiable... handlers)
    {
        this.handlers = handlers;
        this.customName = customName;
        this.defaultName = defaultName;
    }

    @SuppressWarnings(RAWTYPES)
    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = new CompoundNBT();

        int index = 0;
        final ListNBT handlerList = new ListNBT();
        final ListNBT indexList = new ListNBT();
        for (final IItemHandlerModifiable handlerModifiable : handlers)
        {
            if (handlerModifiable instanceof INBTSerializable)
            {
                final INBTSerializable serializable = (INBTSerializable) handlerModifiable;
                handlerList.add(serializable.serializeNBT());
                indexList.add(IntNBT.valueOf(index));
            }

            index++;
        }

        compound.put(NBT_KEY_HANDLERS, handlerList);

        if (customName != null)
        {
            compound.putString(NBT_KEY_NAME, customName);
        }

        return compound;
    }

    @SuppressWarnings({RAWTYPES, UNCHECKED})
    @Override
    public void deserializeNBT(final CompoundNBT nbt)
    {
        final ListNBT handlerList = nbt.getList(NBT_KEY_NAME, Constants.NBT.TAG_COMPOUND);
        final ListNBT indexList = nbt.getList(NBT_KEY_HANDLERS_INDEXLIST, Constants.NBT.TAG_INT);

        if (handlerList.size() == handlers.length)
        {
            for (int i = 0; i < handlerList.size(); i++)
            {
                final CompoundNBT handlerCompound = handlerList.getCompound(i);
                final IItemHandlerModifiable modifiable = handlers[indexList.getInt(i)];
                if (modifiable instanceof INBTSerializable)
                {
                    final INBTSerializable serializable = (INBTSerializable) modifiable;
                    serializable.deserializeNBT(handlerCompound);
                }
            }
        }

        setName(nbt.keySet().contains(NBT_KEY_NAME) ? nbt.getString(NBT_KEY_NAME) : null);
    }

    /**
     * Overrides the stack in the given slot. This method is used by the
     * standard Forge helper methods and classes. It is not intended for
     * general use by other mods, and the handler may throw an error if it
     * is called unexpectedly.
     *
     * @param slot  Slot to modify
     * @param stack ItemStack to set slot to (may be null)
     * @throws RuntimeException if the handler is called in a way that the
     *                          handler was not expecting.
     **/
    @Override
    public void setStackInSlot(final int slot, final ItemStack stack)
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
        int sum = 0;
        for (final IItemHandler handler : handlers)
        {
            if (handler != null)
            {
                sum += handler.getSlots();
            }
        }

        return sum;
    }

    /**
     * Returns the ItemStack in a given slot.
     * <p>
     * The result's stack size may be greater than the itemstacks max size.
     * <p>
     * If the result is null, then the slot is empty.
     * If the result is not null but the stack size is zero, then it represents
     * an empty slot that will only accept* a specific itemstack.
     * <p>
     * IMPORTANT: This ItemStack MUST NOT be modified. This method is not for
     * altering an inventories contents. Any implementers who are able to detect
     * modification through this method should throw an exception.
     * <p>
     * SERIOUSLY: DO NOT MODIFY THE RETURNED ITEMSTACK
     *
     * @param slot Slot to query
     * @return ItemStack in given slot. May be null.
     **/
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

        return null;
    }

    /**
     * Inserts an ItemStack into the given slot and return the remainder.
     * The ItemStack should not be modified in this function!
     * Note: This behaviour is subtly different from IFluidHandlers.fill()
     *
     * @param slot     Slot to insert into.
     * @param stack    ItemStack to insert.
     * @param simulate If true, the insertion is only simulated
     * @return The remaining ItemStack that was not inserted (if the entire
     * stack is accepted, then return null). May be the same as the input
     * ItemStack if unchanged, otherwise a new ItemStack.
     **/
    @Override
    public ItemStack insertItem(final int slot, final ItemStack stack, final boolean simulate)
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
     * Extracts an ItemStack from the given slot. The returned value must be
     * null if nothing is extracted, otherwise it's stack size must not be
     * greater than amount or the itemstacks getMaxStackSize().
     *
     * @param slot     Slot to extract from.
     * @param amount   Amount to extract (may be greater than the current stacks
     *                 max limit)
     * @param simulate If true, the extraction is only simulated
     * @return ItemStack extracted from the slot, must be null, if nothing can
     * be extracted
     **/
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

        return null;
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
    public boolean isItemValid(final int slot, @Nonnull final ItemStack stack)
    {
        return false;
    }

    protected IItemHandlerModifiable[] getHandlers()
    {
        return handlers.clone();
    }

    @Override
    public void setName(@Nullable final String name)
    {
        this.customName = name == null ? "" : name;
    }

    @NotNull
    @Override
    public ITextComponent getName()
    {
        return new StringTextComponent(customName.isEmpty() ? defaultName : customName);
    }
}
