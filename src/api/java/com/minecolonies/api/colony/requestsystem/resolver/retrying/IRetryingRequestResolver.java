package com.minecolonies.api.colony.requestsystem.resolver.retrying;

import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.requestable.IRetryable;
import com.minecolonies.api.colony.requestsystem.resolver.IQueuedRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import net.minecraft.client.renderer.texture.ITickable;
import org.jetbrains.annotations.Nullable;

public interface IRetryingRequestResolver extends IQueuedRequestResolver<IRetryable>, ITickable
{
    /**
     * Update the associated manager data that links this resolver to a manager.
     *
     * @param manager the new associated manager.
     */
    void updateManager(IRequestManager manager);

    /**
     * Method to get the maximal amount of tries that is resolver attempts.
     *
     * @return The maximal amount of tries.
     */
    int getMaximalTries();

    /**
     * Method to get the maximal ticks between retries.
     *
     * @return The maximal amount of ticks between retries.
     */
    int getMaximalDelayBetweenRetriesInTicks();

    /**
     * The current attempt to be reassigned.
     *
     * @return The current attempt of reassignment. -1 if none is undertaken.
     */
    int getCurrentReassignmentAttempt();

    /**
     * Method to get an indication of reassignment.
     *
     * @return an indication of reassignment.
     */
    default boolean isReassigning()
    {
        return getCurrentlyBeingReassignedRequest() != null;
    }

    /**
     * Method to get the token of the request that is currently being reassigned
     *
     * @return The token of the quest that is being reassigned, null if no reassignment is being performed.
     */
    @Nullable
    IToken<?> getCurrentlyBeingReassignedRequest();
}
