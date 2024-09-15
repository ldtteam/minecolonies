package com.minecolonies.core.items;

import net.minecraft.world.item.Item;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

/**
 * Class describing the Ancient Tome item.
 */
public class ItemMistletoe extends Item
{
    /**
     * Sets the name, creative tab, and registers the Ancient Tome item.
     *
     * @param properties the properties.
     */
    public ItemMistletoe(final Properties properties)
    {
        super(properties.stacksTo(STACKSIZE));
    }
}
