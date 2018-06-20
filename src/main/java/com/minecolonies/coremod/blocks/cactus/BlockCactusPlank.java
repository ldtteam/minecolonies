package com.minecolonies.coremod.blocks.cactus;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.blocks.AbstractBlockMinecolonies;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

import java.util.Locale;

public class BlockCactusPlank extends AbstractBlockMinecolonies<BlockCactusPlank>
{


    public BlockCactusPlank()
    {
        super(Material.WOOD, MapColor.GREEN);
        setRegistryName("blockcactusplank");
        setUnlocalizedName(Constants.MOD_ID.toLowerCase(Locale.ENGLISH) + "." + "blockcactusplank");
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setSoundType(SoundType.WOOD);

    }
}
