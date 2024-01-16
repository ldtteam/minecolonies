package com.minecolonies.core.blocks;

import com.minecolonies.api.blocks.AbstractBlockMinecoloniesNamedGrave;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.tileentities.TileEntityNamedGrave;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

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
        super(Properties.of(Material.STONE).strength(BLOCK_HARDNESS, RESISTANCE).noLootTable());
        final BlockState bs = this.defaultBlockState();
        this.registerDefaultState(bs.setValue(FACING, Direction.NORTH));
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return new ResourceLocation(Constants.MOD_ID, BLOCK_NAME);
    }

    @Override
    public void setPlacedBy(final Level worldIn, final BlockPos pos, final BlockState state, @Nullable final LivingEntity placer, final ItemStack stack)
    {
        BlockState tempState = state;
        if (placer != null)
        {
            tempState = tempState.setValue(FACING, placer.getDirection().getOpposite());
        }

        worldIn.setBlock(pos, tempState, 2);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull final BlockPos blockPos, @NotNull final BlockState blockState)
    {
        return new TileEntityNamedGrave(blockPos, blockState);
    }

    @NotNull
    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos, final CollisionContext context)
    {
        return Shapes.box(0, 0, 0, 1, 1.1, 1);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        final Level worldIn = context.getLevel();
        final BlockPos pos = context.getClickedPos();
        final BlockState state = defaultBlockState();
        final BlockEntity entity = worldIn.getBlockEntity(pos);

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
    public static BlockState getPlacementState(final BlockState state, final BlockEntity entity, final BlockPos pos)
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
    public boolean canSurvive(final BlockState state, final LevelReader worldIn, final BlockPos pos)
    {
        return !worldIn.isEmptyBlock(pos.below())
                 && worldIn.getBlockState(pos.below()).getBlock() != ModBlocks.blockNamedGrave;
    }
}
