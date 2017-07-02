package com.minecolonies.coremod.inventory;

import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.tileentities.TileEntityRack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * THe container class for the rack.
 */
public class ContainerRack extends net.minecraft.inventory.Container
{
    /**
     * Amount of columns in the player inventory.
     */
    private static final int INVENTORY_COLUMNS = 9;

    /**
     * Initial x-offset of the inventory slot.
     */
    private static final int PLAYER_INVENTORY_INITIAL_X_OFFSET = 8;

    /**
     * Initial y-offset of the inventory slot.
     */
    private static final int PLAYER_INVENTORY_INITIAL_Y_OFFSET = 84;

    /**
     * Each offset of the inventory slots.
     */
    private static final int PLAYER_INVENTORY_OFFSET_EACH = 18;

    /**
     * Initial y-offset of the inventory slots in the hotbar.
     */
    private static final int PLAYER_INVENTORY_HOTBAR_OFFSET = 142;

    /**
     * Amount of rows in the player inventory.
     */
    private static final int INVENTORY_ROWS = 3;

    /**
     * The size of the the inventory hotbar.
     */
    private static final int INVENTORY_BAR_SIZE = 8;

    /**
     * The colony of the field.
     */
    @Nullable
    private final Colony colony;

    /**
     * The fields location.
     */
    private final BlockPos location;

    /**
     * The inventory.
     */
    private final IItemHandler inventory;

    /**
     * The tileEntity.
     */
    private final TileEntityRack rack;

    /**
     * Creates an instance of our field container, this may be serve to open the GUI.
     *
     * @param tileEntityRack the tileEntity of the field containing the inventory.
     * @param playerInventory     the player inventory.
     * @param world               the world.
     * @param location            the position of the field.
     */
    public ContainerRack(@NotNull final TileEntityRack tileEntityRack, final InventoryPlayer playerInventory, @NotNull final World world, @NotNull final BlockPos location)
    {
        super();
        this.colony = ColonyManager.getColony(world, location);
        this.location = location;
        this.inventory = tileEntityRack.getInventory();
        this.rack = tileEntityRack;

        for (int j = 0; j < INVENTORY_ROWS; ++j)
        {
            for (int k = 0; k < INVENTORY_COLUMNS; ++k)
            {
                this.addSlotToContainer(
                        new SlotItemHandler(inventory, k + j * INVENTORY_COLUMNS,
                                INVENTORY_BAR_SIZE + k * PLAYER_INVENTORY_OFFSET_EACH,
                                PLAYER_INVENTORY_OFFSET_EACH + j * PLAYER_INVENTORY_OFFSET_EACH));
            }
        }

        //Ddd player inventory slots
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
                        PLAYER_INVENTORY_INITIAL_Y_OFFSET + i * PLAYER_INVENTORY_OFFSET_EACH
                ));
            }
        }

        for (i = 0; i < INVENTORY_COLUMNS; i++)
        {
            addSlotToContainer(new Slot(
                    playerInventory, i,
                    PLAYER_INVENTORY_INITIAL_X_OFFSET + i * PLAYER_INVENTORY_OFFSET_EACH,
                    PLAYER_INVENTORY_HOTBAR_OFFSET
            ));
        }
    }

    @Override
    protected final Slot addSlotToContainer(final Slot slotToAdd)
    {
        return super.addSlotToContainer(slotToAdd);
    }

    @Override
    public boolean canInteractWith(final EntityPlayer playerIn)
    {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(final EntityPlayer playerIn, int index)
    {
        Slot slot = this.inventorySlots.get(index);

        if (slot == null || !slot.getHasStack())
        {
            return ItemStackUtils.EMPTY;
        }

        ItemStack stack = slot.getStack();
        ItemStack stackCopy = stack.copy();

        if (index < INVENTORY_ROWS * INVENTORY_COLUMNS + INVENTORY_COLUMNS)
        {
            if (!this.mergeItemStack(stack, INVENTORY_ROWS * INVENTORY_COLUMNS + INVENTORY_COLUMNS, this.inventorySlots.size(), true))
            {
                return ItemStackUtils.EMPTY;
            }
        }
        else if (!this.mergeItemStack(stack, 0, INVENTORY_ROWS * INVENTORY_COLUMNS + INVENTORY_COLUMNS, false))
        {
            return ItemStackUtils.EMPTY;
        }

        if (ItemStackUtils.getSize(stack) == 0)
        {
            slot.putStack(ItemStackUtils.EMPTY);
        }
        else
        {
            slot.onSlotChanged();
        }

        rack.updateItemStorage();
        return stackCopy;
    }
}
