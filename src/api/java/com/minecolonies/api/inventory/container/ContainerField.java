package com.minecolonies.api.inventory.container;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.IFieldView;
import com.minecolonies.api.colony.buildings.workerbuildings.fields.FieldRecord;
import com.minecolonies.api.colony.buildings.workerbuildings.fields.FieldType;
import com.minecolonies.api.inventory.ModContainers;
import com.minecolonies.api.tileentities.AbstractTileEntityScarecrow;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.InventoryConstants.*;

public class ContainerField extends AbstractContainerMenu
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
     * The tile entity.
     */
    private final AbstractTileEntityScarecrow tileEntity;

    /**
     * The field view class.
     */
    private final IFieldView fieldView;

    /**
     * Constructs the GUI with the player.
     *
     * @param windowId        the window id.
     * @param playerInventory the player inventory.
     * @param pos             te world pos.
     */
    public ContainerField(final int windowId, final Inventory playerInventory, BlockPos pos)
    {
        super(ModContainers.field.get(), windowId);

        final Level world = playerInventory.player.level;

        if (world.getBlockState(pos).getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER)
        {
            pos = pos.below();
        }

        this.tileEntity = ((AbstractTileEntityScarecrow) world.getBlockEntity(pos));
        final int extraOffset = 0;

        // Hide the inventory slots of the scarecrow whenever we're not inside a colony.
        final IColonyView colony = IColonyManager.getInstance().getClosestColonyView(world, pos);
        if (colony != null && this.tileEntity != null)
        {
            this.inventory = this.tileEntity.getInventory();
            this.fieldView = colony.getField(FieldType.FARMER_FIELDS, new FieldRecord(this.tileEntity.getBlockPos(), this.tileEntity.getPlant()));
            addSlot(new SlotItemHandler(this.inventory, 0, X_OFFSET, Y_OFFSET));
        }
        else
        {
            this.inventory = null;
            this.fieldView = null;
        }

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

    /**
     * Deserialize packet buffer to container instance.
     *
     * @param windowId     the id of the window.
     * @param inv          the player inventory.
     * @param packetBuffer network buffer
     * @return new instance
     */
    public static ContainerField fromFriendlyByteBuf(final int windowId, final Inventory inv, final FriendlyByteBuf packetBuffer)
    {
        final BlockPos tePos = packetBuffer.readBlockPos();
        return new ContainerField(windowId, inv, tePos);
    }

    /**
     * Get the field view.
     *
     * @return the tile.
     */
    @Nullable
    public IFieldView getFieldView()
    {
        return fieldView;
    }

    /**
     * Get the plant which is put in the inventory slot of the field container.
     *
     * @return the plant, if any.
     */
    public Item getPlant()
    {
        return inventory.getStackInSlot(0).getItem();
    }

    @NotNull
    @Override
    public ItemStack quickMoveStack(@NotNull final Player playerIn, final int index)
    {
        ItemStack transfer = ItemStackUtils.EMPTY;
        Slot slot = this.slots.get(index);

        if (!slot.hasItem())
        {
            return transfer;
        }

        transfer = slot.getItem();

        if (index == 0)
        {
            if (!moveItemStackTo(transfer, 1, 37, true))
            {
                return ItemStackUtils.EMPTY;
            }
        }
        else
        {
            if (!this.moveItemStackTo(transfer, 0, 1, false))
            {
                return ItemStackUtils.EMPTY;
            }
        }

        return transfer;
    }

    @Override
    public boolean stillValid(@NotNull final Player playerIn)
    {
        return tileEntity.canOpenMenu(playerIn);
    }
}
