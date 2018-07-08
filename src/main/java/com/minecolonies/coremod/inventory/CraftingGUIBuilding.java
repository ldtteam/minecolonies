package com.minecolonies.coremod.inventory;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

import static com.minecolonies.api.util.constant.InventoryConstants.*;

public class CraftingGUIBuilding extends Container
{
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
     * Amount of slots in the crafting window.
     */
    private static final int ADDITIONAL_SLOTS = 5;

    /**
     * Start slot of the player hotbar.
     */
    private static final int HOTBAR_START = 32;

    /**
     * Total amount of slots in the GUI.
     */
    private static final int TOTAL_SLOTS = 41;

    /**
     * Amount of slots in the crafting window.
     */
    private static final int CRAFTING_SLOTS = 5;

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
    private final EntityPlayer player;

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
    public CraftingGUIBuilding(final InventoryPlayer playerInventory, final World worldIn, final boolean complete)
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
            public boolean canTakeStack(final EntityPlayer playerIn)
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

                    @Override
                    public ItemStack onTake(final EntityPlayer player, final ItemStack stack)
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
                        PLAYER_INVENTORY_INITIAL_Y_OFFSET + i * PLAYER_INVENTORY_OFFSET_EACH
                ));
            }
        }

        for (i = 0; i < INVENTORY_COLUMNS; i++)
        {
            addSlotToContainer(new Slot(
                    playerInventory, i,
                    PLAYER_INVENTORY_INITIAL_X_OFFSET + i * PLAYER_INVENTORY_OFFSET_EACH,
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
        if (!worldObj.isRemote)
        {
            final EntityPlayerMP entityplayermp = (EntityPlayerMP) player;
            ItemStack itemstack = ItemStack.EMPTY;
            final IRecipe irecipe = CraftingManager.findMatchingRecipe(craftMatrix, worldObj);
            if (irecipe != null && (irecipe.isDynamic()
                    || !worldObj.getGameRules().getBoolean("doLimitedCrafting")
                    || entityplayermp.getRecipeBook().isUnlocked(irecipe)
                    || entityplayermp.isCreative()))
            {
                this.craftResult.setRecipeUsed(irecipe);
                itemstack = irecipe.getCraftingResult(this.craftMatrix);
            }

            this.craftResult.setInventorySlotContents(0, itemstack);
            entityplayermp.connection.sendPacket(new SPacketSetSlot(this.windowId, 0, itemstack));
        }

        super.onCraftMatrixChanged(inventoryIn);
    }

    @Override
    public boolean canInteractWith(final EntityPlayer playerIn)
    {
        return true;
    }

    @Override
    public ItemStack slotClick(final int slotId, final int clickedButton, final ClickType mode, final EntityPlayer playerIn)
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

    @Nullable
    @Override
    public ItemStack transferStackInSlot(final EntityPlayer playerIn, final int index)
    {
        if (index <= CRAFTING_SLOTS + (complete ? ADDITIONAL_SLOTS : 0))
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
                if (!this.mergeItemStack(itemstack1, CRAFTING_SLOTS + (complete ? ADDITIONAL_SLOTS : 0), TOTAL_SLOTS+ (complete ? ADDITIONAL_SLOTS : 0), true))
                {
                    return ItemStackUtils.EMPTY;
                }
                slot.onSlotChange(itemstack1, itemstack);
            }
            else if (index < HOTBAR_START)
            {
                if (!this.mergeItemStack(itemstack1, HOTBAR_START, TOTAL_SLOTS + (complete ? ADDITIONAL_SLOTS : 0), false))
                {
                    return ItemStackUtils.EMPTY;
                }
            }
            else if ((index < TOTAL_SLOTS + (complete ? ADDITIONAL_SLOTS : 0)
                    && !this.mergeItemStack(itemstack1, CRAFTING_SLOTS + (complete ? ADDITIONAL_SLOTS : 0), HOTBAR_START, false))
                    || !this.mergeItemStack(itemstack1, CRAFTING_SLOTS + (complete ? ADDITIONAL_SLOTS : 0), TOTAL_SLOTS+ (complete ? ADDITIONAL_SLOTS : 0), false))
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
                return ItemStackUtils.EMPTY;
            }
        }
        return itemstack;
    }

    public List<Slot> getCraftingSlots()
    {
        if(complete)
        {
            return ImmutableList.of(getSlot(1), getSlot(2), getSlot(3), getSlot(4), getSlot(5), getSlot(6), getSlot(7), getSlot(8), getSlot(9));
        }
        else
        {
            return ImmutableList.of(getSlot(1), getSlot(2), getSlot(3), getSlot(4));
        }
    }

    @Override
    public boolean canMergeSlot(final ItemStack stack, final Slot slotIn)
    {
        return slotIn.inventory != this.craftResult && super.canMergeSlot(stack, slotIn);
    }

    /**
     * Getter for the craft matrix.
     * @return the matric.
     */
    public InventoryCrafting getCraftMatrix()
    {
        return craftMatrix;
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
    public EntityPlayer getPlayer()
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
