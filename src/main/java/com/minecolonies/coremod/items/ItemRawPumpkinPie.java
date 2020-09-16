package com.minecolonies.coremod.items;

import com.minecolonies.api.creativetab.ModCreativeTabs;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

/**
 * Class handling the Santa hat.
 */
public class ItemRawPumpkinPie extends AbstractItemMinecolonies
{
    /**
     * Sets the name, creative tab, and registers the Bread Dough item.
     *
     * @param properties the properties.
     */
    public ItemRawPumpkinPie(final Properties properties)
    {
        super("raw_pumpkin_pie", properties.maxStackSize(STACKSIZE).group(ModCreativeTabs.MINECOLONIES));
    }
}
