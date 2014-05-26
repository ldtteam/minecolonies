package com.minecolonies.util;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class InventoryHelper
{
    /**
     * Tries to put an item into Inventory
     * If you are looking to transfer items, please use transferItem() this is only for dumping items from other sources (like picked up items)
     * @param inventory destination inventory
     * @param stack Item stack with items to be transferred
     * @return returns null if successful, or stack of remaining items
     */
    public static ItemStack setStackInInventory(IInventory inventory, ItemStack stack) {
        if (inventory != null && stack != null){
            ItemStack returnStack = stack;//returning this in the end.
            int slot;//does the destination already have a stack of item?
            while((slot = doesInventoryContainNotFullItem(inventory, stack)) != -1 && returnStack != null){//try and fill the slot
                ItemStack current = inventory.getStackInSlot(slot);
                int spaceLeft = current.getMaxStackSize() - current.stackSize;
                if(spaceLeft > 0){
                    //split stack is not really safe, we could be creating a bigger total if we don't check it for minimum
                    ItemStack toBeAdded = returnStack.splitStack(Math.min(returnStack.stackSize, spaceLeft));//remove the amount from stack
                    if (returnStack.stackSize == 0) {
                        returnStack = null;
                    }
                    current.stackSize += toBeAdded.stackSize; //we add the stackSize from the split above
                    inventory.setInventorySlotContents(slot, current);
                }
            }
            //dump the rest in a free slot if any
            slot = getOpenSlot(inventory);//get a free slot
            if(slot != -1 && returnStack != null){
                inventory.setInventorySlotContents(slot, returnStack);//dump the remaining items in empty slot
                returnStack = null;//we dumped it all!
            }
            return returnStack;//return remaining items if not all are placed in the chest.
        }
        return stack;
    }

    /**
     * returns first open slot in the inventory
     * @param inventory
     * @return slot number or -1 if none found.
     */
    public static int getOpenSlot(IInventory inventory) {
        for(int slot=0; slot < inventory.getSizeInventory(); slot++){
            if(inventory.getStackInSlot(slot) == null){
                return slot;
            }
        }
        return -1;
    }

    /** returns a slot number if a chest contains given ItemStack item
     * @return returns slot number if found, -1 when not found.
     */
    public static int doesInventoryContainItemStack(IInventory inventory, ItemStack stack) {
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack testStack = inventory.getStackInSlot(i);
            if (testStack != null && inventory.getStackInSlot(i).isItemEqual(stack)) {
                return i;
            }
        }
        return -1;
    }

    /** returns a slot number if a chest contains given ItemStack item that is not fully stacked
     * @return returns slot number if found, -1 when not found.
     */
    public static int doesInventoryContainNotFullItem(IInventory inventory, ItemStack stack) {
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack testStack = inventory.getStackInSlot(i);
            if (testStack != null && testStack.isItemEqual(stack) && testStack.stackSize != testStack.getMaxStackSize() ) {
                return i;
            }
        }
        return -1;
    }
}
