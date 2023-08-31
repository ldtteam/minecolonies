package com.minecolonies.api.quests;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Objective data type to take track of activities.
 */
public interface IObjectiveInstance extends INBTSerializable<CompoundTag>
{
    /**
     * Check if the objective has been fulfilled.
     * @return true if so.
     */
    boolean isFulfilled();

    /**
     * Get the missing quantity.
     * @return the quantity.
     */
    int getMissingQuantity();
}
