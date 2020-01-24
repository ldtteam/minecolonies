package com.minecolonies.coremod.blocks;

import com.minecolonies.api.blocks.AbstractBlockBarrel;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.blocks.types.BarrelType;
import com.minecolonies.coremod.tileentities.TileEntityBarrel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.*;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class BlockBarrel extends AbstractBlockBarrel<BlockBarrel>
{
    /**
     * The hardness this block has.
     */
    private static final float                      BLOCK_HARDNESS = 5F;
    /**
     * This blocks name.
     */
    private static final String                     BLOCK_NAME     = "barrel_block";
    /**
     * The resistance this block has.
     */
    private static final float                      RESISTANCE     = 1F;

    public BlockBarrel()
    {
        super(Properties.create(Material.WOOD).hardnessAndResistance(BLOCK_HARDNESS, RESISTANCE));
        this.setDefaultState(this.getDefaultState().with(AbstractBlockBarrel.FACING, Direction.NORTH).with(VARIANT, BarrelType.ZERO));
        setRegistryName(BLOCK_NAME);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(AbstractBlockBarrel.FACING, VARIANT);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new TileEntityBarrel();
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

    @Override
    public ActionResultType onBlockActivated(
      final BlockState state,
      final World worldIn,
      final BlockPos pos,
      final PlayerEntity player,
      final Hand hand,
      final BlockRayTraceResult ray)
    {
        final ItemStack itemstack = player.inventory.getCurrentItem();
        final TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEntityBarrel && !worldIn.isRemote)
        {
            ((TileEntityBarrel) te).useBarrel(worldIn, player, itemstack, state, pos);
            ((TileEntityBarrel) te).updateBlock(worldIn);
        }

        return ActionResultType.SUCCESS;
    }

    @NotNull
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos, final ISelectionContext context)
    {
        return VoxelShapes.create(0,0,0,1,1.5,1);
    }

    /**
     * Convert the BlockState into the correct metadata value.
     *
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
    @NotNull
    @Override
    @Deprecated
    public BlockState rotate(@NotNull final BlockState state, final Rotation rot)
    {
        return state.with(AbstractBlockBarrel.FACING, rot.rotate(state.get(AbstractBlockBarrel.FACING)));
    }

    /**
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
    @NotNull
    @Override
    @Deprecated
    public BlockState mirror(@NotNull final BlockState state, final Mirror mirrorIn)
    {
        return state.rotate(mirrorIn.toRotation(state.get(AbstractBlockBarrel.FACING)));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        return super.getStateForPlacement(context).with(AbstractBlockBarrel.FACING, context.getPlacementHorizontalFacing());
    }

    @Override
    public BlockState updatePostPlacement(@NotNull final BlockState stateIn, final Direction facing, final BlockState state, final IWorld worldIn, final BlockPos currentPos, final BlockPos pos)
    {
        final TileEntity entity = worldIn.getTileEntity(pos);
        if (!(entity instanceof TileEntityBarrel))
        {
            return super.updatePostPlacement(stateIn, facing, state, worldIn, currentPos, pos);
        }

        return AbstractBlockBarrel.changeStateOverFullness((TileEntityBarrel) entity, state);
    }

    @Override
    public boolean isValidPosition(final BlockState state, final IWorldReader worldIn, final BlockPos pos)
    {
        return !worldIn.isAirBlock(pos.down())
                 && worldIn.getBlockState(pos.down()).getBlock() != ModBlocks.blockBarrel;
    }
}
