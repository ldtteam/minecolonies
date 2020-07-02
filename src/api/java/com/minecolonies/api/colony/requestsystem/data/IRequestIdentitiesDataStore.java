package com.minecolonies.api.colony.requestsystem.data;

import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.token.IToken;

/**
 * The KV-Store for the requests and their identities. Extends the {@link IIdentitiesDataStore} with {@link IToken} as key type and {@link IRequest} as value type.
 */
public interface IRequestIdentitiesDataStore extends IIdentitiesDataStore<IToken<?>, IRequest<?>>
{
}
