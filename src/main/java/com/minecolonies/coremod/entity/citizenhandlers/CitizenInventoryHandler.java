package com.minecolonies.coremod.entity.citizenhandlers;

import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.entity.EntityCitizen;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.items.wrapper.InvWrapper;

/**
 * Handles the inventory of the citizen.
 */
public class CitizenInventoryHandler
{
    /**
     * The citizen assigned to this manager.
     */
    private final EntityCitizen citizen;

    /**
     * Constructor for the experience handler.
     * @param citizen the citizen owning the handler.
     */
    public CitizenInventoryHandler(final EntityCitizen citizen)
    {
        this.citizen = citizen;
    }

    /**
     * Returns the first slot in the inventory with a specific item.
     *
     * @param targetItem the item.
     * @param itemDamage the damage value
     * @return the slot.
     */
    public int findFirstSlotInInventoryWith(final Item targetItem, final int itemDamage)
    {
        return InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(citizen.getInventoryCitizen()), targetItem, itemDamage);
    }

    /**
     * Returns the first slot in the inventory with a specific block.
     *
     * @param block      the block.
     * @param itemDamage the damage value
     * @return the slot.
     */
    public int findFirstSlotInInventoryWith(final Block block, final int itemDamage)
    {
        return InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(citizen.getInventoryCitizen()), block, itemDamage);
    }

    /**
     * Returns the amount of a certain block in the inventory.
     *
     * @param block      the block.
     * @param itemDamage the damage value
     * @return the quantity.
     */
    public int getItemCountInInventory(final Block block, final int itemDamage)
    {
        return InventoryUtils.getItemCountInItemHandler(new InvWrapper(citizen.getInventoryCitizen()), block, itemDamage);
    }

    /**
     * Returns the amount of a certain item in the inventory.
     *
     * @param targetItem the block.
     * @param itemDamage the damage value.
     * @return the quantity.
     */
    public int getItemCountInInventory(final Item targetItem, final int itemDamage)
    {
        return InventoryUtils.getItemCountInItemHandler(new InvWrapper(citizen.getInventoryCitizen()), targetItem, itemDamage);
    }

    /**
     * Checks if citizen has a certain block in the inventory.
     *
     * @param block      the block.
     * @param itemDamage the damage value
     * @return true if so.
     */
    public boolean hasItemInInventory(final Block block, final int itemDamage)
    {
        return InventoryUtils.hasItemInItemHandler(new InvWrapper(citizen.getInventoryCitizen()), block, itemDamage);
    }

    /**
     * Checks if citizen has a certain item in the inventory.
     *
     * @param item       the item.
     * @param itemDamage the damage value
     * @return true if so.
     */
    public boolean hasItemInInventory(final Item item, final int itemDamage)
    {
        return InventoryUtils.hasItemInItemHandler(new InvWrapper(citizen.getInventoryCitizen()), item, itemDamage);
    }

    /**
     * On Inventory change, mark the building dirty.
     */
    public void onInventoryChanged()
    {
        if (citizen.getCitizenData() != null)
        {
            final AbstractBuildingWorker building = citizen.getCitizenData().getWorkBuilding();
            if (building != null)
            {
                building.markDirty();
            }
        }
    }

    public boolean isInventoryFull()
    {
        return InventoryUtils.isProviderFull(citizen);
    }
}
