package com.minecolonies.core.items;

/**
 * Class describing the magic potion item.
 */
public class ItemMagicPotion extends AbstractItemMinecolonies
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
