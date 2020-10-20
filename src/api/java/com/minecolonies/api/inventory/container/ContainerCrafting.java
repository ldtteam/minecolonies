package com.minecolonies.api.inventory.container;

import com.ldtteam.structurize.api.util.ItemStackUtils;
import com.minecolonies.api.inventory.ModContainers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.CraftingResultSlot;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.minecolonies.api.util.constant.InventoryConstants.*;

/**
 * Crafting container for the recipe teaching of normal crafting recipes.
 */
public class ContainerCrafting extends Container
{
    /**
     * The crafting matrix inventory (2x2).
     */
    public final CraftingInventory craftMatrix;

    /**
     * The crafting matrix inventory (2x2).
     */
    public final CraftResultInventory craftResult = new CraftResultInventory();

    /**
     * The crafting result slot.
     */
    private final Slot craftResultSlot;

    /**
     * The secondary outputs
     */
    private final List<ItemStack> remainingItems;

    /**
     * Boolean variable defining if complete grid or not 3x3(true), 2x2(false).
     */
    private final boolean complete;

    /**
     * World world
     */
    private final World world;

    /**
     * The player inventory.
     */
    private final PlayerInventory inv;

    /**
     * Position of container.
     */
    private final BlockPos pos;

    /**
     * Creates a crafting container.
     *
     * @param windowId the window id.
     * @param inv      the inventory.
     * @param extra    some extra data.
     */
    public ContainerCrafting(final int windowId, final PlayerInventory inv, final PacketBuffer extra)
    {
        super(ModContainers.craftingGrid, windowId);
        this.world = inv.player.world;
        this.inv = inv;
        this.complete = extra.readBoolean();
        this.pos = extra.readBlockPos();
        if (complete)
        {
            craftMatrix = new CraftingInventory(this, 3, 3);
        }
        else
        {
            craftMatrix = new CraftingInventory(this, 2, 2);
        }

        this.craftResultSlot = this.addSlot(new CraftingResultSlot(inv.player, this.craftMatrix, craftResult, 0, X_CRAFT_RESULT, Y_CRAFT_RESULT)
        {
            @Override
            public boolean canTakeStack(final PlayerEntity playerIn)
            {
                return false;
            }
        });

        for (int i = 0; i < craftMatrix.getWidth(); ++i)
        {
            for (int j = 0; j < craftMatrix.getHeight(); ++j)
            {
                this.addSlot(new Slot(this.craftMatrix, j + i * (complete ? 3 : 2), X_OFFSET_CRAFTING + j * INVENTORY_OFFSET_EACH, Y_OFFSET_CRAFTING + i * INVENTORY_OFFSET_EACH)
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
                  PLAYER_INVENTORY_INITIAL_Y_OFFSET_CRAFTING + i * PLAYER_INVENTORY_OFFSET_EACH
                ));
            }
        }

        for (i = 0; i < INVENTORY_COLUMNS; i++)
        {
            addSlot(new Slot(
              inv, i,
              PLAYER_INVENTORY_INITIAL_X_OFFSET + i * PLAYER_INVENTORY_OFFSET_EACH,
              PLAYER_INVENTORY_HOTBAR_OFFSET_CRAFTING
            ));
        }

        
        remainingItems = new ArrayList<>();

