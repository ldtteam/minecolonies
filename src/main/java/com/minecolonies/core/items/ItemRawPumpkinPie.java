package com.minecolonies.core.items;

import net.minecraft.world.item.Item;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

/**
 * Class handling Raw Pumpkin Pie.
 */
public class ItemRawPumpkinPie extends Item
{
    /**
     * Sets the name, creative tab, and registers the Raw Pumpkin Pie item.
     *
     * @param properties the properties.
     */
    public ItemRawPumpkinPie(final Properties properties)
    {
        super(properties.stacksTo(STACKSIZE));
    }
}
