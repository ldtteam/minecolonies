package com.minecolonies.coremod.blocks;

import com.ldtteam.structurize.blocks.interfaces.IAnchorBlock;
import com.ldtteam.structurize.blocks.interfaces.ILeveledBlueprintAnchorBlock;
import com.minecolonies.api.blocks.AbstractBlockMinecoloniesDirectional;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.entity.ai.citizen.builder.IBuilderUndestroyable;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.tileentities.TileEntityDecorationController;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

import java.util.ArrayList;
import java.util.List;

import static com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE.TAG_BLUEPRINTDATA;
import static com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE.TAG_SCHEMATIC_NAME;
import static com.minecolonies.api.blocks.decorative.AbstractBlockMinecoloniesConstructionTape.WATERLOGGED;
import static com.minecolonies.api.util.constant.BuildingConstants.LEISURE;

/**
 * Creates a decoration controller block.
 */
public class BlockDecorationController extends AbstractBlockMinecoloniesDirectional<BlockDecorationController> implements IBuilderUndestroyable, IAnchorBlock, EntityBlock, ILeveledBlueprintAnchorBlock, SimpleWaterloggedBlock
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
    protected static final VoxelShape AABB_SOUTH = Shapes.box(0.25D, 0.314D, 0.7D, 0.75D, 0.86D, 1.0D);
    protected static final VoxelShape AABB_NORTH = Shapes.box(0.25D, 0.314D, 0.0D, 0.75D, 0.86D, 0.3D);


    protected static final VoxelShape AABB_EAST  = Shapes.box(0.7D, 0.314D, 0.25D, 1.0D, 0.86D, 0.75D);
    protected static final VoxelShape AABB_WEST  = Shapes.box(0.0D, 0.314D, 0.25D, 0.3D, 0.86D, 0.75D);



    protected static final VoxelShape AABB_UP = Shapes.box(0.25D, 0.7D, 0.14D, 0.75D, 1.0D, 0.686D);
    protected static final VoxelShape AABB_DOWN = Shapes.box(0.25D, 0.0D, 0.314D, 0.75D, 0.3D, 0.86D);

    /**
     * Constructor for the deco controller.
     */
    public BlockDecorationController()
    {
        super(Properties.of(Material.WOOD).strength(BLOCK_HARDNESS, RESISTANCE).noCollission());
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(MIRROR, false).setValue(WATERLOGGED, false));
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return new ResourceLocation(Constants.MOD_ID, BLOCK_NAME);
    }

    @Override
    public VoxelShape getShape(final BlockState decoController, final BlockGetter level, final BlockPos pos, final CollisionContext context)
    {
        final Direction direction = decoController.getValue(BlockDecorationController.FACING);
        final BlockPos offsetPos = pos.relative(direction);
        final BlockState state = level.getBlockState(offsetPos);
        final VoxelShape shape = state.getBlock() != this ? state.getShape(level, offsetPos) : Shapes.block();
        if (shape.isEmpty() || Block.isShapeFullBlock(shape))
        {
            return switch (direction)
                     {
                         case EAST -> AABB_EAST;
                         case WEST -> AABB_WEST;
                         case SOUTH -> AABB_SOUTH;
                         case NORTH -> AABB_NORTH;
                         case UP -> AABB_UP;
                         case DOWN -> AABB_DOWN;
                     };
        }

        return switch (direction)
        {
            case UP -> AABB_UP.move(0, shape.min(Direction.Axis.Y), 0);
            case DOWN -> AABB_DOWN.move(0, shape.max(Direction.Axis.Y) - 1, 0);
            case NORTH -> AABB_NORTH.move(0, 0, shape.max(Direction.Axis.Z) - 1);
            case SOUTH -> AABB_SOUTH.move(0, 0, shape.min(Direction.Axis.Z));
            case EAST -> AABB_EAST.move(shape.min(Direction.Axis.X), 0, 0);
            case WEST -> AABB_WEST.move(shape.max(Direction.Axis.X) - 1, 0, 0);
        };
    }

    @NotNull
    @Override
    public BlockState updateShape(
      @NotNull final BlockState stateIn,
      final Direction dir,
      final BlockState state,
      final LevelAccessor worldIn,
      @NotNull final BlockPos currentPos,
      final BlockPos pos)
    {
        if (stateIn.getValue(WATERLOGGED))
        {
            worldIn.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }


        return super.updateShape(stateIn, dir, state, worldIn, currentPos, pos);
    }

    @Override
    public InteractionResult use(
      final BlockState state,
      final Level worldIn,
      final BlockPos pos,
      final Player player,
      final InteractionHand hand,
      final BlockHitResult ray)
    {
        if (worldIn.isClientSide)
        {
            final BlockEntity tileEntity = worldIn.getBlockEntity(pos);
            if (tileEntity instanceof TileEntityDecorationController)
            {
                MineColonies.proxy.openDecorationControllerWindow(pos);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public RenderShape getRenderShape(final BlockState state)
    {
        return RenderShape.INVISIBLE;
    }

    @Override
    public void setPlacedBy(@NotNull final Level worldIn, @NotNull final BlockPos pos, final BlockState state, final LivingEntity placer, final ItemStack stack)
    {
        super.setPlacedBy(worldIn, pos, state, placer, stack);

        /*
        Only work on server side
        */
        if (worldIn.isClientSide)
        {
            return;
        }

        final BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        if (tileEntity instanceof TileEntityDecorationController && ((TileEntityDecorationController) tileEntity).getPositionedTags().getOrDefault(BlockPos.ZERO, new ArrayList<>()).contains(LEISURE))
        {
            @Nullable final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(worldIn, pos);
            if (colony != null)
            {
                colony.getBuildingManager().addLeisureSite(pos);
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, MIRROR, WATERLOGGED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull final BlockPos blockPos, @NotNull final BlockState blockState)
    {
        return new TileEntityDecorationController(blockPos, blockState);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        Fluid fluid = context.getLevel().getFluidState(context.getClickedPos()).getType();
        return super.getStateForPlacement(context).setValue(FACING, context.getClickedFace().getOpposite()).setValue(WATERLOGGED, fluid == Fluids.WATER);
    }

    @Override
    public FluidState getFluidState(final BlockState state)
    {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @NotNull
    @Override
    public BlockState rotate(@NotNull BlockState state, Rotation rot)
    {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @NotNull
    @Override
    public BlockState mirror(@NotNull BlockState state, Mirror mirrorIn)
    {
        return state.setValue(MIRROR, mirrorIn != Mirror.NONE);
    }

    @Override
    public int getLevel(final CompoundTag beData)
    {
        if (beData == null)
        {
            return 0;
        }

        try
        {
            return Integer.parseInt(beData.getCompound(TAG_BLUEPRINTDATA).getString(TAG_SCHEMATIC_NAME).replaceAll("[^0-9]", ""));
        }
        catch (final NumberFormatException exception)
        {
            return 0;
        }
    }
}
