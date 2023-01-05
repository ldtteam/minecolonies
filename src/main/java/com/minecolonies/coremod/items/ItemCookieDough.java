package com.minecolonies.coremod.items;

import com.minecolonies.api.creativetab.ModCreativeTabs;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

import net.minecraft.world.item.Item.Properties;

/**
 * Class handling Cookie Dough.
 */
public class ItemCookieDough extends AbstractItemMinecolonies
{
    /**
     * Sets the name, creative tab, and registers the Cookie Dough item.
     *
     * @param properties the properties.
     */
    public ItemCookieDough(final Properties properties)
    {
        super("cookie_dough", properties.stacksTo(STACKSIZE));
    }
}
