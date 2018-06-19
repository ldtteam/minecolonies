package com.minecolonies.coremod.blocks.cactus;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.blocks.AbstractBlockSlab;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

import java.util.Locale;

/**
 * Implements the half cactus slab.
 */
public class BlockCactusSlabHalf extends AbstractBlockSlab<BlockCactusSlabHalf>
{
    /**
     * Unlocalized name for the slab.
     */
    private static final String NAME = "blockcactusslab_half";

    /**
     * Constructor for the half slab.
     */
    public BlockCactusSlabHalf()
    {
        super(Material.WOOD);
        setRegistryName(NAME);
        setUnlocalizedName(Constants.MOD_ID.toLowerCase(Locale.ENGLISH) + "." + NAME);
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setSoundType(SoundType.WOOD);
    }

    @Override
    public boolean isDouble()
    {
        return false;
    }
}
