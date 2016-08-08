package com.minecolonies.inventory;

import com.minecolonies.colony.materials.MaterialStore;
import com.minecolonies.colony.materials.MaterialSystem;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ReportedException;
import net.minecraftforge.common.util.Constants;

/**
 * Basic inventory for the citizens
 */
public class InventoryCitizen implements IInventory
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
     * NBT tag to store and retrieve the inventory.
     */
    private static final String TAG_INVENTORY = "Inventory";
    /**
     * NBT tag to store and retrieve the custom name.
     */
    private static final String TAG_CUSTOM_NAME = "CustomName";
    /**
     * NBT tag to store and retrieve the custom name.
     */
    private static final String TAG_ITEMS = "Items";
    /**
     * NBT tag to store and retrieve the custom name.
     */
    private static final String TAG_SLOT= "Slot";
    /**
     * Updated after the inventory has been changed
     */
    private boolean inventoryChanged = false;
    /**
     * The returned slot if a slot hasn't been found.
     */
    private static final int NO_SLOT = -1;

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

    private int getInventorySlotContainItem(Item itemIn)
    {
        for (int i = 0; i < this.stacks.length; ++i)
        {
            if (this.stacks[i] != null && this.stacks[i].getItem() == itemIn)
            {
                return i;
            }
        }

        return NO_SLOT;
    }

    /**
     * stores an itemstack in the users inventory
     */
    private int storeItemStack(ItemStack itemStackIn)
    {
        for (int i = 0; i < this.stacks.length; ++i)
        {
            if (this.stacks[i] != null && this.stacks[i].getItem() == itemStackIn.getItem() && this.stacks[i].isStackable()
                && this.stacks[i].stackSize < this.stacks[i].getMaxStackSize() && this.stacks[i].stackSize < this.getInventoryStackLimit()
                && (!this.stacks[i].getHasSubtypes() || this.stacks[i].getMetadata() == itemStackIn.getMetadata())
                && ItemStack.areItemStackTagsEqual(this.stacks[i], itemStackIn))
            {
                return i;
            }
        }

        return NO_SLOT;
    }

    /**
     * Returns the first item stack that is empty.
     */
    public int getFirstEmptyStack()
    {
        for (int i = 0; i < this.stacks.length; ++i)
        {
            if (this.stacks[i] == null)
            {
                return i;
            }
        }

        return NO_SLOT;
    }

    /**
     * Get the size of the citizens hotbar inventory
     * @return the size.
     */
    public int getHotbarSize()
    {
        return 0;
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
     * Get the formatted ChatComponent that will be used for the sender's username in chat
     */
    @Override
    public IChatComponent getDisplayName()
    {
        return this.hasCustomName() ? new ChatComponentText(this.getName()) : new ChatComponentTranslation(this.getName());
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
     * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it
     * hasn't changed and skip it.
     */
    @Override
    public void markDirty()
    {
        this.inventoryChanged = true;
    }

    /**
     * Checks if the inventory has been changed and then resets the boolean.
     * @return true if it changed.
     */
    public boolean hasInventoryChanged()
    {
        if(inventoryChanged)
        {
            inventoryChanged = false;
            return true;
        }
        return false;
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
     * This function stores as many items of an ItemStack as possible in a matching slot and returns the quantity of
     * left over items.
     */
    private int storePartialItemStack(ItemStack itemStackIn)
    {
        int i = itemStackIn.stackSize;
        int j = this.storeItemStack(itemStackIn);

        if (j < 0)
        {
            j = this.getFirstEmptyStack();
        }

        if (j < 0)
        {
            return i;
        }
        else
        {
            if (this.stacks[j] == null)
            {
                // Forge: Replace Item clone above to preserve item capabilities when picking the item up.
                this.stacks[j] = itemStackIn.copy();
                this.stacks[j].stackSize = 0;
            }

            int k = i;

            if (i > this.stacks[j].getMaxStackSize() - this.stacks[j].stackSize)
            {
                k = this.stacks[j].getMaxStackSize() - this.stacks[j].stackSize;
            }

            if (k > this.getInventoryStackLimit() - this.stacks[j].stackSize)
            {
                k = this.getInventoryStackLimit() - this.stacks[j].stackSize;
            }

            if (k == 0)
            {
                return i;
            }
            else
            {
                i = i - k;
                this.stacks[j].stackSize += k;
                return i;
            }
        }
    }


    /**
     * Removes one item of specified Item from inventory (if it is in a stack, the stack size will reduce with 1)
     * @param itemIn the item to consume.
     * @return true if succeed.
     */
    public boolean consumeInventoryItem(Item itemIn)
    {
        int i = this.getInventorySlotContainItem(itemIn);

        if (i < 0)
        {
            return false;
        }
        else
        {
            --this.stacks[i].stackSize;
            if (this.stacks[i].stackSize <= 0)
            {
                this.stacks[i] = null;
            }

            return true;
        }
    }

    /**
     * Adds the item stack to the inventory, returns false if it is impossible.
     * @param itemStackIn the stack to add
     * @return true if succeeded.
     */
    public boolean addItemStackToInventory(final ItemStack itemStackIn)
    {
        if (itemStackIn != null && itemStackIn.stackSize != 0 && itemStackIn.getItem() != null)
        {
            try
            {
                if (itemStackIn.isItemDamaged())
                {
                    int j = this.getFirstEmptyStack();

                    if (j != NO_SLOT)
                    {
                        this.stacks[j] = ItemStack.copyItemStack(itemStackIn);
                        itemStackIn.stackSize = 0;
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
                else
                {
                    int i;

                    while (true)
                    {
                        i = itemStackIn.stackSize;
                        itemStackIn.stackSize = this.storePartialItemStack(itemStackIn);

                        if (itemStackIn.stackSize <= 0 || itemStackIn.stackSize >= i)
                        {
                            break;
                        }
                    }

                    return itemStackIn.stackSize < i;
                }
            }
            catch (RuntimeException exp)
            {
                CrashReport         crashreport         = CrashReport.makeCrashReport(exp, "Adding item to inventory");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Item being added");
                crashreportcategory.addCrashSection("Item ID", Item.getIdFromItem(itemStackIn.getItem()));
                crashreportcategory.addCrashSection("Item data", itemStackIn.getMetadata());
                crashreportcategory.addCrashSectionCallable("Item name", itemStackIn::getDisplayName);
                throw new ReportedException(crashreport);
            }
        }
        else
        {
            return false;
        }
    }

    /**
     * Checks if a specified Item is inside the inventory
     * @param itemIn the item to check for.
     * @return if itemIn in inventory.
     */
    public boolean hasItem(Item itemIn)
    {
        return getInventorySlotContainItem(itemIn) != NO_SLOT;
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

    /**
     * Used to retrieve variables.
     * @param compound with the give tag.
     */
    public void readFromNBT(NBTTagCompound compound)
    {
        NBTTagList nbttaglist = compound.getTagList(TAG_ITEMS, Constants.NBT.TAG_COMPOUND);
        this.stacks = new ItemStack[this.getSizeInventory()];

        for (int i = 0; i < nbttaglist.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            int            j              = nbttagcompound.getByte(TAG_SLOT) & Byte.MAX_VALUE;

            if (j != NO_SLOT && j < this.stacks.length)
            {
                this.stacks[j] = ItemStack.loadItemStackFromNBT(nbttagcompound);
            }
        }

        if (compound.hasKey(TAG_CUSTOM_NAME, Constants.NBT.TAG_STRING))
        {
            this.customName = compound.getString(TAG_CUSTOM_NAME);
        }
    }

    /**
     * Used to store variables.
     * @param compound with the given tag.
     */
    public void writeToNBT(NBTTagCompound compound)
    {
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < this.stacks.length; ++i)
        {
            if (this.stacks[i] != null)
            {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte(TAG_SLOT, (byte) i);
                this.stacks[i].writeToNBT(nbttagcompound);
                nbttaglist.appendTag(nbttagcompound);
            }
        }

        compound.setTag(TAG_ITEMS, nbttaglist);

        if (this.hasCustomName())
        {
            compound.setString(TAG_CUSTOM_NAME, this.customName);
        }
        if (MaterialSystem.isEnabled)
        {
            materialStore.writeToNBT(compound);
        }

        compound.setTag(TAG_INVENTORY, nbttaglist);
    }
}