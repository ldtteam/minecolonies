package com.minecolonies.core.items;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

/**
 * Class handling Cake Batter.
 */
public class ItemCakeBatter extends AbstractItemMinecolonies
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
