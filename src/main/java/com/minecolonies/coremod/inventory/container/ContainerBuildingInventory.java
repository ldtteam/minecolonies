package com.minecolonies.coremod.inventory.container;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.inventory.ModContainers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.InventoryConstants.*;

/**
 * Container for Mie
 */
public class ContainerBuildingInventory extends Container
{
    /**
     * Lower chest inventory.
     */
    private final IInventory lowerChestInventory;

    /**
     * Amount of rows.
     */
    private final int inventorySize;

    /**
     * Constructor to create an instance of this container.
     * @param windowId the id of the window.
     * @param inv the player inventory.
     * @param extra some extra data
     */
    public ContainerBuildingInventory(final int windowId, final PlayerInventory inv, final PacketBuffer extra)
    {
        super(ModContainers.buildingInv, windowId);
        this.lowerChestInventory = inv;
        this.inventorySize = inv.getSizeInventory() / INVENTORY_COLUMNS;
        final int size = inv.getSizeInventory();

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
                      new Slot(inv, index,
                                INVENTORY_BAR_SIZE + k * PLAYER_INVENTORY_OFFSET_EACH,
                                PLAYER_INVENTORY_OFFSET_EACH + j * PLAYER_INVENTORY_OFFSET_EACH)
                      {
                          @Override
                          public void putStack(final ItemStack stack)
                          {
                              super.putStack(stack);
                              if (!inv.player.world.isRemote && !ItemStackUtils.isEmpty(stack))
                              {
                                  final IColony colony = IColonyManager.getInstance().getColonyByWorld(extra.readInt(), inv.player.world);
                                  final IBuilding building = colony.getBuildingManager().getBuilding(extra.readBlockPos());
                                  if (building != null)
                                  {
                                      building.overruleNextOpenRequestWithStack(stack);
                                  }
                              }
                          }
                      });
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

    /**
     * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
     * inventory and the other inventory(s).
     *
     * @param playerIn Player that interacted with this {@code Container}.
     * @param index    Index of the {@link Slot}. This index is relative to the list of slots in this {@code Container},
     *                 {@link #inventorySlots}.
     */
    @Override
    public ItemStack transferStackInSlot(final PlayerEntity playerIn, final int index)
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
        }

        return stackCopy;
    }

    /**
     * Called when the container is closed.
     */
    @Override
    public void onContainerClosed(final PlayerEntity playerIn)
    {
        super.onContainerClosed(playerIn);
        this.lowerChestInventory.closeInventory(playerIn);
    }

    /**
     * Determines whether supplied player can use this container
     */
    @Override
    public boolean canInteractWith(@NotNull final PlayerEntity playerIn)
    {
        return this.lowerChestInventory.isUsableByPlayer(playerIn);
    }

    /**
     * Get the size of the inventory.
     * @return the size.
     */
    public int getSlots()
    {
        return inventorySlots.size();
    }
}