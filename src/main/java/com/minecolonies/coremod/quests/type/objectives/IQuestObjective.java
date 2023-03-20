package com.minecolonies.coremod.quests.type.objectives;

import com.minecolonies.coremod.quests.type.sideeffects.IQuestSideEffect;

public interface IQuestObjective
{
    /**
     * Whether this objective is fulfilled
     *
     * @return true if so
     */
    boolean isFulfilled();

    /**
     * Maximum required
     *
     * @return mxa
     */
    int getMaxObjectiveCount();

    /**
     * Current achieved
     *
     * @return achieved count
     */
    int getCurrentObjectiveCount();
}
