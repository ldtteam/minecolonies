package com.minecolonies.inventory;

import com.minecolonies.colony.materials.MaterialStore;
import com.minecolonies.colony.materials.MaterialSystem;
import com.minecolonies.util.Log;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.util.Constants;

import java.util.Random;

/**
 * Basic inventory for the citizens
 */
public class InventoryCitizen extends TileEntityLockable implements IInventory
{

    /**
     * The inventory content.
     */
    private ItemStack[] stacks = new ItemStack[27];
    /**
     * The inventories custom name. In our case the citizens name.
     */
    protected String customName;
    /**
     * The held item.
     */
    private int heldItem;
    /**
     * The material store object.
     */
    private MaterialStore materialStore;

    /**
     * Creates the inventory of the citizen
     *
     * @param title         Title of the inventory
     * @param localeEnabled Boolean whether the inventory has a custom name
     * @param size          Size of the inventory
     */
    public InventoryCitizen(String title, boolean localeEnabled, int size)
    {
        if(localeEnabled)
        {
            customName = title;
        }
    }

    /**
     * Contains the size of the inventory.
     * @return the size.
     */
    @Override
    public int getSizeInventory()
    {
        return 27;
    }

    /**
     * Returns the stack in the given slot.
     */
    public ItemStack getStackInSlot(int index)
    {
        return this.stacks[index];
    }

    /**
     * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
     */
    public ItemStack decrStackSize(int index, int count)
    {
        if (this.stacks[index] != null)
        {
            if (this.stacks[index].stackSize <= count)
            {
                ItemStack itemstack1 = this.stacks[index];
                this.stacks[index] = null;
                this.markDirty();
                return itemstack1;
            }
            else
            {
                ItemStack itemstack = this.stacks[index].splitStack(count);

                if (this.stacks[index].stackSize == 0)
                {
                    this.stacks[index] = null;
                }

                this.markDirty();
                return itemstack;
            }
        }
        else
        {
            return null;
        }
    }

    /**
     * Removes a stack from the given slot and returns it.
     */
    public ItemStack removeStackFromSlot(int index)
    {
        if (this.stacks[index] != null)
        {
            ItemStack itemstack = this.stacks[index];
            this.stacks[index] = null;
            return itemstack;
        }
        else
        {
            return null;
        }
    }

    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
     */
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        this.stacks[index] = stack;

        if (stack != null && stack.stackSize > this.getInventoryStackLimit())
        {
            stack.stackSize = this.getInventoryStackLimit();
        }

        this.markDirty();
    }

    /**
     * Add the given ItemStack to this Dispenser. Return the Slot the Item was placed in or -1 if no free slot is
     * available.
     */
    public int addItemStack(ItemStack stack)
    {
        for (int i = 0; i < this.stacks.length; ++i)
        {
            if (this.stacks[i] == null || this.stacks[i].getItem() == null)
            {
                this.setInventorySlotContents(i, stack);
                return i;
            }
        }

        return -1;
    }

    /**
     * Get the name of this object. For players this returns their username
     */
    public String getName()
    {
        return this.hasCustomName() ? this.customName : "citizen.inventory";
    }

    /**
     * Sets the name of the inventory.
     * @param customName the string to use to set the name.
     */
    public void setCustomName(String customName)
    {
        this.customName = customName;
    }

    /**
     * Returns true if this thing is named
     */
    public boolean hasCustomName()
    {
        return this.customName != null;
    }

    /**
     * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
     */
    public int getInventoryStackLimit()
    {
        return 64;
    }

    //todo may restrict it here!
    /**
     * Do not give this method the name canInteractWith because it clashes with Container
     */
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return true;
    }

    //todo don't know yet for what that serves
    public void openInventory(EntityPlayer player)
    {
    }

    //todo don't know yet for what that serves
    public void closeInventory(EntityPlayer player)
    {
    }

    /**
     * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot.
     */
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        return true;
    }

    public String getGuiID()
    {
        return "citizen:inventory";
    }

    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn)
    {
        return new ContainerChest(playerInventory, this, playerIn);
    }

    public int getField(int id)
    {
        return 0;
    }

    public void setField(int id, int value)
    {
    }

    public int getFieldCount()
    {
        return 0;
    }

    public void clear()
    {
        for (int i = 0; i < this.stacks.length; ++i)
        {
            this.stacks[i] = null;
        }
    }

    /**
     * Set item to be held by citizen
     *
     * @param slot Slot index with item to be held by citizen
     */
    public void setHeldItem(int slot)
    {
        this.heldItem = slot;
    }

    /**
     * Returns the item that is currently being held by citizen
     *
     * @return {@link ItemStack} currently being held by citizen
     */
    public ItemStack getHeldItem()
    {
        return getStackInSlot(heldItem);//TODO when tool breaks material handling isn't updated
    }

    /**
     * Gets slot that hold item that is being held by citizen
     *
     * @return Slot index of held item
     * @see {@link #getHeldItem()}
     */
    public int getHeldItemSlot()
    {
        return heldItem;
    }

    public boolean isSlotEmpty(int index)
    {
        return getStackInSlot(index) == null;
    }

    //-----------------------------Material Handling--------------------------------

    public void createMaterialStore(MaterialSystem system)
    {
        if (materialStore == null)
        {
            materialStore = new MaterialStore(MaterialStore.Type.INVENTORY, system);
        }
    }

    public MaterialStore getMaterialStore()
    {
        return materialStore;
    }

    //todo missing now
    /*
    @Override
    public ItemStack getStackInSlotOnClosing(int index)
    {
            ItemStack removed = super.getStackInSlotOnClosing(index);

                    removeStackFromMaterialStore(removed);

                    return removed;
    }*/

    private void addStackToMaterialStore(ItemStack stack)
    {
        if (stack == null)
        {
            return;
        }

        if (MaterialSystem.isEnabled)
        {
            materialStore.addMaterial(stack.getItem(), stack.stackSize);
        }
    }

    private void removeStackFromMaterialStore(ItemStack stack)
    {
        if (stack == null)
        {
            return;
        }

        if (MaterialSystem.isEnabled)
        {
            materialStore.removeMaterial(stack.getItem(), stack.stackSize);
        }
    }

    private static final String TAG_INVENTORY = "Inventory";

    public void readFromNBT(NBTTagCompound compound)
    {
        NBTTagList nbttaglist = compound.getTagList("Items", 10);
        this.stacks = new ItemStack[this.getSizeInventory()];

        for (int i = 0; i < nbttaglist.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            int            j              = nbttagcompound.getByte("Slot") & 255;

            if (j >= 0 && j < this.stacks.length)
            {
                this.stacks[j] = ItemStack.loadItemStackFromNBT(nbttagcompound);
            }
        }

        if (compound.hasKey("CustomName", 8))
        {
            this.customName = compound.getString("CustomName");
        }
    }

    public void writeToNBT(NBTTagCompound compound)
    {
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < this.stacks.length; ++i)
        {
            if (this.stacks[i] != null)
            {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte) i);
                this.stacks[i].writeToNBT(nbttagcompound);
                nbttaglist.appendTag(nbttagcompound);
            }
        }

        compound.setTag("Items", nbttaglist);

        if (this.hasCustomName())
        {
            compound.setString("CustomName", this.customName);
        }
        if (MaterialSystem.isEnabled)
        {
            materialStore.writeToNBT(compound);
        }

        compound.setTag(TAG_INVENTORY, nbttaglist);
    }
}