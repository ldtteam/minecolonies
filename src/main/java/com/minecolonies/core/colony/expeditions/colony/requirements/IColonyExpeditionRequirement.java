package com.minecolonies.core.colony.expeditions.colony.requirements;

import net.minecraftforge.items.IItemHandler;

/**
 * Interface for defining different types of colony expedition requirements.
 */
public interface IColonyExpeditionRequirement
{
    /**
     * Whether the requirement is present in the provided inventory.
     *
     * @param itemHandler the inventory.
     * @return whether the requirement is present or not.
     */
    boolean isFulFilled(final IItemHandler itemHandler);
}