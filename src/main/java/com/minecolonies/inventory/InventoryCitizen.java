package com.minecolonies.inventory;

import com.minecolonies.colony.materials.MaterialStore;
import com.minecolonies.colony.materials.MaterialSystem;
import com.minecolonies.colony.permissions.Permissions;
import com.minecolonies.entity.EntityCitizen;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ReportedException;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Basic inventory for the citizens.
 */
public class InventoryCitizen implements IInventory
{
    /**
     * Number of slots in the inventory.
     */
    private static final int    INVENTORY_SIZE  = 27;
    /**
     * Max size of the stacks.
     */
    private static final int    MAX_STACK_SIZE  = 64;
    /**
     * NBT tag to store and retrieve the inventory.
     */
    private static final String TAG_INVENTORY   = "Inventory";
    /**
     * NBT tag to store and retrieve the custom name.
     */
    private static final String TAG_CUSTOM_NAME = "CustomName";
    /**
     * NBT tag to store and retrieve the custom name.
     */
    private static final String TAG_ITEMS       = "Items";
    /**
     * NBT tag to store and retrieve the custom name.
     */
    private static final String TAG_SLOT        = "Slot";
    /**
     * The returned slot if a slot hasn't been found.
     */
    private static final int    NO_SLOT         = -1;
    /**
     * Size of the hotbar.
     */
    private static final int HOTBAR_SIZE        = 0;

    /**
     * The inventory content.
     */
    @NotNull
    private ItemStack[] stacks = new ItemStack[INVENTORY_SIZE];
    /**
     * The inventories custom name. In our case the citizens name.
     */
    private String        customName;
    /**
     * The held item.
     */
    private int           heldItem;
    /**
     * The material store object.
     */
    private MaterialStore materialStore;
    /**
     * Updated after the inventory has been changed.
     */
    private boolean inventoryChanged = false;
    /**
     * The citizen which owns the inventory.
     */
    private EntityCitizen citizen;
    /**
     * Creates the inventory of the citizen.
     *
     * @param title         Title of the inventory.
     * @param localeEnabled Boolean whether the inventory has a custom name.
     * @param citizen       Citizen owner of the inventory.
     */
    public InventoryCitizen(final String title, final boolean localeEnabled, final EntityCitizen citizen)
    {
        this.citizen = citizen;
        if (localeEnabled)
        {
            customName = title;
        }
    }

    /**
     * Creates the inventory of the citizen.
     *
     * @param title         Title of the inventory.
     * @param localeEnabled Boolean whether the inventory has a custom name.
     */
    public InventoryCitizen(final String title, final boolean localeEnabled)
    {
        if (localeEnabled)
        {
            customName = title;
        }
    }

    /**
     * Get the size of the citizens hotbar inventory.
     *
     * @return the size.
     */
    public int getHotbarSize()
    {
        return HOTBAR_SIZE;
    }

    /**
     * Sets the name of the inventory.
     *
     * @param customName the string to use to set the name.
     */
    public void setCustomName(final String customName)
    {
        this.customName = customName;
    }

    /**
     * Checks if the inventory has been changed and then resets the boolean.
     *
     * @return true if it changed.
     */
    public boolean hasInventoryChanged()
    {
        if (inventoryChanged)
        {
            inventoryChanged = false;
            return true;
        }
        return false;
    }

    /**
     * Returns the item that is currently being held by citizen.
     *
     * @return {@link ItemStack} currently being held by citizen.
     */
    public ItemStack getHeldItemMainhand()
    {
        return getStackInSlot(heldItem);
    }

    /**
     * Set item to be held by citizen.
     *
     * @param slot Slot index with item to be held by citizen.
     */
    public void setHeldItem(final int slot)
    {
        this.heldItem = slot;
    }

