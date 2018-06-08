package com.minecolonies.coremod.blocks;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

import java.util.Locale;

public class BlockCactusPlank extends AbstractBlockMinecolonies<BlockCactusPlank> {

    protected BlockCactusPlank() {
        super(Material.CACTUS, MapColor.GREEN);
        setRegistryName("blockcactusplank");
        setUnlocalizedName(Constants.MOD_ID.toLowerCase(Locale.ENGLISH) + "." + "blockcactusplant");
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setSoundType(SoundType.WOOD);
        setHarvestLevel("axe", 0);

    }

}
