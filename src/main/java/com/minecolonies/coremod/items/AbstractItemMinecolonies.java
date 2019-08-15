package com.minecolonies.coremod.items;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.item.Item;

/**
 * Handles simple things that all items need.
 */
public abstract class AbstractItemMinecolonies extends Item
{
    /**
     * The name of the item.
     */
    private final String name;

    /**
     * Sets the name, creative tab, and registers the item.
     *
     * @param name The name of this item
     */
    public AbstractItemMinecolonies(final String name,final Item.Properties properties)
    {
        super(properties);
        this.name = name;
        setRegistryName(this.name);
    }
}
