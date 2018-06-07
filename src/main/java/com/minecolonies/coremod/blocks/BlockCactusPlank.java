package com.minecolonies.coremod.blocks;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

import java.util.Locale;

public class BlockCactusPlank extends AbstractBlockMinecolonies<BlockCactusPlank> {

    protected BlockCactusPlank() {
        super(Material.CACTUS, MapColor.GREEN);
        setRegistryName("blockcactusplant");
        setUnlocalizedName(Constants.MOD_ID.toLowerCase(Locale.ENGLISH) + "." + "blockcactusplant");
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        

    }

}
