package com.minecolonies.coremod.blocks;

import com.minecolonies.api.blocks.AbstractBlockMinecoloniesNamedGrave;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.tileentities.TileEntityNamedGrave;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class BlockMinecoloniesNamedGrave extends AbstractBlockMinecoloniesNamedGrave<BlockMinecoloniesNamedGrave>
{
    /**
     * The hardness this block has.
     */
    private static final float  BLOCK_HARDNESS = 5F;

    /**
     * This blocks name.
     */
    private static final String BLOCK_NAME     = "blockminecoloniesnamedgrave";

    /**
     * The resistance this block has.
     */
    private static final float  RESISTANCE     = 1F;

    public BlockMinecoloniesNamedGrave()
    {
        super(Properties.of(Material.STONE).strength(BLOCK_HARDNESS, RESISTANCE).noDrops());
        setRegistryName(Constants.MOD_ID.toLowerCase() + ":" + BLOCK_NAME);
        final BlockState bs = this.defaultBlockState();
        this.registerDefaultState(bs.setValue(FACING, Direction.NORTH));
    }

    @Override
    public void setPlacedBy(final World worldIn, final BlockPos pos, final BlockState state, @Nullable final LivingEntity placer, final ItemStack stack)
    {
        BlockState tempState = state;
        if (placer != null)
        {
            tempState = tempState.setValue(FACING, placer.getDirection().getOpposite());
        }

        worldIn.setBlock(pos, tempState, 2);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new TileEntityNamedGrave();
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

    @NotNull
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos, final ISelectionContext context)
    {
        return VoxelShapes.box(0, 0, 0, 1, 1.1, 1);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        final World worldIn = context.getLevel();
        final BlockPos pos = context.getClickedPos();
        final BlockState state = defaultBlockState();
        final TileEntity entity = worldIn.getBlockEntity(pos);

        if (!(entity instanceof TileEntityNamedGrave))
        {
            return super.getStateForPlacement(context);
        }

        return getPlacementState(state, entity, pos);
    }

    /**
     * Get the statement ready.
     *
     * @param state  the state to place.
     * @param entity the tileEntity.
     * @param pos    the position.
     * @return the next state.
     */
    public static BlockState getPlacementState(final BlockState state, final TileEntity entity, final BlockPos pos)
    {
        return state;
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
        return state.setValue(AbstractBlockMinecoloniesNamedGrave.FACING, rot.rotate(state.getValue(AbstractBlockMinecoloniesNamedGrave.FACING)));
    }

    /**
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
    @NotNull
    @Override
    @Deprecated
    public BlockState mirror(@NotNull final BlockState state, final Mirror mirrorIn)
    {
        return state.rotate(mirrorIn.getRotation(state.getValue(AbstractBlockMinecoloniesNamedGrave.FACING)));
    }

    @Override
    public boolean canSurvive(final BlockState state, final IWorldReader worldIn, final BlockPos pos)
    {
        return !worldIn.isEmptyBlock(pos.below())
                 && worldIn.getBlockState(pos.below()).getBlock() != ModBlocks.blockNamedGrave;
    }
}
