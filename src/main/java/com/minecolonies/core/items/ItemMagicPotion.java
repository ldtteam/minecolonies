package com.minecolonies.core.items;

import net.minecraft.world.item.Item;

/**
 * Class describing the magic potion item.
 */
public class ItemMagicPotion extends Item
{
    /**
     * Sets the name, creative tab, and registers the magic potion item.
     *
     * @param properties the properties.
     */
    public ItemMagicPotion(Properties properties)
    {
        super(properties.stacksTo(16));
    }
}
