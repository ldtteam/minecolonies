package com.minecolonies.api.entity.citizen.citizenhandlers;

import net.minecraft.util.ITickable;

public interface ICitizenStuckHandler extends ITickable
{
    /**
     * Let worker AIs check if the citizen is stuck to not track it on their own.
     * @return true if tried to move away already.
     */
    boolean isStuck();
}
