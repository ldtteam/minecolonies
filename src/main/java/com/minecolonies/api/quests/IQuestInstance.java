package com.minecolonies.api.quests;

import com.minecolonies.api.colony.IColony;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Quest instance
 */
public interface IQuestInstance extends INBTSerializable<CompoundTag>
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
     * @return the next objective instance.
     */
    IObjectiveInstance advanceObjective(final Player player, int nextObjective);

    /**
     * On question completion call.
     */
    void onCompletion();

    /**
     * Get the current objective index.
     * @return the index number.
     */
    int getObjectiveIndex();

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

    /**
     * Get the id of the current task holder in the quest.
     * @return the quest participant.
     */
    int getQuestTarget();

    /**
     * Get the objective data of the current objective.
     * @return the data.
     */
    @Nullable
    IObjectiveInstance getCurrentObjectiveInstance();

    /**
     * Get the colony matching the quest.
     * @return the colony.
     */
    IColony getColony();

    /**
     * Get the player UUID that accepted the quest.
     * @return the player uuid.
     */
    UUID getAssignedPlayer();

    /**
     * Simple advance objective by one.
     * @param player the player involved.
     */
    void advanceObjective(Player player);

    /**
     * On world load trigger.
     */
    void onWorldLoad();
}
