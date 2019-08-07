package com.minecolonies.api.entity.citizen.citizenhandlers;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

public interface ICitizenInventoryHandler
{
    /**
     * Returns the first slot in the inventory with a specific item.
     *
     * @param targetItem the item.
     * @return the slot.
     */
    int findFirstSlotInInventoryWith(Item targetItem);

    /**
     * Returns the first slot in the inventory with a specific block.
     *
     * @param block      the block.
     * @return the slot.
     */
    int findFirstSlotInInventoryWith(Block block);

    /**
     * Returns the amount of a certain block in the inventory.
     *
     * @param block      the block.
     * @return the quantity.
     */
    int getItemCountInInventory(Block block);

    /**
     * Returns the amount of a certain item in the inventory.
     *
     * @param targetItem the block.
     * @return the quantity.
     */
    int getItemCountInInventory(Item targetItem);

    /**
     * Checks if citizen has a certain block in the inventory.
     *
     * @param block      the block.
     * @return true if so.
     */
    boolean hasItemInInventory(Block block);

    /**
     * Checks if citizen has a certain item in the inventory.
     *
     * @param item       the item.
     * @return true if so.
     */
    boolean hasItemInInventory(Item item);

    /**
     * On Inventory change, mark the building dirty.
     */
    void onInventoryChanged();

    boolean isInventoryFull();
}
