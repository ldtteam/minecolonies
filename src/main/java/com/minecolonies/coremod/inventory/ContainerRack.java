package com.minecolonies.coremod.inventory;

import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.tileentities.AbstractTileEntityRack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.InventoryConstants.*;

/**
 * The container class for the rack.
 */
public class ContainerRack extends net.minecraft.inventory.Container
{
    /**
     * The inventory.
     */
    private final IItemHandler inventory;

    /**
     * The tileEntity.
     */
    private final AbstractTileEntityRack rack;

    /**
     * The tileEntity.
     */
    private final AbstractTileEntityRack neighborRack;

    /**
     * Amount of rows.
     */
    private final int inventorySize;

    /**
     * Creates an instance of our field container, this may be serve to open the GUI.
     *
     * @param abstractTileEntityRack  the tileEntity of the field containing the inventory.
     * @param neighborRack    the neighboring rack.
     * @param playerInventory the player inventory.
     */
    public ContainerRack(
                          @NotNull final AbstractTileEntityRack abstractTileEntityRack, @Nullable final AbstractTileEntityRack neighborRack,
                          final InventoryPlayer playerInventory)
    {
        super();
        if (neighborRack != null)
        {
            if (abstractTileEntityRack.isMain())
            {
                this.inventory = new CombinedInvWrapper(abstractTileEntityRack.getInventory(), neighborRack.getInventory());
            }
            else
            {
                this.inventory = new CombinedInvWrapper(neighborRack.getInventory(), abstractTileEntityRack.getInventory());
            }
        }
        else
        {
            this.inventory = abstractTileEntityRack.getInventory();
        }

        this.rack = abstractTileEntityRack;
        this.neighborRack = neighborRack;
        this.inventorySize = this.inventory.getSlots() / INVENTORY_COLUMNS;
        final int size = this.inventory.getSlots();

        final int columns = inventorySize <= INVENTORY_BAR_SIZE ? INVENTORY_COLUMNS : ((size / INVENTORY_BAR_SIZE) + 1);
        final int extraOffset = inventorySize <= INVENTORY_BAR_SIZE ? 0 : 2;
        int index = 0;

        for (int j = 0; j < Math.min(this.inventorySize, INVENTORY_BAR_SIZE); ++j)
        {
            for (int k = 0; k < columns; ++k)
            {
                if (index < size)
                {
                    this.addSlotToContainer(
                      new SlotItemHandler(inventory, index,
                                           INVENTORY_BAR_SIZE + k * PLAYER_INVENTORY_OFFSET_EACH,
                                           PLAYER_INVENTORY_OFFSET_EACH + j * PLAYER_INVENTORY_OFFSET_EACH));
                    index++;
                }
            }
        }

        // Player inventory slots
        // Note: The slot numbers are within the player inventory and may be the same as the field inventory.
        int i;
        for (i = 0; i < INVENTORY_ROWS; i++)
        {
            for (int j = 0; j < INVENTORY_COLUMNS; j++)
            {
                addSlotToContainer(new Slot(
                                             playerInventory,
                                             j + i * INVENTORY_COLUMNS + INVENTORY_COLUMNS,
                                             PLAYER_INVENTORY_INITIAL_X_OFFSET + j * PLAYER_INVENTORY_OFFSET_EACH,
                                             PLAYER_INVENTORY_INITIAL_Y_OFFSET + extraOffset + PLAYER_INVENTORY_OFFSET_EACH * Math.min(this.inventorySize, INVENTORY_BAR_SIZE)
                                               + i * PLAYER_INVENTORY_OFFSET_EACH
                ));
            }
        }

        for (i = 0; i < INVENTORY_COLUMNS; i++)
        {
            addSlotToContainer(new Slot(
                                         playerInventory, i,
                                         PLAYER_INVENTORY_INITIAL_X_OFFSET + i * PLAYER_INVENTORY_OFFSET_EACH,
                                         PLAYER_INVENTORY_HOTBAR_OFFSET + extraOffset + PLAYER_INVENTORY_OFFSET_EACH * Math.min(this.inventorySize,
                                           INVENTORY_BAR_SIZE)
            ));
        }
    }

    @Override
    protected final Slot addSlotToContainer(final Slot slotToAdd)
    {
        return super.addSlotToContainer(slotToAdd);
    }

    @Override
    public ItemStack transferStackInSlot(final EntityPlayer playerIn, final int index)
    {
        final Slot slot = this.inventorySlots.get(index);

        if (slot == null || !slot.getHasStack())
        {
            return ItemStackUtils.EMPTY;
        }

        final ItemStack stackCopy = slot.getStack().copy();

        final int maxIndex = this.inventorySize * INVENTORY_COLUMNS;

        if (index < maxIndex)
        {
            if (!this.mergeItemStack(stackCopy, maxIndex, this.inventorySlots.size(), true))
            {
                return ItemStackUtils.EMPTY;
            }
        }
        else if (!this.mergeItemStack(stackCopy, 0, maxIndex, false))
        {
            return ItemStackUtils.EMPTY;
        }

        if (ItemStackUtils.getSize(stackCopy) == 0)
        {
            slot.putStack(ItemStackUtils.EMPTY);
        }
        else
        {
            slot.putStack(stackCopy);
            slot.onSlotChanged();
        }

        rack.updateItemStorage();
        if (neighborRack != null)
        {
            neighborRack.updateItemStorage();
        }
        return stackCopy;
    }

    @Override
    public boolean canInteractWith(final EntityPlayer playerIn)
    {
        return true;
    }
}
