package com.minecolonies.coremod.entity.citizen.citizenhandlers;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenInventoryHandler;
import com.minecolonies.api.util.InventoryUtils;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

/**
 * Handles the inventory of the citizen.
 */
public class CitizenInventoryHandler implements ICitizenInventoryHandler
{
    /**
     * The citizen assigned to this manager.
     */
    private final AbstractEntityCitizen citizen;

    /**
     * Constructor for the experience handler.
     *
     * @param citizen the citizen owning the handler.
     */
    public CitizenInventoryHandler(final AbstractEntityCitizen citizen)
    {
        this.citizen = citizen;
    }

    /**
     * Returns the first slot in the inventory with a specific item.
     *
     * @param targetItem the item.
     * @return the slot.
     */
    @Override
    public int findFirstSlotInInventoryWith(final Item targetItem)
    {
        return InventoryUtils.findFirstSlotInItemHandlerWith(citizen.getInventoryCitizen(), targetItem);
    }

    /**
     * Returns the first slot in the inventory with a specific block.
     *
     * @param block the block.
     * @return the slot.
     */
    @Override
    public int findFirstSlotInInventoryWith(final Block block)
    {
        return InventoryUtils.findFirstSlotInItemHandlerWith(citizen.getInventoryCitizen(), block);
    }

    /**
     * Returns the amount of a certain block in the inventory.
     *
     * @param block the block.
     * @return the quantity.
     */
    @Override
    public int getItemCountInInventory(final Block block)
    {
        return InventoryUtils.getItemCountInItemHandler(citizen.getInventoryCitizen(), block);
    }

    /**
     * Returns the amount of a certain item in the inventory.
     *
     * @param targetItem the block.
     * @return the quantity.
     */
    @Override
    public int getItemCountInInventory(final Item targetItem)
    {
        return InventoryUtils.getItemCountInItemHandler(citizen.getInventoryCitizen(), targetItem);
    }

    /**
     * Checks if citizen has a certain block in the inventory.
     *
     * @param block the block.
     * @return true if so.
     */
    @Override
    public boolean hasItemInInventory(final Block block)
    {
        return InventoryUtils.hasItemInItemHandler(citizen.getInventoryCitizen(), block);
    }

    /**
     * Checks if citizen has a certain item in the inventory.
     *
     * @param item the item.
     * @return true if so.
     */
    @Override
    public boolean hasItemInInventory(final Item item)
    {
        return InventoryUtils.hasItemInItemHandler(citizen.getInventoryCitizen(), item);
    }

    @Override
    public boolean isInventoryFull()
    {
        return !citizen.getInventoryCitizen().hasSpace();
    }
}
