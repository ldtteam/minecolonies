package com.minecolonies.core.colony.requestsystem.management.handlers.update.implementation;

import com.minecolonies.api.colony.requestsystem.management.update.UpdateType;
import com.minecolonies.core.colony.requestsystem.management.IStandardRequestManager;
import com.minecolonies.core.colony.requestsystem.management.handlers.update.IUpdateStep;
import org.jetbrains.annotations.NotNull;

/**
 * Update fix to restaurant.
 */
public class ResetRSToUpdateRestaurantResolver implements IUpdateStep
{
    @Override
    public int updatesToVersion()
    {
        return 14;
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
