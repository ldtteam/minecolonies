package com.minecolonies.core.colony.expeditions.colony.requirements;

import com.minecolonies.api.util.InventoryUtils;
import net.minecraft.world.item.Item;
import net.minecraftforge.items.IItemHandler;

/**
 * Colony expedition requirements for providing any kind of item, with a minimum amount.
 */
public class ColonyExpeditionItemRequirement implements IColonyExpeditionRequirement
{
    /**
     * The item to request.
     */
    private final Item item;

    /**
     * The minimum amount to fulfill this requirement.
     */
    private final int amount;

    /**
     * Default constructor.
     *
     * @param item   the item to request.
     * @param amount the minimum amount.
     */
    public ColonyExpeditionItemRequirement(final Item item, final int amount)
    {
        this.item = item;
        this.amount = amount;
    }

    @Override
    public boolean isFulFilled(final IItemHandler itemHandler)
    {
        return InventoryUtils.getItemCountInItemHandler(itemHandler, this.item) >= this.amount;
    }
}