package com.minecolonies.api.inventory.container;

import com.minecolonies.api.colony.*;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.inventory.InventoryCitizen;
import com.minecolonies.api.inventory.ModContainers;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.InventoryConstants.*;

/**
 * Container for Mie
 */
public class ContainerCitizenInventory extends Container
{
    /**
     * Player inventory.
     */
    private final PlayerInventory playerInventory;

    /**
     * Amount of rows.
     */
    private final int inventorySize;
    private String displayName;

    /**
     * Creating the citizen inventory container.
     * @param windowId the window id.
     * @param inv the inventory.
     * @param extra extra data.
     */
    public ContainerCitizenInventory(final int windowId, final PlayerInventory inv, final PacketBuffer extra)
    {
        super(ModContainers.citizenInv, windowId);
        final int colonyId = extra.readVarInt();
        final int citizenId = extra.readVarInt();
        this.playerInventory = inv;

        final IColony colony;
        if (inv.player.world.isRemote)
        {
            colony = IColonyManager.getInstance().getColonyView(colonyId, inv.player.dimension.getId());
        }
        else
        {
            colony = IColonyManager.getInstance().getColonyByWorld(colonyId, inv.player.world);
        }

        if (colony == null)
        {
            inventorySize = 0;
            return;
        }

        final InventoryCitizen inventory;
        final BlockPos workBuilding;

        if (inv.player.world.isRemote)
        {
            final ICitizenDataView data = ((IColonyView) colony).getCitizen(citizenId);
            inventory = data.getInventory();
            this.displayName = data.getName();
            workBuilding = data.getWorkBuilding();
        }
        else
        {
            final ICitizenData data = colony.getCitizenManager().getCitizen(citizenId);
            inventory = data.getInventory();
            this.displayName = data.getName();
            workBuilding = data.getWorkBuilding() == null ? null : data.getWorkBuilding().getID();
        }

        this.inventorySize = inventory.getSlots() / INVENTORY_COLUMNS;
        final int size = inventory.getSlots();

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
                                    PLAYER_INVENTORY_OFFSET_EACH + j * PLAYER_INVENTORY_OFFSET_EACH)
                            {
                                @Override
                                public void putStack(@NotNull final ItemStack stack)
                                {
                                    if (workBuilding != null && !playerInventory.player.world.isRemote && !ItemStackUtils.isEmpty(stack))
                                    {
                                        final IColony colony = IColonyManager.getInstance().getColonyByWorld(colonyId, inv.player.world);
                                        final IBuilding building = colony.getBuildingManager().getBuilding(workBuilding);
                                        final ICitizenData citizenData = colony.getCitizenManager().getCitizen(citizenId);

                                        building.overruleNextOpenRequestOfCitizenWithStack(citizenData, stack);
                                    }
                                    super.putStack(stack);
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
            addSlot(new Slot(
                    playerInventory, i,
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
    @NotNull
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
     * Determines whether supplied player can use this container
     */
    @Override
    public boolean canInteractWith(@NotNull final PlayerEntity playerIn)
    {
        return true;
    }

    /**
     * Getter for the display name.
     * @return the display name.
     */
    public String getDisplayName()
    {
        return displayName;
    }
}