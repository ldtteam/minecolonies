package com.minecolonies.api.entity.citizen.citizenhandlers;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

public interface ICitizenInventoryHandler
{
    /**
     * Returns the first slot in the inventory with a specific item.
     *
     * @param targetItem the item.
     * @param itemDamage the damage value
     * @return the slot.
     */
    int findFirstSlotInInventoryWith(Item targetItem, int itemDamage);

    /**
     * Returns the first slot in the inventory with a specific block.
     *
     * @param block      the block.
     * @param itemDamage the damage value
     * @return the slot.
     */
    int findFirstSlotInInventoryWith(Block block, int itemDamage);

    /**
     * Returns the amount of a certain block in the inventory.
     *
     * @param block      the block.
     * @param itemDamage the damage value
     * @return the quantity.
     */
    int getItemCountInInventory(Block block, int itemDamage);

    /**
     * Returns the amount of a certain item in the inventory.
     *
     * @param targetItem the block.
     * @param itemDamage the damage value.
     * @return the quantity.
     */
    int getItemCountInInventory(Item targetItem, int itemDamage);

    /**
     * Checks if citizen has a certain block in the inventory.
     *
     * @param block      the block.
     * @param itemDamage the damage value
     * @return true if so.
     */
    boolean hasItemInInventory(Block block, int itemDamage);

    /**
     * Checks if citizen has a certain item in the inventory.
     *
     * @param item       the item.
     * @param itemDamage the damage value
     * @return true if so.
     */
    boolean hasItemInInventory(Item item, int itemDamage);

    /**
     * On Inventory change, mark the building dirty.
     */
    void onInventoryChanged();

    boolean isInventoryFull();
}
