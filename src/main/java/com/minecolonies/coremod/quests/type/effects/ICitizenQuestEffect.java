package com.minecolonies.coremod.quests.type.effects;

import com.minecolonies.api.colony.ICitizenData;

/**
 * Citizen related quest effect
 */
public interface ICitizenQuestEffect extends IQuestEffect
{
    /**
     * Gets the citizen data
     *
     * @return
     */
    ICitizenData getCitizenData();

    /**
     * Applies the effect to the given citizen
     *
     * @param data
     */
    void applyToCitizen(final ICitizenData data);
}
