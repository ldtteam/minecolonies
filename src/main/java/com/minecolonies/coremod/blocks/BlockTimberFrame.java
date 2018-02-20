package com.minecolonies.coremod.blocks;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
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
    BlockTimberFrame(final String name)
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
    
    /**
     * Calc the default state depending on the neighboring blocks.
     * @deprecated remove this when not needed anymore
     * @param state
     * @param world
     * @param pos
     * @return
     */
    @Deprecated
    @Override
    public IBlockState getActualState(final IBlockState state, final IBlockAccess world, final BlockPos pos)
    {
        final IBlockState upState = world.getBlockState(pos.up());
        final IBlockState downState = world.getBlockState(pos.down());

        final IBlockState leftState = world.getBlockState(pos.east());
        final IBlockState rightState = world.getBlockState(pos.west());

        final IBlockState straightState = world.getBlockState(pos.south());
        final IBlockState backState = world.getBlockState(pos.north());

        final boolean up = isConnectable(upState);
        final boolean down = isConnectable(downState);

        final boolean left = isConnectable(leftState);
        final boolean right = isConnectable(rightState);

        final boolean straight = isConnectable(straightState);
        final boolean back = isConnectable(backState);

        if(!isConnectable(state) || state.getBlock().getUnlocalizedName().contains(TimberFrameType.HORIZONTALNOCAP.getName())
                || (state.getValue(AXIS) == EnumFacing.Axis.Y && !up && !down)
                || (state.getValue(AXIS) == EnumFacing.Axis.X && !left && !right)
                || (state.getValue(AXIS) == EnumFacing.Axis.Z && !straight && !back))
        {
            return state;
        }

        String name = getRegistryName().toString();
        final int underline = name.indexOf('_', name.indexOf('_') + 1);
        name = name.substring(0, underline + 1);

        if(state.getValue(AXIS) == EnumFacing.Axis.Y)
        {
            final IBlockState returnState;
            if(up && down)
            {
                returnState = Block.getBlockFromName(name + TimberFrameType.SIDEFRAMED.getName()).getDefaultState();
            }
            else if(down)
            {
                returnState = Block.getBlockFromName(name + TimberFrameType.GATEFRAMED.getName()).getDefaultState();
            }
            else
            {
                returnState = Block.getBlockFromName(name + TimberFrameType.DOWNGATED.getName()).getDefaultState();
            }
            return returnState.withProperty(AXIS, EnumFacing.Axis.Y);
        }
        else if(state.getValue(AXIS) == EnumFacing.Axis.X)
        {
            final IBlockState returnState;
            if(left && right)
            {
                returnState = Block.getBlockFromName(name + TimberFrameType.SIDEFRAMED.getName()).getDefaultState();
            }
            else if(right)
            {
                returnState = Block.getBlockFromName(name + TimberFrameType.GATEFRAMED.getName()).getDefaultState();
            }
            else
            {
                returnState = Block.getBlockFromName(name + TimberFrameType.DOWNGATED.getName()).getDefaultState();
            }
            return returnState.withProperty(AXIS, EnumFacing.Axis.X);
        }
        else if(state.getValue(AXIS) == EnumFacing.Axis.Z)
        {
            final IBlockState returnState;
            if(straight && back)
            {
                returnState = Block.getBlockFromName(name + TimberFrameType.SIDEFRAMED.getName()).getDefaultState();
            }
            else if(straight)
            {
                returnState = Block.getBlockFromName(name + TimberFrameType.GATEFRAMED.getName()).getDefaultState();
            }
            else
            {
                returnState = Block.getBlockFromName(name + TimberFrameType.DOWNGATED.getName()).getDefaultState();
            }
            return returnState.withProperty(AXIS, EnumFacing.Axis.Z);
        }
        return state;
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