    /**
     * Removes one item of specified Item from inventory (if it is in a stack, the stack size will reduce with 1).
     *
     * @param itemIn the item to consume.
     * @return true if succeed.
     */
    public boolean consumeInventoryItem(final Item itemIn)
    {
        final int i = this.getInventorySlotContainItem(itemIn);

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

    private int getInventorySlotContainItem(final Item itemIn)
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
     * Adds the item stack to the inventory, returns false if it is impossible.
     *
     * @param itemStackIn the stack to add
     * @return true if succeeded.
     */
    public boolean addItemStackToInventory(@Nullable final ItemStack itemStackIn)
    {
        if (itemStackIn != null && itemStackIn.stackSize != 0 && itemStackIn.getItem() != null)
        {
            try
            {
                if (itemStackIn.isItemDamaged())
                {
                    final int j = this.getFirstEmptySlot();

                    if (j == NO_SLOT)
                    {
                        return false;
                    }
                    this.stacks[j] = ItemStack.copyItemStack(itemStackIn);
                    itemStackIn.stackSize = 0;
                    return true;
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
            catch (final RuntimeException exp)
            {
                final CrashReport crashreport = CrashReport.makeCrashReport(exp, "Adding item to inventory");
                final CrashReportCategory crashreportcategory = crashreport.makeCategory("Item being added");
                crashreportcategory.addCrashSection("Item ID", Item.getIdFromItem(itemStackIn.getItem()));
                crashreportcategory.addCrashSection("Item data", itemStackIn.getMetadata());
                try
                {
                    crashreportcategory.addCrashSection("Item name", itemStackIn.getDisplayName());
                }
                catch (final RuntimeException e)
                {
                    crashreportcategory.addCrashSectionThrowable("Item name", e);
                }
                throw new ReportedException(crashreport);
            }
        }
        else
        {
            return false;
        }
    }

    /**
     * Returns the first item slot that is empty.
     *
     * @return the id of the first empty slot.
     */
    public int getFirstEmptySlot()
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
     * This function stores as many items of an ItemStack as possible in a matching slot and returns the quantity of
     * left over items.
     */
    private int storePartialItemStack(@NotNull final ItemStack itemStackIn)
    {
        int i = itemStackIn.stackSize;
        int j = this.storeItemStack(itemStackIn);

        if (j < 0)
        {
            j = this.getFirstEmptySlot();
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
     * stores an itemstack in the users inventory.
     */
    private int storeItemStack(@NotNull final ItemStack itemStackIn)
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
     * Checks if a specified Item is inside the inventory.
     *
     * @param itemIn the item to check for.
     * @return if itemIn in inventory.
     */
    public boolean hasItem(final Item itemIn)
    {
        return getInventorySlotContainItem(itemIn) != NO_SLOT;
    }

    /**
     * Gets slot that hold item that is being held by citizen.
     * {@link #getHeldItemMainhand()}.
     *
     * @return Slot index of held item
     */
    public int getHeldItemSlot()
    {
        return heldItem;
    }

    /**
     * Checks if a certain slot is empty.
     *
     * @param index the slot.
     * @return true if empty.
     */
    public boolean isSlotEmpty(final int index)
    {
        return getStackInSlot(index) == null;
    }

    /**
     * Create a material store.
     * @param system the system to use.
     */
    public void createMaterialStore(@NotNull final MaterialSystem system)
    {
        if (materialStore == null)
        {
            materialStore = new MaterialStore(MaterialStore.Type.INVENTORY, system);
        }
    }

    /**
     * Get the name of this object. For citizens this returns their name.
     *
     * @return the name of the inventory.
     */
    @NotNull
    @Override
    public String getName()
    {
        return this.hasCustomName() ? this.customName : "citizen.inventory";
    }

    public MaterialStore getMaterialStore()
    {
        return materialStore;
    }

    private void addStackToMaterialStore(@Nullable final ItemStack stack)
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

    private void removeStackFromMaterialStore(@Nullable final ItemStack stack)
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
     *
     * @param compound with the give tag.
     */
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        final NBTTagList nbttaglist = compound.getTagList(TAG_ITEMS, Constants.NBT.TAG_COMPOUND);
        this.stacks = new ItemStack[this.getSizeInventory()];

        for (int i = 0; i < nbttaglist.tagCount(); ++i)
        {
            final NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            final int j = nbttagcompound.getByte(TAG_SLOT) & Byte.MAX_VALUE;

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
     * Contains the size of the inventory.
     *
     * @return the size.
     */
    @Override
    public int getSizeInventory()
    {
        return INVENTORY_SIZE;
    }

    /**
     * Gets the stack of a certain slot.
     *
     * @param index the slot.
     * @return the ItemStack.
     */
    @Override
    public ItemStack getStackInSlot(final int index)
    {
        return this.stacks[index];
    }

    /**
     * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
     *
     * @param index the slot of the itemStack.
     * @param count the amount of items to reduce.
     * @return the resulting stack.
     */
    @Nullable
    @Override
    public ItemStack decrStackSize(final int index, final int count)
    {
        if (this.stacks[index] != null)
        {
            if (this.stacks[index].stackSize <= count)
            {
                final ItemStack itemstack1 = this.stacks[index];
                this.stacks[index] = null;
                this.markDirty();
                if (index == heldItem)
                {
                    if (citizen != null)
                    {
                        citizen.removeHeldItem();
                    }
                    heldItem = 0;
                }
                return itemstack1;
            }
            else
            {
                @NotNull final ItemStack itemstack = this.stacks[index].splitStack(count);

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
     * Checks if the inventory is named.
     *
     * @return true if the inventory has a custom name.
     */
    @Override
    public boolean hasCustomName()
    {
        return this.customName != null;
    }

    /**
     * Removes a stack from the given slot and returns it.
     *
     * @param index the slot of the stack.
     * @return the removed itemStack.
     */
    @Nullable
    @Override
    public ItemStack removeStackFromSlot(final int index)
    {
        if (this.stacks[index] == null)
        {
            return null;
        }

        final ItemStack itemstack = this.stacks[index];
        this.stacks[index] = null;
        return itemstack;
    }

    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
     *
     * @param index the slot to set the itemStack.
     * @param stack the itemStack to set.
     */
    @Override
    public void setInventorySlotContents(final int index, @Nullable final ItemStack stack)
    {
        if (index == heldItem && stack == null)
        {
            if (citizen != null)
            {
                citizen.removeHeldItem();
            }
            heldItem = 0;
        }

        this.stacks[index] = stack;

        if (stack != null && stack.stackSize > this.getInventoryStackLimit())
        {
            stack.stackSize = this.getInventoryStackLimit();
        }

        this.markDirty();
    }

    /**
     * Contains the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
     *
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
     * Do not give this method the name canInteractWith because it clashes with Container.
     *
     * @param player the player acessing the inventory.
     * @return if the player is allowed to access.
     */
    @Override
    public boolean isUseableByPlayer(@NotNull final EntityPlayer player)
    {
        return this.citizen.getColony().getPermissions().hasPermission(player, Permissions.Action.ACCESS_HUTS);
    }

    /**
     * Called when inventory is opened by a player.
     *
     * @param player the player who opened the inventory.
     */
    @Override
    public void openInventory(final EntityPlayer player)
    {
        /*
         * This may be filled in order to specify some custom handling.
         */
    }

    /**
     * Get the formatted TextComponent that will be used for the sender's username in chat.
     */
    @NotNull
    @Override
    public ITextComponent getDisplayName()
    {
        return this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName());
    }

    /**
     * Called after the inventory has been closed by a player.
     *
     * @param player the player who opened the inventory.
     */
    @Override
    public void closeInventory(final EntityPlayer player)
    {
        /*
         * This may be filled in order to specify some custom handling.
         */
    }

    /**
     * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot.
     *
     * @param index the accessing slot.
     * @param stack the stack trying to enter.
     * @return if the stack may be inserted.
     */
    @Override
    public boolean isItemValidForSlot(final int index, final ItemStack stack)
    {
        return true;
    }

    /**
     * This may be used in order to return values of different GUI areas like the ones in the beacon.
     *
     * @param id the id of the field.
     * @return the value of the field.
     */
    @Override
    public int getField(final int id)
    {
        return 0;
    }

    /**
     * This may be used to set GUI areas with a certain id and value.
     *
     * @param id    some id.
     * @param value some value.
     */
    @Override
    public void setField(final int id, final int value)
    {
        /*
         * We currently need no fields.
         */
    }

    /**
     * Returns the number of fields.
     *
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
     * Used to store variables.
     *
     * @param compound with the given tag.
     */
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        @NotNull final NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < this.stacks.length; ++i)
        {
            if (this.stacks[i] != null)
            {
                @NotNull final NBTTagCompound nbttagcompound = new NBTTagCompound();
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


    //-----------------------------Material Handling--------------------------------


    //todo missing now
    /*
    @Override
    public ItemStack getStackInSlotOnClosing(int index)
    {
            ItemStack removed = super.getStackInSlotOnClosing(index);

                    removeStackFromMaterialStore(removed);

                    return removed;
    }*/
}
