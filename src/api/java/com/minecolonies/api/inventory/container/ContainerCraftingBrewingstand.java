package com.minecolonies.api.inventory.container;

import com.minecolonies.api.inventory.ModContainers;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

import static com.minecolonies.api.util.constant.InventoryConstants.*;

/**
 * Crafting container for the recipe teaching of furnace recipes.
 */
public class ContainerCraftingBrewingstand extends AbstractContainerMenu
{
    /**
     * The furnace inventory.
     */
    private final IItemHandler brewingStandInventory;

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
    public static ContainerCraftingBrewingstand fromFriendlyByteBuf(final int windowId, final Inventory inv, final FriendlyByteBuf packetBuffer)
    {
        final BlockPos tePos = packetBuffer.readBlockPos();
        final int moduleId = packetBuffer.readInt();
        return new ContainerCraftingBrewingstand(windowId, inv, tePos, moduleId);
    }

    /**
     * Constructs the GUI with the player.
     *
     * @param windowId the window id.
     * @param inv      the player inventory.
     * @param pos      te world pos
     */
    public ContainerCraftingBrewingstand(final int windowId, final Inventory inv, final BlockPos pos, final int moduleId)
    {
        super(ModContainers.craftingBrewingstand.get(), windowId);
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
        public boolean mayPickup(final Player par1PlayerEntity)
        {
            return false;
        }
    }

    @Override
    public void clicked(final int slotId, final int clickedButton, final ClickType mode, final Player playerIn)
    {
        if (slotId >= 0 && slotId < brewingStandInventory.getSlots())
        {
            if (mode == ClickType.PICKUP
                  || mode == ClickType.PICKUP_ALL
                  || mode == ClickType.SWAP
                  || mode == ClickType.QUICK_MOVE)
            {
                final Slot slot = this.slots.get(slotId);
                handleSlotClick(slot, this.getCarried());
            }
        }
        else
        {
            super.clicked(slotId, clickedButton, mode, playerInventory.player);
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
    public boolean stillValid(@NotNull final Player playerIn)
    {
        return true;
    }

    @NotNull
    @Override
    public ItemStack quickMoveStack(final Player playerIn, final int index)
    {
        final Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem())
        {
            final ItemStack stack = slot.getItem();
            if (index < 3)
            {
                setContainer(ItemStack.EMPTY);
                return ItemStack.EMPTY;
            }
            if (index == 3)
            {
                setInput(ItemStack.EMPTY);
                return ItemStack.EMPTY;
            }

            if (BrewingRecipeRegistry.isValidIngredient(stack))
            {
                setInput(stack);
                return ItemStack.EMPTY;
            }
            else if (BrewingRecipeRegistry.isValidInput(stack) && stack.getCount() == 1)
            {
                setContainer(stack);
                return ItemStack.EMPTY;
            }
        }

        return ItemStack.EMPTY;
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
    public int getModuleId()
    {
        return this.moduleId;
    }
}
