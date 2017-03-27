package com.minecolonies.coremod.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nullable;

/**
 * Created by asie on 2/16/17.
 */
public class ContainerItemHandler extends Container
{
    private static final int INVENTORYROWSPLAYER = 3;
    private static final int INVENTORYCOLUMNS    = 9;
    private static final int SLOTSIZE = 18;
    private static final int SLOTXOFFSET = 8;
    private static final int SLOTYOFFSET = 8;
    private static final int PLAYERSLOTINDEXOFFSET   = 9;
    private static final int PLAYERSLOTYOFFSET       = 103;
    private static final int PLAYERHOTBARYOFFSET     = 161;
    private static final int PLAYERINVENTORYROWCOUNT = 4;
    private final IItemHandler handler;
    private final int          numRows;

    /**
     * Constructor for a Container that wraps an IItemHandler.
     *
     * @param playerInventory The inventory of the player opening the container.
     * @param chestInventory  The IItemHandler that this container wraps.
     */
    public ContainerItemHandler(IInventory playerInventory, IItemHandler chestInventory)
    {
        super();
        this.handler = chestInventory;
        this.numRows = chestInventory.getSlots() / INVENTORYCOLUMNS;
        final int playerInventoryYOffset = (this.numRows - PLAYERINVENTORYROWCOUNT) * SLOTSIZE;

        for (int chestRowIndex = 0; chestRowIndex < this.numRows; ++chestRowIndex)
        {
            for (int chestColumnIndex = 0; chestColumnIndex < INVENTORYCOLUMNS; ++chestColumnIndex)
            {
                this.addSlotToContainer(new SlotItemHandler(chestInventory,
                                                             chestColumnIndex + chestRowIndex * INVENTORYCOLUMNS,
                                                             SLOTXOFFSET + chestColumnIndex * SLOTSIZE,
                                                             SLOTYOFFSET + chestRowIndex * SLOTSIZE));
            }
        }

        for (int playerRowIndex = 0; playerRowIndex < INVENTORYROWSPLAYER; ++playerRowIndex)
        {
            for (int playerColumnIndex = 0; playerColumnIndex < INVENTORYCOLUMNS; ++playerColumnIndex)
            {
                this.addSlotToContainer(new Slot(playerInventory,
                                                  playerColumnIndex + playerRowIndex * INVENTORYCOLUMNS + PLAYERSLOTINDEXOFFSET,
                                                  SLOTXOFFSET + playerColumnIndex * SLOTSIZE,
                                                  PLAYERSLOTYOFFSET + playerRowIndex * SLOTSIZE + playerInventoryYOffset));
            }
        }

        for (int playerHotBarColumnIndex = 0; playerHotBarColumnIndex < INVENTORYCOLUMNS; ++playerHotBarColumnIndex)
        {
            this.addSlotToContainer(new Slot(playerInventory, playerHotBarColumnIndex,
                                              SLOTXOFFSET + playerHotBarColumnIndex * SLOTSIZE,
                                              PLAYERHOTBARYOFFSET + playerInventoryYOffset));
        }
    }

    /**
     * Take a stack from the specified inventory slot.
     */
    @Nullable
    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
    {
        ItemStack itemstack = null;
        final Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            final ItemStack stackInSlot = slot.getStack();
            itemstack = stackInSlot.copy();

            if (index < this.numRows * INVENTORYCOLUMNS)
            {
                if (!this.mergeItemStack(stackInSlot, this.numRows * INVENTORYCOLUMNS, this.inventorySlots.size(), true))
                {
                    return null;
                }
            }
            else if (!this.mergeItemStack(stackInSlot, 0, this.numRows * INVENTORYCOLUMNS, false))
            {
                return null;
            }

            if (stackInSlot.stackSize == 0)
            {
                slot.putStack((ItemStack) null);
            }
            else
            {
                slot.onSlotChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return this.handler instanceof IInteractiveItemHandler && ((IInteractiveItemHandler) this.handler).isUseableByPlayer(playerIn);
    }

    public IItemHandler getHandler()
    {
        return this.handler;
    }
}
