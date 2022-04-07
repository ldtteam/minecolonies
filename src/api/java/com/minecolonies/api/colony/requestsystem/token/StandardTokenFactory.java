package com.minecolonies.api.colony.requestsystem.token;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.util.constant.SerializationIdentifierConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Factory for the standard request token, {@link StandardToken}
 */
public class StandardTokenFactory extends AbstractTokenFactory<UUID>
{

    @NotNull
    @Override
    public TypeToken<UUID> getFactoryInputType()
    {
        return TypeConstants.UUID;
    }

    /**
     * Method to get a new instance of a token given the input and token.
     *
     * @param input The input to build a new token for.
     * @return The new output instance for a given input.
     */
    @NotNull
    @Override
    public StandardToken getNewInstance(@NotNull final UUID input)
    {
        return new StandardToken(input);
    }

    @Override
    public short getSerializationId()
    {
        return SerializationIdentifierConstants.STANDARD_TOKEN_ID;
    }
}
