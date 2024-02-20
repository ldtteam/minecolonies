package com.minecolonies.api.entity.visitor;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractVisitorExtraData<S> implements IVisitorExtraData<S>
{
    @NotNull
    private final String key;

    @NotNull
    private S value;

    protected AbstractVisitorExtraData(@NotNull final String key, @NotNull final S defaultValue)
    {
        this.key = key;
        this.value = defaultValue;
    }

    @Override
    @NotNull
    public final String getKey()
    {
        return key;
    }

    @Override
    @NotNull
    public final S getValue()
    {
        return value;
    }

    @Override
    public final void setValue(@NotNull final S value)
    {
        this.value = value;
    }

    @Override
    public int hashCode()
    {
        return key.hashCode();
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final AbstractVisitorExtraData<?> that = (AbstractVisitorExtraData<?>) o;

        return key.equals(that.key);
    }
}
