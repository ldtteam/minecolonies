package com.minecolonies.coremod.colony.requestsystem.management.handlers;

import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.token.IToken;

public interface ITokenHandler
{
    IRequestManager getManager();

    /**
     * Generates a new Token for the request system.
     *
     * @return The new token.
     */
    IToken generateNewToken();
}
