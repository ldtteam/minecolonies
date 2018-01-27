package com.minecolonies.coremod.inventory;

import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import static com.minecolonies.api.util.constant.InventoryConstants.*;

/**
 * Container for Mie
 */
public class ContainerMinecoloniesBuildingInventory extends Container
{
    /**
     * Lower chest inventory.
     */
    private final IInventory lowerChestInventory;

    /**
     * Player inventory.
     */
    private final IInventory playerInventory;

    /**
     * Amount of rows.
     */
    private final int inventorySize;

    /**
     * Public constructor to create the minecolonies building container.
     * @param playerInventory the player inv.
     * @param inventory the inv itself.
     * @param colonyId the colony id.
     * @param buildingId the building id.
     */
    public ContainerMinecoloniesBuildingInventory(
                                                   final IInventory playerInventory,
                                                   final IInventory inventory,
                                                   final int colonyId,
                                                   final BlockPos buildingId)
    {
        this.lowerChestInventory = inventory;
        this.inventorySize = inventory.getSizeInventory() / INVENTORY_COLUMNS;
        final int size = inventory.getSizeInventory();

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
                      new Slot(inventory, index,
                                INVENTORY_BAR_SIZE + k * PLAYER_INVENTORY_OFFSET_EACH,
                                PLAYER_INVENTORY_OFFSET_EACH + j * PLAYER_INVENTORY_OFFSET_EACH)
                      {
                          @Override
                          public void putStack(final ItemStack stack)
                          {
                              super.putStack(stack);
                              if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER && !ItemStackUtils.isEmpty(stack))
                              {
                                  final Colony colony = ColonyManager.getColony(colonyId);
                                  final AbstractBuilding building = colony.getBuildingManager().getBuilding(buildingId);
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
        this.playerInventory = playerInventory;
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
            slot.onSlotChanged();
        }

        return stackCopy;
    }

    /**
     * Called when the container is closed.
     */
    @Override
    public void onContainerClosed(final EntityPlayer playerIn)
    {
        super.onContainerClosed(playerIn);
        this.lowerChestInventory.closeInventory(playerIn);
    }

    /**
     * Determines whether supplied player can use this container
     */
    public boolean canInteractWith(final EntityPlayer playerIn)
    {
        return this.lowerChestInventory.isUsableByPlayer(playerIn);
    }

    /**
     * Return this chest container's lower chest inventory.
     */
    public IInventory getLowerChestInventory()
    {
        return this.lowerChestInventory;
    }

    public IInventory getPlayerInventory()
    {
        return playerInventory;
    }
}