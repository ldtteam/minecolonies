package com.minecolonies.items;

import com.minecolonies.creativetab.ModCreativeTabs;
import com.minecolonies.lib.Constants;
import com.minecolonies.lib.IColony;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;

public abstract class ItemMinecolonies extends Item implements IColony
{
    public ItemMinecolonies()
    {
        setUnlocalizedName(getName());
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        GameRegistry.registerItem(this, getName());
    }

    @Override
    public void registerIcons(IIconRegister par1IconRegister)
    {
        this.itemIcon = par1IconRegister.registerIcon(Constants.MOD_ID + ":" + getName());
    }
}
