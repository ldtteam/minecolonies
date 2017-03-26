package com.minecolonies.coremod.inventory;

import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.inventory.api.AbstractCombinedItemHandler;
import com.minecolonies.coremod.util.InventoryUtils;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

public class InventoryCitizen extends AbstractCombinedItemHandler {

    public static final int INVENTORY_SIZE = 27;
    public static final int HOTBAR_SIZE = 1;

    public static final int INVENTORY_INDEX = 0;
    public static final int HOTBAR_INDEX = 1;

    private EntityCitizen citizen;

    /**
     * Creates the inventory of the citizen.
     *
     * @param title         Title of the inventory.
     * @param localeEnabled Boolean whether the inventory has a custom name.
     * @param citizen       Citizen owner of the inventory.
     */
    public InventoryCitizen(final String title, final boolean localeEnabled, final EntityCitizen citizen)
    {
        super(title, new ItemStackHandler(INVENTORY_SIZE), new ItemStackHandler(HOTBAR_SIZE));
        this.citizen = citizen;
    }

    /**
     * Creates the inventory of the citizen.
     *
     * @param title         Title of the inventory.
     * @param localeEnabled Boolean whether the inventory has a custom name.
     */
    public InventoryCitizen(final String title, final boolean localeEnabled)
    {
        super(title, new ItemStackHandler(INVENTORY_SIZE), new ItemStackHandler(HOTBAR_SIZE));
    }

    /**
     * Method to switch a given ItemStack from the inventory to the Hotbar.
     * @param slotIndex
     */
    public void switchItemStackInSlotToHotbar(int slotIndex) {
        InventoryUtils.swapItemStacksInItemHandlers(getInventoryHandler(),
          slotIndex,
          getHotbarHandler(),
          0);
    }

    public IItemHandlerModifiable getInventoryHandler() {
        return getHandlers()[INVENTORY_INDEX];
    }

    public IItemHandlerModifiable getHotbarHandler() {
        return getHandlers()[HOTBAR_INDEX];
    }
}