package com.minecolonies.coremod.colony.requestsystem.management.handlers.update.implementation;

import com.minecolonies.coremod.colony.requestsystem.management.IStandardRequestManager;
import com.minecolonies.coremod.colony.requestsystem.management.handlers.update.IUpdateStep;
import com.minecolonies.coremod.colony.requestsystem.management.handlers.update.UpdateType;
import org.jetbrains.annotations.NotNull;

/**
 * Update fix to register the new Pickup-Resolver for deliverymen.
 */
public class ResetRSToAddDelivermanPickups implements IUpdateStep
{
    @Override
    public int updatesToVersion()
    {
        return 3;
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
