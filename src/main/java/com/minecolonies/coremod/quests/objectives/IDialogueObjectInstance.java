package com.minecolonies.coremod.quests.objectives;

import com.minecolonies.api.quests.IObjectiveInstance;

/**
 * Objective data type for track of activities of dialogue interactions.
 */
public interface IDialogueObjectInstance extends IObjectiveInstance
{
    /**
     * Set the has interacted state.
     *
     * @param hasInteracted the new value.
     */
    void setHasInteracted(boolean hasInteracted);
}
