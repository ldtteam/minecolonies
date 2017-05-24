package com.minecolonies.coremod.inventory;

import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The custom chest of the field.
 */
public class InventoryField implements IInventory
{
    /**
     * NBTTag to store the slot.
     */
    private static final String TAG_SLOT = "slot";

    /**
     * NBTTag to store the items.
     */
    private static final String TAG_ITEMS = "items";

    /**
     * NBTTag to store the custom name.
     */
    private static final String TAG_CUSTOM_NAME = "name";

    /**
     * NBTTag to store the inventory.
     */
    private static final String TAG_INVENTORY = "inventory";

    /**
     * Returned slot if no slat has been found.
     */
    private static final int NO_SLOT = -1;

    /**
     * The inventory stack.
     */
    @NotNull
    private ItemStack[] stackResult = new ItemStack[1];

    /**
     * The custom name of the inventory.
     */
    private String customName = "";

    /**
     * Updated after the inventory has been changed.
     */
    private boolean inventoryChanged = false;

    /**
     * Creates the inventory of the citizen.
     *
     * @param title Title of the inventory.
     */
    public InventoryField(final String title)
    {
        super();
        this.customName = title;
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
     * Used to retrieve variables.
     *
     * @param compound with the give tag.
     */
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        final NBTTagList nbttaglist = compound.getTagList(TAG_ITEMS, Constants.NBT.TAG_COMPOUND);
        this.stackResult = new ItemStack[this.getSizeInventory()];

        for (int i = 0; i < nbttaglist.tagCount(); ++i)
        {
            final NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            final int j = nbttagcompound.getByte(TAG_SLOT) & Byte.MAX_VALUE;

            if (j != NO_SLOT && j < this.stackResult.length)
            {
                this.stackResult[j] = ItemStack.loadItemStackFromNBT(nbttagcompound);
            }
        }

        if (compound.hasKey(TAG_CUSTOM_NAME, Constants.NBT.TAG_STRING))
        {
            this.customName = compound.getString(TAG_CUSTOM_NAME);
        }
    }

    @Override
    public int getSizeInventory()
    {
        return 1;
    }

    /**
     * Getter for the stack in the inventory. Since there is only one slot return always that one.
     *
     * @param index the slot.
     * @return the itemStack in it.
     */
    @Override
    public ItemStack getStackInSlot(final int index)
    {
        return this.stackResult[0];
    }

    @Nullable
    @Override
    public ItemStack decrStackSize(final int index, final int count)
    {
        if (ItemStackUtils.isEmpty(this.stackResult[index]))
        {
            return ItemStackUtils.EMPTY;
        }

        if (ItemStackUtils.getSize(this.stackResult[index]) <= count)
        {
            final ItemStack itemStack1 = this.stackResult[index];
            this.stackResult[index] = ItemStackUtils.EMPTY;
            this.markDirty();
            return itemStack1;
        }
        else
        {
            @NotNull final ItemStack itemstack = this.stackResult[index].splitStack(count);

            if (ItemStackUtils.isEmpty(this.stackResult[index]))
            {
                this.stackResult[index] = ItemStackUtils.EMPTY;
            }

            this.markDirty();
            return itemstack;
        }
    }

    @Nullable
    @Override
    public ItemStack removeStackFromSlot(final int index)
    {
        if (ItemStackUtils.isEmpty(this.stackResult[index]))
        {
            return ItemStackUtils.EMPTY;
        }

        final ItemStack itemstack = this.stackResult[index];
        this.stackResult[index] = ItemStackUtils.EMPTY;
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
        this.stackResult[index] = stack;

        if (!ItemStackUtils.isEmpty(stack) && ItemStackUtils.getSize(stack) > this.getInventoryStackLimit())
        {
            ItemStackUtils.setSize(stack, this.getInventoryStackLimit());
        }

        this.markDirty();
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 1;
    }

    @Override
    public void markDirty()
    {
        this.inventoryChanged = true;
    }

    @Override
    public boolean isUseableByPlayer(final EntityPlayer entityPlayer)
    {
        return true;
    }

    /**
     * Called when inventory is opened by a player.
     *
     * @param entityPlayer the player who opened the inventory.
     */
    @Override
    public void openInventory(final EntityPlayer entityPlayer)
    {
        /*
         * This may be filled in order to specify some custom handling.
         */
    }

    /**
     * Called after the inventory has been closed by a player.
     *
     * @param entityPlayer the player who opened the inventory.
     */
    @Override
    public void closeInventory(final EntityPlayer entityPlayer)
    {
        /*
         * This may be filled in order to specify some custom handling.
         */
    }

    @Override
    public boolean isItemValidForSlot(final int index, @Nullable final ItemStack itemStack)
    {
        return index == 0 && ItemStackUtils.isEmpty(itemStack) && itemStack.getItem() instanceof ItemSeeds;
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
        for (int i = 0; i < this.stackResult.length; ++i)
        {
            this.stackResult[i] = ItemStackUtils.EMPTY;
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

        for (int i = 0; i < this.stackResult.length; ++i)
        {
            if (!ItemStackUtils.isEmpty(stackResult[i]))
            {
                @NotNull final NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte(TAG_SLOT, (byte) i);
                this.stackResult[i].writeToNBT(nbttagcompound);
                nbttaglist.appendTag(nbttagcompound);
            }
        }

        compound.setTag(TAG_ITEMS, nbttaglist);

        if (this.hasCustomName())
        {
            compound.setString(TAG_CUSTOM_NAME, this.customName);
        }

        compound.setTag(TAG_INVENTORY, nbttaglist);
    }

    /**
     * Setter of the customName of the inventory.
     *
     * @param customName the name to set.
     */
    public void setCustomName(final String customName)
    {
        this.customName = customName;
    }

    @Override
    public boolean hasCustomName()
    {
        return true;
    }

    @NotNull
    @Override
    public ITextComponent getDisplayName()
    {
        return new TextComponentTranslation(this.getName());
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
        return this.hasCustomName() ? this.customName : "field.inventory";
    }
}
