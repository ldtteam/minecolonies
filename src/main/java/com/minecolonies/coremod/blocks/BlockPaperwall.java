package com.minecolonies.coremod.blocks;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import net.minecraft.block.Block;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public class BlockPaperwall extends Block
{
    public static final PropertyBool NORTH = PropertyBool.create("north");
    public static final PropertyBool EAST = PropertyBool.create("east");
    public static final PropertyBool SOUTH = PropertyBool.create("south");
    public static final PropertyBool WEST = PropertyBool.create("west");
    private static final AxisAlignedBB[] AABB_BY_INDEX = new AxisAlignedBB[]{
            new AxisAlignedBB(0.4375D, 0.0D, 0.4375D, 0.5625D, 1.0D, 0.5625D),
            new AxisAlignedBB(0.4375D, 0.0D, 0.4375D, 0.5625D, 1.0D, 1.0D),
            new AxisAlignedBB(0.0D, 0.0D, 0.4375D, 0.5625D, 1.0D, 0.5625D),
            new AxisAlignedBB(0.0D, 0.0D, 0.4375D, 0.5625D, 1.0D, 1.0D),
            new AxisAlignedBB(0.4375D, 0.0D, 0.0D, 0.5625D, 1.0D, 0.5625D),
            new AxisAlignedBB(0.4375D, 0.0D, 0.0D, 0.5625D, 1.0D, 1.0D),
            new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.5625D, 1.0D, 0.5625D),
            new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.5625D, 1.0D, 1.0D),
            new AxisAlignedBB(0.4375D, 0.0D, 0.4375D, 1.0D, 1.0D, 0.5625D),
            new AxisAlignedBB(0.4375D, 0.0D, 0.4375D, 1.0D, 1.0D, 1.0D),
            new AxisAlignedBB(0.0D, 0.0D, 0.4375D, 1.0D, 1.0D, 0.5625D),
            new AxisAlignedBB(0.0D, 0.0D, 0.4375D, 1.0D, 1.0D, 1.0D),
            new AxisAlignedBB(0.4375D, 0.0D, 0.0D, 1.0D, 1.0D, 0.5625D),
            new AxisAlignedBB(0.4375D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D),
            new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.5625D),
            new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D)};


    /**
     * The hardness this block has.
     */
    private static final float BLOCK_HARDNESS = 3F;

    /**
     * This blocks name.
     */
    private static final String BLOCK_NAME = "blockPaperwall";

    /**
     * The resistance this block has.
     */
    private static final float RESISTANCE = 1F;

    BlockPaperwall()
    {
        super(Material.WOOD);
        initBlock();
    }

    private void initBlock()
    {
        setRegistryName(BLOCK_NAME);
        setUnlocalizedName(String.format("%s.%s", Constants.MOD_ID.toLowerCase(), BLOCK_NAME));
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        GameRegistry.register(this);
        GameRegistry.register((new ItemBlock(this)).setRegistryName(this.getRegistryName()));
        setHardness(BLOCK_HARDNESS);
        setResistance(RESISTANCE);
    }

    public void addCollisionBoxToList(@NotNull final IBlockState iBlockState,
                                      @NotNull final World worldIn,
                                      @NotNull final BlockPos pos,
                                      @NotNull final AxisAlignedBB entityBox,
                                      @NotNull final List<AxisAlignedBB> collidingBoxes,
                                      @Nullable final Entity entityIn)
    {
        final IBlockState tempState = this.getActualState(iBlockState, worldIn, pos);
        addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_BY_INDEX[0]);

        if (tempState.getValue(NORTH))
        {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_BY_INDEX[getBoundingBoxIndex(EnumFacing.NORTH)]);
        }

        if (tempState.getValue(SOUTH))
        {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_BY_INDEX[getBoundingBoxIndex(EnumFacing.SOUTH)]);
        }

        if (tempState.getValue(EAST))
        {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_BY_INDEX[getBoundingBoxIndex(EnumFacing.EAST)]);
        }

        if (tempState.getValue(WEST))
        {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_BY_INDEX[getBoundingBoxIndex(EnumFacing.WEST)]);
        }
    }


    private static int getBoundingBoxIndex(final EnumFacing side)
    {
        return 1 << side.getHorizontalIndex();
    }

    @NotNull
    public AxisAlignedBB getBoundingBox(@NotNull final IBlockState blockState,
                                        @NotNull final IBlockAccess source,
                                        @NotNull final BlockPos pos)
    {
       final IBlockState tempState = this.getActualState(blockState, source, pos);
        {
            return AABB_BY_INDEX[getBoundingBoxIndex(tempState)];
        }
    }

    private static int getBoundingBoxIndex(final IBlockState state)
    {
        int i = 0;

        if (state.getValue(NORTH))
        {
            i |= getBoundingBoxIndex(EnumFacing.NORTH);
        }

        if (state.getValue(EAST))
        {
            i |= getBoundingBoxIndex(EnumFacing.EAST);
        }

        if (state.getValue(SOUTH))
        {
            i |= getBoundingBoxIndex(EnumFacing.SOUTH);
        }

        if (state.getValue(WEST))
        {
            i |= getBoundingBoxIndex(EnumFacing.WEST);
        }

        return i;
    }

    /**
     * Get the actual Block state of this Block at the given position. This applies properties not visible in the
     * metadata, such as fence connections.
     */
    @NotNull
    public IBlockState getActualState(@NotNull final IBlockState tempState,
                                      @NotNull final IBlockAccess worldIn,
                                      @NotNull final BlockPos pos)
    {
        return tempState.withProperty(NORTH, canPaneConnectTo(worldIn, pos, EnumFacing.NORTH))
                .withProperty(SOUTH, canPaneConnectTo(worldIn, pos, EnumFacing.SOUTH))
                .withProperty(WEST, canPaneConnectTo(worldIn, pos, EnumFacing.WEST))
                .withProperty(EAST, canPaneConnectTo(worldIn, pos, EnumFacing.EAST));
    }

    /**
    * Used to determine ambient occlusion and culling when rebuilding chunks for render
    */

    public boolean isOpaqueCube(@NotNull final IBlockState state)
    {
        return false;
    }

    public boolean isFullCube(@NotNull final IBlockState state)
    {
        return false;
    }

    private boolean canPaneConnectToBlock(final Block blockIn)
    {
        return blockIn.getDefaultState().isFullCube()
                || blockIn == this || blockIn == Blocks.GLASS || blockIn == Blocks.STAINED_GLASS ||
                blockIn == Blocks.STAINED_GLASS_PANE || blockIn instanceof BlockPaperwall;
    }

    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(@NotNull final IBlockState blockState,
                                        @NotNull final IBlockAccess blockAccess,
                                        @NotNull final BlockPos pos,
                                        @NotNull final EnumFacing side)
    {
        return blockAccess.getBlockState
                (pos.offset(side)).getBlock() != this && super.shouldSideBeRendered(blockState, blockAccess, pos, side);
    }

    @NotNull
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.TRANSLUCENT;
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(@NotNull final IBlockState state)
    {
        return 0;
    }

    /**
     * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     */
    @NotNull
    public IBlockState withRotation(@NotNull final IBlockState tempState,
                                    @NotNull final Rotation rot)
    {
        switch (rot)
        {
            case CLOCKWISE_180:
                return tempState.withProperty
                        (NORTH, tempState.getValue(SOUTH)).withProperty
                        (EAST, tempState.getValue(WEST)).withProperty
                        (SOUTH, tempState.getValue(NORTH)).withProperty
                        (WEST, tempState.getValue(EAST));
            case COUNTERCLOCKWISE_90:
                return tempState.withProperty
                        (NORTH, tempState.getValue(EAST)).withProperty
                        (EAST, tempState.getValue(SOUTH)).withProperty
                        (SOUTH, tempState.getValue(WEST)).withProperty
                        (WEST, tempState.getValue(NORTH));
            case CLOCKWISE_90:
                return tempState.withProperty
                        (NORTH, tempState.getValue(WEST)).withProperty
                        (EAST, tempState.getValue(NORTH)).withProperty
                        (SOUTH, tempState.getValue(EAST)).withProperty
                        (WEST, tempState.getValue(SOUTH));
            default:
                return tempState;
        }
    }

    /**
     * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     */
    @NotNull
    public IBlockState withMirror(@NotNull final IBlockState tempState, final @NotNull Mirror mirrorIn)
    {
        switch (mirrorIn)
        {
            case LEFT_RIGHT:
                return tempState.withProperty(NORTH, tempState.getValue(SOUTH)).withProperty(SOUTH, tempState.getValue(NORTH));
            case FRONT_BACK:
                return tempState.withProperty(EAST, tempState.getValue(WEST)).withProperty(WEST, tempState.getValue(EAST));
            default:
                return super.withMirror(tempState, mirrorIn);
        }
    }

    @NotNull
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, NORTH, EAST, WEST, SOUTH);
    }

    private boolean canPaneConnectTo(final IBlockAccess world, final BlockPos pos, final EnumFacing dir)
    {
        final BlockPos off = pos.offset(dir);
        final IBlockState state = world.getBlockState(off);
        return canPaneConnectToBlock(state.getBlock()) || state.isSideSolid(world, off, dir.getOpposite());
    }
}
