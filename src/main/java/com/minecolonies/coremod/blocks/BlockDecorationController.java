package com.minecolonies.coremod.blocks;

import com.ldtteam.structurize.blocks.interfaces.IAnchorBlock;
import com.minecolonies.api.blocks.AbstractBlockMinecoloniesHorizontal;
import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.entity.ai.citizen.builder.IBuilderUndestroyable;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.tileentities.TileEntityDecorationController;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.BooleanProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.LivingEntityBase;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import static com.minecolonies.api.util.constant.Suppression.DEPRECATION;

/**
 * Creates a decoration placerholder block.
 */
public class BlockDecorationController extends AbstractBlockMinecoloniesHorizontal<BlockDecorationController> implements IBuilderUndestroyable, IAnchorBlock
{
    /**
     * The hardness this block has.
     */
    private static final float BLOCK_HARDNESS = 5F;

    /**
     * This blocks name.
     */
    private static final String BLOCK_NAME = "decorationcontroller";

    /**
     * The resistance this block has.
     */
    private static final float RESISTANCE = 1F;

    /**
     * If the block is mirrored.
     */
    public static BooleanProperty MIRROR = BooleanProperty.create("mirror");

    /**
     * The bounding boxes.
     */
    protected static final AxisAlignedBB AABB_NORTH = new AxisAlignedBB(0.4125D, 0.375D, 0.950D, 0.5875D, 0.625D, 1.0D);
    protected static final AxisAlignedBB AABB_SOUTH = new AxisAlignedBB(0.4125D, 0.375D, 0.0D, 0.5875D, 0.625D, 0.050D);
    protected static final AxisAlignedBB AABB_WEST = new AxisAlignedBB(0.950D, 0.375D, 0.4125D, 1.0D, 0.625D, 0.5875D);
    protected static final AxisAlignedBB AABB_EAST  = new AxisAlignedBB(0.0D, 0.375D, 0.4125D, 0.050D, 0.625D, 0.5875D);

    /**
     * Constructor for the placerholder.
     *
     * @param blockMaterialIn the material.
     */
    public BlockDecorationController(final Material blockMaterialIn)
    {
        super(blockMaterialIn);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, Direction.NORTH).withProperty(MIRROR, false));
        initBlock();
    }

    /**
     * Initialize the block.
     */
    private void initBlock()
    {
        setRegistryName(Constants.MOD_ID.toLowerCase() + ":" + BLOCK_NAME);
        setTranslationKey(String.format("%s.%s", Constants.MOD_ID.toLowerCase(), BLOCK_NAME));
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setHardness(BLOCK_HARDNESS);
        setResistance(RESISTANCE);
    }

    /**
     * @deprecated
     */
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(BlockState blockState, @NotNull IBlockAccess worldIn, @NotNull BlockPos pos)
    {
        return NULL_AABB;
    }

    /**
     * @deprecated
     */
    @NotNull
    @Override
    public AxisAlignedBB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos)
    {
        Direction Direction = state.getValue(FACING);
        switch (Direction)
        {
            case EAST:
                return AABB_EAST;
            case WEST:
                return AABB_WEST;
            case SOUTH:
                return AABB_SOUTH;
            case NORTH:
            default:
                return AABB_NORTH;
        }
    }

    @Override
    public boolean onBlockActivated(
      final World worldIn,
      final BlockPos pos,
      final BlockState state,
      final PlayerEntity playerIn,
      final Hand hand,
      final Direction facing,
      final float hitX,
      final float hitY,
      final float hitZ)
    {
        if (worldIn.isRemote)
        {
            final TileEntity tileEntity = worldIn.getTileEntity(pos);
            if (tileEntity instanceof TileEntityDecorationController)
            {
                MineColonies.proxy.openDecorationControllerWindow(pos);
            }
        }
        return true;
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

    @NotNull
    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, FACING, MIRROR);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(@NotNull final World world, @NotNull final BlockState state)
    {
        return new TileEntityDecorationController();
    }

    @Override
    public int getMetaFromState(final BlockState state)
    {
        return state.getValue(FACING).getHorizontalIndex() + (state.getValue(MIRROR) ? 4 : 0);
    }

    @NotNull
    @Override
    @SuppressWarnings(DEPRECATION)
    public BlockState getStateFromMeta(final int meta)
    {
        int theMeta = meta;
        boolean mirrored = false;
        if (meta > 3)
        {
            mirrored = true;
            theMeta = meta - 4;
        }

        return getDefaultState().withProperty(FACING, Direction.byHorizontalIndex(theMeta)).withProperty(MIRROR, mirrored);
    }

    @NotNull
    @Override
    public BlockState getStateForPlacement(World worldIn, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntityBase placer)
    {
        if (facing.getAxis().isHorizontal())
        {
            return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(FACING, facing);
        }
        return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);
    }

    /**
     * @deprecated
     */
    @NotNull
    @Override
    public BlockState withRotation(@NotNull BlockState state, Rotation rot)
    {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    /**
     * @deprecated
     */
    @NotNull
    @Override
    public BlockState withMirror(@NotNull BlockState state, Mirror mirrorIn)
    {
        return state.withProperty(MIRROR, mirrorIn != Mirror.NONE);
    }

    /**
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
    @SuppressWarnings(DEPRECATION)
    @Override
    @Deprecated
    public boolean isFullBlock(final BlockState state)
    {
        return false;
    }

    /**
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
    @SuppressWarnings(DEPRECATION)
    @Override
    @Deprecated
    public boolean isFullCube(final BlockState state)
    {
        return false;
    }

    /**
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
    @SuppressWarnings(DEPRECATION)
    @Override
    @Deprecated
    public boolean isOpaqueCube(final BlockState state)
    {
        return false;
    }

    @NotNull
    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer()
    {
        return BlockRenderLayer.SOLID;
    }

    @Override
    public boolean doesSideBlockRendering(final BlockState state, final IBlockAccess world, final BlockPos pos, final Direction face)
    {
        return false;
    }
}
