package com.minecolonies.api.colony.requestsystem.request;

/**
 * Marker interface for requests that should be retried when they initially failed a couple of seconds later.
 */
public interface IRetryableRequest<R> extends IRequest<R>
{
}
