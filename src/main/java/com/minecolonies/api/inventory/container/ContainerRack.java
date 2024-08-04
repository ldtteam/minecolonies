package com.minecolonies.api.inventory.container;

import com.minecolonies.api.blocks.AbstractBlockMinecoloniesRack;
import com.minecolonies.api.blocks.types.RackType;
import com.minecolonies.api.inventory.ModContainers;
import com.minecolonies.api.tileentities.AbstractTileEntityRack;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.InventoryConstants.*;

/**
 * The container class for the rack.
 */
public class ContainerRack extends AbstractContainerMenu
{
    /**
     * The inventory.
     */
    private final IItemHandler inventory;

    /**
     * The tileEntity.
     */
    public final AbstractTileEntityRack rack;

    /**
     * The tileEntity.
     */
    public final AbstractTileEntityRack neighborRack;

    /**
     * Amount of rows.
     */
    private final int inventorySize;

    /**
     * Deserialize packet buffer to container instance.
     *
     * @param windowId     the id of the window.
     * @param inv          the player inventory.
     * @param packetBuffer network buffer
     * @return new instance
     */
    public static ContainerRack fromFriendlyByteBuf(final int windowId, final Inventory inv, final FriendlyByteBuf packetBuffer)
    {
        final BlockPos tePos = packetBuffer.readBlockPos();
        final BlockPos neighborPos = packetBuffer.readBlockPos();
        return new ContainerRack(windowId, inv, tePos, neighborPos);
    }

    /**
     * The container constructor.
     *
     * @param windowId the window id.
     * @param inv      the inventory.
     * @param rack     te world pos.
     * @param neighbor neighbor te world pos
     */
    public ContainerRack(final int windowId, final Inventory inv, final BlockPos rack, final BlockPos neighbor)
    {
        super(ModContainers.rackInv.get(), windowId);

        final AbstractTileEntityRack abstractTileEntityRack = (AbstractTileEntityRack) inv.player.level().getBlockEntity(rack);
        // TODO: bug, what if neighbor is actually bp.ZERO? (unlikely to happen)
        final AbstractTileEntityRack neighborRack = neighbor.equals(BlockPos.ZERO) ? null : (AbstractTileEntityRack) inv.player.level().getBlockEntity(neighbor);

        if (neighborRack != null)
        {
            if (abstractTileEntityRack.getBlockState().getValue(AbstractBlockMinecoloniesRack.VARIANT) != RackType.NO_RENDER)
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
                    this.addSlot(
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
                addSlot(new Slot(
                  inv,
                  j + i * INVENTORY_COLUMNS + INVENTORY_COLUMNS,
                  PLAYER_INVENTORY_INITIAL_X_OFFSET + j * PLAYER_INVENTORY_OFFSET_EACH,
                  PLAYER_INVENTORY_INITIAL_Y_OFFSET + extraOffset + PLAYER_INVENTORY_OFFSET_EACH * Math.min(this.inventorySize, INVENTORY_BAR_SIZE)
                    + i * PLAYER_INVENTORY_OFFSET_EACH
                ));
            }
        }

        for (i = 0; i < INVENTORY_COLUMNS; i++)
        {
            addSlot(new Slot(
              inv, i,
              PLAYER_INVENTORY_INITIAL_X_OFFSET + i * PLAYER_INVENTORY_OFFSET_EACH,
              PLAYER_INVENTORY_HOTBAR_OFFSET + extraOffset + PLAYER_INVENTORY_OFFSET_EACH * Math.min(this.inventorySize,
                INVENTORY_BAR_SIZE)
            ));
        }
    }

    @Override
    public void clicked(int slotId, int dragType, @NotNull ClickType clickTypeIn, Player player)
    {
        if (player.level().isClientSide || slotId >= inventory.getSlots() || slotId < 0)
        {
            super.clicked(slotId, dragType, clickTypeIn, player);
            return;
        }
        final ItemStack currentStack = inventory.getStackInSlot(slotId).copy();
        super.clicked(slotId, dragType, clickTypeIn, player);
        final ItemStack afterStack = inventory.getStackInSlot(slotId).copy();

        if (!ItemStack.matches(currentStack, afterStack))
        {
            this.updateRacks(afterStack);
        }
    }

    @NotNull
    @Override
    public ItemStack quickMoveStack(final Player playerIn, final int index)
    {
        final Slot slot = this.slots.get(index);

        if (slot == null || !slot.hasItem())
        {
            return ItemStackUtils.EMPTY;
        }

        final ItemStack stackCopy = slot.getItem().copy();

        final int maxIndex = this.inventorySize * INVENTORY_COLUMNS;

        if (index < maxIndex)
        {
            if (!this.moveItemStackTo(stackCopy, maxIndex, this.slots.size(), true))
            {
                return ItemStackUtils.EMPTY;
            }
        }
        else if (!this.moveItemStackTo(stackCopy, 0, maxIndex, false))
        {
            return ItemStackUtils.EMPTY;
        }

        if (ItemStackUtils.getSize(stackCopy) == 0)
        {
            slot.set(ItemStackUtils.EMPTY);
        }
        else
        {
            slot.set(stackCopy);
            slot.setChanged();
        }

        if (playerIn instanceof ServerPlayer)
        {
            this.updateRacks(stackCopy);
        }

        return stackCopy;
    }

    @Override
    protected boolean moveItemStackTo(final ItemStack stack, final int startIndex, final int endIndex, final boolean reverseDirection)
    {
        final ItemStack before = stack.copy();
        final boolean merge =  super.moveItemStackTo(stack, startIndex, endIndex, reverseDirection);
        if (merge)
        {
            this.updateRacks(before);
        }
        return merge;
    }

    /**
     * Update the racks (combined inv and warehouse).
     * @param stack the stack to set.
     */
    private void updateRacks(final ItemStack stack)
    {
        rack.updateItemStorage();
        rack.updateWarehouseIfAvailable(stack);
        if (neighborRack != null)
        {
            neighborRack.updateItemStorage();
            neighborRack.updateWarehouseIfAvailable(stack);
        }
    }

    @Override
    public boolean stillValid(final Player playerIn)
    {
        return true;
    }
}
