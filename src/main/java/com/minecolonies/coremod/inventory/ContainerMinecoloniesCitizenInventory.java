package com.minecolonies.coremod.inventory;

import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Container for Mie
 */
public class ContainerMinecoloniesCitizenInventory extends Container
{
    /**
     * Amount of columns in the player inventory.
     */
    private static final int INVENTORY_COLUMNS = 9;

    private final IInventory lowerChestInventory;
    private final IInventory playerInventory;
    private final int        numRows;

    private final int colonyId;
    private final BlockPos buildingId;
    private final int citizenId;

    public ContainerMinecoloniesCitizenInventory(
                                                  IInventory playerInventory,
                                                  IInventory chestInventory,
                                                  EntityPlayer player,
                                                  final int colonyId,
                                                  final BlockPos buildingId, final int citizenId)
    {
        this.lowerChestInventory = chestInventory;
        this.numRows = chestInventory.getSizeInventory() / 9;
        this.colonyId = colonyId;
        this.buildingId = buildingId;
        this.citizenId = citizenId;
        chestInventory.openInventory(player);
        int i = (this.numRows - 4) * 18;

        for (int j = 0; j < this.numRows; ++j)
        {
            for (int k = 0; k < 9; ++k)
            {
                this.addSlotToContainer(new Slot(chestInventory, k + j * 9, 8 + k * 18, 18 + j * 18) {
                    @Override
                    public void putStack(final ItemStack stack)
                    {
                        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER && !ItemStackUtils.isEmpty(stack))
                        {
                            final Colony colony = ColonyManager.getColony(colonyId);
                            final AbstractBuilding building = colony.getBuilding(buildingId);
                            final CitizenData citizenData = colony.getCitizen(citizenId);

                            building.overruleNextOpenRequestOfCitizenWithStack(citizenData, stack);
                        }
                        super.putStack(stack);
                    }
                });
            }
        }

        for (int l = 0; l < 3; ++l)
        {
            for (int j1 = 0; j1 < 9; ++j1)
            {
                this.addSlotToContainer(new Slot(playerInventory, j1 + l * 9 + 9, 8 + j1 * 18, 103 + l * 18 + i));
            }
        }

        for (int i1 = 0; i1 < 9; ++i1)
        {
            this.addSlotToContainer(new Slot(playerInventory, i1, 8 + i1 * 18, 161 + i));
        }
        this.playerInventory = playerInventory;
    }

    /**
     * Determines whether supplied player can use this container
     */
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return this.lowerChestInventory.isUsableByPlayer(playerIn);
    }

    /**
     * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
     * inventory and the other inventory(s).
     *
     * @param playerIn Player that interacted with this {@code Container}.
     * @param index Index of the {@link Slot}. This index is relative to the list of slots in this {@code Container},
     * {@link #inventorySlots}.
     */
    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
    {
        final Slot slot = this.inventorySlots.get(index);

        if (slot == null || !slot.getHasStack())
        {
            return ItemStackUtils.EMPTY;
        }

        final ItemStack stackCopy = slot.getStack().copy();

        final int maxIndex = this.numRows * INVENTORY_COLUMNS;

        if (index < maxIndex)
        {
            if (!this.mergeItemStack(stackCopy, maxIndex, this.inventorySlots.size(), true))
            {
                return ItemStackUtils.EMPTY;
            }
        }
        else if (!this.mergeItemStack(stackCopy, 0, maxIndex, false))
        {
            return ItemStackUtils.EMPTY;
        }

        if (ItemStackUtils.getSize(stackCopy) == 0)
        {
            slot.putStack(ItemStackUtils.EMPTY);
        }
        else
        {
            slot.onSlotChanged();
        }

        return stackCopy;
    }

    /**
     * Called when the container is closed.
     */
    public void onContainerClosed(EntityPlayer playerIn)
    {
        super.onContainerClosed(playerIn);
        this.lowerChestInventory.closeInventory(playerIn);
    }

    /**
     * Return this chest container's lower chest inventory.
     */
    public IInventory getLowerChestInventory()
    {
        return this.lowerChestInventory;
    }

    public IInventory getPlayerInventory()
    {
        return playerInventory;
    }
}