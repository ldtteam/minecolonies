package com.minecolonies.api.colony.requestsystem.data;

import com.minecolonies.api.colony.requestsystem.token.IToken;

import java.util.LinkedList;

public interface IRequestSystemDeliveryManJobDataStore extends IDataStore
{
    LinkedList<IToken<?>> getQueue();
}
