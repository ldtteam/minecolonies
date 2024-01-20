package com.minecolonies.core.colony.expeditions.colony.requirements;

import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.IToolType;
import net.minecraftforge.items.IItemHandler;

/**
 * Colony expedition requirements for providing any kind of {@link com.minecolonies.api.util.constant.ToolType}.
 */
public class ColonyExpeditionToolRequirement implements IColonyExpeditionRequirement
{
    /**
     * The required tool type.
     */
    private final IToolType toolType;

    /**
     * The minimum amount to fulfill this requirement.
     */
    private final int amount;

    /**
     * Default constructor.
     *
     * @param toolType the required tool type.
     * @param amount   the minimum amount.
     */
    public ColonyExpeditionToolRequirement(final IToolType toolType, final int amount)
    {
        this.toolType = toolType;
        this.amount = amount;
    }

    @Override
    public boolean isFulFilled(final IItemHandler itemHandler)
    {
        return InventoryUtils.getItemCountInItemHandler(itemHandler, stack -> ItemStackUtils.hasToolLevel(stack, this.toolType, 0, 5)) >= this.amount;
    }
}