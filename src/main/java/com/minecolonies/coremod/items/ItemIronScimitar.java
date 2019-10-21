package com.minecolonies.coremod.items;

import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.item.ItemSword;

import static com.minecolonies.api.util.constant.Constants.SCIMITAR_NAME;

/**
 * Class handling the Scimitar item.
 */
public class ItemIronScimitar extends ItemSword
{
    /**
     * Constructor method for the Scimitar Item
     */
    public ItemIronScimitar()
    {
        super(ToolMaterial.IRON);
        super.setTranslationKey(Constants.MOD_ID.toLowerCase() + "." + SCIMITAR_NAME);
        setRegistryName(Constants.MOD_ID.toLowerCase() + ":" + SCIMITAR_NAME);
        super.setCreativeTab(ModCreativeTabs.MINECOLONIES);
    }

    /**
     * returns the items name
     *
     * @return Returns the items name in the form of a string
     */
    public final String getName()
    {
        return SCIMITAR_NAME;
    }
}
