package com.minecolonies.core.util;

import net.minecraft.util.RandomSource;

import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * A collection that is able to generate a random selection item selection, based on weighted items.
 * <a href="https://stackoverflow.com/questions/6409652/random-weighted-selection-in-java">Source</a>
 *
 * @param <E> the collection type.
 */
public class RandomCollection<E>
{
    /**
     * The underlying data map.
     */
    private final NavigableMap<Double, E> map = new TreeMap<>();

    /**
     * The total weight of the collection.
     */
    private double total = 0;

    /**
     * Weighted add, provide a weight and the resulting item.
     *
     * @param weight the weight of the item.
     * @param result the input item.
     */
    public void add(double weight, E result)
    {
        if (weight <= 0)
        {
            return;
        }
        total += weight;
        map.put(total, result);
    }

    /**
     * Pick a random selection from the collection.
     *
     * @param random the input random.
     * @return the underlying random.
     */
    public E next(final RandomSource random)
    {
        final double value = random.nextDouble() * total;
        return map.higherEntry(value).getValue();
    }
}
