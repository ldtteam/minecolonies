package com.minecolonies.api.quests;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Quest objective interface for all objective types.
 */
public interface IQuestObjective
{
    /**
     * Initialization of an objective.
     *
     * @param colonyQuest the colony quest it belongs to.
     * @return potentially related objective data.
     */
    @Nullable
    IObjectiveData init(final IColonyQuest colonyQuest);

    /**
     * On objective abort.
     * @param colonyQuest related colony quest.
     */
    default void onAbort(final IColonyQuest colonyQuest) { }

    /**
     * On world load trigger.
     * @param colonyQuest the quest.
     */
    default void onWorldLoad(IColonyQuest colonyQuest) { };

    @Nullable
    default IObjectiveData getObjectiveData() { return null; }

    /**
     * Get the list of reward unlocks from this objective.
     * @return the unlocked rewards by this objective.
     */
    List<Integer> getRewardUnlocks();
}
