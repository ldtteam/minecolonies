package com.minecolonies.coremod.inventory;

import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.math.BlockPos;
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

    public CraftingGUIBuilding(final InventoryPlayer playerInventory, final World worldIn, final BlockPos posIn)
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
    public void onCraftMatrixChanged(IInventory inventoryIn)
    {
        super.onCraftMatrixChanged(inventoryIn);
        //todo can use this to get the recipe on button click
        this.craftResult.setInventorySlotContents(0, CraftingManager.getInstance().findMatchingRecipe(this.craftMatrix, this.worldObj));
    }

    /**
     * Called when the container is closed.
     */
    @Override
    public void onContainerClosed(EntityPlayer playerIn)
    {
        super.onContainerClosed(playerIn);

        if (!this.worldObj.isRemote)
        {
            for (int i = 0; i < 9; ++i)
            {
                ItemStack itemstack = this.craftMatrix.removeStackFromSlot(i);

                if (itemstack != null)
                {
                    playerIn.dropItem(itemstack, false);
                }
            }
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return true;
    }


    @Nullable
    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
    {
        ItemStack itemstack = null;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index == 0)
            {
                if (!this.mergeItemStack(itemstack1, 5, 41, true))
                {
                    return null;
                }

                slot.onSlotChange(itemstack1, itemstack);
            }
            else if (index >= 5 && index < 32)
            {
                if (!this.mergeItemStack(itemstack1, 32, 41, false))
                {
                    return null;
                }
            }
            else if (index >= 32 && index < 41)
            {
                if (!this.mergeItemStack(itemstack1, 5, 32, false))
                {
                    return null;
                }
            }
            else if (!this.mergeItemStack(itemstack1, 5, 41, false))
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
    public boolean canMergeSlot(ItemStack stack, Slot slotIn)
    {
        return slotIn.inventory != this.craftResult && super.canMergeSlot(stack, slotIn);
    }
}
