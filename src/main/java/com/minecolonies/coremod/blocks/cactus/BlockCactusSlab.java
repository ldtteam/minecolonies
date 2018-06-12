package com.minecolonies.coremod.blocks.cactus;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.blocks.AbstractBlockSlab;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;

import java.util.Locale;

public class BlockCactusSlab extends AbstractBlockSlab<BlockCactusSlab> {

    public BlockCactusSlab() {
        super(Material.WOOD);
        setRegistryName("blockcactusslab");
        setUnlocalizedName(Constants.MOD_ID.toLowerCase(Locale.ENGLISH) + "." + "blockcactusslab");
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setSoundType(SoundType.WOOD);
    }

    @Override
    public String getUnlocalizedName(final int i)
    {
        return null;
    }

    @Override
    public boolean isDouble()
    {
        return false;
    }

    @Override
    public Comparable<?> getTypeForItem(final ItemStack itemStack)
    {
        return null;
    }
}
