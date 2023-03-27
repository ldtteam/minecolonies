package com.minecolonies.coremod.quests.sideeffects;

import com.minecolonies.api.colony.ICitizenData;

/**
 * Citizen related quest effect
 */
public interface ICitizenQuestSideEffect extends IQuestSideEffect
{
    /**
     * Gets the citizen data
     *
     * @return the affected citizen.
     */
    ICitizenData getCitizenData();

    /**
     * Applies the effect to the given citizen
     *
     * @param data the citizen to apply it to.
     */
    void applyToCitizen(final ICitizenData data);
}
