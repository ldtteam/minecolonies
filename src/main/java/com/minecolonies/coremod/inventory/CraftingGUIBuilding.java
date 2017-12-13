package com.minecolonies.coremod.inventory;

import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.world.World;

import javax.annotation.Nullable;

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

        this.addSlotToContainer(new SlotCrafting(playerInventory.player, this.craftMatrix, this.craftResult, 0, X_CRAFT_RESULT, Y_CRAFT_RESULT));

        for (int i = 0; i < 2; ++i)
        {
            for (int j = 0; j < 2; ++j)
            {
                this.addSlotToContainer(new Slot(this.craftMatrix, j + i * 2, X_OFFSET_CRAFTING + j * INVENTORY_OFFSET_EACH, Y_OFFSET_CRAFTING + i * INVENTORY_OFFSET_EACH));
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

    /**
     * Called when the container is closed.
     */
    @Override
    public void onContainerClosed(final EntityPlayer playerIn)
    {
        super.onContainerClosed(playerIn);

        if (!this.worldObj.isRemote)
        {
            for (int i = 0; i < SLOTS_PER_LINE; ++i)
            {
                final ItemStack itemstack = this.craftMatrix.removeStackFromSlot(i);

                if (itemstack != null)
                {
                    playerIn.dropItem(itemstack, false);
                }
            }
        }
    }

    @Override
    public boolean canInteractWith(final EntityPlayer playerIn)
    {
        return true;
    }


    @Nullable
    @Override
    public ItemStack transferStackInSlot(final EntityPlayer playerIn, final int index)
    {
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
                slot.putStack((ItemStack) ItemStackUtils.EMPTY);
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

    @Override
    public boolean canMergeSlot(final ItemStack stack, final Slot slotIn)
    {
        return slotIn.inventory != this.craftResult && super.canMergeSlot(stack, slotIn);
    }
}