        this.onCraftMatrixChanged(this.craftMatrix);
    }

    /**
     * Callback for when the crafting matrix is changed.
     */
    @Override
    public void onCraftMatrixChanged(final IInventory inventoryIn)
    {
        if (!world.isRemote)
        {
            final ServerPlayerEntity player = (ServerPlayerEntity) inv.player;
            final Optional<ICraftingRecipe> iRecipe = ((ServerPlayerEntity) inv.player).server.getRecipeManager().getRecipe(IRecipeType.CRAFTING, craftMatrix, world);
            final ItemStack stack;
            if (iRecipe.isPresent() && (iRecipe.get().isDynamic()
                                          || !world.getGameRules().getBoolean(GameRules.DO_LIMITED_CRAFTING)
                                          || player.getRecipeBook().isUnlocked(iRecipe.get())
                                          || player.isCreative()))
            {
                stack = iRecipe.get().getCraftingResult(this.craftMatrix);
                this.craftResultSlot.putStack(stack);
                player.connection.sendPacket(new SSetSlotPacket(this.windowId, 0, stack));
            }
            else
            {
                this.craftResultSlot.putStack(ItemStack.EMPTY);
                player.connection.sendPacket(new SSetSlotPacket(this.windowId, 0, ItemStack.EMPTY));
            }
        }

        super.onCraftMatrixChanged(inventoryIn);
    }

    @Override
    public boolean canInteractWith(@NotNull final PlayerEntity playerIn)
    {
        return true;
    }

    @NotNull
    @Override
    public ItemStack slotClick(final int slotId, final int clickedButton, final ClickType mode, final PlayerEntity playerIn)
    {
        if (slotId >= 1 && slotId < CRAFTING_SLOTS + (complete ? ADDITIONAL_SLOTS : 0))
        {
            // 1 is shift-click
            if (mode == ClickType.PICKUP
                  || mode == ClickType.PICKUP_ALL
                  || mode == ClickType.SWAP)
            {
                final Slot slot = this.inventorySlots.get(slotId);

                final ItemStack dropping = playerIn.inventory.getItemStack();

                return handleSlotClick(slot, dropping);
            }

            return ItemStack.EMPTY;
        }

        if (mode == ClickType.QUICK_MOVE)
        {
            return ItemStack.EMPTY;
        }

        return super.slotClick(slotId, clickedButton, mode, playerIn);
    }

    /**
     * Handle a slot click.
     *
     * @param slot  the clicked slot.
     * @param stack the used stack.
     * @return the result.
     */
    public ItemStack handleSlotClick(final Slot slot, final ItemStack stack)
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

    @NotNull
    @Override
    public ItemStack transferStackInSlot(final PlayerEntity playerIn, final int index)
    {
        final int total_crafting_slots = CRAFTING_SLOTS + (complete ? ADDITIONAL_SLOTS : 0);
        if (index <= total_crafting_slots)
        {
            return ItemStack.EMPTY;
        }

        final int total_slots = TOTAL_SLOTS + (complete ? ADDITIONAL_SLOTS : 0);

        ItemStack itemstack = ItemStackUtils.EMPTY;
        final Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack())
        {
            final ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (index == 0)
            {
                if (!this.mergeItemStack(itemstack1, total_crafting_slots, total_slots, true))
                {
                    return ItemStackUtils.EMPTY;
                }
                slot.onSlotChange(itemstack1, itemstack);
            }
            else if (index < HOTBAR_START)
            {
                if (!this.mergeItemStack(itemstack1, HOTBAR_START, total_slots, false))
                {
                    return ItemStackUtils.EMPTY;
                }
            }
            else if ((index < total_slots
                        && !this.mergeItemStack(itemstack1, total_crafting_slots, HOTBAR_START, false))
                       || !this.mergeItemStack(itemstack1, total_crafting_slots, total_slots, false))
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
        return slotIn.inventory != this.craftResultSlot && super.canMergeSlot(stack, slotIn);
    }

    /**
     * Getter for the world obj.
     *
     * @return the world obj.
     */
    public World getWorldObj()
    {
        return world;
    }

    /**
     * Getter for the player.
     *
     * @return the player.
     */
    public PlayerEntity getPlayer()
    {
        return inv.player;
    }

    /**
     * Getter for completeness.
     *
     * @return true if 3x3 and false for 2x2.
     */
    public boolean isComplete()
    {
        return complete;
    }

    /**
     * Get the craft matrix inv.
     *
     * @return the inv.
     */
    public CraftingInventory getInv()
    {
        return craftMatrix;
    }

    /**
     * Get for the container position.
     *
     * @return the position.
     */
    public BlockPos getPos()
    {
        return pos;
    }

    /**
     * Get for the remaining items. 
     * @return
     */
    public List<ItemStack> getRemainingItems()
    {
        final Optional<ICraftingRecipe> iRecipe = this.world.getRecipeManager().getRecipe(IRecipeType.CRAFTING, craftMatrix, world);
        if (iRecipe.isPresent())
        {
            List<ItemStack> ri = iRecipe.get().getRemainingItems(this.craftMatrix);
            remainingItems.clear();
            for(int i = 0; i< ri.size(); i++)
            {
                if(!ri.get(i).isEmpty())
                {
                    remainingItems.add(ri.get(i));
                }
            }
        }
        return remainingItems;
    }
}
