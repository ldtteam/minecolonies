package com.minecolonies.core.blocks;

import com.minecolonies.api.blocks.huts.AbstractBlockMinecoloniesDefault;
import com.minecolonies.api.blocks.interfaces.IBuildingBrowsableBlock;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildingextensions.registry.BuildingExtensionRegistries;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.containers.WindowField;
import com.minecolonies.core.colony.buildingextensions.FarmField;
import com.minecolonies.core.tileentities.TileEntityScarecrow;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The net.minecraft.core.Directions, placement and activation.
 */
@SuppressWarnings("PMD.ExcessiveImports")
public class BlockScarecrow extends AbstractBlockMinecoloniesDefault<BlockScarecrow> implements EntityBlock, IBuildingBrowsableBlock
{
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    public static final BooleanProperty LANTERN = BooleanProperty.create("lantern");

    /**
     * Constructor called on block placement.
     */
    public BlockScarecrow()
    {
        super(Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(HARDNESS, RESISTANCE));
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(HALF, DoubleBlockHalf.LOWER).setValue(LANTERN, false));
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return new ResourceLocation(Constants.MOD_ID, REGISTRY_NAME);
    }

    @Override
    public int getLightEmission(final BlockState state, final BlockGetter level, final BlockPos pos)
    {
        return state.getValue(LANTERN) ? Blocks.LANTERN.defaultBlockState().getLightEmission(level, pos) : 0;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull final BlockPos blockPos, @NotNull final BlockState blockState)
    {
        if (blockState.getValue(HALF) == DoubleBlockHalf.UPPER)
        {
            return null;
        }
        return new TileEntityScarecrow(blockPos, blockState);
    }

    @Override
    public ItemInteractionResult useItemOn(
        final ItemStack stack,
        final BlockState state,
        final Level worldIn,
        final BlockPos pos,
        final Player player,
        final InteractionHand hand,
        final BlockHitResult ray)
    {
        if (player.getItemInHand(hand).is(Items.LANTERN) && !state.getValue(LANTERN))
        {
            worldIn.setBlock(pos, state.setValue(LANTERN, true), 3);
            worldIn.playSound(player, pos, SoundEvents.LANTERN_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            InventoryUtils.reduceStackInItemHandler(new InvWrapper(player.getInventory()), player.getItemInHand(hand));
            return ItemInteractionResult.CONSUME_PARTIAL;
        }

        // If the world is client, open the inventory of the field.
        if (worldIn.isClientSide)
        {
            // Get the entity of the bottom half
            DoubleBlockHalf half = state.getValue(HALF);
            final BlockEntity entity = worldIn.getBlockEntity(half == DoubleBlockHalf.UPPER ? pos.below() : pos);

            if (entity instanceof TileEntityScarecrow scarecrow)
            {
                new WindowField(scarecrow).open();
                return ItemInteractionResult.SUCCESS;
            }
            else
            {
                return ItemInteractionResult.FAIL;
            }
        }

        final IColony iColony = IColonyManager.getInstance().getIColony(worldIn, pos);
        if (iColony != null)
        {
            iColony.getServerBuildingManager().addBuildingExtensionIfMissing(BuildingExtensionRegistries.farmField.get(), getFieldBasePos(state, pos), player);
        }
        // This must succeed in Remote to stop more right click interactions like placing blocks
        return ItemInteractionResult.SUCCESS;
    }

    @NotNull
    @Override
    public RenderShape getRenderShape(final BlockState state)
    {
        return RenderShape.INVISIBLE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos)
    {
        BlockPos blockpos = pos.below();
        BlockState blockstate = worldIn.getBlockState(blockpos);
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER)
        {
            return blockstate.isFaceSturdy(worldIn, blockpos, Direction.UP);
        }
        else
        {
            return blockstate.getBlock() == this;
        }
    }

    @Override
    public VoxelShape getShape(
      final BlockState state, final BlockGetter worldIn, final BlockPos pos, final CollisionContext context)
    {
        // Force the different halves to share the same collision space;
        // the user will think it is one big block
        return Shapes.box(
          (float) START_COLLISION,
          (float) (BOTTOM_COLLISION - (state.getValue(HALF) == DoubleBlockHalf.UPPER ? 1 : 0)),
          (float) START_COLLISION,
          (float) END_COLLISION,
          (float) (HEIGHT_COLLISION - (state.getValue(HALF) == DoubleBlockHalf.UPPER ? 1 : 0)),
          (float) END_COLLISION
        );
    }

    @Override
    public void neighborChanged(final BlockState state, final Level worldIn, final BlockPos pos, final Block block, final BlockPos fromPos, final boolean isMoving)
    {
        super.neighborChanged(state, worldIn, pos, block, fromPos, isMoving);
        final DoubleBlockHalf half = state.getValue(HALF);
        final BlockPos otherPos = half == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
        if (!fromPos.equals(otherPos))
        {
            return;
        }
        final BlockState otherState = worldIn.getBlockState(otherPos);
        if (otherState.getBlock() == this && otherState.getValue(HALF) != half && otherState.getValue(LANTERN) != state.getValue(LANTERN))
        {
            worldIn.setBlock(pos, state.setValue(LANTERN, otherState.getValue(LANTERN)), UPDATE_ALL);
        }
    }

    @Override
    public List<ItemStack> getDrops(final BlockState state, final LootParams.Builder params)
    {
        final List<ItemStack> drops = super.getDrops(state, params);
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER && state.getValue(LANTERN))
        {
            drops.add(new ItemStack(Items.LANTERN));
        }
        return drops;
    }

    @Override
    public void wasExploded(final Level worldIn, final BlockPos pos, final Explosion explosionIn)
    {
        notifyColonyAboutDestruction(worldIn, pos);
        super.wasExploded(worldIn, pos, explosionIn);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        @NotNull final Direction dir = (context.getPlayer() == null) ? Direction.NORTH : Direction.fromYRot(context.getPlayer().getYRot() + 180);

        if (context.getClickedPos().getY() < context.getLevel().getMaxBuildHeight() && context.getLevel().getBlockState(context.getClickedPos().above()).canBeReplaced(context))
        {
            return this.defaultBlockState().setValue(FACING, dir).setValue(HALF, DoubleBlockHalf.LOWER);
        }
        else
        {
            return null;
        }
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
    {
        super.setPlacedBy(worldIn, pos, state, placer, stack);
        worldIn.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);

        if (worldIn.isClientSide)
        {
            return;
        }

        final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(worldIn, pos);
        if (colony != null)
        {
            colony.getServerBuildingManager().addBuildingExtension(FarmField.create(pos, worldIn));
        }
    }

    @Override
    public BlockState playerWillDestroy(final Level worldIn, @NotNull final BlockPos pos, final BlockState state, @NotNull final Player player)
    {
        DoubleBlockHalf half = state.getValue(HALF);
        BlockPos otherpos = half == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
        BlockState otherstate = worldIn.getBlockState(otherpos);

        // just double-check the other block is also the scarecrow and not the same half,
        // then destroy it (make it air)
        if (otherstate.getBlock() == this && otherstate.getValue(HALF) != half)
        {
            worldIn.setBlock(otherpos, Blocks.AIR.defaultBlockState(), 35);
        }

        notifyColonyAboutDestruction(worldIn, pos);
        return super.playerWillDestroy(worldIn, pos, state, player);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(HALF, FACING, LANTERN);
    }

    /**
     * Notify the colony about the destruction of the field.
     *
     * @param worldIn the world.
     * @param pos     the position of the block.
     */
    private void notifyColonyAboutDestruction(final Level worldIn, final BlockPos pos)
    {
        if (!worldIn.isClientSide())
        {
            final BlockPos fieldBasePos = getFieldBasePos(worldIn.getBlockState(pos), pos);
            final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(worldIn, fieldBasePos);
            if (colony != null)
            {
                colony.getServerBuildingManager().removeBuildingExtension(field -> field.getBuildingExtensionType().equals(BuildingExtensionRegistries.farmField.get()) && field.getPosition().equals(fieldBasePos));
            }
        }
    }

    /**
     * Resolve a scarecrow block position to the lower-half block that owns the field data.
     *
     * @param state the currently interacted scarecrow state.
     * @param pos the currently interacted block position.
     * @return the lower-half block position.
     */
    private static BlockPos getFieldBasePos(final BlockState state, final BlockPos pos)
    {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
    }
}
