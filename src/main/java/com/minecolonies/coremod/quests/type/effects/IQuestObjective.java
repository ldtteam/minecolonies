package com.minecolonies.coremod.quests.type.effects;

public interface IQuestObjective extends IQuestEffect
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
