package com.minecolonies.api.quests;


/**
 * Quest objective interface for all objective types.
 */
public interface IQuestObjective
{
    /**
     * Initialization of an objective.
     *
     * @param colonyQuest the colony quest it belongs to.
     * @return
     */
    IObjectiveData init(final IColonyQuest colonyQuest);

    /**
     * On objective abort.
     */
    default void onAbort() { }
}
