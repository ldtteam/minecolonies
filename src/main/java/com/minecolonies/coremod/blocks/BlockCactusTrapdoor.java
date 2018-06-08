package com.minecolonies.coremod.blocks;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

import java.util.Locale;

public class BlockCactusTrapdoor extends AbstractBlockTrapdoor<BlockCactusTrapdoor> {

    BlockCactusTrapdoor() {
        super(Material.WOOD);
        setRegistryName("blockcactustrapdoor");
        setUnlocalizedName(Constants.MOD_ID.toLowerCase(Locale.ENGLISH) + "." + "blockcactustrapdoordoor");
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setSoundType(SoundType.WOOD);
        setHarvestLevel("axe", 0);
    }
}
