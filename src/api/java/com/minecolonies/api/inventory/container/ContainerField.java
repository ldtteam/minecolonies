package com.minecolonies.api.inventory.container;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.inventory.ModContainers;
import com.minecolonies.api.tileentities.AbstractScarescrowTileEntity;
import net.minecraft.block.CropsBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
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
    private final IInventory inventory;

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

        if (world.getBlockState(pos).get(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) {
            pos = pos.down();
        }

        this.colony = IColonyManager.getInstance().getColonyByPosFromWorld(world, pos);
        this.tileEntity = ((AbstractScarescrowTileEntity) world.getTileEntity(pos));
        this.inventory = getTileEntity().getInventory();
        final int extraOffset = 0;

        // New anonymous slot type for stack validation
        addSlot(new Slot(inventory, 0, X_OFFSET, Y_OFFSET) {
            @Override
            public boolean isItemValid(@NotNull final ItemStack stack)
            {
                return Tags.Items.SEEDS.contains(stack.getItem()) || (stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock() instanceof CropsBlock);
            }

            @Override
            public int getSlotStackLimit() { return 1; }
        });

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
        ItemStack transfer = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot == null || !slot.getHasStack()) return transfer;

        transfer = slot.getStack();

        if (index == 0) {
            if (!mergeItemStack(transfer, 1, 37, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!this.mergeItemStack(transfer, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        }

        return transfer;
    }

    @Override
    public boolean canInteractWith(@NotNull final PlayerEntity playerIn)
    {
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
