package com.minecolonies.coremod.colony.requestsystem.management.handlers.update;

import com.minecolonies.coremod.colony.requestsystem.management.IStandardRequestManager;
import org.jetbrains.annotations.NotNull;

public interface IUpdateStep
{
    int updatesToVersion();

    default void update(@NotNull final UpdateType type, @NotNull final IStandardRequestManager manager)
    {
        this.update(manager);
    }

    default void update(@NotNull final IStandardRequestManager manager)
    {

    }
}
