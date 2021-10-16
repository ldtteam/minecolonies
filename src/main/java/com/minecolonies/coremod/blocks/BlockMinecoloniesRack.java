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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

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
    private static final VoxelShape SHAPE = VoxelShapes.box(0.1, 0.1, 0.1, 0.9, 0.9, 0.9);

    public BlockMinecoloniesRack()
    {
        super(Properties.of(Material.WOOD).strength(BLOCK_HARDNESS, RESISTANCE));
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(VARIANT, RackType.DEFAULT));
        setRegistryName(Constants.MOD_ID.toLowerCase() + ":" + BLOCK_NAME);
    }

    @Override
    public boolean propagatesSkylightDown(final BlockState state, @NotNull final IBlockReader reader, @NotNull final BlockPos pos)
    {
        return true;
    }

    @NotNull
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos, final ISelectionContext context)
    {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        final World worldIn = context.getLevel();
        final BlockPos pos = context.getClickedPos();
        final BlockState state = defaultBlockState();
        final TileEntity entity = worldIn.getBlockEntity(pos);

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
    public static BlockState getPlacementState(final BlockState state, final TileEntity entity, final BlockPos pos)
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
      final IWorld worldIn,
      final BlockPos currentPos,
      final BlockPos pos)
    {
        if (state.getBlock() instanceof BlockMinecoloniesRack || stateIn.getBlock() instanceof BlockMinecoloniesRack)
        {
            final TileEntity rack = worldIn.getBlockEntity(pos);
            if (rack instanceof TileEntityRack)
            {
                ((AbstractTileEntityRack) rack).neighborChanged(currentPos);
            }
            final TileEntity rack2 = worldIn.getBlockEntity(currentPos);
            if (rack2 instanceof TileEntityRack)
            {
                ((AbstractTileEntityRack) rack2).neighborChanged(pos);
            }
        }
        return super.updateShape(stateIn, facing, state, worldIn, currentPos, pos);
    }

    @Override
    public void spawnAfterBreak(final BlockState state, final ServerWorld worldIn, final BlockPos pos, final ItemStack stack)
    {
        final TileEntity tileentity = worldIn.getBlockEntity(pos);
        if (tileentity instanceof TileEntityRack)
        {
            final IItemHandler handler = ((AbstractTileEntityRack) tileentity).getInventory();
            InventoryUtils.dropItemHandler(handler, worldIn, pos.getX(), pos.getY(), pos.getZ());
        }
        super.spawnAfterBreak(state, worldIn, pos, stack);
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
        final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(worldIn, pos);
        final TileEntity tileEntity = worldIn.getBlockEntity(pos);

        if ((colony == null || colony.getPermissions().hasPermission(player, Action.ACCESS_HUTS))
              && tileEntity instanceof TileEntityRack)
        {
            final TileEntityRack rack = (TileEntityRack) tileEntity;
            if (!worldIn.isClientSide)
            {
                NetworkHooks.openGui((ServerPlayerEntity) player,
                  rack,
                  buf -> buf.writeBlockPos(rack.getBlockPos()).writeBlockPos(rack.getOtherChest() == null ? BlockPos.ZERO : rack.getOtherChest().getBlockPos()));
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.FAIL;
    }

    @Override
    public void setPlacedBy(final World worldIn, final BlockPos pos, final BlockState state, @Nullable final LivingEntity placer, final ItemStack stack)
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
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, VARIANT);
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new TileEntityRack();
    }

    @Override
    public List<ItemStack> getDrops(final BlockState state, final LootContext.Builder builder)
    {
        final List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(this, 1));
        return drops;
    }

    @Override
    public void onRemove(BlockState state, @NotNull World worldIn, @NotNull BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (state.getBlock() != newState.getBlock())
        {
            TileEntity tileEntity = worldIn.getBlockEntity(pos);
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
