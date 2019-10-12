package com.minecolonies.api.util;

/**
 * Our own tuple implementation with hashcode and equals.
 * @param <A> The first obj.
 * @param <B> The second obj.
 */
public class Tuple<A, B> extends net.minecraft.util.Tuple<A, B>
{
    public Tuple(final A aIn, final B bIn)
    {
        super(aIn, bIn);
    }

    @Override
    public int hashCode()
    {
        return getFirst().hashCode() * 31 + getSecond().hashCode() * 31;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (o instanceof net.minecraft.util.Tuple)
        {
            return ((net.minecraft.util.Tuple) o).getFirst().equals(this.getFirst()) && ((net.minecraft.util.Tuple) o).getSecond().equals(this.getSecond());
        }
        return false;
    }
}
