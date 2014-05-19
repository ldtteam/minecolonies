package com.minecolonies.items;

import com.minecolonies.creativetab.ModCreativeTabs;
import com.minecolonies.lib.Constants;
import com.minecolonies.util.IColony;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * Created by Colton on 5/11/2014.
 */
public class ItemBuildTool extends Item implements IColony
{
    private String name = "scepterGold";

    public ItemBuildTool()
    {
        setUnlocalizedName(getName());
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setMaxStackSize(1);
        GameRegistry.registerItem(this, getName());
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void registerIcons(IIconRegister par1IconRegister)
    {
        this.itemIcon = par1IconRegister.registerIcon(Constants.MODID + ":" + getName());
    }

    //TODO onItemUse?

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        //TODO
        return stack;
    }
}
