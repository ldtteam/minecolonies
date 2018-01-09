package com.minecolonies.coremod.colony.requestsystem.management.handlers.update;

import com.minecolonies.coremod.colony.requestsystem.management.IStandardRequestManager;
import org.jetbrains.annotations.NotNull;

public interface IUpdateStep
{

    int updatesToVersion();

    void update(@NotNull final IStandardRequestManager manager);
}
