package com.minecolonies.coremod.items;

import com.minecolonies.coremod.creativetab.ModCreativeTabs;

/**
 * Class handling the AncientTome item.
 */
public class ItemAncientTome extends AbstractItemMinecolonies
{

    /**
     * Sets the name, creative tab, and registers the item.
     */
    public ItemAncientTome()
    {
        super("ancientTome");
        super.setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setMaxStackSize(64);
    }
}
