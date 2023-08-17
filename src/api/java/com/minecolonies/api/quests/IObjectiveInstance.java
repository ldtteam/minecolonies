package com.minecolonies.api.quests;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Objective data type to take track of activities.
 */
public interface IObjectiveInstance extends INBTSerializable<CompoundTag>
{
    /**
     * Check if the objective has been fulfilled.
     *
     * @return true if so.
     */
    boolean isFulfilled();

    /**
     * Get the missing quantity.
     *
     * @return the quantity.
     */
    int getMissingQuantity();

    /**
     * Get a {@link Component} instance with the text containing the progress of this objective.
     *
     * @param quest the quest to get the info from.
     * @return the chat component.
     */
    MutableComponent getProgressText(IQuestInstance quest);
}
