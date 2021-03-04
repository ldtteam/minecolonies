package com.minecolonies.api.util;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Useful math stuff to use statically.
 */
public final class MathUtils
{
    /**
     * Private constructor to hide the public one.
     */
    private MathUtils()
    {
    }

    /**
     * Returns the square product of a number.
     *
     * @param number Number to square.
     * @return Answer of calculation.
     */
    public static double square(final double number)
    {
        return number * number;
    }

    /**
     * Calculates the distance between two points without considering the y-value.
     *
     * @param position the start position.
     * @param target   the end position.
     * @return the distance.
     */
    public static double twoDimDistance(@NotNull final BlockPos position, @NotNull final BlockPos target)
    {
        final int x1 = position.getX();
        final int x2 = target.getX();
        final int z1 = position.getZ();
        final int z2 = target.getZ();

        //Hypot returns sqrt(x²+ y²) without intermediate overflow or underflow.
        return Math.hypot((double) x2 - x1, (double) z2 - z1);
    }
}
