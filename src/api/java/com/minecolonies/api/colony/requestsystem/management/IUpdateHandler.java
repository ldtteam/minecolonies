package com.minecolonies.api.colony.requestsystem.management;

import com.minecolonies.api.colony.requestsystem.management.update.UpdateType;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;

public interface IUpdateHandler
{
    IRequestManager getManager();

    void handleUpdate(final UpdateType type);

    int getCurrentVersion();
}
