package com.minecolonies.coremod.blocks.cactus;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.blocks.AbstractBlockMinecoloniesStairs;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;

import java.util.Locale;

public class BlockCactusStair extends AbstractBlockMinecoloniesStairs<BlockCactusStair>
{

    public BlockCactusStair(final IBlockState modelState)
    {
        super(modelState);
        setRegistryName("blockcactusstair");
        setUnlocalizedName(Constants.MOD_ID.toLowerCase(Locale.ENGLISH) + "." + "blockcactusstair");
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setSoundType(SoundType.WOOD);
    }
}
