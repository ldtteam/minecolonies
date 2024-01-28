package com.minecolonies.api.inventory.container;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.inventory.ModContainers;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.InventoryConstants.*;

/**
 * Container for Mie
 */
public class ContainerBuildingInventory extends AbstractContainerMenu
{
    /**
     * Lower chest inventory.
     */
    private final IItemHandler buildingInventory;

    private final TileEntityColonyBuilding tileEntityColonyBuilding;

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
    public static ContainerBuildingInventory fromFriendlyByteBuf(final int windowId, final Inventory inv, final FriendlyByteBuf packetBuffer)
    {
        final int colonyId = packetBuffer.readVarInt();
        final BlockPos tePos = packetBuffer.readBlockPos();
        return new ContainerBuildingInventory(windowId, inv, colonyId, tePos);
    }

    /**
     * Constructor to create an instance of this container.
     *
     * @param windowId the id of the window.
     * @param inv      the player inventory.
     * @param colonyId colony id
     * @param pos      te world pos
     */
    public ContainerBuildingInventory(final int windowId, final Inventory inv, final int colonyId, final BlockPos pos)
    {
        super(ModContainers.buildingInv.get(), windowId);

        tileEntityColonyBuilding = (TileEntityColonyBuilding) inv.player.level().getBlockEntity(pos);
        this.buildingInventory = tileEntityColonyBuilding.getInventory();
        final int size = buildingInventory.getSlots();
        this.inventorySize = size / INVENTORY_COLUMNS;

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
                      new SlotItemHandler(buildingInventory, index,
                        INVENTORY_BAR_SIZE + k * PLAYER_INVENTORY_OFFSET_EACH,
                        PLAYER_INVENTORY_OFFSET_EACH + j * PLAYER_INVENTORY_OFFSET_EACH)
                      {
                          @Override
                          public void set(final ItemStack stack)
                          {
                              super.set(stack);
                              if (!inv.player.level().isClientSide && !ItemStackUtils.isEmpty(stack))
                              {
                                  final IColony colony = IColonyManager.getInstance().getColonyByWorld(colonyId, inv.player.level());
                                  final IBuilding building = colony.getBuildingManager().getBuilding(pos);
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
     * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player inventory and the other inventory(s).
     *
     * @param playerIn Player that interacted with this {@code Container}.
     * @param index    Index of the {@link Slot}. This index is relative to the list of slots in this {@code AbstractContainerMenu}, {@link #slots}.
     */
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
        }

        return stackCopy;
    }

    /**
     * Called when the container is closed.
     */
    @Override
    public void removed(final Player playerIn)
    {
        super.removed(playerIn);
    }

    /**
     * Determines whether supplied player can use this container
     */
    @Override
    public boolean stillValid(@NotNull final Player playerIn)
    {
        return this.tileEntityColonyBuilding.isUsableByPlayer(playerIn);
    }

    /**
     * Get the size of the inventory.
     *
     * @return the size.
     */
    public int getSize()
    {
        return inventorySize;
    }
}