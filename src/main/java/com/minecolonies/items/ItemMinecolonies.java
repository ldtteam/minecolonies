package com.minecolonies.items;

import com.minecolonies.creativetab.ModCreativeTabs;
import com.minecolonies.lib.Constants;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;

public abstract class ItemMinecolonies extends Item
{
    public ItemMinecolonies()
    {
        setUnlocalizedName(Constants.MOD_ID.toLowerCase() + "." + getName());
        setRegistryName(getName());
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        GameRegistry.registerItem(this);
    }

    /**
     * Returns the name of the item
     *
     * @return      Name of the item
     */
    public abstract String getName();
}
