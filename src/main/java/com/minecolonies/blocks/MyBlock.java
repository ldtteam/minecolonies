package com.minecolonies.blocks;

import com.minecolonies.lib.Constants;
import com.minecolonies.util.IColony;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;

public class MyBlock extends net.minecraft.block.Block implements IColony
{
    private final String name = "myBlock";

    public MyBlock()
    {
        super(Material.rock);
        setBlockName(name);
        setCreativeTab(CreativeTabs.tabBlock);
        GameRegistry.registerBlock(this, name);
    }

    @Override
    public String getName()
    {
        return name;
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iIconRegister)
    {
        this.blockIcon = iIconRegister.registerIcon(Constants.MODID + ":" + name);
    }
}
