package com.minecolonies.api.quests;

import com.minecolonies.api.colony.IColony;

/**
 * Quest Data Instance.
 */
public interface IQuestData
{
    /**
     * This is where we actually check if the colony fulfills the quest requirements.
     * @param colony the colony to check.
     * @return the colony quest instance if successful.
     */
    IColonyQuest attemptStart(final IColony colony);

    /**
     * Timeout in ingame days until an available quest or in progress quest gets deleted.
     * @return the timeout.
     */
    int getQuestTimeout();

    /**
     * How often we can redo this quest.
     * @return the number.
     */
    int getMaxOccurrence();

    /**
     * Get the objective at a given index.
     * @param index the index of the objective.
     * @return the current objective.
     */
    IQuestObjective getObjective(final int index);

    /**
     * Get the objective count of this quest.
     * @return the count.
     */
    int getObjectiveCount();
}
