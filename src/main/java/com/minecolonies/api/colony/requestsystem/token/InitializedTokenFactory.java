package com.minecolonies.api.colony.requestsystem.token;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.util.constant.SerializationIdentifierConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import org.jetbrains.annotations.NotNull;

/**
 * {@link IToken} factory that produces an {@link IToken} from a random {@link java.util.UUID}
 */
public class InitializedTokenFactory extends AbstractTokenFactory<FactoryVoidInput>
{
    @NotNull
    @Override
    public StandardToken getNewInstance(@NotNull final FactoryVoidInput input)
    {
        return new StandardToken();
    }

    @NotNull
    @Override
    public TypeToken<? extends FactoryVoidInput> getFactoryInputType()
    {
        return TypeConstants.FACTORYVOIDINPUT;
    }

    @Override
    public short getSerializationId()
    {
        return SerializationIdentifierConstants.INITIALIZED_TOKEN_ID;
    }
}
