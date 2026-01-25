package com.minecolonies.api.entity.visitor;

import org.jetbrains.annotations.NotNull;

/**
 * Base class for extra visitor data.
 *
 * @param <S> the data type of the stored data.
 */
public abstract class AbstractVisitorExtraData<S> implements IVisitorExtraData<S>
{
    /**
     * The NBT key under which the data is stored.
     */
    @NotNull
    private final String key;

    /**
     * The current value of the extra data.
     */
    @NotNull
    private S value;

    /**
     * Internal constructor.
     *
     * @param key          the NBT key.
     * @param defaultValue the default value that the extra data initially has.
     */
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
