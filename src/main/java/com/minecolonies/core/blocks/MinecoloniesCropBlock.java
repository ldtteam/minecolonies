package com.minecolonies.core.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Abstract Minecolonies crop type. We have our own to avoid cheesing the crop.s
 */
public abstract class MinecoloniesCropBlock extends Block
{
    public static final  IntegerProperty AGE = BlockStateProperties.AGE_7;
    private static final VoxelShape[]    SHAPE_BY_AGE = new VoxelShape[] {Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0), Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0), Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0),
      Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0), Block.box(0.0, 0.0, 0.0, 16.0, 10.0, 16.0), Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0),
      Block.box(0.0, 0.0, 0.0, 16.0, 14.0, 16.0), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)};

    /**
     * Constructor to create a block of this type.
     * @param properties the block properties.
     */
    public MinecoloniesCropBlock(BlockBehaviour.Properties properties)
    {
        Blocks.FARMLAND
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx)
    {
        return SHAPE_BY_AGE[state.getValue(AGE)];
    }

    /**
     * Check if the block is of max age.
     * @param state the state its at.
     * @return true if max age.
     */
    public final boolean isMaxAge(BlockState state)
    {
        return state.getValue(AGE) >= this.getMaxAge();
    }

    /**
     * Get the default max crop age.
     * @return the max age.
     */
    protected int getMaxAge()
    {
        return 7;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state)
    {
        return !this.isMaxAge(state);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource randomSource)
    {
        if (level.isAreaLoaded(pos, 1))
        {
            if (level.getRawBrightness(pos, 0) >= 9)
            {
                int i = state.getValue(AGE);
                if (i < this.getMaxAge())
                {
                    //todo make sure there is a correct block underneath.
                    if (randomSource.nextInt(25) == 0)
                    {
                        level.setBlock(pos, this.defaultBlockState().setValue(AGE, (i + 1)), 2);
                    }
                }
            }
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos)
    {
        return (level.getRawBrightness(pos, 0) >= 8 || level.canSeeSky(pos)) && super.canSurvive(state, level, pos);
    }

    abstract ItemLike getBaseSeedId();

    @Override
    public ItemStack getCloneItemStack(BlockGetter p_52254_, BlockPos p_52255_, BlockState p_52256_)
    {
        return new ItemStack(this.getBaseSeedId());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_52286_)
    {
        p_52286_.add(AGE);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState newState, LevelAccessor level, BlockPos pos, BlockPos neighborPos)
    {
        return !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, dir, newState, level, pos, neighborPos);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos)
    {
        return state.getFluidState().isEmpty();
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType pathComputationType)
    {
        return pathComputationType == PathComputationType.AIR && !this.hasCollision || super.isPathfindable(state, level, pos, pathComputationType);
    }
}
