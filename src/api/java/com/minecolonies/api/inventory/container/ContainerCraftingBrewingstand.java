package com.minecolonies.api.inventory.container;

import com.minecolonies.api.inventory.ModContainers;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

import static com.minecolonies.api.util.constant.InventoryConstants.*;

/**
 * Crafting container for the recipe teaching of furnace recipes.
 */
public class ContainerCraftingBrewingstand extends Container
{
    /**
     * The furnace inventory.
     */
    private final IItemHandler brewingStandInventory;

    /**
     * The player assigned to it.
     */
    private final PlayerInventory playerInventory;

    /**
     * The colony building.
     */
    public final BlockPos buildingPos;

    /**
     * The module id.
     */
    private String moduleId;

    /**
     * Deserialize packet buffer to container instance.
     *
     * @param windowId     the id of the window.
     * @param inv          the player inventory.
     * @param packetBuffer network buffer
     * @return new instance
     */
    public static ContainerCraftingBrewingstand fromFriendlyByteBuf(final int windowId, final PlayerInventory inv, final PacketBuffer packetBuffer)
    {
        final BlockPos tePos = packetBuffer.readBlockPos();
        final String moduleId = packetBuffer.readUtf(32767);
        return new ContainerCraftingBrewingstand(windowId, inv, tePos, moduleId);
    }

    /**
     * Constructs the GUI with the player.
     *
     * @param windowId the window id.
     * @param inv      the player inventory.
     * @param pos      te world pos
     */
    public ContainerCraftingBrewingstand(final int windowId, final PlayerInventory inv, final BlockPos pos, final String moduleId)
    {
        super(ModContainers.craftingBrewingstand, windowId);
        this.moduleId = moduleId;

        this.brewingStandInventory = new IItemHandlerModifiable()
        {
            ItemStack ingredient = ItemStack.EMPTY;
            ItemStack potion = ItemStack.EMPTY;

            @Override
            public void setStackInSlot(final int slot, @Nonnull final ItemStack stack)
            {
                if (!isItemValid(slot, stack) && !ItemStackUtils.isEmpty(stack))
                {
                    return;
                }

                final ItemStack copy = stack.copy();
                copy.setCount(1);
                if (slot == 3)
                {
                    ingredient = copy;
                }
                else
                {
                    potion = copy;
                }
            }

            @Override
            public int getSlots()
            {
                return 4;
            }

            @Nonnull
            @Override
            public ItemStack getStackInSlot(final int slot)
            {
                if (slot == 3)
                {
                    return ingredient;
                }
                else
                {
                    return potion;
                }
            }

            @Nonnull
            @Override
            public ItemStack insertItem(final int slot, @Nonnull final ItemStack stack, final boolean simulate)
            {
                if (!isItemValid(slot, stack) && !ItemStackUtils.isEmpty(stack))
                {
                    return stack;
                }

                final ItemStack copy = stack.copy();
                copy.setCount(1);
                if (slot == 3)
                {
                    ingredient = copy;
                }
                else
                {
                    potion = copy;
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
                if (slot == 3)
                {
                    return net.minecraftforge.common.brewing.BrewingRecipeRegistry.isValidIngredient(stack);
                }
                else if (slot >= 0 && slot < 3)
                {
                    return net.minecraftforge.common.brewing.BrewingRecipeRegistry.isValidInput(stack);
                }
                else
                {
                    return false;
                }
            }
        };
        this.playerInventory = inv;
        this.buildingPos = pos;

        this.addSlot(new SlotItemHandler(brewingStandInventory, 3, 79, 17));

        this.addSlot(new InputItemHandler(brewingStandInventory, 0, 56, 51));
        this.addSlot(new InputItemHandler(brewingStandInventory, 1, 79, 58));
        this.addSlot(new InputItemHandler(brewingStandInventory, 2, 102, 51));

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

    /**
     * Special input item handler for the brewing stand.
     */
    private static class InputItemHandler extends SlotItemHandler
    {
        /**
         * Default constructor.
         * @param itemHandler the inventory.
         * @param index the index.
         * @param xPosition x positon.
         * @param yPosition y position.
         */
        public InputItemHandler(final IItemHandler itemHandler, final int index, final int xPosition, final int yPosition)
        {
            super(itemHandler, index, xPosition, yPosition);
        }

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
        public boolean mayPlace(final @NotNull ItemStack par1ItemStack)
        {
            return true;
        }

        @Override
        public boolean mayPickup(final PlayerEntity par1PlayerEntity)
        {
            return false;
        }
    }

    @Override
    public @NotNull ItemStack clicked(final int slotId, final int clickedButton, final @NotNull ClickType mode, final @NotNull PlayerEntity playerIn)
    {
        if (slotId >= 0 && slotId < brewingStandInventory.getSlots())
        {
            // 1 is shift-click
            if (mode == ClickType.PICKUP
                  || mode == ClickType.PICKUP_ALL
                  || mode == ClickType.SWAP)
            {
                final Slot slot = this.slots.get(slotId);
                return handleSlotClick(slot, playerIn.inventory.getCarried());
            }
            return ItemStack.EMPTY;
        }
        else
        {
            return super.clicked(slotId, clickedButton, mode, playerInventory.player);
        }
    }

    /**
     * Sets the input item (intended mainly for crafting teaching).
     *
     * @param stack The input stack.
     */
    public void setInput(final ItemStack stack)
    {
        handleSlotClick(getSlot(0), stack);
    }

    /**
     * Sets the container (input potion, intended mostly for crafting teaching).
     *
     * @param stack The container stack.
     */
    public void setContainer(final ItemStack stack)
    {
        handleSlotClick(getSlot(1), stack);
        handleSlotClick(getSlot(2), stack);
        handleSlotClick(getSlot(3), stack);
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

    @Override
    public boolean stillValid(@NotNull final PlayerEntity playerIn)
    {
        return true;
    }

    @NotNull
    @Override
    public ItemStack quickMoveStack(final PlayerEntity playerIn, final int index)
    {
        if (index <= brewingStandInventory.getSlots())
        {
            return ItemStack.EMPTY;
        }
        final int furnaceSlots = brewingStandInventory.getSlots();
        final int totalSlots = playerInventory.getContainerSize() + furnaceSlots;

        ItemStack itemstack = ItemStackUtils.EMPTY;
        final Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem())
        {
            final ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index == 0)
            {
                if (!this.moveItemStackTo(itemstack1, furnaceSlots, totalSlots, true))
                {
                    return ItemStackUtils.EMPTY;
                }
                slot.onQuickCraft(itemstack1, itemstack);
            }
            else if (index < HOTBAR_START)
            {
                if (!this.moveItemStackTo(itemstack1, HOTBAR_START, totalSlots, false))
                {
                    return ItemStackUtils.EMPTY;
                }
            }
            else if ((index < totalSlots
                        && !this.moveItemStackTo(itemstack1, furnaceSlots, HOTBAR_START, false))
                       || !this.moveItemStackTo(itemstack1, furnaceSlots, totalSlots, false))
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

    /**
     * Getter for the player.
     *
     * @return the player.
     */
    public PlayerEntity getPlayer()
    {
        return playerInventory.player;
    }

    /**
     * Getter for the world obj.
     *
     * @return the world obj.
     */
    public World getWorldObj()
    {
        return playerInventory.player.level;
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
    public String getModuleId()
    {
        return this.moduleId;
    }
}
