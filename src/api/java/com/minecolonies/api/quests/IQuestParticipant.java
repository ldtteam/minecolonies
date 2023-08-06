package com.minecolonies.api.quests;

import net.minecraft.resources.ResourceLocation;

/**
 * Type of entity that participates somehow in quests.
 */
public interface IQuestParticipant
{
    /**
     * Notify quest participant about their participation.
     * @param quest the quest to assign to the entity.
     */
    void addQuestParticipation(final IQuestInstance quest);

    /**
     * Method for cleanup purposes on quest deletion.
     * @param questId the id of the quest.
     */
    void onQuestDeletion(final ResourceLocation questId);

    /**
     * Check if the citizen has a quest open.
     * @param questId the id of the quest.
     * @return true if so.
     */
    boolean isParticipantOfQuest(final ResourceLocation questId);

    /**
     * Initiates a dialogue at the citizen.
     * @param quest the quest to open it for.
     * @param index the current objective index
     */
    void openDialogue(final IQuestInstance quest, final int index);

    /**
     * Get a display name for the quest participant.
     * @return the display name.
     */
    String getName();
}



