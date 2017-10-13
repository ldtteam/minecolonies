package com.minecolonies.api.colony.requestsystem.token;

import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
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
    public Class<? extends FactoryVoidInput> getFactoryInputType()
    {
        return FactoryVoidInput.class;
    }
}
