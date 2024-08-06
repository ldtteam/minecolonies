package com.minecolonies.api.inventory.container;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.inventory.ModContainers;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

import static com.minecolonies.api.util.constant.InventoryConstants.*;

/**
 * Crafting container for the recipe teaching of furnace recipes.
 */
public class ContainerCraftingFurnace extends AbstractContainerMenu
{
    /**
     * The furnace inventory.
     */
    private final IItemHandler furnaceInventory;

    /**
     * The player assigned to it.
     */
    private final Inventory playerInventory;

    /**
     * The colony building.
     */
    public final BlockPos buildingPos;

    /**
     * The module id.
     */
    private int moduleId;

    /**
     * Deserialize packet buffer to container instance.
     *
     * @param windowId     the id of the window.
     * @param inv          the player inventory.
     * @param packetBuffer network buffer
     * @return new instance
     */
    public static ContainerCraftingFurnace fromFriendlyByteBuf(final int windowId, final Inventory inv, final RegistryFriendlyByteBuf packetBuffer)
    {
        final BlockPos tePos = packetBuffer.readBlockPos();
        final int moduleId = packetBuffer.readInt();
        return new ContainerCraftingFurnace(windowId, inv, tePos, moduleId);
    }

    /**
     * Constructs the GUI with the player.
     *
     * @param windowId the window id.
     * @param inv      the player inventory.
     * @param pos      te world pos
     */
    public ContainerCraftingFurnace(final int windowId, final Inventory inv, final BlockPos pos, final int moduleId)
    {
        super(ModContainers.craftingFurnace.get(), windowId);
        this.moduleId = moduleId;
        this.furnaceInventory = new IItemHandlerModifiable()
        {
            ItemStack input = ItemStack.EMPTY;
            ItemStack output = ItemStack.EMPTY;

            @Override
            public void setStackInSlot(final int slot, @Nonnull final ItemStack stack)
            {
                final ItemStack copy = stack.copy();
                copy.setCount(1);
                if (slot == 0)
                {
                    input = copy;
                }
                else
                {
                    output = copy;
                }
            }

            @Override
            public int getSlots()
            {
                return 3;
            }

            @Nonnull
            @Override
            public ItemStack getStackInSlot(final int slot)
            {
                if (slot == 0)
                {
                    return input;
                }
                else
                {
                    return output;
                }
            }

            @Nonnull
            @Override
            public ItemStack insertItem(final int slot, @Nonnull final ItemStack stack, final boolean simulate)
            {
                final ItemStack copy = stack.copy();
                copy.setCount(1);
                if (slot == 0)
                {
                    input = copy;
                }
                else
                {
                    output = copy;
                }
                return stack;
            }

            @Nonnull
            @Override
            public ItemStack extractItem(final int slot, final int amount, final boolean simulate)
            {
                return ItemStack.EMPTY;
            }

            @Override
            public int getSlotLimit(final int slot)
            {
                return 1;
            }

            @Override
            public boolean isItemValid(final int slot, @Nonnull final ItemStack stack)
            {
                if (slot == 0)
                {
                    return !IMinecoloniesAPI.getInstance().getFurnaceRecipes().getSmeltingResult(stack).isEmpty();
                }
                else
                {
                    return false;
                }
            }
        };
        this.playerInventory = inv;
        this.buildingPos = pos;
        this.addSlot(new SlotItemHandler(furnaceInventory, 0, 56, 17)
        {
            @Override
            public int getMaxStackSize()
            {
                return 1;
            }

            @NotNull
            @Override
            public ItemStack remove(final int par1)
            {
                return ItemStack.EMPTY;
            }

            @Override
            public boolean mayPlace(final ItemStack par1ItemStack)
            {
                return true;
            }

            @Override
            public boolean mayPickup(final Player par1PlayerEntity)
            {
                return false;
            }
        });

        this.addSlot(new SlotItemHandler(furnaceInventory, 1, 116, 35));

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
                  PLAYER_INVENTORY_INITIAL_Y_OFFSET_CRAFTING + i * PLAYER_INVENTORY_OFFSET_EACH
                ));
            }
        }

        for (i = 0; i < INVENTORY_COLUMNS; i++)
        {
            addSlot(new Slot(
              playerInventory, i,
              PLAYER_INVENTORY_INITIAL_X_OFFSET + i * PLAYER_INVENTORY_OFFSET_EACH,
              PLAYER_INVENTORY_HOTBAR_OFFSET_CRAFTING
            ));
        }
    }

    @Override
    public void clicked(final int slotId, final int clickedButton, final ClickType mode, final Player playerIn)
    {
        if (slotId >= 0 && slotId < FURNACE_SLOTS)
        {
            // 1 is shift-click
            if (mode == ClickType.PICKUP
                  || mode == ClickType.PICKUP_ALL
                  || mode == ClickType.SWAP)
            {
                final Slot slot = this.slots.get(slotId);
                handleSlotClick(slot, this.getCarried());
            }
        }
        else
        {
            super.clicked(slotId, clickedButton, mode, playerInventory.player);
        }

        updateFurnaceOutput();
    }

    /**
     * Sets the furnace input item (intended mainly for crafting teaching).
     *
     * @param stack The input stack.
     */
    public void setFurnaceInput(final ItemStack stack)
    {
        handleSlotClick(getSlot(0), stack);
        updateFurnaceOutput();
    }

    /**
     * Handle a slot click.
     *
     * @param slot  the clicked slot.
     * @param stack the used stack.
     * @return the result.
     */
    private ItemStack handleSlotClick(final Slot slot, final ItemStack stack)
    {
        if (stack.getCount() > 0)
        {
            final ItemStack copy = stack.copy();
            copy.setCount(1);
            slot.set(copy);
        }
        else if (slot.getItem().getCount() > 0)
        {
            slot.set(ItemStack.EMPTY);
        }

        return slot.getItem().copy();
    }

    /**
     * Update the furnace output slot when called server-side.
     */
    private void updateFurnaceOutput()
    {
        if (!playerInventory.player.level().isClientSide)
        {
            final ServerPlayer player = (ServerPlayer) playerInventory.player;
            final ItemStack result = IMinecoloniesAPI.getInstance().getFurnaceRecipes().getSmeltingResult(furnaceInventory.getStackInSlot(0));

            this.furnaceInventory.insertItem(1, result, false);
            player.connection.send(new ClientboundContainerSetSlotPacket(this.containerId, 0, 1, result));
        }
    }

    @Override
    public boolean stillValid(@NotNull final Player playerIn)
    {
        return true;
    }

    @NotNull
    @Override
    public ItemStack quickMoveStack(final Player playerIn, final int index)
    {
        if (index <= FURNACE_SLOTS)
        {
            return ItemStack.EMPTY;
        }

        ItemStack itemstack = ItemStackUtils.EMPTY;
        final Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem())
        {
            final ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index == 0)
            {
                if (!this.moveItemStackTo(itemstack1, FURNACE_SLOTS, TOTAL_SLOTS_FURNACE, true))
                {
                    return ItemStackUtils.EMPTY;
                }
                slot.onQuickCraft(itemstack1, itemstack);
            }
            else if (index < HOTBAR_START)
            {
                if (!this.moveItemStackTo(itemstack1, HOTBAR_START, TOTAL_SLOTS_FURNACE, false))
                {
                    return ItemStackUtils.EMPTY;
                }
            }
            else if ((index < TOTAL_SLOTS_FURNACE
                        && !this.moveItemStackTo(itemstack1, FURNACE_SLOTS, HOTBAR_START, false))
                       || !this.moveItemStackTo(itemstack1, FURNACE_SLOTS, TOTAL_SLOTS_FURNACE, false))
            {
                return ItemStack.EMPTY;
            }
            if (itemstack1.getCount() == 0)
            {
                slot.set(ItemStackUtils.EMPTY);
            }
            else
            {
                slot.setChanged();
            }
            if (itemstack1.getCount() == itemstack.getCount())
            {
                return ItemStackUtils.EMPTY;
            }
        }
        return itemstack;
    }

    @Override
    public boolean canTakeItemForPickAll(final ItemStack stack, final Slot slotIn)
    {
        return !(slotIn instanceof FurnaceResultSlot) && super.canTakeItemForPickAll(stack, slotIn);
    }

    /**
     * Getter for the player.
     *
     * @return the player.
     */
    public Player getPlayer()
    {
        return playerInventory.player;
    }

    /**
     * Getter for the world obj.
     *
     * @return the world obj.
     */
    public Level getWorldObj()
    {
        return playerInventory.player.level();
    }

    /**
     * Get the position of the container.
     *
     * @return the position.
     */
    public BlockPos getPos()
    {
        return buildingPos;
    }

    /**
     * Get the module if of the container.
     * @return the module id.
     */
    public int getModuleId()
    {
        return this.moduleId;
    }
}
