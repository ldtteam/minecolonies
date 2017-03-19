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
public class ContainerItemHandler extends Container {
    private final IItemHandler handler;
    private final int numRows;

    final int CONSTANT_INVENTORY_ROWS_PLAYER = 3;
    final int CONSTANT_INVENTORY_COLUMNS     = 9;

    public ContainerItemHandler(IInventory playerInventory, IItemHandler chestInventory, EntityPlayer player)
    {
        final int SLOT_SIZE = 18;

        final int SLOT_X_OFFSET = 8;
        final int SLOT_Y_OFFSET = 8;

        final int PLAYER_SLOT_INDEX_OFFSET = 9;
        final int PLAYER_SLOT_Y_OFFSET = 103;
        final int PLAYER_HOTBAR_Y_OFFSET = 161;


        this.handler = chestInventory;
        this.numRows = chestInventory.getSlots() / CONSTANT_INVENTORY_COLUMNS;
        int i = (this.numRows - 4) * 18;

        for (int j = 0; j < this.numRows; ++j)
        {
            for (int k = 0; k < CONSTANT_INVENTORY_COLUMNS; ++k)
            {
                this.addSlotToContainer(new SlotItemHandler(chestInventory,
                                                             k + j * CONSTANT_INVENTORY_COLUMNS,
                                                             SLOT_X_OFFSET + k * SLOT_SIZE,
                                                             SLOT_Y_OFFSET + j * SLOT_SIZE));
            }
        }

        for (int l = 0; l < CONSTANT_INVENTORY_ROWS_PLAYER; ++l)
        {
            for (int j1 = 0; j1 < CONSTANT_INVENTORY_COLUMNS; ++j1)
            {
                this.addSlotToContainer(new Slot(playerInventory,
                                                  j1 + l * CONSTANT_INVENTORY_COLUMNS + PLAYER_SLOT_INDEX_OFFSET,
                                                  SLOT_X_OFFSET + j1 * SLOT_SIZE,
                                                  PLAYER_SLOT_Y_OFFSET + l * SLOT_SIZE + i));
            }
        }

        for (int i1 = 0; i1 < CONSTANT_INVENTORY_COLUMNS; ++i1)
        {
            this.addSlotToContainer(new Slot(playerInventory, i1,
                                              SLOT_X_OFFSET + i1 * SLOT_SIZE,
                                              PLAYER_HOTBAR_Y_OFFSET + i));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return this.handler instanceof IInteractiveItemHandler && ((IInteractiveItemHandler) this.handler).isUseableByPlayer(playerIn);
    }

    /**
     * Take a stack from the specified inventory slot.
     */
    @Nullable
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
    {
        ItemStack itemstack = null;
        Slot slot = (Slot)this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            final ItemStack stackInSlot = slot.getStack();
            itemstack = stackInSlot.copy();

            if (index < this.numRows * CONSTANT_INVENTORY_COLUMNS)
            {
                if (!this.mergeItemStack(stackInSlot, this.numRows * CONSTANT_INVENTORY_COLUMNS, this.inventorySlots.size(), true))
                {
                    return null;
                }
            }
            else if (!this.mergeItemStack(stackInSlot, 0, this.numRows * CONSTANT_INVENTORY_COLUMNS, false))
            {
                return null;
            }

            if (stackInSlot.stackSize == 0)
            {
                slot.putStack((ItemStack)null);
            }
            else
            {
                slot.onSlotChanged();
            }
        }

        return itemstack;
    }

    public IItemHandler getHandler()
    {
        return this.handler;
    }
}
