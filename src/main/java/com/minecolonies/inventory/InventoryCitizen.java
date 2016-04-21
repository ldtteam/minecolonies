package com.minecolonies.inventory;

import com.minecolonies.colony.materials.MaterialStore;
import com.minecolonies.colony.materials.MaterialSystem;
import net.minecraft.inventory.IInvBasic;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;

/**
 * Basic inventory for the citizens
 */
public class InventoryCitizen extends InventoryBasic
{
    private int heldItem;

    private MaterialStore materialStore;

    /**
     * Creates the inventory of the citizen
     *
     * @param title             Title of the inventory
     * @param localeEnabled     Boolean whether the inventory has a custom name
     * @param size              Size of the inventory
     */
    public InventoryCitizen(String title, boolean localeEnabled, int size)
    {
        super(title, localeEnabled, size);
    }

    /**
     * Adds item to the inventory
     *
     * @param inventory     Inventory item to add
     */
    public void addIInvBasic(IInvBasic inventory)
    {
        addInventoryChangeListener(inventory);
    }

    /**
     * Removes item from inventory
     *
     * @param inventory     Inventory item to remove
     */
    public void removeIInvBasic(IInvBasic inventory)
    {
        removeInventoryChangeListener(inventory);
    }

    /**
     * Sets the inventory name
     *
     * @param name          Name of the inventory
     */
    public void setInventoryName(String name)
    {
        setCustomName(name);
    }

    /**
     * Set item to be held by citizen
     *
     * @param slot          Slot index with item to be held by citizen
     */
    public void setHeldItem(int slot)
    {
        this.heldItem = slot;
    }

    /**
     * Returns the item that is currently being held by citizen
     *
     * @return              {@link ItemStack} currently being held by citizen
     */
    public ItemStack getHeldItem()
    {
        return super.getStackInSlot(heldItem);//TODO when tool breaks material handling isn't updated
    }

    /**
     * Gets slot that hold item that is being held by citizen
     *
     * @return              Slot index of held item
     * @see                 {@link #getHeldItem()}
     */
    public int getHeldItemSlot()
    {
        return heldItem;
    }

    public boolean isSlotEmpty(int index)
    {
        return super.getStackInSlot(index) == null;
    }

    //-----------------------------Material Handling--------------------------------

    public void createMaterialStore(MaterialSystem system)
    {
        if(materialStore == null)
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


    /**
     * Makes sure ItemStacks inside of the inventory aren't affected by changes to the returned stack.
     */
    @Override
    public ItemStack getStackInSlot(int index)
    {
        ItemStack stack = super.getStackInSlot(index);
        if(stack == null)
        {
            return null;
        }
        return stack.copy();
    }

    @Override
    public ItemStack decrStackSize(int index, int quantity)
    {
        ItemStack removed = super.decrStackSize(index, quantity);

        removeStackFromMaterialStore(removed);

        return removed;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        ItemStack previous = getStackInSlot(index);
        removeStackFromMaterialStore(previous);

        super.setInventorySlotContents(index, stack);

        addStackToMaterialStore(stack);
    }

    /**
     * Put stack in inventory.
     *
     * @param stack The {@link ItemStack} to put into the inventory
     * @return null if successful, otherwise return whatever couldn't be put into the inventory
     */
    public ItemStack func_174894_a(ItemStack stack)
    {
        //Minecraft code makes a copy of stack and doesn't change it, so its safe for us to use later on
        ItemStack returned = super.func_174894_a(stack);

        if(returned == null)
        {
            addStackToMaterialStore(stack);
        }
        else if(stack.stackSize != returned.stackSize)
        {
            materialStore.addMaterial(stack.getItem(), stack.stackSize - returned.stackSize);
        }

        return returned;
    }

    /**
     * Removes a stack from the given slot and returns it.
     */
    @Override
    public ItemStack removeStackFromSlot(int index)
    {
        ItemStack stack = super.removeStackFromSlot(index);
        removeStackFromMaterialStore(stack);

        return stack;
    }

    @Override
    public void clear()
    {
        materialStore.clear();

        super.clear();
    }

    private void addStackToMaterialStore(ItemStack stack)
    {
        if(stack == null){
            return;
        }
        //todo: colton reenable it
        materialStore.addMaterial(stack.getItem(), stack.stackSize);
    }

    private void removeStackFromMaterialStore(ItemStack stack)
    {
        if(stack == null){
            return;
        }
        //todo: colton reenable it
        materialStore.removeMaterial(stack.getItem(), stack.stackSize);
    }
}