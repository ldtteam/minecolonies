package com.minecolonies.coremod.items;

import net.minecraft.world.item.Item;

/**
 * Handles simple things that all items need.
 */
public abstract class AbstractItemMinecolonies extends Item
{
    /**
     * Sets the name, creative tab, and registers the item.
     *
     * @param properties the properties.
     */
    public AbstractItemMinecolonies(final Item.Properties properties)
    {
        super(properties);
    }
}
