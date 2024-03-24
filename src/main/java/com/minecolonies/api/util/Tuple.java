package com.minecolonies.api.util;

import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Our own tuple implementation with hashcode and equals.
 *
 * @param <A> The first obj.
 * @param <B> The second obj.
 */
public class Tuple<A, B>
{
    private A a;
    private B b;

    public Tuple(@Nullable final A aIn, @Nullable final B bIn)
    {
        this.a = aIn;
        this.b = bIn;
    }

    public Tuple(final Pair<A, B> codecPair)
    {
        this(codecPair.getFirst(), codecPair.getSecond());
    }

    @Nullable
    public A getA()
    {
        return this.a;
    }

    @Nullable
    public B getB()
    {
        return this.b;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(a, b);
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
        final Tuple<?, ?> tuple = (Tuple<?, ?>) o;
        return Objects.equals(a, tuple.a) &&
                 Objects.equals(b, tuple.b);
    }

    public Pair<A, B> toCodecPair()
    {
        return new Pair<A,B>(a, b);
    }
}
