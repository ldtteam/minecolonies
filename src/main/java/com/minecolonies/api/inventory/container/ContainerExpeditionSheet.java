package com.minecolonies.api.inventory.container;

import com.minecolonies.api.inventory.ModContainers;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.core.items.ItemExpeditionSheet.ExpeditionSheetContainerManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.InventoryConstants.*;

/**
 * Container for the expedition sheet item inventory.
 */
public class ContainerExpeditionSheet extends AbstractContainerMenu
{
    /**
     * Amount of rows.
     */
    private final int inventoryRows;

    /**
     * The container constructor.
     *
     * @param windowId        the window id.
     * @param playerInventory the player inventory.
     * @param stack           the item stack containing the expedition sheet.
     */
    public ContainerExpeditionSheet(final int windowId, final Inventory playerInventory, final ItemStack stack)
    {
        super(ModContainers.expeditionSheet.get(), windowId);
        final ExpeditionSheetContainerManager inventory = new ExpeditionSheetContainerManager(stack);

        inventoryRows = inventory.getContainerSize() / INVENTORY_COLUMNS;
        final int size = inventory.getContainerSize();

        final int columns = inventoryRows <= INVENTORY_BAR_SIZE ? INVENTORY_COLUMNS : ((size / INVENTORY_BAR_SIZE) + 1);
        final int extraOffset = inventoryRows <= INVENTORY_BAR_SIZE ? 0 : 2;
        int index = 0;

        for (int j = 0; j < Math.min(this.inventoryRows, INVENTORY_BAR_SIZE); ++j)
        {
            for (int k = 0; k < columns; ++k)
            {
                if (index < size)
                {
                    this.addSlot(new Slot(inventory,
                      index,
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
                  playerInventory,
                  j + i * INVENTORY_COLUMNS + INVENTORY_COLUMNS,
                  PLAYER_INVENTORY_INITIAL_X_OFFSET + j * PLAYER_INVENTORY_OFFSET_EACH,
                  PLAYER_INVENTORY_INITIAL_Y_OFFSET + extraOffset + PLAYER_INVENTORY_OFFSET_EACH * Math.min(this.inventoryRows, INVENTORY_BAR_SIZE)
                    + i * PLAYER_INVENTORY_OFFSET_EACH
                ));
            }
        }

        for (i = 0; i < INVENTORY_COLUMNS; i++)
        {
            addSlot(new Slot(
              playerInventory, i,
              PLAYER_INVENTORY_INITIAL_X_OFFSET + i * PLAYER_INVENTORY_OFFSET_EACH,
              PLAYER_INVENTORY_HOTBAR_OFFSET + extraOffset + PLAYER_INVENTORY_OFFSET_EACH * Math.min(this.inventoryRows, INVENTORY_BAR_SIZE)
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
    public static ContainerExpeditionSheet fromFriendlyByteBuf(final int windowId, final Inventory inv, final FriendlyByteBuf packetBuffer)
    {
        return new ContainerExpeditionSheet(windowId, inv, inv.player.getItemInHand(packetBuffer.readEnum(InteractionHand.class)));
    }

    @Override
    @NotNull
    public ItemStack quickMoveStack(final @NotNull Player player, final int index)
    {
        final Slot slot = this.slots.get(index);

        if (!slot.hasItem())
        {
            return ItemStackUtils.EMPTY;
        }

        final ItemStack stackCopy = slot.getItem().copy();

        final int maxIndex = this.inventoryRows * INVENTORY_COLUMNS;

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
        }

        return stackCopy;
    }

    @Override
    public boolean stillValid(final @NotNull Player player)
    {
        return true;
    }
}
