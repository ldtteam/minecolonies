package com.minecolonies.coremod.colony.requestsystem.management.handlers.update.implementation;

import com.minecolonies.api.colony.requestsystem.management.update.UpdateType;
import com.minecolonies.coremod.colony.requestsystem.management.IStandardRequestManager;
import com.minecolonies.coremod.colony.requestsystem.management.handlers.update.IUpdateStep;
import org.jetbrains.annotations.NotNull;

/**
 * Update fix to lumberjack
 */
public class ResetRSToFixRecipeHandling implements IUpdateStep
{
    @Override
    public int updatesToVersion()
    {
        return 10;
    }

    @Override
    public void update(@NotNull final UpdateType type, @NotNull final IStandardRequestManager manager)
    {
        if (type == UpdateType.DATA_LOAD)
        {
            manager.reset();
        }
    }
}
