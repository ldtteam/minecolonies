package com.minecolonies.items;

import com.minecolonies.creativetab.ModCreativeTabs;
import com.minecolonies.lib.Constants;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Handles simple things that all items need.
 */
public abstract class AbstractItemMinecolonies extends Item
{
    /**
     * Sets the name, creative tab, and registers the item.
     */
    public AbstractItemMinecolonies()
    {
        super.setUnlocalizedName(Constants.MOD_ID.toLowerCase() + "." + getName());
        super.setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setRegistryName(getName());
        GameRegistry.registerItem(this);
    }

    /**
     * Returns the name of the item
     *
     * @return      Name of the item
     */
    public abstract String getName();
}
