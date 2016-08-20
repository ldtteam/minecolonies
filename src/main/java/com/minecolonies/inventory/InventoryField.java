package com.minecolonies.inventory;
import net.minecraft.item.ItemStack;

/**
 * The custom chest of the field.
 */
public class InventoryField extends InventoryCitizen
{
    private ItemStack[] stackResult = new ItemStack[1];
    private String customName = "";
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
}
