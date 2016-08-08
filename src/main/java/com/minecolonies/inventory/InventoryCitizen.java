package com.minecolonies.inventory;

import com.minecolonies.colony.materials.MaterialStore;
import com.minecolonies.colony.materials.MaterialSystem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityLockable;

/**
 * Basic inventory for the citizens
 */
public class InventoryCitizen extends TileEntityLockable
{
    /**
     * Number of slots in the inventory.
     */
    private static final int INVENTORY_SIZE = 27;
    /**
     * Max size of the stacks.
     */
    private static final int MAX_STACK_SIZE = 64;
    /**
     * The inventory content.
     */
    private ItemStack[] stacks = new ItemStack[INVENTORY_SIZE];
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
        return INVENTORY_SIZE;
    }

    /**
     * Gets the stack of a certain slot.
     * @param index the slot.
     * @return the ItemStack.
     */
    @Override
    public ItemStack getStackInSlot(int index)
    {
        return this.stacks[index];
    }

    /**
     * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
     * @param index the slot of the itemStack.
     * @param count the amount of items to reduce.
     * @return the resulting stack.
     */
    @Override
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
     * @param index the slot of the stack.
     * @return the removed itemStack.
     */
    @Override
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
     * @param index the slot to set the itemStack.
     * @param stack the itemStack to set.
     */
    @Override
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
     * Add the given itemStack to this inventory Return the Slot the Item was placed in or -1 if no free slot is available.
     * @param stack the stack to add.
     * @return the slot it was placed in.
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
     *  Get the name of this object. For citizens this returns their name.
     * @return the name of the inventory.
     */
    @Override
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
     * Checks if the inventory is named.
     * @return true if the inventory has a custom name.
     */
    @Override
    public boolean hasCustomName()
    {
        return this.customName != null;
    }

    /**
     * Contains the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
     * @return the stack size.
     */
    @Override
    public int getInventoryStackLimit()
    {
        return MAX_STACK_SIZE;
    }

    /**
     * Do not give this method the name canInteractWith because it clashes with Container
     * @param player the player acessing the inventory.
     * @return if the player is allowed to access.
     */
    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        //TODO We may restrict its access according to colony rules here.
        return true;
    }

    /**
     *  Called when inventory is opened by a player.
      * @param player the player who opened the inventory.
     */
    @Override
    public void openInventory(EntityPlayer player)
    {
        /*
         * This may be filled in order to specify some custom handling.
         */
    }

    /**
     * Called after the inventory has been closed by a player.
      * @param player the player who opened the inventory.
     */
    @Override
    public void closeInventory(EntityPlayer player)
    {
        /*
         * This may be filled in order to specify some custom handling.
         */
    }

    /**
     * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot.
     * @param index the accessing slot.
     * @param stack the stack trying to enter.
     * @return if the stack may be inserted.
     */
    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        return true;
    }

    /**
     * ID of the GUI.
     * @return a string describing the inventory GUI.
     */
    @Override
    public String getGuiID()
    {
        return "citizen:inventory";
    }

    /**
     * This method loades the inventory of the player under the citizen inventory and handles the interactions.
     * @param playerInventory the player inventory.
     * @param playerIn the player accessing.
     * @return the container.
     */
    @Override
    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn)
    {
        return new ContainerChest(playerInventory, this, playerIn);
    }

    /**
     * This may be used in order to return values of different GUI areas like the ones in the beacon.
     * @param id the id of the field.
     * @return the value of the field.
     */
    @Override
    public int getField(int id)
    {
        return 0;
    }

    /**
     * This may be used to set GUI areas with a certain id and value.
     * @param id some id.
     * @param value some value.
     */
    @Override
    public void setField(int id, int value)
    {
        /*
         * We currently need no fields.
         */
    }

    /**
     * Returns the number of fields.
     * @return the amount.
     */
    @Override
    public int getFieldCount()
    {
        return 0;
    }

    /**
     * Completely clears the inventory.
     */
    @Override
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
        //TODO when tool breaks material handling isn't updated
        return getStackInSlot(heldItem);
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

    /**
     * Checks if a certain slot is empty.
     * @param index the slot.
     * @return true if empty.
     */
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