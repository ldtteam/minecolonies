package com.minecolonies.coremod.quests;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IQuestGiver;
import com.minecolonies.coremod.quests.type.effects.IQuestEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Quest instance
 */
public interface IQuest extends INBTSerializable<CompoundNBT>
{
    /**
     * Trigger for completion of a certain effect
     *
     * @param effect beeing completed
     */
    void onEffectComplete(final IQuestEffect effect);

    /**
     * Triggered when the quest is accepted
     *
     * @param player player accepting
     */
    void onStart(final PlayerEntity player);

    /**
     * Gets the colony this quest is associated with
     *
     * @return colony
     */
    IColony getColony();

    /**
     * Get the quest giver
     *
     * @return questgiver
     */
    IQuestGiver getQuestGiver();
}
