package com.minecolonies.coremod.colony.requestsystem.exceptions;

/**
 * Exception thrown by the RS, when an {@link com.minecolonies.coremod.colony.requestsystem.resolvers.core.AbstractBuildingDependentRequestResolver}
 * can not find the building a request should be associated with during resolving.
 */
public class NoBuildingWithAssignedRequestFoundException extends RuntimeException
{

    public NoBuildingWithAssignedRequestFoundException(final String message)
    {
        super(message);
    }
}
