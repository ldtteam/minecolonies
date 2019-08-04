package com.minecolonies.coremod.inventory;

import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.InventoryConstants.*;

/**
 * Crafting container for the recipe teaching of normal crafting recipes.
 */
public class CraftingGUIBuilding extends Container
{
    /**
     * The crafting matrix inventory (2x2).
     */
    private final InventoryCrafting craftMatrix;

    /**
     * The crafting result slot.
     */
    private final InventoryCraftResult craftResult = new InventoryCraftResult();

    /**
     * The world object.
     */
    private final World worldObj;

    /**
     * The player assigned to it.
     */
    private final PlayerEntity player;

    /**
     * Boolean variable defining if complete grid or not 3x3(true), 2x2(false).
     */
    private final boolean complete;

    /**
     * Creates a crafting container.
     * @param playerInventory the players inv.
     * @param worldIn the world.
     * @param complete if 3x3(true) or 2x2(false).
     */
    public CraftingGUIBuilding(final PlayerInventory playerInventory, final World worldIn, final boolean complete)
    {
        super();
        this.worldObj = worldIn;
        this.player = playerInventory.player;
        this.complete = complete;
        if(complete)
        {
            craftMatrix = new InventoryCrafting(this, 3, 3);
        }
        else
        {
            craftMatrix = new InventoryCrafting(this, 2, 2);
        }

        this.addSlotToContainer(new SlotCrafting(playerInventory.player, this.craftMatrix, this.craftResult, 0, X_CRAFT_RESULT, Y_CRAFT_RESULT)
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
                this.addSlotToContainer(new Slot(this.craftMatrix, j + i * (complete ? 3 : 2) , X_OFFSET_CRAFTING + j * INVENTORY_OFFSET_EACH, Y_OFFSET_CRAFTING + i * INVENTORY_OFFSET_EACH)
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
                addSlotToContainer(new Slot(
                        playerInventory,
                        j + i * INVENTORY_COLUMNS + INVENTORY_COLUMNS,
                        PLAYER_INVENTORY_INITIAL_X_OFFSET + j * PLAYER_INVENTORY_OFFSET_EACH,
                        PLAYER_INVENTORY_INITIAL_Y_OFFSET_CRAFTING + i * PLAYER_INVENTORY_OFFSET_EACH
                ));
            }
        }

        for (i = 0; i < INVENTORY_COLUMNS; i++)
        {
            addSlotToContainer(new Slot(
                    playerInventory, i,
                    PLAYER_INVENTORY_INITIAL_X_OFFSET + i * PLAYER_INVENTORY_OFFSET_EACH,
              PLAYER_INVENTORY_HOTBAR_OFFSET_CRAFTING
            ));
        }

        this.onCraftMatrixChanged(this.craftMatrix);
    }

    /**
     * Callback for when the crafting matrix is changed.
     */
    @Override
    public void onCraftMatrixChanged(final IInventory inventoryIn)
    {
        if (!worldObj.isRemote)
        {
            final ServerPlayerEntity PlayerEntitymp = (ServerPlayerEntity) player;
            ItemStack itemstack = ItemStack.EMPTY;
            final IRecipe irecipe = CraftingManager.findMatchingRecipe(craftMatrix, worldObj);
            if (irecipe != null && (irecipe.isDynamic()
                    || !worldObj.getGameRules().getBoolean("doLimitedCrafting")
                    || ServerPlayerEntity.getRecipeBook().isUnlocked(irecipe)
                    || ServerPlayerEntity.isCreative()))
            {
                this.craftResult.setRecipeUsed(irecipe);
                itemstack = irecipe.getCraftingResult(this.craftMatrix);
            }

            this.craftResult.setInventorySlotContents(0, itemstack);
            ServerPlayerEntity.connection.sendPacket(new SPacketSetSlot(this.windowId, 0, itemstack));
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

        if(mode == ClickType.QUICK_MOVE)
        {
            return ItemStack.EMPTY;
        }

        return super.slotClick(slotId, clickedButton, mode, playerIn);
    }

    /**
     * Handle a slot click.
     * @param slot the clicked slot.
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
        return slotIn.inventory != this.craftResult && super.canMergeSlot(stack, slotIn);
    }

    /**
     * Getter for the world obj.
     * @return the world obj.
     */
    public World getWorldObj()
    {
        return worldObj;
    }

    /**
     * Getter for the player.
     * @return the player.
     */
    public PlayerEntity getPlayer()
    {
        return player;
    }

    /**
     * Getter for completeness.
     * @return true if 3x3 and false for 2x2.
     */
    public boolean isComplete()
    {
        return complete;
    }
}
