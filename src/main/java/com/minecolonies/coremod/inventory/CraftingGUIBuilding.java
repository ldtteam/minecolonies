package com.minecolonies.coremod.inventory;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CraftingGUIBuilding extends Container
{
    /**
     * Amount of rows in the player inventory.
     */
    private static final int PLAYER_INVENTORY_ROWS = 3;

    /**
     * Amount of columns in the player inventory.
     */
    private static final int PLAYER_INVENTORY_COLUMNS = 9;

    /**
     * Initial x-offset of the inventory slot.
     */
    private static final int PLAYER_INVENTORY_INITIAL_X_OFFSET = 8;

    /**
     * Initial y-offset of the inventory slot.
     */
    private static final int PLAYER_INVENTORY_INITIAL_Y_OFFSET = 84;

    /**
     * Each offset of the inventory slots.
     */
    private static final int INVENTORY_OFFSET_EACH = 18;

    /**
     * Initial y-offset of the inventory slots in the hotbar.
     */
    private static final int PLAYER_INVENTORY_HOTBAR_OFFSET = 142;

    /**
     * The x position of the crafting result slot position.
     */
    private static final int X_CRAFT_RESULT = 124;

    /**
     * The y position of the crafting result slot position.
     */
    private static final int Y_CRAFT_RESULT = 35;

    /**
     * The x offset of the crafting slot position.
     */
    private static final int X_OFFSET_CRAFTING = 30;

    /**
     * The y offset of the crafting slot position.
     */
    private static final int Y_OFFSET_CRAFTING = 17;

    /**
     * Amount of slots in the crafting window.
     */
    private static final int CRAFTING_SLOTS = 5;

    /**
     * Amount of slots per line in the GUI.
     */
    private static final int SLOTS_PER_LINE = 9;

    /**
     * Start slot of the player hotbar.
     */
    private static final int HOTBAR_START = 32;

    /**
     * Total amount of slots in the GUI.
     */
    private static final int TOTAL_SLOTS = 41;

    /**
     * The crafting matrix inventory (2x2).
     */
    private final InventoryCrafting craftMatrix = new InventoryCrafting(this, 2, 2);

    /**
     * The crafting result slot.
     */
    private final IInventory craftResult = new InventoryCraftResult();

    /**
     * The world object.
     */
    private final World worldObj;

    public CraftingGUIBuilding(final InventoryPlayer playerInventory, final World worldIn)
    {
        super();
        this.worldObj = worldIn;

        this.addSlotToContainer(new SlotCrafting(playerInventory.player, this.craftMatrix, this.craftResult, 0, X_CRAFT_RESULT, Y_CRAFT_RESULT) {
            @Override
            public boolean canTakeStack(final EntityPlayer playerIn)
            {
                return false;
            }
        });

        for (int i = 0; i < 2; ++i)
        {
            for (int j = 0; j < 2; ++j)
            {
                this.addSlotToContainer(new Slot(this.craftMatrix, j + i * 2, X_OFFSET_CRAFTING + j * INVENTORY_OFFSET_EACH, Y_OFFSET_CRAFTING + i * INVENTORY_OFFSET_EACH){
                    @Override
                    public int getSlotStackLimit()
                    {
                        return 1;
                    }

                    @Override
                    public ItemStack onTake(EntityPlayer p_190901_1_, ItemStack p_190901_2_)
                    {
                        return ItemStack.EMPTY;
                    }

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
                    public boolean canTakeStack(final EntityPlayer par1EntityPlayer)
                    {
                        return false;
                    }
                });
            }
        }

        int i;
        for (i = 0; i < PLAYER_INVENTORY_ROWS; i++)
        {
            for (int j = 0; j < PLAYER_INVENTORY_COLUMNS; j++)
            {
                addSlotToContainer(new Slot(
                        playerInventory,
                        j + i * PLAYER_INVENTORY_COLUMNS + PLAYER_INVENTORY_COLUMNS,
                        PLAYER_INVENTORY_INITIAL_X_OFFSET + j * INVENTORY_OFFSET_EACH,
                        PLAYER_INVENTORY_INITIAL_Y_OFFSET + i * INVENTORY_OFFSET_EACH
                ));
            }
        }

        for (i = 0; i < PLAYER_INVENTORY_COLUMNS; i++)
        {
            addSlotToContainer(new Slot(
                    playerInventory, i,
                    PLAYER_INVENTORY_INITIAL_X_OFFSET + i * INVENTORY_OFFSET_EACH,
                    PLAYER_INVENTORY_HOTBAR_OFFSET
            ));
        }

        this.onCraftMatrixChanged(this.craftMatrix);
    }

    @Override
    protected final Slot addSlotToContainer(final Slot slotToAdd)
    {
        return super.addSlotToContainer(slotToAdd);
    }

    /**
     * Callback for when the crafting matrix is changed.
     */
    @Override
    public void onCraftMatrixChanged(final IInventory inventoryIn)
    {
        super.onCraftMatrixChanged(inventoryIn);
        this.craftResult.setInventorySlotContents(0, CraftingManager.getInstance().findMatchingRecipe(this.craftMatrix, this.worldObj));
    }

    @Override
    public boolean canInteractWith(final EntityPlayer playerIn)
    {
        return true;
    }

    @Override
    public ItemStack slotClick(int slotId, int clickedButton, ClickType mode, EntityPlayer playerIn)
    {
        if (slotId >= 1 && slotId < 5)
        {
            if (mode == ClickType.PICKUP || mode == ClickType.PICKUP_ALL ||
                  mode == ClickType.SWAP) // 1 is shift-click
            {
                Slot slot = this.inventorySlots.get(slotId);

                ItemStack dropping = playerIn.inventory.getItemStack();

                return handleSlotClick(slot, dropping);
            }

            return ItemStack.EMPTY;
        }

        return super.slotClick(slotId, clickedButton, mode, playerIn);
    }

    public ItemStack handleSlotClick(Slot slot, ItemStack stack)
    {
        if (stack.getCount() > 0)
        {
            ItemStack copy = stack.copy();
            copy.setCount(1);
            slot.putStack(copy);
        }
        else if (slot.getStack().getCount() > 0)
        {
            slot.putStack(ItemStack.EMPTY);
        }

        return slot.getStack().copy();
    }

    @Nullable
    @Override
    public ItemStack transferStackInSlot(final EntityPlayer playerIn, final int index)
    {
        if (index <= 5)
        {
            return ItemStack.EMPTY;
        }

        ItemStack itemstack = null;
        final Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            final ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index == 0)
            {
                if (!this.mergeItemStack(itemstack1, CRAFTING_SLOTS, TOTAL_SLOTS, true))
                {
                    return null;
                }

                slot.onSlotChange(itemstack1, itemstack);
            }
            else if (index >= CRAFTING_SLOTS && index < HOTBAR_START)
            {
                if (!this.mergeItemStack(itemstack1, HOTBAR_START, TOTAL_SLOTS, false))
                {
                    return null;
                }
            }
            else if (index >= HOTBAR_START && index < TOTAL_SLOTS)
            {
                if (!this.mergeItemStack(itemstack1, CRAFTING_SLOTS, HOTBAR_START, false))
                {
                    return null;
                }
            }
            else if (!this.mergeItemStack(itemstack1, CRAFTING_SLOTS, TOTAL_SLOTS, false))
            {
                return null;
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
                return null;
            }

            slot.onTake(playerIn, itemstack1);
        }

        return itemstack;
    }

    public List<Slot> getCraftingSlots()
    {
        return ImmutableList.of(getSlot(1), getSlot(2), getSlot(3), getSlot(4));
    }

    @Override
    public boolean canMergeSlot(final ItemStack stack, final Slot slotIn)
    {
        return slotIn.inventory != this.craftResult && super.canMergeSlot(stack, slotIn);
    }
}
