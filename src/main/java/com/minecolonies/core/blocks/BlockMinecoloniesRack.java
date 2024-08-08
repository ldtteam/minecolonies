package com.minecolonies.core.blocks;

import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlockComponent;
import com.minecolonies.api.blocks.AbstractBlockMinecoloniesRack;
import com.minecolonies.api.blocks.types.RackType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.tileentities.AbstractTileEntityRack;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.tileentities.TileEntityRack;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import net.minecraft.core.Direction;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

/**
 * Block for the shelves of the warehouse.
 */
public class BlockMinecoloniesRack extends AbstractBlockMinecoloniesRack<BlockMinecoloniesRack> implements IMateriallyTexturedBlock
{
    /**
     * Normal translation we use.
     */
    private static final Long2ObjectMap<Direction> BY_NORMAL = Arrays.stream(Direction.values()).collect(Collectors.toMap((p_235679_) -> {
        return (new BlockPos(p_235679_.getNormal())).asLong();
    }, (p_235675_) -> {
        return p_235675_;
    }, (p_235670_, p_235671_) -> {
        throw new IllegalArgumentException("Duplicate keys");
    }, Long2ObjectOpenHashMap::new));

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
        super(Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(BLOCK_HARDNESS, RESISTANCE));
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(VARIANT, RackType.EMPTY));
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
        if (context.getPlayer() != null)
        {
            return defaultBlockState().setValue(FACING, context.getPlayer().getDirection().getOpposite());
        }
        return super.getStateForPlacement(context);
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
    @NotNull
    public BlockState updateShape(
      @NotNull final BlockState state,
      @NotNull final Direction dir,
      final BlockState neighbourState,
      @NotNull final LevelAccessor level,
      @NotNull final BlockPos pos,
      @NotNull final BlockPos neighbourPos)
    {
        if (state.getBlock() != this || pos.subtract(neighbourPos).getY() != 0)
        {
            return super.updateShape(state, dir, neighbourState, level, pos, neighbourPos);
        }

        if (neighbourState.getBlock() != this)
        {
            // Reset to single
            if (state.getValue(VARIANT).isDoubleVariant() && pos.relative(state.getValue(FACING)).equals(neighbourPos))
            {
                return state.setValue(VARIANT, ((TileEntityRack) level.getBlockEntity(pos)).isEmpty() ? RackType.EMPTY : RackType.FULL);
            }

            return super.updateShape(state, dir, neighbourState, level, pos, neighbourPos);
        }

        // Connect two
        if (!state.getValue(VARIANT).isDoubleVariant() && !neighbourState.getValue(VARIANT).isDoubleVariant())
        {
            final BlockEntity here = level.getBlockEntity(pos);
            final BlockEntity neighbour = level.getBlockEntity(neighbourPos);

            if (!(here instanceof TileEntityRack) || !(neighbour instanceof TileEntityRack))
            {
                return super.updateShape(state, dir, neighbourState, level, pos, neighbourPos);
            }

            boolean isEmpty = ((TileEntityRack) here).isEmpty() && ((TileEntityRack) neighbour).isEmpty();

            level.setBlock(neighbourPos,
              neighbourState.setValue(FACING, BY_NORMAL.get(neighbourPos.subtract(pos).asLong()).getOpposite()).setValue(VARIANT, RackType.NO_RENDER),
              1);
            return state.setValue(VARIANT, isEmpty ? RackType.EMPTY_DOUBLE : RackType.FULL_DOUBLE)
                     .setValue(FACING, BY_NORMAL.get(neighbourPos.subtract(pos).asLong()));
        }

        // Validate double variant
        if (state.getValue(VARIANT).isDoubleVariant() && pos.relative(state.getValue(FACING)).equals(neighbourPos))
        {
            if (!neighbourState.getValue(FACING).equals(state.getValue(FACING).getOpposite()) || !neighbourState.getValue(VARIANT).isDoubleVariant())
            {
                return state.setValue(VARIANT, ((TileEntityRack) level.getBlockEntity(pos)).isEmpty() ? RackType.EMPTY : RackType.FULL);
            }

            if (neighbourState.getValue(VARIANT) != RackType.NO_RENDER && state.getValue(VARIANT) != RackType.NO_RENDER)
            {
                return state.setValue(VARIANT, RackType.NO_RENDER);
            }
        }

        return super.updateShape(state, dir, neighbourState, level, pos, neighbourPos);
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
    public ItemInteractionResult useItemOn(
      final ItemStack p_316304_,
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
                ((ServerPlayer) player).openMenu(rack,
                  buf -> buf.writeBlockPos(rack.getBlockPos()).writeBlockPos(rack.getOtherChest() == null ? BlockPos.ZERO : rack.getOtherChest().getBlockPos()));
            }
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.FAIL;
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
    public List<ItemStack> getDrops(final BlockState state, final LootParams.Builder builder)
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

    @Override
    public @NotNull Collection<IMateriallyTexturedBlockComponent> getComponents()
    {
        return Collections.emptyList();
    }

    @Override
    public void buildRecipes(final RecipeOutput recipeOutput)
    {
        // noop, for DO blocks only        
    }
}
