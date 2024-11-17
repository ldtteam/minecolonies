package com.minecolonies.core.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;

/**
 * A collection that is able to generate a random item selection, based on weighted items.
 * <a href="https://stackoverflow.com/questions/6409652/random-weighted-selection-in-java">Source</a>
 *
 * @param <K> the collection key.
 * @param <E> the collection type.
 */
public class RandomCollection<K, E>
{
    /**
     * The underlying pointer map.
     */
    private final Map<K, Double> pointers = new HashMap<>();

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
     * @param key    the input key.
     * @param result the input item.
     */
    public void add(double weight, K key, E result)
    {
        if (weight <= 0)
        {
            return;
        }
        total += weight;
        map.put(total, result);
        pointers.put(key, total);
    }

    /**
     * Pick a random selection from the collection.
     *
     * @param random the input random.
     * @return the underlying random.
     */
    @Nullable
    public E next(final Random random)
    {
        final double value = random.nextDouble() * total;
        final Entry<Double, E> entry = map.higherEntry(value);
        if (entry == null)
        {
            return null;
        }
        return entry.getValue();
    }

    /**
     * Get a collection of all items.
     *
     * @return the collection of items.
     */
    @NotNull
    public Collection<E> getAll()
    {
        return map.values();
    }

    /**
     * Get the item for the given key.
     *
     * @param key the input key.
     * @return the value or null.
     */
    @Nullable
    public E get(final K key)
    {
        final Double pointer = pointers.get(key);
        if (pointer == null)
        {
            return null;
        }
        return map.get(pointer);
    }
}