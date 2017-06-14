package com.minecolonies.coremod.items;

import com.minecolonies.coremod.creativetab.ModCreativeTabs;

/**
 * Class handling the AncientTome item.
 */
public class ItemAncientTome extends AbstractItemMinecolonies
{
    /* default */ private static final int MAX_STACK_SIZE = 64;

    /**
     * Sets the name, creative tab, and registers the item.
     */
    public ItemAncientTome()
    {
        super("ancientTome");
        super.setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setMaxStackSize(MAX_STACK_SIZE);
    }
}
