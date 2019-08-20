package com.minecolonies.api.entity.citizen.citizenhandlers;

import net.minecraft.tileentity.ITickableTileEntity;

public interface ICitizenStuckHandler extends ITickableTileEntity
{
    /**
     * Let worker AIs check if the citizen is stuck to not track it on their own.
     * @return true if tried to move away already.
     */
    boolean isStuck();
}
