package com.minecolonies.coremod.blocks;

import com.ldtteam.structurize.blocks.interfaces.IAnchorBlock;
import com.minecolonies.api.blocks.AbstractBlockMinecoloniesHorizontal;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.entity.ai.citizen.builder.IBuilderUndestroyable;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.tileentities.TileEntityDecorationController;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;

import static com.minecolonies.api.util.constant.BuildingConstants.LEISURE;

/**
 * Creates a decoration controller block.
 */
public class BlockDecorationController extends AbstractBlockMinecoloniesHorizontal<BlockDecorationController> implements IBuilderUndestroyable, IAnchorBlock
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
    protected static final VoxelShape AABB_SOUTH = VoxelShapes.box(0.25D, 0.314D, 0.97D, 0.75D, 0.86D, 1.0D);
    protected static final VoxelShape AABB_NORTH = VoxelShapes.box(0.25D, 0.314D, 0.0D, 0.75D, 0.86D, 0.3D);
    protected static final VoxelShape AABB_EAST  = VoxelShapes.box(0.97D, 0.314D, 0.25D, 1.0D, 0.86D, 0.75D);
    protected static final VoxelShape AABB_WEST  = VoxelShapes.box(0.0D, 0.314D, 0.25D, 0.3D, 0.86D, 0.75D);

    /**
     * Constructor for the deco controller.
     */
    public BlockDecorationController()
    {
        super(Properties.of(Material.WOOD).strength(BLOCK_HARDNESS, RESISTANCE).noCollission());
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(MIRROR, false));
        setRegistryName(BLOCK_NAME);
    }

    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos, final ISelectionContext context)
    {
        Direction Direction = state.getValue(FACING);
        switch (Direction)
        {
            case EAST:
                return AABB_EAST;
            case WEST:
                return AABB_WEST;
            case SOUTH:
                return AABB_SOUTH;
            case NORTH:
            default:
                return AABB_NORTH;
        }
    }

    @Override
    public ActionResultType use(
      final BlockState state,
      final World worldIn,
      final BlockPos pos,
      final PlayerEntity player,
      final Hand hand,
      final BlockRayTraceResult ray)
    {
        if (worldIn.isClientSide)
        {
            final TileEntity tileEntity = worldIn.getBlockEntity(pos);
            if (tileEntity instanceof TileEntityDecorationController)
            {
                MineColonies.proxy.openDecorationControllerWindow(pos);
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public void setPlacedBy(@NotNull final World worldIn, @NotNull final BlockPos pos, final BlockState state, final LivingEntity placer, final ItemStack stack)
    {
        super.setPlacedBy(worldIn, pos, state, placer, stack);

        /*
        Only work on server side
        */
        if (worldIn.isClientSide)
        {
            return;
        }

        final TileEntity tileEntity = worldIn.getBlockEntity(pos);
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
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, MIRROR);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new TileEntityDecorationController();
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        return super.getStateForPlacement(context).setValue(FACING, context.getHorizontalDirection());
    }

    /**
     * @deprecated
     */
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
}
