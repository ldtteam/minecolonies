package com.minecolonies.api.blocks.decorative;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractBlockMinecoloniesConstructionTape<B extends AbstractBlockMinecoloniesConstructionTape<B>> extends FallingBlock implements SimpleWaterloggedBlock
{
    public static final BooleanProperty NORTH       = PipeBlock.NORTH;
    public static final BooleanProperty EAST        = PipeBlock.EAST;
    public static final BooleanProperty SOUTH       = PipeBlock.SOUTH;
    public static final BooleanProperty WEST        = PipeBlock.WEST;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    protected VoxelShape[] shapes = new VoxelShape[] {};

    /**
     * The default face for when there are no connections.
     */
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    /**
     * Implies that the tape should revert to a corner if there are no connections. Must be set explicitly. For use by the builder handler.
     */
    public static final BooleanProperty CORNER = BooleanProperty.create("corner");

    public AbstractBlockMinecoloniesConstructionTape(final Properties properties)
    {
        super(properties);
    }

    @NotNull
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
    {
        return this.shapes[this.getIndex(state)];
    }

    private static int getMask(Direction facing)
    {
        return 1 << facing.get2DDataValue();
    }

    protected int getIndex(BlockState state)
    {
        int i = 0;
        if (state.getValue(NORTH))
        {
            i |= getMask(Direction.NORTH);
        }

        if (state.getValue(EAST))
        {
            i |= getMask(Direction.EAST);
        }

        if (state.getValue(SOUTH))
        {
            i |= getMask(Direction.SOUTH);
        }

        if (state.getValue(WEST))
        {
            i |= getMask(Direction.WEST);
        }

        return i;
    }

    @Override
    public FluidState getFluidState(final BlockState state)
    {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    protected VoxelShape[] makeShapes(float nodeWidth, float limbWidth, float nodeHeight, float limbBase, float limbTop)
    {
        float nodeStart = 8.0F - nodeWidth;
        float nodeEnd = 8.0F + nodeWidth;
        float limbStart = 8.0F - limbWidth;
        float limbEnd = 8.0F + limbWidth;

        VoxelShape node = Block.box(nodeStart, 0.0F, nodeStart, nodeEnd, nodeHeight, nodeEnd);
        VoxelShape north = Block.box(limbStart, limbBase, 0.0F, limbEnd, limbTop, limbEnd);
        VoxelShape south = Block.box(limbStart, limbBase, limbStart, limbEnd, limbTop, 16.0D);
        VoxelShape west = Block.box(0.0F, limbBase, limbStart, limbEnd, limbTop, limbEnd);
        VoxelShape east = Block.box(limbStart, limbBase, limbStart, 16.0D, limbTop, limbEnd);
        VoxelShape cornernw = Shapes.or(north, east);
        VoxelShape cornerse = Shapes.or(south, west);

        // All 16 possible block combinations, in a specific index to be retrieved by getIndex
        VoxelShape[] avoxelshape = new VoxelShape[]
                                     {
                                       Shapes.empty(), south, west, cornerse, north,
                                       Shapes.or(south, north),
                                       Shapes.or(west, north),
                                       Shapes.or(cornerse, north), east,
                                       Shapes.or(south, east),
                                       Shapes.or(west, east),
                                       Shapes.or(cornerse, east), cornernw,
                                       Shapes.or(south, cornernw),
                                       Shapes.or(west, cornernw),
                                       Shapes.or(cornerse, cornernw)
                                     };

        // Combine the arm voxel shapes with the main node for all combinations
        for (int i = 0; i < 16; ++i)
        {
            avoxelshape[i] = Shapes.or(node, avoxelshape[i]);
        }

        return avoxelshape;
    }
}
