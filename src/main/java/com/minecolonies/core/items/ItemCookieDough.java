package com.minecolonies.core.items;

import net.minecraft.world.item.Item;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

/**
 * Class handling Cookie Dough.
 */
public class ItemCookieDough extends Item
{
    /**
     * Sets the name, creative tab, and registers the Cookie Dough item.
     *
     * @param properties the properties.
     */
    public ItemCookieDough(final Properties properties)
    {
        super(properties.stacksTo(STACKSIZE));
    }
}
