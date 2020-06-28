package com.minecolonies.api.inventory.container;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.inventory.ModContainers;
import com.minecolonies.api.tileentities.AbstractScarescrowTileEntity;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

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
    private final IColony colony;

    /**
     * The tile entity.
     */
    private final AbstractScarescrowTileEntity tileEntity;

    /**
     * Constructs the GUI with the player.
     * @param windowId the window id.
     * @param playerInventory the player inventory.
     * @param extra extra data.
     */
    public ContainerField(final int windowId, final PlayerInventory playerInventory, final PacketBuffer extra)
    {
        super(ModContainers.field, windowId);

        final World world = playerInventory.player.world;
        BlockPos pos = extra.readBlockPos();

        if (world.getBlockState(pos).get(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER)
        {
            pos = pos.down();
        }

        this.colony = IColonyManager.getInstance().getColonyByPosFromWorld(world, pos);
        this.tileEntity = ((AbstractScarescrowTileEntity) world.getTileEntity(pos));
        this.inventory = getTileEntity().getInventory();
        final int extraOffset = 0;

        addSlot(new SlotItemHandler(inventory, 0, X_OFFSET, Y_OFFSET));

        // Player inventory slots
        // Note: The slot numbers are within the player inventory and may be the same as the field inventory.
        int i;
        for (i = 0; i < INVENTORY_ROWS; i++)
        {
            for (int j = 0; j < INVENTORY_COLUMNS; j++)
            {
                addSlot(new Slot(
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
            addSlot(new Slot(
                    playerInventory, i,
                    PLAYER_INVENTORY_INITIAL_X_OFFSET + i * PLAYER_INVENTORY_OFFSET_EACH,
                    PLAYER_INVENTORY_HOTBAR_OFFSET + extraOffset + PLAYER_INVENTORY_OFFSET_EACH * LINES_PER_OFFSET
            ));
        }
    }

    @NotNull
    @Override
    public ItemStack transferStackInSlot(@NotNull final PlayerEntity playerIn, final int index)
    {
        ItemStack transfer = ItemStackUtils.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot == null || !slot.getHasStack()) return transfer;

        transfer = slot.getStack();

        if (index == 0)
        {
            if (!mergeItemStack(transfer, 1, 37, true)) {
                return ItemStackUtils.EMPTY;
            }
        }
        else
        {
            if (!this.mergeItemStack(transfer, 0, 1, false))
            {
                return ItemStackUtils.EMPTY;
            }
        }

        return transfer;
    }

    @Override
    public boolean canInteractWith(@NotNull final PlayerEntity playerIn)
    {
        if (colony == null)
            return false;
        return colony.getPermissions().hasPermission(playerIn, Action.ACCESS_HUTS);
    }

    /**
     * Get the assigned tile entity.
     * @return the tile.
     */
    public AbstractScarescrowTileEntity getTileEntity()
    {
        return tileEntity;
    }
}
