package com.minecolonies.coremod.inventory;

import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.InventoryConstants.*;

/**
 * Crafting container for the recipe teaching of furnace recipes.
 */
public class ContainerGUICraftingFurnace extends Container
{
    /**
     * The furnace inventory.
     */
    private final IInventory furnaceInventory;

    /**
     * The world object.
     */
    private final World worldObj;

    /**
     * The player assigned to it.
     */
    private final PlayerEntity player;

    /**
     * Constructs the GUI with the player and furnace inventory.
     * @param playerInventory the player inventory.
     * @param worldIn the world.
     */
    public ContainerGUICraftingFurnace(final InventoryPlayer playerInventory, final World worldIn)
    {
        super();
        this.furnaceInventory = new TileEntityFurnace();
        this.worldObj = worldIn;
        this.player = playerInventory.player;
        this.addSlotToContainer(new Slot(furnaceInventory, 0 , 56, 17)
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
            public boolean canTakeStack(final PlayerEntity par1EntityPlayer)
            {
                return false;
            }
        });

        this.addSlotToContainer(new SlotFurnaceOutput(playerInventory.player, furnaceInventory, 1, 116, 35));

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

                final ItemStack dropping = player.inventory.getItemStack();

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
            clickResult = super.slotClick(slotId, clickedButton, mode, player);
        }

        if (!worldObj.isRemote)
        {
            final EntityPlayerMP entityPlayerMP = (EntityPlayerMP) player;
            final ItemStack result = FurnaceRecipes.instance().getSmeltingResult(furnaceInventory.getStackInSlot(0)).copy();

            this.furnaceInventory.setInventorySlotContents(1, result);
            entityPlayerMP.connection.sendPacket(new SPacketSetSlot(this.windowId, 1, result));
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
        return !(slotIn instanceof SlotFurnaceOutput) && super.canMergeSlot(stack, slotIn);
    }
}
