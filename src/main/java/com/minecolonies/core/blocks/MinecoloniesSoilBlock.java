package com.minecolonies.core.blocks;

import java.util.Iterator;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.FarmlandWaterManager;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;

public class MinecoloniesSoilBlock extends Block
{
    public static final    IntegerProperty MOISTURE     = BlockStateProperties.MOISTURE;
    protected static final VoxelShape      SHAPE        = Block.box(0.0, 0.0, 0.0, 16.0, 15.0, 16.0);
    public static final    int             MAX_MOISTURE = 7;

    public MinecoloniesSoilBlock(BlockBehaviour.Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(MOISTURE, 0));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState newState, LevelAccessor level, BlockPos pos, BlockPos neighborPos)
    {
        if (direction == Direction.UP && !state.canSurvive(level, pos))
        {
            level.scheduleTick(pos, this, 1);
        }

        return super.updateShape(state, direction, newState, level, pos, neighborPos);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos)
    {
        BlockState aboveState = level.getBlockState(pos.above());
        return !aboveState.isSolid();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        return !this.defaultBlockState().canSurvive(ctx.getLevel(), ctx.getClickedPos()) ? Blocks.DIRT.defaultBlockState() : super.getStateForPlacement(ctx);
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state)
    {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx)
    {
        return SHAPE;
    }


    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rng)
    {
        if (!state.canSurvive(level, pos))
        {
            turnToDirt(null, state, level, pos);
            return;
        }

        int i = state.getValue(MOISTURE);
        if (!isNearWater(level, pos) && !level.isRainingAt(pos.above()))
        {
            if (i > 0)
            {
                level.setBlock(pos, state.setValue(MOISTURE, i - 1), 2);
            }
            else if (!shouldMaintainFarmland(level, pos))
            {
                turnToDirt( null, state, level, pos);
            }
        }
        else if (i < 7)
        {
            level.setBlock(pos, state.setValue(MOISTURE, 7), 2);
        }
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float light)
    {
        if (!level.isClientSide && ForgeHooks.onFarmlandTrample(level, pos, Blocks.DIRT.defaultBlockState(), light, entity))
        {
            turnToDirt(entity, state, level, pos);
        }

        super.fallOn(level, state, pos, entity, light);
    }

    public static void turnToDirt(@Nullable Entity p_270981_, BlockState p_270402_, Level p_270568_, BlockPos p_270551_)
    {
        BlockState blockstate = pushEntitiesUp(p_270402_, Blocks.DIRT.defaultBlockState(), p_270568_, p_270551_);
        p_270568_.setBlockAndUpdate(p_270551_, blockstate);
        p_270568_.gameEvent(GameEvent.BLOCK_CHANGE, p_270551_, Context.of(p_270981_, blockstate));
    }

    private static boolean shouldMaintainFarmland(BlockGetter p_279219_, BlockPos p_279209_)
    {
        BlockState plant = p_279219_.getBlockState(p_279209_.above());
        BlockState state = p_279219_.getBlockState(p_279209_);
        return plant.getBlock() instanceof IPlantable && state.canSustainPlant(p_279219_, p_279209_, Direction.UP, (IPlantable) plant.getBlock());
    }

    private static boolean isNearWater(LevelReader p_53259_, BlockPos p_53260_)
    {
        BlockState state = p_53259_.getBlockState(p_53260_);
        Iterator var3 = BlockPos.betweenClosed(p_53260_.offset(-4, 0, -4), p_53260_.offset(4, 1, 4)).iterator();

        BlockPos blockpos;
        do
        {
            if (!var3.hasNext())
            {
                return FarmlandWaterManager.hasBlockWaterTicket(p_53259_, p_53260_);
            }

            blockpos = (BlockPos) var3.next();
        }
        while (!state.canBeHydrated(p_53259_, p_53260_, p_53259_.getFluidState(blockpos), blockpos));

        return true;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_53283_)
    {
        p_53283_.add(new Property[] {MOISTURE});
    }

    public boolean isPathfindable(BlockState p_53267_, BlockGetter p_53268_, BlockPos p_53269_, PathComputationType p_53270_)
    {
        return false;
    }
}
