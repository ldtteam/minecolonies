package com.minecolonies.api.quests;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Quest objective interface for all objectives.
 */
public interface IQuestObjectiveTemplate
{
    /**
     * Initialization of an objective.
     *
     * @param colonyQuest the colony quest it belongs to.
     * @return potentially related objective data.
     */
    @Nullable
    IObjectiveInstance startObjective(final IQuestInstance colonyQuest);

    /**
     * On objective abort.
     * @param colonyQuest related colony quest.
     */
    default void onCancellation(final IQuestInstance colonyQuest) { }

    /**
     * On world load trigger.
     * @param colonyQuest the quest.
     */
    default void onWorldLoad(IQuestInstance colonyQuest) { };

    /**
     * Get objective data related to the objective.
     * @return the data, default null.
     */
    @Nullable
    default IObjectiveInstance createObjectiveInstance() { return null; }

    /**
     * Get the list of reward unlocks from this objective.
     * @return the unlocked rewards by this objective.
     */
    List<Integer> getRewardUnlocks();
}
