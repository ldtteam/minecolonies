package com.minecolonies.coremod.inventory;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.InventoryConstants.*;

public class ContainerField extends Container
{
    /**
     * Inventory lines until the player inv starts.
     */
    private static final int LINES_PER_OFFSET = 3;

    /**
     * The inventory.
     */
    private final IItemHandler inventory;

    /**
     * The colony.
     */
    private final Colony colony;

    /**
     * Creates an instance of our field container, this may be serve to open the GUI.
     *
     * @param scarecrowTileEntity the tileEntity of the field containing the inventory.
     * @param playerInventory     the player inventory.
     * @param world               the world.
     * @param location            the position of the field.
     */
    public ContainerField(@NotNull final ScarecrowTileEntity scarecrowTileEntity,
            final InventoryPlayer playerInventory,
            @NotNull final World world,
            @NotNull final BlockPos location)
    {
        super();
        this.colony = ColonyManager.getColonyByPosFromWorld(world, location);
        this.inventory = scarecrowTileEntity.getInventory();
        final int extraOffset = 0;

        addSlotToContainer(new SlotItemHandler(inventory, 0, X_OFFSET, Y_OFFSET));

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
                        PLAYER_INVENTORY_INITIAL_Y_OFFSET + extraOffset + PLAYER_INVENTORY_OFFSET_EACH * LINES_PER_OFFSET
                                + i * PLAYER_INVENTORY_OFFSET_EACH
                ));
            }
        }

        for (i = 0; i < INVENTORY_COLUMNS; i++)
        {
            addSlotToContainer(new Slot(
                    playerInventory, i,
                    PLAYER_INVENTORY_INITIAL_X_OFFSET + i * PLAYER_INVENTORY_OFFSET_EACH,
                    PLAYER_INVENTORY_HOTBAR_OFFSET + extraOffset + PLAYER_INVENTORY_OFFSET_EACH * LINES_PER_OFFSET
            ));
        }
    }

    @Override
    protected final Slot addSlotToContainer(final Slot slotToAdd)
    {
        return super.addSlotToContainer(slotToAdd);
    }

    @Nullable
    @Override
    public ItemStack transferStackInSlot(@NotNull final EntityPlayer playerIn, final int slotIndex)
    {
        if (slotIndex == 0)
        {
            playerIn.inventory.addItemStackToInventory(inventory.getStackInSlot(0));
            inventory.insertItem(0, ItemStackUtils.EMPTY, false);
        }
        else if (inventory.getStackInSlot(0) == ItemStackUtils.EMPTY || ItemStackUtils.getSize(inventory.getStackInSlot(0)) == 0)
        {
            final int playerIndex = slotIndex < MAX_INVENTORY_INDEX ? (slotIndex + INVENTORY_BAR_SIZE) : (slotIndex - MAX_INVENTORY_INDEX);
            if (playerIn.inventory.getStackInSlot(playerIndex) != ItemStackUtils.EMPTY)
            {
                @NotNull final ItemStack stack = playerIn.inventory.getStackInSlot(playerIndex).splitStack(1);
                inventory.insertItem(0, stack, false);
                if (ItemStackUtils.getSize(playerIn.inventory.getStackInSlot(playerIndex)) == 0)
                {
                    playerIn.inventory.removeStackFromSlot(playerIndex);
                }
            }
        }

        return ItemStackUtils.EMPTY;
    }

    @Override
    public boolean canInteractWith(@NotNull final EntityPlayer playerIn)
    {
        return colony.getPermissions().hasPermission(playerIn, Action.ACCESS_HUTS);
    }

}
