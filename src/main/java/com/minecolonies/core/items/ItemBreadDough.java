package com.minecolonies.core.items;

import net.minecraft.world.item.Item;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

/**
 * Class handling Bread Dough.
 */
public class ItemBreadDough extends Item
{
    /**
     * Sets the name, creative tab, and registers the Bread Dough item.
     *
     * @param properties the properties.
     */
    public ItemBreadDough(final Properties properties)
    {
        super(properties.stacksTo(STACKSIZE));
    }
}
