package com.minecolonies.coremod.items;

import net.minecraft.item.ItemGroup;

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
    public ItemMagicPotion(String name, ItemGroup tab, Properties properties)
    {
        super(name, properties.stacksTo(16).tab(tab));
    }
}
