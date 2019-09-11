package com.minecolonies.coremod.blocks.schematic;

import com.minecolonies.api.blocks.AbstractBlockMinecolonies;
import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.Locale;

import static com.minecolonies.api.util.constant.Suppression.DEPRECATION;

/**
 * This block is used as a substitution block for the Builder.
 * Every solid block can be substituted by this block in schematics.
 * This helps make schematics independent from location and ground.
 */
public class BlockWaypoint extends AbstractBlockMinecolonies<BlockWaypoint>
{

    /**
     * The hardness this block has.
     */
    private static final float BLOCK_HARDNESS = 0.0F;

    /**
     * This blocks name.
     */
    private static final String BLOCK_NAME = "blockWayPoint";

    /**
     * The resistance this block has.
     */
    private static final float RESISTANCE = 1F;

    /**
     * Constructor for the Substitution block.
     * sets the creative tab, as well as the resistance and the hardness.
     */
    public BlockWaypoint()
    {
        super(Material.WOOD);
        initBlock();
    }

    /**
     * initialize the block
     * sets the creative tab, as well as the resistance and the hardness.
     */
    private void initBlock()
    {
        setRegistryName(Constants.MOD_ID.toLowerCase() + ":" + BLOCK_NAME);
        setTranslationKey(String.format("%s.%s", Constants.MOD_ID.toLowerCase(Locale.ENGLISH), BLOCK_NAME));
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setHardness(BLOCK_HARDNESS);
        setResistance(RESISTANCE);
    }

    /**
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
    @SuppressWarnings(DEPRECATION)
    @Override
    @Deprecated
    public boolean isFullBlock(final IBlockState state)
    {
        return false;
    }

    /**
     * Used to determine ambient occlusion and culling when rebuilding chunks
     * for render.
     *
     * @return true
     *
     * @deprecated
     */
    //todo: remove once we no longer need to support this
    @SuppressWarnings(DEPRECATION)
    @Override
    @Deprecated
    public boolean isOpaqueCube(final IBlockState state)
    {
        return false;
    }

    @Override
    public boolean doesSideBlockRendering(final IBlockState state, final IBlockAccess world, final BlockPos pos, final EnumFacing face)
    {
        return false;
    }

    @Override
    public boolean isPassable(final IBlockAccess worldIn, final BlockPos pos)
    {
        return true;
    }
}
