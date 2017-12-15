package com.minecolonies.coremod.blocks;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import net.minecraft.block.state.IBlockState;

public class BlockShingle extends AbstractBlockMinecoloniesStairs<BlockShingle>
{
    /**
     * The hardness this block has.
     */
    private static final float BLOCK_HARDNESS = 0.0F;

    /**
     * This blocks name.
     */
    private static final String BLOCK_NAME = "blockshingle";

    /**
     * The resistance this block has.
     */
    private static final float RESISTANCE = 1F;

    protected BlockShingle(final IBlockState modelState)
    {
        super(modelState);
        init();
    }

    private void init()
    {
        setRegistryName(BLOCK_NAME);
        setUnlocalizedName(String.format("%s.%s", Constants.MOD_ID.toLowerCase(), BLOCK_NAME));
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setHardness(BLOCK_HARDNESS);
        setResistance(RESISTANCE);
        this.useNeighborBrightness = true;
    }
}
