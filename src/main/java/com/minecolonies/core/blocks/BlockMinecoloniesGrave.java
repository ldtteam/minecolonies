package com.minecolonies.core.blocks;

import com.minecolonies.api.blocks.AbstractBlockMinecoloniesGrave;
import com.minecolonies.api.blocks.types.GraveType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.tileentities.AbstractTileEntityGrave;
import com.minecolonies.core.tileentities.TileEntityGrave;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

/**
 * Block for the graves
 */
public class BlockMinecoloniesGrave extends AbstractBlockMinecoloniesGrave<BlockMinecoloniesGrave>
{
    /**
     * The hardness this block has.
     */
    private static final float BLOCK_HARDNESS = 1.5F;

    /**
     * This blocks name.
     */
    private static final String BLOCK_NAME = "blockminecoloniesgrave";

    /**
     * The resistance this block has.
     */
    private static final float RESISTANCE = 5F;

    /**
     * Smaller shape.
     */
    private static final VoxelShape SHAPE = Shapes.box(0.1, 0.1, 0.1, 0.9, 0.9, 0.9);

    public BlockMinecoloniesGrave()
    {
        super(Properties.of().mapColor(MapColor.STONE).sound(SoundType.STONE).strength(BLOCK_HARDNESS, RESISTANCE));
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(VARIANT, GraveType.DEFAULT));
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return new ResourceLocation(Constants.MOD_ID, BLOCK_NAME);
    }

    @Override
    public boolean propagatesSkylightDown(final BlockState state, @NotNull final BlockGetter reader, @NotNull final BlockPos pos)
    {
        return false;
    }

    @NotNull
    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos, final CollisionContext context)
    {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        final Level worldIn = context.getLevel();
        final BlockPos pos = context.getClickedPos();
        final BlockState state = defaultBlockState();
        final BlockEntity entity = worldIn.getBlockEntity(pos);

        if (!(entity instanceof TileEntityGrave))
        {
            return super.getStateForPlacement(context);
        }

        return getPlacementState(state, pos);
    }

    /**
     * Get the statement ready.
     *
     * @param state  the state to place.
     * @param pos    the position.
     * @return the next state.
     */
    public static BlockState getPlacementState(final BlockState state, final BlockPos pos)
    {
        return state.setValue(VARIANT, GraveType.DEFAULT);
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
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    /**
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
    @NotNull
    @Override
    @Deprecated
    public BlockState mirror(@NotNull final BlockState state, final Mirror mirrorIn)
    {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    public void spawnAfterBreak(final BlockState state, final ServerLevel worldIn, final BlockPos pos, final ItemStack stack, final boolean p_222953_)
    {
        final BlockEntity tileentity = worldIn.getBlockEntity(pos);
        if (tileentity instanceof TileEntityGrave)
        {
            final IItemHandler handler = ((AbstractTileEntityGrave) tileentity).getInventory();
            InventoryUtils.dropItemHandler(handler, worldIn, pos.getX(), pos.getY(), pos.getZ());
        }
        super.spawnAfterBreak(state, worldIn, pos, stack, p_222953_);
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
        final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(worldIn, pos);
        final BlockEntity tileEntity = worldIn.getBlockEntity(pos);

        if ((colony == null || colony.getPermissions().hasPermission(player, Action.ACCESS_HUTS))
              && tileEntity instanceof TileEntityGrave)
        {
            final TileEntityGrave grave = (TileEntityGrave) tileEntity;
            if (!worldIn.isClientSide)
            {
                NetworkHooks.openScreen((ServerPlayer) player,
                  grave,
                  buf -> buf.writeBlockPos(grave.getBlockPos()));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void setPlacedBy(final Level worldIn, final BlockPos pos, final BlockState state, @Nullable final LivingEntity placer, final ItemStack stack)
    {
        BlockState tempState = state;
        tempState = tempState.setValue(VARIANT, GraveType.DEFAULT);
        if (placer != null)
        {
            tempState = tempState.setValue(FACING, placer.getDirection().getOpposite());
        }

        worldIn.setBlock(pos, tempState, 2);
    }

    @Override
    public VoxelShape getCollisionShape(final BlockState p_60572_, final BlockGetter p_60573_, final BlockPos p_60574_, final CollisionContext p_60575_)
    {
        return Shapes.empty();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, VARIANT);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull final BlockPos blockPos, @NotNull final BlockState blockState)
    {
        return new TileEntityGrave(blockPos, blockState);
    }

    @Override
    public void onRemove(BlockState state, @NotNull Level worldIn, @NotNull BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (state.getBlock() != newState.getBlock())
        {
            BlockEntity tileEntity = worldIn.getBlockEntity(pos);
            if (tileEntity instanceof TileEntityGrave)
            {
                TileEntityGrave tileEntityGrave = (TileEntityGrave) tileEntity;
                InventoryUtils.dropItemHandler(tileEntityGrave.getInventory(),
                  worldIn,
                  tileEntityGrave.getBlockPos().getX(),
                  tileEntityGrave.getBlockPos().getY(),
                  tileEntityGrave.getBlockPos().getZ());
                worldIn.updateNeighbourForOutputSignal(pos, this);
            }

            super.onRemove(state, worldIn, pos, newState, isMoving);
        }
    }
}
