package com.minecolonies.api.entity.ai.combat;

import com.minecolonies.api.entity.ai.statemachine.states.IAIState;

/**
 * Combat AI States
 */
public enum CombatAIStates implements IAIState
{
    ATTACKING(false),
    NO_TARGET(true);

    /**
     * Is it okay to eat.
     */
    private boolean isOkayToEat;

    /**
     * Create a new one.
     *
     * @param okayToEat if okay.
     */
    CombatAIStates(final boolean okayToEat)
    {
        this.isOkayToEat = okayToEat;
    }

    /**
     * Method to check if it is okay.
     *
     * @return true if so.
     */
    public boolean isOkayToEat()
    {
        return isOkayToEat;
    }
}
