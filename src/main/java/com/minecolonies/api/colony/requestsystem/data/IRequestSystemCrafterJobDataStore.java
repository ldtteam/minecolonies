package com.minecolonies.api.colony.requestsystem.data;

import com.minecolonies.api.colony.requestsystem.token.IToken;

import java.util.LinkedList;
import java.util.List;

/**
 * Interface defining the datastore for crafters.
 */
public interface IRequestSystemCrafterJobDataStore extends IDataStore
{
    /**
     * The task queue that needs to be processed by the worker.
     *
     * @return The task queue of the worker
     */
    LinkedList<IToken<?>> getQueue();

    /**
     * The list of tasks that is assigned to this worker, but for which the RS is still collecting resources.
     *
     * @return The assigned tasks list.
     */
    List<IToken<?>> getAssignedTasks();
}
