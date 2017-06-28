package com.minecolonies.coremod.items;

import com.minecolonies.coremod.creativetab.ModCreativeTabs;

/**
 * Class describing the Ancient Tome item.
 */
public class ItemAncientTome extends AbstractItemMinecolonies
{
    /**
     * Sets the name, creative tab, and registers the item.
     */
    public ItemAncientTome()
    {
        super("ancienttome");

        super.setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setMaxStackSize(1);
    }
}
