package com.minecolonies.coremod.colony.requestsystem.management.handlers;

import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.requestsystem.management.IStandardRequestManager;
import java.util.UUID;

/**
 * Class used to handle the inner workings of the request system with regards to tokens.
 */
public class TokenHandler implements ITokenHandler
{
    private final IStandardRequestManager manager;

    public TokenHandler(final IStandardRequestManager manager)
    {
        this.manager = manager;
    }

    @Override
    public IRequestManager getManager()
    {
        return manager;
    }

    /**
     * Generates a new Token for the request system.
     *
     * @return The new token.
     */
    @Override
    public IToken<?> generateNewToken()
    {
        // Force generic type to be correct.
        return manager.getFactoryController().getNewInstance(TypeConstants.ITOKEN, UUID.randomUUID());
    }
}
