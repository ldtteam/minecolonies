package com.minecolonies.util;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntConsumer;

/**
 * Some functional helper methods.
 */
public class FunctionalUtils
{

    /**
     * @param xStart   the lower bound to start iterating on x
     * @param xEnd     the upper bound to stop iterating on x
     * @param yStart   the lower bound to start iterating on y
     * @param yEnd     the upper bound to stop iterating on y
     * @param zStart   the lower bound to start iterating on z
     * @param zEnd     the upper bound to stop iterating on z
     * @param consumer the consumer function to execute each step
     */
    public static void forXYZLoop(int xStart, int xEnd, int yStart, int yEnd, int zStart, int zEnd, Function<Integer, BiConsumer<Integer, Integer>> consumer)
    {

        forXYLoop(xStart, xEnd, yStart, yEnd, (x, y) -> {
            forLoop(zStart, zEnd, z -> {
                consumer.apply(x).accept(y, z);
            });
        });

    }

    /**
     * loop over two variables from lower to upper bound.
     *
     * @param xStart   the lower bound to start iterating on x
     * @param xEnd     the upper bound to stop iterating on x
     * @param yStart   the lower bound to start iterating on y
     * @param yEnd     the upper bound to stop iterating on y
     * @param consumer the consumer function to execute each step
     */
    public static void forXYLoop(int xStart, int xEnd, int yStart, int yEnd, BiConsumer<Integer, Integer> consumer)
    {
        forLoop(xStart, xEnd, x -> {
            forLoop(yStart, yEnd, y -> {
                consumer.accept(x, y);
            });
        });
    }

    /**
     * Loop from lowerBound to upperBound and apply consumer each step.
     *
     * @param lowerBound the lower bound to start iterating on
     * @param upperBound the upper bound to stop iterating on
     * @param consumer   the consumer function to execute each step
     */
    public static void forLoop(int lowerBound, int upperBound, IntConsumer consumer)
    {
        for (int i = lowerBound; i < upperBound; i++)
        {
            consumer.accept(i);
        }
    }

}
