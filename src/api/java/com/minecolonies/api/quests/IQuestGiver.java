package com.minecolonies.api.quests;

/**
 * Interface describing an entity that hands out quests.
 */
public interface IQuestGiver extends IQuestParticipant
{
    /**
     * Assign quest authority to quest giver.
     * @param quest the quest to assign to the entity.
     */
    void assignQuest(final IQuestInstance quest);
}
