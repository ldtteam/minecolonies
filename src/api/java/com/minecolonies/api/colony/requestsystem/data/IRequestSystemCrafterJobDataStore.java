package com.minecolonies.api.colony.requestsystem.data;

import com.minecolonies.api.colony.requestsystem.token.IToken;

import java.util.LinkedList;
import java.util.List;

/**
 * Interface defining the datastore for crafters.
 */
public interface IRequestSystemCrafterJobDataStore extends IDataStore
{
    LinkedList<IToken<?>> getQueue();

    List<IToken<?>> getAssignedTasks();
}
