package com.minecolonies.colony.materials;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Material Data Structure, also keeps note of where this material is stored
 * Created: December 14, 2015
 *
 * @author Colton
 */
public class Material
{
    /**
     * Map of where each Material is stored and how much is there.
     */
    private Map<MaterialStore, Integer> locations = new HashMap<>();

    /**
     * Universal Item/Block ID
     */
    private Integer id;

    Material(Integer id)
    {
        this.id = id;
    }

    Integer getID()
    {
        return id;
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
    public boolean equals(Object material)
    {
        return material != null && material.getClass() == this.getClass() && id.equals(((Material)material).id);
    }

    /**
     * @return An unmodifiable version of locations
     */
    public Map<MaterialStore, Integer> getLocationsStored()
    {
        return Collections.unmodifiableMap(locations);
    }

    /**
     * Returns how much material is at a location.
     *
     * @param store Location we are checking
     * @param material Material that we are checking
     * @return How many of material is stored at store
     */
    public int getMaterialCount(MaterialStore store, Material material)
    {
        if(locations.containsKey(store))
        {
            return store.getMaterialCount(material);
        }

        return 0;
    }

    void add(MaterialStore store, int quantity)
    {
        Integer count = locations.get(store);
        if(count == null)
        {
            locations.put(store, quantity);
        }
        else
        {
            locations.put(store, count + quantity);
        }
    }

    void remove(MaterialStore store, int quantity)
    {
        Integer count = locations.get(store);
        if(count == null || count < quantity)
        {
            throw new QuantityNotFound("MaterialStore (Material)", getID(), count == null ? 0 : count, quantity);
        }
        else if(count == quantity)
        {
            locations.remove(store);
        }
        else
        {
            locations.put(store, count - quantity);
        }
    }

    void remove(MaterialStore store)
    {
        locations.remove(store);
    }
}
