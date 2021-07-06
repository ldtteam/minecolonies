package com.minecolonies.api.blocks.decorative;

import com.minecolonies.api.blocks.AbstractBlockMinecoloniesFalling;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.*;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractBlockMinecoloniesConstructionTape<B extends AbstractBlockMinecoloniesConstructionTape<B>> extends AbstractBlockMinecoloniesFalling<B>
  implements IWaterLoggable
{
    public static final BooleanProperty NORTH       = SixWayBlock.NORTH;
    public static final BooleanProperty EAST        = SixWayBlock.EAST;
    public static final BooleanProperty SOUTH       = SixWayBlock.SOUTH;
    public static final BooleanProperty WEST        = SixWayBlock.WEST;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    protected VoxelShape[] shapes = new VoxelShape[] {};

    private final Object2IntMap<BlockState> stateShapeMap = new Object2IntOpenHashMap<>();

    /**
     * The default face for when there are no connections.
     */
    public static final DirectionProperty FACING = HorizontalBlock.FACING;

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
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
    {
        return this.shapes[this.getIndex(state)];
    }

    private static int getMask(Direction facing)
    {
        return 1 << facing.get2DDataValue();
    }

    protected int getIndex(BlockState state)
    {
        return this.stateShapeMap.computeIntIfAbsent(state, (computeState) -> {
            int i = 0;
            if (computeState.getValue(NORTH))
            {
                i |= getMask(Direction.NORTH);
            }

            if (computeState.getValue(EAST))
            {
                i |= getMask(Direction.EAST);
            }

            if (computeState.getValue(SOUTH))
            {
                i |= getMask(Direction.SOUTH);
            }

            if (computeState.getValue(WEST))
            {
                i |= getMask(Direction.WEST);
            }

            return i;
        });
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
        VoxelShape cornernw = VoxelShapes.or(north, east);
        VoxelShape cornerse = VoxelShapes.or(south, west);

        // All 16 possible block combinations, in a specific index to be retrieved by getIndex
        VoxelShape[] avoxelshape = new VoxelShape[]
                                     {
                                       VoxelShapes.empty(), south, west, cornerse, north,
                                       VoxelShapes.or(south, north),
                                       VoxelShapes.or(west, north),
                                       VoxelShapes.or(cornerse, north), east,
                                       VoxelShapes.or(south, east),
                                       VoxelShapes.or(west, east),
                                       VoxelShapes.or(cornerse, east), cornernw,
                                       VoxelShapes.or(south, cornernw),
                                       VoxelShapes.or(west, cornernw),
                                       VoxelShapes.or(cornerse, cornernw)
                                     };

        // Combine the arm voxel shapes with the main node for all combinations
        for (int i = 0; i < 16; ++i)
        {
            avoxelshape[i] = VoxelShapes.or(node, avoxelshape[i]);
        }

        return avoxelshape;
    }
}
