package com.minecolonies.core.colony.expeditions.colony.requirements;

import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraftforge.items.IItemHandler;

/**
 * Colony expedition requirements for providing any kind of food, with a minimum amount.
 */
public class ColonyExpeditionFoodRequirement implements IColonyExpeditionRequirement
{
    /**
     * The minimum amount to fulfill this requirement.
     */
    private final int amount;

    /**
     * Default constructor.
     *
     * @param amount the minimum amount.
     */
    public ColonyExpeditionFoodRequirement(final int amount)
    {
        this.amount = amount;
    }

    @Override
    public boolean isFulFilled(final IItemHandler itemHandler)
    {
        return InventoryUtils.getItemCountInItemHandler(itemHandler, ItemStackUtils.ISFOOD) >= this.amount;
    }
}