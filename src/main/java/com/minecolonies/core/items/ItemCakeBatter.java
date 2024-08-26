package com.minecolonies.core.items;

import net.minecraft.world.item.Item;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

/**
 * Class handling Cake Batter.
 */
public class ItemCakeBatter extends Item
{
    /**
     * Sets the name, creative tab, and registers the Cake Batter item.
     *
     * @param properties the properties.
     */
    public ItemCakeBatter(final Properties properties)
    {
        super(properties.stacksTo(STACKSIZE));
    }
}
