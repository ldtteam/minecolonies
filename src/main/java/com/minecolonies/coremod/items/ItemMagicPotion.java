package com.minecolonies.coremod.items;

import net.minecraft.world.item.CreativeModeTab;

import net.minecraft.world.item.Item.Properties;

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
    public ItemMagicPotion(String name, CreativeModeTab tab, Properties properties)
    {
        super(name, properties.stacksTo(16).tab(tab));
    }
}
