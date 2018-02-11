package com.minecolonies.coremod.blocks;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

import static com.minecolonies.api.util.constant.Suppression.DEPRECATION;

/**
 * Decorative block
 */
public class BlockTimberFrame extends AbstractBlockMinecoloniesPillar<BlockTimberFrame>
{
    /**
     * This blocks name.
     */
    public static final String                         BLOCK_NAME     = "blockTimberFrame";
    /**
     * The hardness this block has.
     */
    private static final float                         BLOCK_HARDNESS = 3F;
    /**
     * The resistance this block has.
     */
    private static final float                         RESISTANCE     = 1F;
    /**
     * Constructor for the TimberFrame
     */
    BlockTimberFrame(final String name, final TimberFrameType type)
    {
        super(Material.WOOD);
        initBlock(name);
    }

    /**
     * initialize the block
     * sets the creative tab, as well as the resistance and the hardness.
     */
    private void initBlock(final String name)
    {
        setRegistryName(name);
        setUnlocalizedName(String.format("%s.%s", Constants.MOD_ID.toLowerCase(Locale.US), name));
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setHardness(BLOCK_HARDNESS);
        setResistance(RESISTANCE);
    }

    @Override
    public IBlockState getActualState(final IBlockState state, final IBlockAccess world, final BlockPos pos)
    {
        final IBlockState upState = world.getBlockState(pos.up());
        final IBlockState downState = world.getBlockState(pos.down());
        final boolean up = isConnectable(upState);
        final boolean down = isConnectable(downState);

        if(!isConnectable(state) || state.getBlock().getUnlocalizedName().contains(TimberFrameType.HORIZONTALNOCAP.getName()) || (!up && !down))
        {
            return super.getActualState(state, world, pos);
        }
        else
        {
            String name = getUnlocalizedName();
            final int underline = name.indexOf('_', name.indexOf('_') + 1);
            name = name.substring(0, underline + 1);
            if(up && down)
            {
                return Block.getBlockFromName(name + TimberFrameType.SIDEFRAMED.getName()).getDefaultState();
            }
            else if(down)
            {
                return Block.getBlockFromName(name + TimberFrameType.GATEFRAMED.getName()).getDefaultState();
            }
            else
            {
                return Block.getBlockFromName(name + TimberFrameType.DOWNGATED.getName()).getDefaultState();
            }
        }
    }

    private static boolean isConnectable(final IBlockState state)
    {
        return state.getBlock() instanceof BlockTimberFrame && (state.getBlock().getUnlocalizedName().contains(TimberFrameType.SIDEFRAMED.getName())
                || state.getBlock().getUnlocalizedName().contains(TimberFrameType.GATEFRAMED.getName())
                || state.getBlock().getUnlocalizedName().contains(TimberFrameType.DOWNGATED.getName())
                || state.getBlock().getUnlocalizedName().contains(TimberFrameType.HORIZONTALNOCAP.getName()));
    }

    @SuppressWarnings(DEPRECATION)
    @Override
    public boolean isOpaqueCube(@NotNull final IBlockState state)
    {
        return true;
    }
}
