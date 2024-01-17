package com.minecolonies.core.items;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

/**
 * Class handling Raw Pumpkin Pie.
 */
public class ItemRawPumpkinPie extends AbstractItemMinecolonies
{
    /**
     * Sets the name, creative tab, and registers the Raw Pumpkin Pie item.
     *
     * @param properties the properties.
     */
    public ItemRawPumpkinPie(final Properties properties)
    {
        super("raw_pumpkin_pie", properties.stacksTo(STACKSIZE));
    }
}
