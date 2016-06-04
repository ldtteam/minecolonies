package com.minecolonies.colony.materials;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameData;

import javax.annotation.Nullable;
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
        return material instanceof Material && id.equals(((Material)material).id);
    }

    @Override
    public String toString()
    {
        return id +
                " Block: " + GameData.getBlockRegistry().getObjectById(id) +
                " Item: " + GameData.getItemRegistry().getObjectById(id) +
                " Damage: " + this.getDamage();
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
            throw new QuantityNotFound("MaterialStore (Material)", this, count == null ? 0 : count, quantity);
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

    /**
     * Get the item associated with this material.
     *
     * @return Item.
     */
    public Item getItem()
    {
        return Item.getItemById(id >> 4);
    }

    public int getDamage()
    {
        return id & 0xF;
    }

    /**
     * @return true if this material is a block.
     */
    public boolean isBlock()
    {
        return getItem() instanceof ItemBlock;
    }

    /**
     * @return The block for this material. null if it isn't a Block.
     */
    @Nullable
    public Block getBlock()
    {
        return isBlock() ? ((ItemBlock) getItem()).getBlock() : null;
    }

    /**
     * @return ItemStack (quantity 1) for this Material.
     */
    public ItemStack getItemStack()
    {
        return getItemStack(1);
    }

    /**
     * @param quantity ItemStack quantity.
     * @return ItemStack for this Material.
     */
    public ItemStack getItemStack(int quantity)
    {
        return new ItemStack(getItem(), quantity, getDamage());
    }
}