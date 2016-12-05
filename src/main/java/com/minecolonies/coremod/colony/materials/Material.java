package com.minecolonies.coremod.colony.materials;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Material Data Structure, also keeps note of where this material is stored.
 * Created: December 14, 2015
 *
 * @author Colton
 */
public class Material
{
    /**
     * Map of where each Material is stored and how much is there.
     */
    @NotNull
    private final Map<MaterialStore, Integer> locations = new HashMap<>();

    /**
     * Universal Item/Block ID.
     */
    private final Integer id;

    Material(final Integer id)
    {
        this.id = id;
    }

    /**
     * id should be unique.
     *
     * @return id
     */
    @Override
    public int hashCode()
    {
        return id;
    }

    @Override
    public boolean equals(@Nullable final Object material)
    {
        return material != null && material.getClass() == this.getClass() && id.equals(((Material) material).id);
    }

    /**
     * @return An unmodifiable version of locations.
     */
    @NotNull
    public Map<MaterialStore, Integer> getLocationsStored()
    {
        return Collections.unmodifiableMap(locations);
    }

    /**
     * Returns how much material is at a location.
     *
     * @param store    Location we are checking.
     * @param material Material that we are checking.
     * @return How many of material is stored at store.
     */
    public int getMaterialCount(@NotNull final MaterialStore store, final Material material)
    {
        if (locations.containsKey(store))
        {
            return store.getMaterialCount(material);
        }

        return 0;
    }

    void add(final MaterialStore store, final int quantity)
    {
        final Integer count = locations.get(store);
        if (count == null)
        {
            locations.put(store, quantity);
        }
        else
        {
            locations.put(store, count + quantity);
        }
    }

    void remove(final MaterialStore store, final int quantity)
    {
        final Integer count = locations.get(store);
        if (count == null || count < quantity)
        {
            throw new QuantityNotFound("MaterialStore (Material)", getID(), count == null ? 0 : count, quantity);
        }
        else if (count == quantity)
        {
            locations.remove(store);
        }
        else
        {
            locations.put(store, count - quantity);
        }
    }

    Integer getID()
    {
        return id;
    }

    void remove(final MaterialStore store)
    {
        locations.remove(store);
    }
}
