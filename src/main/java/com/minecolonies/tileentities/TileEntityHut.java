package com.minecolonies.tileentities;

import com.minecolonies.inventory.InventoryCitizen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;

public abstract class TileEntityHut extends TileEntityBuildable
{
    private int maxInhabitants;
    private int maleInhabitants;
    private int femaleInhabitants;

    public abstract void breakBlock();

    public int getMaxInhabitants()
    {
        return maxInhabitants;
    }

    public void setMaxInhabitants(int maxInhabitants)
    {
        this.maxInhabitants = maxInhabitants;
    }

    public int getMaleInhabitants()
    {
        return maleInhabitants;
    }

    public void setMaleInhabitants(int maleInhabitants)
    {
        this.maleInhabitants = maleInhabitants;
    }

    public int getFemaleInhabitants()
    {
        return femaleInhabitants;
    }

    public void setFemaleInhabitants(int femaleInhabitants)
    {
        this.femaleInhabitants = femaleInhabitants;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return super.isUseableByPlayer(player) && this.isPlayerOwner(player);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
    {
        this.readFromNBT(packet.func_148857_g());
    }

    @Override
    public S35PacketUpdateTileEntity getDescriptionPacket()
    {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        this.writeToNBT(nbtTagCompound);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 0, nbtTagCompound);
    }

    /**
     * Tries to put an item into Inventory
     *
     * @param stack Item stack with items to be transferred
     * @return returns null if successful, or stack of remaining items
     */
    public ItemStack setStackInInventory(ItemStack stack)
    {
        if(stack != null)
        {
            ItemStack returnStack = stack;
            int slot;
            while((slot = containsPartialItemStack(stack)) != -1 && returnStack != null)
            {
                ItemStack current = getStackInSlot(slot);
                int spaceLeft = current.getMaxStackSize() - current.stackSize;
                if(spaceLeft > 0)
                {
                    ItemStack toBeAdded = returnStack.splitStack(Math.min(returnStack.stackSize, spaceLeft));
                    if(returnStack.stackSize == 0)
                    {
                        returnStack = null;
                    }
                    current.stackSize += toBeAdded.stackSize;
                    setInventorySlotContents(slot, current);
                }
            }

            slot = getOpenSlot();
            if(slot != -1 && returnStack != null)
            {
                setInventorySlotContents(slot, returnStack);
                returnStack = null;
            }
            return returnStack;
        }
        return null;
    }

    /**
     * returns first open slot in the inventory
     *
     * @return slot number or -1 if none found.
     */
    private int getOpenSlot()
    {
        for(int slot = 0; slot < getSizeInventory(); slot++)
        {
            if(getStackInSlot(slot) == null)
            {
                return slot;
            }
        }
        return -1;
    }

    /**
     * returns a slot number if a chest contains given ItemStack item
     *
     * @return returns slot number if found, -1 when not found.
     */
    public int containsItemStack(ItemStack stack)
    {
        for(int i = 0; i < getSizeInventory(); i++)
        {
            ItemStack testStack = getStackInSlot(i);
            if(testStack != null && testStack.isItemEqual(stack))
            {
                return i;
            }
        }
        return -1;
    }

    /**
     * returns a slot number if a chest contains given ItemStack item that is not fully stacked
     *
     * @return returns slot number if found, -1 when not found.
     */
    private int containsPartialItemStack(ItemStack stack)
    {
        for(int i = 0; i < getSizeInventory(); i++)
        {
            ItemStack testStack = getStackInSlot(i);
            if(testStack != null && testStack.isItemEqual(stack) && testStack.stackSize != testStack.getMaxStackSize())
            {
                return i;
            }
        }
        return -1;
    }

    public boolean takeItem(InventoryCitizen inventory, int slotID, int amount)
    {
        if(inventory != null && slotID >= 0 && amount >= 0)
        {
            ItemStack stack = getStackInSlot(slotID);
            if(stack != null)
            {
                stack = decrStackSize(slotID, Math.min(amount, stack.stackSize));
                stack = inventory.setStackInInventory(stack);
                if(stack != null)
                {
                    this.setStackInInventory(stack);
                    return false;
                }
                return true;
            }
        }
        return false;
    }
}
