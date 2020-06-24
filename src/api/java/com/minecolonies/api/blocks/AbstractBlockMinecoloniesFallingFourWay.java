package com.minecolonies.api.blocks;

import com.minecolonies.api.blocks.interfaces.IBlockMinecolonies;
import com.minecolonies.api.util.constant.Suppression;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.*;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

public abstract class AbstractBlockMinecoloniesFallingFourWay<B extends AbstractBlockMinecoloniesFallingFourWay<B>> extends FallingBlock implements IWaterLoggable, IBlockMinecolonies<B>
{
    public static final BooleanProperty NORTH = SixWayBlock.NORTH;
    public static final BooleanProperty EAST = SixWayBlock.EAST;
    public static final BooleanProperty SOUTH = SixWayBlock.SOUTH;
    public static final BooleanProperty WEST = SixWayBlock.WEST;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    
    protected VoxelShape[] collisionShapes = new VoxelShape[]{};
    protected VoxelShape[] shapes = new VoxelShape[]{};
    
    private final Object2IntMap<BlockState> stateShapeMap = new Object2IntOpenHashMap<>();

    public AbstractBlockMinecoloniesFallingFourWay(final Properties properties)
    {
        super(properties);
    }

    @NotNull
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return this.shapes[this.getIndex(state)];
    }

    @NotNull
    @ParametersAreNonnullByDefault
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return this.collisionShapes[this.getIndex(state)];
    }

    private static int getMask(Direction facing) {
        return 1 << facing.getHorizontalIndex();
    }

    protected int getIndex(BlockState state) {
        return this.stateShapeMap.computeIntIfAbsent(state, (p_223007_0_) -> {
            int i = 0;
            if (p_223007_0_.get(NORTH)) {
                i |= getMask(Direction.NORTH);
            }

            if (p_223007_0_.get(EAST)) {
                i |= getMask(Direction.EAST);
            }

            if (p_223007_0_.get(SOUTH)) {
                i |= getMask(Direction.SOUTH);
            }

            if (p_223007_0_.get(WEST)) {
                i |= getMask(Direction.WEST);
            }

            return i;
        });
    }

    @NotNull
    public IFluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    protected VoxelShape[] makeShapes(float nodeWidth, float limbWidth, float nodeHeight, float limbBase, float limbTop) {
        float nodeStart = 8.0F - nodeWidth;
        float nodeEnd = 8.0F + nodeWidth;
        float limbStart = 8.0F - limbWidth;
        float limbEnd = 8.0F + limbWidth;

        VoxelShape node  = Block.makeCuboidShape(nodeStart, 0.0F,     nodeStart, nodeEnd, nodeHeight, nodeEnd);
        VoxelShape north = Block.makeCuboidShape(limbStart, limbBase, 0.0F,      limbEnd, limbTop, limbEnd);
        VoxelShape south = Block.makeCuboidShape(limbStart, limbBase, limbStart, limbEnd, limbTop, 16.0D);
        VoxelShape west  = Block.makeCuboidShape(0.0F,      limbBase, limbStart, limbEnd, limbTop, limbEnd);
        VoxelShape east  = Block.makeCuboidShape(limbStart, limbBase, limbStart, 16.0D,   limbTop, limbEnd);
        VoxelShape cornernw = VoxelShapes.or(north, east);
        VoxelShape cornerse = VoxelShapes.or(south, west);

        // All 16 possible block combinations, in a specific index to be retrieved by getIndex
        VoxelShape[] avoxelshape = new VoxelShape[]{
                VoxelShapes.empty(),    south,   west, cornerse, north,
                VoxelShapes.or(south,   north),
                VoxelShapes.or(west,    north),
                VoxelShapes.or(cornerse,north),  east,
                VoxelShapes.or(south,   east),
                VoxelShapes.or(west,    east),
                VoxelShapes.or(cornerse,east),   cornernw,
                VoxelShapes.or(south,   cornernw),
                VoxelShapes.or(west,    cornernw),
                VoxelShapes.or(cornerse,cornernw)
        };

        // Combine the arm voxel shapes with the main node for all combinations
        for(int i = 0; i < 16; ++i) {
            avoxelshape[i] = VoxelShapes.or(node, avoxelshape[i]);
        }

        return avoxelshape;
    }

    /**
     * Registery block at gameregistry.
     *
     * @param registry the registry to use.
     * @return the block itself.
     */
    @Override
    @SuppressWarnings(Suppression.UNCHECKED)
    public B registerBlock(final IForgeRegistry<Block> registry)
    {
        registry.register(this);
        return (B) this;
    }
    /**
     * Registery block at gameregistry.
     *
     * @param registry the registry to use.
     */
    @Override
    public void registerBlockItem(final IForgeRegistry<Item> registry, final Item.Properties properties)
    {
        registry.register((new BlockItem(this, properties)).setRegistryName(this.getRegistryName()));
    }
}
