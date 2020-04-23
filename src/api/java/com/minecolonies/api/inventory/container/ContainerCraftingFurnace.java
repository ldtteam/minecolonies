package com.minecolonies.api.inventory.container;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.inventory.ModContainers;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.FurnaceResultSlot;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

import static com.minecolonies.api.util.constant.InventoryConstants.*;

/**
 * Crafting container for the recipe teaching of furnace recipes.
 */
public class ContainerCraftingFurnace extends Container
{
    /**
     * The furnace inventory.
     */
    private final IItemHandler furnaceInventory;

    /**
     * The player assigned to it.
     */
    private final PlayerInventory playerInventory;

    /**
     * The colony building.
     */
    public final BlockPos buildingPos;

    /**
     * Constructs the GUI with the player.
     * @param windowId the window id.
     * @param inv the player inventory.
     * @param extra extra data.
     */
    public ContainerCraftingFurnace(final int windowId, final PlayerInventory inv, final PacketBuffer extra)
    {
        super(ModContainers.craftingFurnace, windowId);
        this.furnaceInventory = new IItemHandlerModifiable() {

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
        buildingPos = extra.readBlockPos();
        this.addSlot(new SlotItemHandler(furnaceInventory, 0 , 56, 17)
        {
            @Override
            public int getSlotStackLimit()
            {
                return 1;
            }

            @NotNull
            @Override
            public ItemStack onTake(final PlayerEntity player, @NotNull final ItemStack stack)
            {
                return ItemStack.EMPTY;
            }

            @NotNull
            @Override
            public ItemStack decrStackSize(final int par1)
            {
                return ItemStack.EMPTY;
            }

            @Override
            public boolean isItemValid(final ItemStack par1ItemStack)
            {
                return false;
            }

            @Override
            public boolean canTakeStack(final PlayerEntity par1PlayerEntity)
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

    @NotNull
    @Override
    public ItemStack slotClick(final int slotId, final int clickedButton, final ClickType mode, final PlayerEntity playerIn)
    {
        final ItemStack clickResult;
        if (slotId >= 0 && slotId < FURNACE_SLOTS)
        {
            // 1 is shift-click
            if (mode == ClickType.PICKUP
                  || mode == ClickType.PICKUP_ALL
                  || mode == ClickType.SWAP)
            {
                final Slot slot = this.inventorySlots.get(slotId);

                final ItemStack dropping = playerIn.inventory.getItemStack();

                clickResult = handleSlotClick(slot, dropping);
            }
            else
            {
                clickResult = ItemStack.EMPTY;
            }
        }
        else if(mode == ClickType.QUICK_MOVE)
        {
            clickResult = ItemStack.EMPTY;
        }
        else
        {
            clickResult = super.slotClick(slotId, clickedButton, mode, playerInventory.player);
        }

        if (!playerInventory.player.world.isRemote)
        {
            final ServerPlayerEntity player = (ServerPlayerEntity) playerIn;
            final ItemStack result = IMinecoloniesAPI.getInstance().getFurnaceRecipes().getSmeltingResult(furnaceInventory.getStackInSlot(0));

            if (result != ItemStack.EMPTY)
            {
                this.furnaceInventory.insertItem(1, result, false);
                player.connection.sendPacket(new SSetSlotPacket(this.windowId, 1, result));
            }
        }
        return clickResult;
    }

    /**
     * Handle a slot click.
     * @param slot the clicked slot.
     * @param stack the used stack.
     * @return the result.
     */
    private ItemStack handleSlotClick(final Slot slot, final ItemStack stack)
    {
        if (stack.getCount() > 0)
        {
            final ItemStack copy = stack.copy();
            copy.setCount(1);
            slot.putStack(copy);
        }
        else if (slot.getStack().getCount() > 0)
        {
            slot.putStack(ItemStack.EMPTY);
        }

        return slot.getStack().copy();
    }

    @Override
    public boolean canInteractWith(@NotNull final PlayerEntity playerIn)
    {
        return true;
    }

    @NotNull
    @Override
    public ItemStack transferStackInSlot(final PlayerEntity playerIn, final int index)
    {
        if (index <= FURNACE_SLOTS)
        {
            return ItemStack.EMPTY;
        }

        ItemStack itemstack = ItemStackUtils.EMPTY;
        final Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack())
        {
            final ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (index == 0)
            {
                if (!this.mergeItemStack(itemstack1, FURNACE_SLOTS, TOTAL_SLOTS_FURNACE, true))
                {
                    return ItemStackUtils.EMPTY;
                }
                slot.onSlotChange(itemstack1, itemstack);
            }
            else if (index < HOTBAR_START)
            {
                if (!this.mergeItemStack(itemstack1, HOTBAR_START, TOTAL_SLOTS_FURNACE, false))
                {
                    return ItemStackUtils.EMPTY;
                }
            }
            else if ((index < TOTAL_SLOTS_FURNACE
                        && !this.mergeItemStack(itemstack1, FURNACE_SLOTS, HOTBAR_START, false))
                       || !this.mergeItemStack(itemstack1, FURNACE_SLOTS, TOTAL_SLOTS_FURNACE, false))
            {
                return ItemStack.EMPTY;
            }
            if (itemstack1.getCount() == 0)
            {
                slot.putStack(ItemStackUtils.EMPTY);
            }
            else
            {
                slot.onSlotChanged();
            }
            if (itemstack1.getCount() == itemstack.getCount())
            {
                return ItemStackUtils.EMPTY;
            }
        }
        return itemstack;
    }

    @Override
    public boolean canMergeSlot(final ItemStack stack, final Slot slotIn)
    {
        return !(slotIn instanceof FurnaceResultSlot) && super.canMergeSlot(stack, slotIn);
    }

    /**
     * Get the position of the container.
     * @return the position.
     */
    public BlockPos getPos()
    {
        return buildingPos;
    }
}
