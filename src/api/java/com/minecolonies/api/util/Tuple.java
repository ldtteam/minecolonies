package com.minecolonies.api.util;

import org.jetbrains.annotations.Nullable;

/**
 * Our own tuple implementation with hashcode and equals.
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

    @Nullable
    public A getA() {
        return this.a;
    }

    @Nullable
    public B getB() {
        return this.b;
    }

    @Override
    public int hashCode()
    {
        return getA().hashCode() * 31 + getB().hashCode() * 31;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (o instanceof Tuple)
        {
            return ((Tuple<?, ?>) o).getA().equals(this.getA()) && ((Tuple<?, ?>) o).getB().equals(this.getB());
        }
        return false;
    }
}
