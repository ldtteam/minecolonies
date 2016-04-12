package com.minecolonies.util;

/**
 * Useful math stuff to use statically.
 */
public final class MathUtils
{
    private static final int NANO_TIME_DIVIDER = 1000 * 1000 * 1000;

    /**
     * Private constructor to hide the public one
     */
    private MathUtils()
    {
    }

    /**
     * Returns the square product of a number
     *
     * @param number Number to square
     * @return Answer of calculation
     */
    public static double square(double number)
    {
        return number * number;
    }

    /**
     * Reduces nanosecond time to seconds
     *
     * @param nanoSeconds as input
     * @return nanoSeconds to seconds
     */
    public static long nanoSecondsToSeconds(long nanoSeconds)
    {
        return nanoSeconds / NANO_TIME_DIVIDER;
    }
}
