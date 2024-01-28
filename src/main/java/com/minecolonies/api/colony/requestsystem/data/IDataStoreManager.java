package com.minecolonies.api.colony.requestsystem.data;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.token.IToken;

import java.util.function.Supplier;

public interface IDataStoreManager
{
    <T extends IDataStore> T get(IToken<?> id, TypeToken<T> type);

    <T extends IDataStore> T get(IToken<?> id, Supplier<T> factory);

    void remove(IToken<?> id);

    void removeAll();
}
