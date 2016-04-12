package com.minecolonies.items;

import com.minecolonies.creativetab.ModCreativeTabs;
import com.minecolonies.lib.Constants;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;

public abstract class ItemMinecolonies extends Item
{
    public ItemMinecolonies()
    {
        setUnlocalizedName(getName());
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        GameRegistry.registerItem(this, getName());
    }

    /**
     * Returns the name of the item
     *
     * @return      Name of the item
     */
    public abstract String getName();

    @Override
    public void registerIcons(IIconRegister par1IconRegister)
    {
        this.itemIcon = par1IconRegister.registerIcon(Constants.MOD_ID + ":" + getName());
    }
}
