package com.minecolonies.coremod.colony.buildings.inventory;

import com.minecolonies.api.util.Tuple;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Generic index class which holds an index of type Key and a Set of Values.
 */
public class HashSetIndex<Key, Value>
{
    /**
     * The index map
     */
    private HashMap<Key, Set<Value>> indexMap;

    /**
     * Create new index.
     */
    public HashSetIndex()
    {
        indexMap = new HashMap<>();
    }

    /**
     * Add a value to the index for the given Key
     *
     * @param key Key of the value to add
     * @param val the new value
     */
    public void addToIndex(Key key, Value val)
    {
        if (key == null || val == null)
        {
            return;
        }

        if (indexMap.containsKey(key))
        {
            indexMap.get(key).add(val);
        }
        else
        {
            Set<Value> set = new HashSet<>();
            set.add(val);
            indexMap.put(key, set);
        }
    }

    /**
     * Removes a value from a key.
     *
     * @param key Key to index from
     * @param val value to remove
     */
    public void removeFromIndex(Key key, Value val)
    {
        if (!indexMap.containsKey(key))
        {
            return;
        }

        indexMap.get(key).remove(val);

        if (indexMap.get(key).isEmpty())
        {
            indexMap.remove(key);
        }
    }

    /**
     * Removes a key and all its values from the index.
     *
     * @param key Key to be removed
     */
    public void removeKeyFromIndex(Key key)
    {
        indexMap.remove(key);
    }

    /**
     * Get a value from a key
     *
     * @param key Key entry
     * @return Value for this key
     */
    public Set<Value> getValueForKey(Key key)
    {
        return indexMap.get(key);
    }

    /**
     * Access the index map
     *
     * @return index map
     */
    public Map<Key, Set<Value>> getIndexMap()
    {
        return indexMap;
    }

    /**
     * Checks the index's Key for fitting values for a given predicate
     *
     * @param valueSelectionPredicate predicate to check
     * @return Set of values fitting the predicate
     */
    public Tuple<Key, Set<Value>> getFirstEntryForPredicate(@NotNull final Predicate<Key> valueSelectionPredicate)
    {
        for (final Map.Entry<Key, Set<Value>> entry : indexMap.entrySet())
        {
            if (valueSelectionPredicate.test(entry.getKey()))
            {
                return new Tuple<>(entry.getKey(), entry.getValue());
            }
        }
        return null;
    }
}
