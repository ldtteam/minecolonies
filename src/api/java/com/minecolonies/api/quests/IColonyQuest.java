package com.minecolonies.api.quests;

import com.minecolonies.api.colony.IColony;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Quest instance
 */
public interface IColonyQuest extends INBTSerializable<CompoundTag>
{
    /**
     * Triggered when the quest is accepted
     *
     * @param player player accepting
     */
    void onStart(final Player player, final IColony colony);

    /**
     * Get the id of the quest giver.
     *
     * @return the id of the quest giver.
     */
    IQuestGiver getQuestGiver();

    /**
     * Check if the expiration date relative to the colony day count was reached.
     * @param colony the colony to check the validity for.
     * @return true if so.
     */
    boolean isValid(IColony colony);

    /**
     * Get the quest id of the quest.
     * @return the id.
     */
    ResourceLocation getId();

    /**
     * On deletion of the quest.
     */
    void onDeletion();

    /**
     * Advance the quest objective to this id.
     * @param nextObjective the id to advance it to.
     */
    void advanceObjective(int nextObjective);

    /**
     * Get the current objective index.
     * @return the index number.
     */
    int getIndex();
}
