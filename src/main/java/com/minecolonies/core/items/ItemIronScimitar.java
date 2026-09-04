package com.minecolonies.core.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

/**
 * Class handling the Scimitar item.
 */
public class ItemIronScimitar extends Item
{
    /**
     * Constructor method for the Scimitar Item
     *
     * @param properties the properties.
     */
    public ItemIronScimitar(final Item.Properties properties)
    {
        super(properties.sword(ToolMaterial.IRON, 3.0F, -2.4F));
    }
}
