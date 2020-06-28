package com.minecolonies.coremod.colony.requestsystem.management.handlers;

import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.coremod.colony.requestsystem.management.handlers.update.UpdateType;

public interface IUpdateHandler
{
    IRequestManager getManager();

    void handleUpdate(final UpdateType type);

    int getCurrentVersion();
}
