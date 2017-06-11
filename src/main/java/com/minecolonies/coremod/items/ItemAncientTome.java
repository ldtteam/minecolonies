package com.minecolonies.coremod.items;

import com.minecolonies.coremod.creativetab.ModCreativeTabs;

/**
 * Created by Asher on 11/6/17.
 */
public class ItemAncientTome extends AbstractItemMinecolonies
{

    /**
     * Sets the name, creative tab, and registers the item.
     */
    public ItemAncientTome() {
        super("ancientTome");
        super.setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setMaxStackSize(1);
    }
}
