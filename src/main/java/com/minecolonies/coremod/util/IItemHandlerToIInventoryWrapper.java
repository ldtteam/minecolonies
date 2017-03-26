package com.minecolonies.coremod.util;

import com.minecolonies.coremod.inventory.api.IWorldNameableModifyable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;

/**
 * Class wrapper for an IItemHandler to an IInventory.
 */
public class IItemHandlerToIInventoryWrapper implements IInventory
{
    private final IItemHandlerModifiable wrapped;
    private final IWorldNameableModifyable named;

    public IItemHandlerToIInventoryWrapper(final IItemHandlerModifiable wrapped, final IWorldNameableModifyable named) {this.wrapped = wrapped;
        this.named = named;
    }

    /**
     * Returns the number of slots in the inventory.
     */
    @Override
    public int getSizeInventory()
    {
        return wrapped.getSlots();
    }

    /**
     * Returns the stack in the given slot.
     */
    @Nullable
    @Override
    public ItemStack getStackInSlot(final int index)
    {
        return wrapped.getStackInSlot(index);
    }

    /**
     * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
     */
    @Nullable
    @Override
    public ItemStack decrStackSize(final int index, final int count)
    {
        return wrapped.extractItem(index, count, false);
    }

    /**
     * Removes a stack from the given slot and returns it.
     */
    @Nullable
    @Override
    public ItemStack removeStackFromSlot(final int index)
    {
        return decrStackSize(index, Integer.MAX_VALUE);
    }

    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
     */
    @Override
    public void setInventorySlotContents(final int index, @Nullable final ItemStack stack)
    {
        wrapped.setStackInSlot(index, stack);
    }

    /**
     * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
     */
    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    /**
     * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it
     * hasn't changed and skip it.
     */
    @Override
    public void markDirty()
    {

    }

    /**
     * Don't rename this method to canInteractWith due to conflicts with Container
     */
    @Override
    public boolean isUseableByPlayer(final EntityPlayer player)
    {
        return true;
    }

    @Override
    public void openInventory(final EntityPlayer player)
    {

    }

    @Override
    public void closeInventory(final EntityPlayer player)
    {

    }

    /**
     * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot. For
     * guis use Slot.isItemValid
     */
    @Override
    public boolean isItemValidForSlot(final int index, final ItemStack stack)
    {
        return wrapped.insertItem(index, stack, true) == InventoryUtils.EMPTY;
    }

    @Override
    public int getField(final int id)
    {
        return 0;
    }

    @Override
    public void setField(final int id, final int value)
    {

    }

    @Override
    public int getFieldCount()
    {
        return 0;
    }

    @Override
    public void clear()
    {
        InventoryUtils.clearItemHandler(wrapped);
    }

    /**
     * Get the name of this object. For players this returns their username
     */
    @Override
    public String getName()
    {
        return named.getName();
    }

    /**
     * Returns true if this thing is named
     */
    @Override
    public boolean hasCustomName()
    {
        return named.hasCustomName();
    }

    /**
     * Get the formatted ChatComponent that will be used for the sender's username in chat
     */
    @Override
    public ITextComponent getDisplayName()
    {
        return named.getDisplayName();
    }
}
