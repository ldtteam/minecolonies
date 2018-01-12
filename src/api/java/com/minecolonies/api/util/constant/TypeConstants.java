package com.minecolonies.api.util.constant;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requestable.IRetryable;
import com.minecolonies.api.colony.requestsystem.resolver.player.IPlayerRequestResolver;
import com.minecolonies.api.colony.requestsystem.resolver.retrying.IRetryingRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.colony.requestsystem.token.StandardToken;
import com.minecolonies.api.crafting.RecipeStorage;

import java.util.UUID;

import static com.minecolonies.api.util.constant.Suppression.RAWTYPES;

/**
 * Class holds type constants to reduce the formatting errors.
 */
public final class TypeConstants
{
    /////Java types
    public static final TypeToken<Integer>  INTEGER = TypeToken.of(Integer.class);
    public static final TypeToken<TypeToken> TYPETOKEN = TypeToken.of(TypeToken.class);
    public static final TypeToken<Class> CLASS = TypeToken.of(Class.class);

    /////General purpose
    @SuppressWarnings(RAWTYPES)
    public static final TypeToken<IToken>           ITOKEN           = TypeToken.of(IToken.class);
    public static final TypeToken<ILocation>        ILOCATION        = TypeToken.of(ILocation.class);
    public static final TypeToken<UUID>             UUID             = TypeToken.of(java.util.UUID.class);
    public static final TypeToken<FactoryVoidInput> FACTORYVOIDINPUT = TypeToken.of(FactoryVoidInput.class);
    public static final TypeToken<Object>           OBJECT           = TypeToken.of(Object.class);
    public static final TypeToken<IRequestable>     REQUESTABLE      = TypeToken.of(IRequestable.class);
    public static final TypeToken<IRetryable>       RETRYABLE        = TypeToken.of(IRetryable.class);
    public static final TypeToken<RecipeStorage>    RECIPE           = TypeToken.of(RecipeStorage.class);
    public static final TypeToken<IDeliverable>     DELIVERY         = TypeToken.of(IDeliverable.class);

    /////Request system specific
    public static final TypeToken<IPlayerRequestResolver>   PLAYER_REQUEST_RESOLVER   = TypeToken.of(IPlayerRequestResolver.class);
    public static final TypeToken<IRetryingRequestResolver> RETRYING_REQUEST_RESOLVER = TypeToken.of(IRetryingRequestResolver.class);

    /////Implementations
    public static final TypeToken<StandardToken> STANDARDTOKEN = TypeToken.of(StandardToken.class);
}
