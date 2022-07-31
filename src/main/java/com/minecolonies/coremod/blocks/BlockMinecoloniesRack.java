package com.minecolonies.coremod.blocks;

import com.minecolonies.api.blocks.AbstractBlockMinecoloniesRack;
import com.minecolonies.api.blocks.types.RackType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.tileentities.AbstractTileEntityRack;
import com.minecolonies.api.tileentities.TileEntityRack;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

/**
 * Block for the shelves of the warehouse.
 */
public class BlockMinecoloniesRack extends AbstractBlockMinecoloniesRack<BlockMinecoloniesRack>
{

    /**
     * The hardness this block has.
     */
    private static final float BLOCK_HARDNESS = 10.0F;

    /**
     * This blocks name.
     */
    private static final String BLOCK_NAME = "blockminecoloniesrack";

    /**
     * The resistance this block has.
     */
    private static final float RESISTANCE = Float.POSITIVE_INFINITY;

    /**
     * Smaller shape.
     */
    private static final VoxelShape SHAPE = Shapes.box(0.1, 0.1, 0.1, 0.9, 0.9, 0.9);

    public BlockMinecoloniesRack()
    {
        super(Properties.of(Material.WOOD).strength(BLOCK_HARDNESS, RESISTANCE));
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(VARIANT, RackType.DEFAULT));
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return new ResourceLocation(Constants.MOD_ID, BLOCK_NAME);
    }

    @Override
    public boolean propagatesSkylightDown(final BlockState state, @NotNull final BlockGetter reader, @NotNull final BlockPos pos)
    {
        return true;
    }

    @NotNull
    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos, final CollisionContext context)
    {
        return SHAPE;
    }

    @NotNull
    @Override
    public VoxelShape getCollisionShape(final BlockState state, final BlockGetter level, final BlockPos pos, final CollisionContext ctx)
    {
        return Shapes.block();
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        final Level worldIn = context.getLevel();
        final BlockPos pos = context.getClickedPos();
        final BlockState state = defaultBlockState();
        final BlockEntity entity = worldIn.getBlockEntity(pos);

        if (!(entity instanceof TileEntityRack))
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
        final AbstractTileEntityRack rack = (AbstractTileEntityRack) entity;
        if (rack.isEmpty() && (rack.getOtherChest() == null || rack.getOtherChest().isEmpty()))
        {
            if (rack.getOtherChest() != null)
            {
                state.setValue(FACING, BlockPosUtil.getFacing(pos, rack.getNeighbor()));
                if (rack.isMain())
                {
                    return state.setValue(AbstractBlockMinecoloniesRack.VARIANT, RackType.DEFAULTDOUBLE);
                }
                else
                {
                    return state.setValue(AbstractBlockMinecoloniesRack.VARIANT, RackType.EMPTYAIR);
                }
            }
            else
            {
                return state.setValue(AbstractBlockMinecoloniesRack.VARIANT, RackType.DEFAULT);
            }
        }
        else
        {
            if (rack.getOtherChest() != null)
            {
                state.setValue(FACING, BlockPosUtil.getFacing(pos, rack.getNeighbor()));
                if (rack.isMain())
                {
                    return state.setValue(AbstractBlockMinecoloniesRack.VARIANT, RackType.FULLDOUBLE);
                }
                else
                {
                    return state.setValue(AbstractBlockMinecoloniesRack.VARIANT, RackType.EMPTYAIR);
                }
            }
            else
            {
                return state.setValue(AbstractBlockMinecoloniesRack.VARIANT, RackType.FULL);
            }
        }
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

    @NotNull
    @Override
    public BlockState updateShape(
      @NotNull final BlockState stateIn,
      final Direction facing,
      final BlockState state,
      final LevelAccessor worldIn,
      final BlockPos currentPos,
      final BlockPos pos)
    {
        if (state.getBlock() instanceof BlockMinecoloniesRack || stateIn.getBlock() instanceof BlockMinecoloniesRack)
        {
            final BlockEntity rack = worldIn.getBlockEntity(pos);
            if (rack instanceof TileEntityRack)
            {
                ((AbstractTileEntityRack) rack).neighborChanged(currentPos);
            }
            final BlockEntity rack2 = worldIn.getBlockEntity(currentPos);
            if (rack2 instanceof TileEntityRack)
            {
                ((AbstractTileEntityRack) rack2).neighborChanged(pos);
            }
        }
        return super.updateShape(stateIn, facing, state, worldIn, currentPos, pos);
    }

    @Override
    public void spawnAfterBreak(final BlockState state, final ServerLevel worldIn, final BlockPos pos, final ItemStack stack, final boolean p_222953_)
    {
        final BlockEntity tileentity = worldIn.getBlockEntity(pos);
        if (tileentity instanceof TileEntityRack)
        {
            final IItemHandler handler = ((AbstractTileEntityRack) tileentity).getInventory();
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
              && tileEntity instanceof TileEntityRack)
        {
            final TileEntityRack rack = (TileEntityRack) tileEntity;
            if (!worldIn.isClientSide)
            {
                NetworkHooks.openScreen((ServerPlayer) player,
                  rack,
                  buf -> buf.writeBlockPos(rack.getBlockPos()).writeBlockPos(rack.getOtherChest() == null ? BlockPos.ZERO : rack.getOtherChest().getBlockPos()));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void setPlacedBy(final Level worldIn, final BlockPos pos, final BlockState state, @Nullable final LivingEntity placer, final ItemStack stack)
    {
        BlockState tempState = state;
        tempState = tempState.setValue(VARIANT, RackType.DEFAULT);
        if (placer != null)
        {
            tempState = tempState.setValue(FACING, placer.getDirection().getOpposite());
        }

        worldIn.setBlock(pos, tempState, 2);
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
        return new TileEntityRack(blockPos, blockState);
    }

    @Override
    public List<ItemStack> getDrops(final BlockState state, final LootContext.Builder builder)
    {
        final List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(this, 1));
        return drops;
    }

    @Override
    public void onRemove(BlockState state, @NotNull Level worldIn, @NotNull BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (state.getBlock() != newState.getBlock())
        {
            BlockEntity tileEntity = worldIn.getBlockEntity(pos);
            if (tileEntity instanceof TileEntityRack)
            {
                TileEntityRack tileEntityRack = (TileEntityRack) tileEntity;
                InventoryUtils.dropItemHandler(tileEntityRack.getInventory(),
                  worldIn,
                  tileEntityRack.getBlockPos().getX(),
                  tileEntityRack.getBlockPos().getY(),
                  tileEntityRack.getBlockPos().getZ());
                worldIn.updateNeighbourForOutputSignal(pos, this);
            }

            super.onRemove(state, worldIn, pos, newState, isMoving);
        }
    }
}
