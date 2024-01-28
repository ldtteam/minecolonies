package com.minecolonies.api.colony.requestsystem.management;

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
    IToken<?> generateNewToken();
}
