package com.minecolonies.coremod.blocks;

import com.google.common.collect.ImmutableList;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlockComponent;
import com.ldtteam.domumornamentum.block.components.SimpleRetexturableComponent;
import com.ldtteam.domumornamentum.tag.ModTags;
import com.minecolonies.api.blocks.AbstractBlockMinecoloniesRack;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.blocks.types.RackType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.tileentities.AbstractTileEntityRack;
import com.minecolonies.api.tileentities.TileEntityRack;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.Constants;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.loot.LootParams;
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
import java.util.*;
import java.util.stream.Collectors;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

/**
 * Block for the shelves of the warehouse.
 */
public class BlockMinecoloniesRack extends AbstractBlockMinecoloniesRack<BlockMinecoloniesRack>  implements IMateriallyTexturedBlock
{
    /**
     * Normal translation we use.
     */
    private static final  Long2ObjectMap<Direction> BY_NORMAL      = Arrays.stream(Direction.values()).collect(Collectors.toMap((p_235679_) -> {
        return (new BlockPos(p_235679_.getNormal())).asLong();
    }, (p_235675_) -> {
        return p_235675_;
    }, (p_235670_, p_235671_) -> {
        throw new IllegalArgumentException("Duplicate keys");
    }, Long2ObjectOpenHashMap::new));

    public static final List<IMateriallyTexturedBlockComponent> COMPONENTS = ImmutableList.<IMateriallyTexturedBlockComponent>builder()
                                                                               .add(new SimpleRetexturableComponent(new ResourceLocation("block/bricks"), ModTags.FRAMED_LIGHT_CENTER, Blocks.BRICKS))
                                                                               .add(new SimpleRetexturableComponent(new ResourceLocation("block/sand"), ModTags.TIMBERFRAMES_FRAME, Blocks.SAND))
                                                                               .add(new SimpleRetexturableComponent(new ResourceLocation("block/orange_wool"), ModTags.FRAMED_LIGHT_CENTER, Blocks.ORANGE_WOOL))
                                                                               .add(new SimpleRetexturableComponent(new ResourceLocation("block/dirt"), ModTags.FRAMED_LIGHT_CENTER, Blocks.DIRT))
                                                                               .add(new SimpleRetexturableComponent(new ResourceLocation("block/obsidian"), ModTags.FRAMED_LIGHT_CENTER, Blocks.OBSIDIAN))
                                                                               .add(new SimpleRetexturableComponent(new ResourceLocation("block/polished_andesite"), ModTags.FRAMED_LIGHT_CENTER, Blocks.POLISHED_ANDESITE))
                                                                               .add(new SimpleRetexturableComponent(new ResourceLocation("block/andesite"), ModTags.FRAMED_LIGHT_CENTER, Blocks.ANDESITE))
                                                                               .build();

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
        final BlockPos pos = context.getClickedPos();
        final BlockState state = defaultBlockState();

        for(Direction direction : UPDATE_SHAPE_ORDER)
        {
            if (direction != Direction.DOWN && direction != Direction.UP)
            {
                final BlockState relativeState = context.getLevel().getBlockState(pos.relative(direction));
                if (relativeState.getBlock() == ModBlocks.blockRack && !relativeState.getValue(VARIANT).isDoubleVariant())
                {
                    return state.setValue(VARIANT, RackType.EMPTYAIR).setValue(FACING, direction);
                }
            }
        }

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
      final BlockState state2,
      @NotNull final LevelAccessor level,
      @NotNull final BlockPos pos1,
      @NotNull final BlockPos pos2)
    {
        final BlockEntity bEntity1 = level.getBlockEntity(pos1);
        final BlockEntity bEntity2 = level.getBlockEntity(pos2);

        if (pos1.subtract(pos2).getY() != 0)
        {
            return super.updateShape(state, dir, state2, level, pos1, pos2);
        }

        // Is this a double chest and our connection is being removed.
        if (bEntity1 instanceof TileEntityRack)
        {
            if (bEntity2 == null && state.getValue(VARIANT).isDoubleVariant() && pos1.relative(state.getValue(FACING)).equals(pos2))
            {
                // Reset to single
                return state.setValue(VARIANT, ((TileEntityRack) bEntity1).isEmpty() ? RackType.DEFAULT : RackType.FULL);
            }
            // If its not a double variant and the new neighbor is neither, then connect.
            else if (bEntity2 instanceof TileEntityRack && !state.getValue(VARIANT).isDoubleVariant() && state2.hasProperty(VARIANT) && state2.getValue(VARIANT).isDoubleVariant() && state2.getValue(FACING).equals(BY_NORMAL.get(((pos2.subtract(pos1)).asLong())).getOpposite()))
            {
                return state.setValue(VARIANT, ((TileEntityRack) bEntity1).isEmpty() ? RackType.DEFAULTDOUBLE : RackType.FULLDOUBLE).setValue(FACING, BY_NORMAL.get(pos2.subtract(pos1).asLong()));
            }
        }
        return super.updateShape(state, dir, state2, level, pos1, pos2);
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
    public @NotNull Block getBlock()
    {
        return this;
    }

    @Override
    public @NotNull Collection<IMateriallyTexturedBlockComponent> getComponents()
    {
        return Collections.emptyList();
    }
}
