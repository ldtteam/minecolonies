package com.minecolonies.core.colony.buildings.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a bucket of items the builder workers will pick up with them to go to the buildsite.
 */
public class BuilderBucket
{
    /**
     * The required resources (String identifier, and int count).
     */
    final Map<String, Integer> requiredResources = new HashMap<>();

    /**
     * The total accumulated stacks (required slots to hold this bucket.
     */
    int totalStacks = 0;

    /**
     * Get the required resources from the bucket.
     *
     * @return the map with the identifiers and count.
     */
    public Map<String, Integer> getResourceMap()
    {
        return requiredResources;
    }

    /**
     * Get how many total stacks were required up to now.
     *
     * @return the total stacks.
     */
    public int getTotalStacks()
    {
        return totalStacks;
    }

    /**
     * Set the required total stacks.
     *
     * @param totalStacks the total stucks.
     */
    public void setTotalStacks(final int totalStacks)
    {
        this.totalStacks = totalStacks;
    }

    /**
     * Add additional resources.
     *
     * @param key the key to add.
     * @param qty the quantity to add.
     */
    public void addOrAdjustResource(final String key, final int qty)
    {
        this.requiredResources.put(key, qty);
    }

    /**
     * Remove a specific resource.
     *
     * @param name the key.
     */
    public void removeResources(final String name)
    {
        this.requiredResources.remove(name);
    }
}
