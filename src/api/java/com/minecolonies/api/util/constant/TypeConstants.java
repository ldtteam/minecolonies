package com.minecolonies.api.util.constant;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requestable.IRetryable;
import com.minecolonies.api.colony.requestsystem.resolver.player.IPlayerRequestResolver;
import com.minecolonies.api.colony.requestsystem.resolver.retrying.IRetryingRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.colony.requestsystem.token.StandardToken;

import java.util.UUID;

/**
 * Class holds type constants to reduce the formatting errors.
 */
public class TypeConstants
{
    /////General purpose
    public static final TypeToken<IToken>           ITOKEN           = TypeToken.of(IToken.class);
    public static final TypeToken<ILocation>        ILOCATION        = TypeToken.of(ILocation.class);
    public static final TypeToken<UUID>             UUID             = TypeToken.of(java.util.UUID.class);
    public static final TypeToken<FactoryVoidInput> FACTORYVOIDINPUT = TypeToken.of(FactoryVoidInput.class);
    public static final TypeToken<Object>           OBJECT           = TypeToken.of(Object.class);
    public static final TypeToken<IRequestable>     REQUESTABLE      = TypeToken.of(IRequestable.class);
    public static final TypeToken<IRetryable>       RETRYABLE        = TypeToken.of(IRetryable.class);

    /////Request system specific
    public static final TypeToken<IPlayerRequestResolver>   PLAYER_REQUEST_RESOLVER   = TypeToken.of(IPlayerRequestResolver.class);
    public static final TypeToken<IRetryingRequestResolver> RETRYING_REQUEST_RESOLVER = TypeToken.of(IRetryingRequestResolver.class);

    /////Implementations
    public static final TypeToken<StandardToken> STANDARDTOKEN = TypeToken.of(StandardToken.class);
}
