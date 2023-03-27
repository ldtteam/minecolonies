package com.minecolonies.api.quests;

import net.minecraft.resources.ResourceLocation;

/**
 * Type of entity that participates somehow in quests.
 */
public interface IQuestParticipant
{
    /**
     * Check if the quest giver is still available.
     * @return true if so.
     */
    boolean isAlive();

    /**
     * Notify quest participant about their participation.
     * @param quest the quest to assign to the entity.
     */
    void addQuestParticipation(final IColonyQuest quest);

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
    boolean hasQuestOpen(final ResourceLocation questId);
}


