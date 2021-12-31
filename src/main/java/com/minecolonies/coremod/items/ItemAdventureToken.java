package com.minecolonies.coremod.items;

import com.minecolonies.api.creativetab.ModCreativeTabs;

public class ItemAdventureToken extends AbstractItemMinecolonies
{
    /**
     * This item is purely for matching, and carrying data in Tags
     * @param properties
     */
    public ItemAdventureToken(Properties properties)
    {
        super("adventure_token", properties.tab(ModCreativeTabs.MINECOLONIES));
    }
}