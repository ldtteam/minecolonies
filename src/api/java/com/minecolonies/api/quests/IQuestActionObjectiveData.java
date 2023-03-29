package com.minecolonies.api.quests;

/**
 * Quest objective data. For in progress quests (e.g. kill x monsters, mine x blocks, etc).
 */
public interface IQuestActionObjectiveData
{
    /**
     * If the objective has been finished.
     * @return true if so.
     */
    boolean finishedObjective();
}
