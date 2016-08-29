package com.minecolonies.inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

/**
 * The custom chest of the field.
 */
public class InventoryField extends InventoryCitizen
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
    private ItemStack[] stackResult   = new ItemStack[1];

    /**
     * The custom name of the inventory.
     */
    private String customName         = "";

    /**
     * Creates the inventory of the citizen.
     *
     * @param title         Title of the inventory.
     * @param localeEnabled Boolean whether the inventory has a custom name.
     */
    public InventoryField(final String title, final boolean localeEnabled)
    {
        super(title, localeEnabled);
        customName = title;
    }

    @Override
    public int getSizeInventory()
    {
        return 1;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 1;
    }

    @Override
    public int getHotbarSize()
    {
        return 0;
    }

    /**
     * Getter for the stack in the inventory. Since there is only one slot return always that one.
     * @param index the slot.
     * @return the itemStack in it.
     */
    public ItemStack getStackInSlot(int index)
    {
        return this.stackResult[0];
    }

    @Override
    public boolean hasCustomName()
    {
        return true;
    }

    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
     * @param index the slot to set the itemStack.
     * @param stack the itemStack to set.
     */
    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        this.stackResult[index] = stack;

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
        return this.hasCustomName() ? this.customName : "field.inventory";
    }

    /**
     * Used to retrieve variables.
     * @param compound with the give tag.
     */
    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        NBTTagList nbttaglist = compound.getTagList(TAG_ITEMS, Constants.NBT.TAG_COMPOUND);
        this.stackResult = new ItemStack[this.getSizeInventory()];

        for (int i = 0; i < nbttaglist.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            int            j              = nbttagcompound.getByte(TAG_SLOT) & Byte.MAX_VALUE;

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

    /**
     * Used to store variables.
     * @param compound with the given tag.
     */
    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < this.stackResult.length; ++i)
        {
            if (this.stackResult[i] != null)
            {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
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
}
