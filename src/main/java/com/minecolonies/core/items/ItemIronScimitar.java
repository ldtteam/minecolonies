package com.minecolonies.core.items;

import com.minecolonies.api.creativetab.ModCreativeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.SwordItem;

/**
 * Class handling the Scimitar item.
 */
public class ItemIronScimitar extends SwordItem
{
    /**
     * Constructor method for the Scimitar Item
     *
     * @param properties the properties.
     */
    public ItemIronScimitar(final Item.Properties properties)
    {
        super(Tiers.IRON, 3, -2.4f, properties.tab(ModCreativeTabs.MINECOLONIES));
    }
}
