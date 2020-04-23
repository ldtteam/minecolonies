package com.minecolonies.coremod.items;

import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.item.Item;
import net.minecraft.item.ItemTier;
import net.minecraft.item.SwordItem;

import static com.minecolonies.api.util.constant.Constants.SCIMITAR_NAME;

/**
 * Class handling the Scimitar item.
 */
public class ItemIronScimitar extends SwordItem
{
    /**
     * Constructor method for the Scimitar Item
     * @param properties the properties.
     */
    public ItemIronScimitar(final Item.Properties properties)
    {
        super(ItemTier.IRON, 3, -2.4f, properties.group(ModCreativeTabs.MINECOLONIES));
        setRegistryName(Constants.MOD_ID.toLowerCase() + ":" + SCIMITAR_NAME);
    }
}
