package com.minecolonies.inventory;

import com.minecolonies.colony.materials.MaterialStore;
import com.minecolonies.colony.materials.MaterialSystem;
import net.minecraft.inventory.IInvBasic;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;

public class InventoryCitizen extends InventoryBasic
{
    private int heldItem;

    private MaterialStore materialStore;

    public InventoryCitizen(String title, boolean localeEnabled, int size)
    {
        super(title, localeEnabled, size);
    }

    public void addIInvBasic(IInvBasic inventory)
    {
        func_110134_a(inventory);
    }

    public void removeIInvBasic(IInvBasic inventory)
    {
        func_110132_b(inventory);
    }

    public void setInventoryName(String name)
    {
        func_110133_a(name);
    }

    public void setHeldItem(int slot)
    {
        this.heldItem = slot;
    }

    public ItemStack getHeldItem()
    {
        return super.getStackInSlot(heldItem);//TODO when tool breaks material handling isn't updated
    }

    public int getHeldItemSlot()
    {
        return heldItem;
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

    /**
     * Makes sure ItemStacks inside of the inventory aren't affected by changes to the returned stack.
     */
    @Override
    public ItemStack getStackInSlot(int index)
    {
        return super.getStackInSlot(index).copy();
    }

    @Override
    public ItemStack decrStackSize(int index, int quantity)
    {
        ItemStack removed = super.decrStackSize(index, quantity);

        removeStackFromMaterialStore(removed);

        return removed;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index)
    {
        ItemStack removed = super.getStackInSlotOnClosing(index);

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

    private void addStackToMaterialStore(ItemStack stack)
    {
        materialStore.addMaterial(stack.getItem(), stack.stackSize);
    }

    private void removeStackFromMaterialStore(ItemStack stack)
    {
        materialStore.removeMaterial(stack.getItem(), stack.stackSize);
    }
}