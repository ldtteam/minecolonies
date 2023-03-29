package com.minecolonies.api.quests;

import com.minecolonies.api.colony.IColony;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.List;

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
     * Server only!
     *
     * @return the id of the quest giver.
     */
    IQuestGiver getQuestGiver();

    /**
     * Id of questgiver.
     * @return the id.
     */
    int getQuestGiverId();

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
     * @param player the player advancing this objective.
     * @param nextObjective the id to advance it to.
     */
    void advanceObjective(final Player player, int nextObjective);

    /**
     * Get the current objective index.
     * @return the index number.
     */
    int getIndex();

    /**
     * Get one of the other participants by index.
     * @param target the target participant id.
     * @return the quest participant.
     */
    IQuestParticipant getParticipant(int target);

    /**
     * Get the full list of quest participants.
     * @return the list of participants.
     */
    List<Integer> getParticipants();
}
