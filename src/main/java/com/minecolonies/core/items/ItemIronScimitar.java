package com.minecolonies.core.items;

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
        super(Tiers.IRON, properties.attributes(SwordItem.createAttributes(Tiers.WOOD, 3, -2.4F)));
    }
}
