package com.minecolonies.coremod.blocks;

import com.minecolonies.api.blocks.AbstractBlockBarrel;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.blocks.types.BarrelType;
import com.minecolonies.coremod.tileentities.TileEntityBarrel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class BlockBarrel extends AbstractBlockBarrel<BlockBarrel>
{
    /**
     * The hardness this block has.
     */
    private static final float  BLOCK_HARDNESS = 5F;
    /**
     * This blocks name.
     */
    private static final String BLOCK_NAME     = "barrel_block";
    /**
     * The resistance this block has.
     */
    private static final float  RESISTANCE     = 1F;

    public BlockBarrel()
    {
        super(Properties.of(Material.WOOD).strength(BLOCK_HARDNESS, RESISTANCE));
        this.registerDefaultState(this.defaultBlockState().setValue(AbstractBlockBarrel.FACING, Direction.NORTH).setValue(VARIANT, BarrelType.ZERO));
        setRegistryName(BLOCK_NAME);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder)
    {
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

    @NotNull
    @Override
    public ActionResultType use(
      final BlockState state,
      final World worldIn,
      final BlockPos pos,
      final PlayerEntity player,
      final Hand hand,
      final BlockRayTraceResult ray)
    {
        final ItemStack itemstack = player.inventory.getSelected();
        final TileEntity te = worldIn.getBlockEntity(pos);
        if (te instanceof TileEntityBarrel && !worldIn.isClientSide)
        {
            ((TileEntityBarrel) te).useBarrel(player, itemstack);
            ((TileEntityBarrel) te).updateBlock(worldIn);
        }

        return ActionResultType.SUCCESS;
    }

    @NotNull
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos, final ISelectionContext context)
    {
        return VoxelShapes.box(0, 0, 0, 1, 1.5, 1);
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
        return state.setValue(AbstractBlockBarrel.FACING, rot.rotate(state.getValue(AbstractBlockBarrel.FACING)));
    }

    /**
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
    @NotNull
    @Override
    @Deprecated
    public BlockState mirror(@NotNull final BlockState state, final Mirror mirrorIn)
    {
        return state.rotate(mirrorIn.getRotation(state.getValue(AbstractBlockBarrel.FACING)));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        return super.getStateForPlacement(context).setValue(AbstractBlockBarrel.FACING, context.getHorizontalDirection());
    }

    @Override
    public boolean canSurvive(final BlockState state, final IWorldReader worldIn, final BlockPos pos)
    {
        return !worldIn.isEmptyBlock(pos.below())
                 && worldIn.getBlockState(pos.below()).getBlock() != ModBlocks.blockBarrel;
    }
}
