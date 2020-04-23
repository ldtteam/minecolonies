package com.minecolonies.coremod.items;

import net.minecraft.item.Item;

/**
 * Handles simple things that all items need.
 */
public abstract class AbstractItemMinecolonies extends Item
{
    /**
     * The name of the item.
     */
    private final String name;

    /**
     * Sets the name, creative tab, and registers the item.
     *
     * @param name The name of this item
     * @param properties the properties.
     */
    public AbstractItemMinecolonies(final String name,final Item.Properties properties)
    {
        super(properties);
        this.name = name;
        setRegistryName(this.name);
    }
}
