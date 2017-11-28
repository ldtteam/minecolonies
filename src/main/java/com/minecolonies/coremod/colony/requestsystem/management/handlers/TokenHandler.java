package com.minecolonies.coremod.colony.requestsystem.management.handlers;

import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.constant.TypeConstants;

import java.util.UUID;

/**
 * Class used to handle the inner workings of the request system with regards to tokens.
 */
public final class TokenHandler
{

    /**
     * Generates a new Token for the request system.
     *
     * @param manager The manager to generate a new token for.
     * @return The new token.
     */
    public static IToken<UUID> generateNewToken(final IRequestManager manager)
    {
        //Force generic type to be correct.
        return (IToken<UUID>) manager.getFactoryController().getNewInstance(TypeConstants.ITOKEN, UUID.randomUUID());
    }
}
