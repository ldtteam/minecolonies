package com.minecolonies.api.util.constant;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
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


    /////Implementations
    public static final TypeToken<StandardToken> STANDARDTOKEN = TypeToken.of(StandardToken.class);
}
