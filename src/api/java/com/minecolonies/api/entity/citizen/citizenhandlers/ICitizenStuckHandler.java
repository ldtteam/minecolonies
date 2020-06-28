package com.minecolonies.api.entity.citizen.citizenhandlers;

public interface ICitizenStuckHandler
{
    /**
     * Let worker AIs check if the citizen is stuck to not track it on their own.
     * @return true if tried to move away already.
     */
    boolean isStuck();

    /**
     * To tick the handler.
     */
    void tick();
}
