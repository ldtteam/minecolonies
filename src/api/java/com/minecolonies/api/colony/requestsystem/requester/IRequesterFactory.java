package com.minecolonies.api.colony.requestsystem.requester;

import com.minecolonies.api.colony.requestsystem.factory.IFactory;

/**
 * Interface describing objects that can construct IRequester objects.
 */
public interface IRequesterFactory<Input, Output extends IRequester> extends IFactory<Input, Output>
{

}
