package com.minecolonies.core.blocks;

import com.minecolonies.api.blocks.huts.AbstractBlockMinecoloniesDefault;
import com.minecolonies.api.blocks.interfaces.IBuildingBrowsableBlock;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.fields.registry.FieldRegistries;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.containers.WindowField;
import com.minecolonies.core.colony.fields.FarmField;
import com.minecolonies.core.tileentities.TileEntityScarecrow;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The net.minecraft.core.Directions, placement and activation.
 */
@SuppressWarnings("PMD.ExcessiveImports")
public class BlockScarecrow extends AbstractBlockMinecoloniesDefault<BlockScarecrow> implements EntityBlock, IBuildingBrowsableBlock
{
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    /**
     * Constructor called on block placement.
     */
    public BlockScarecrow()
    {
        super(Properties.of(Material.WOOD).strength(HARDNESS, RESISTANCE));
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return new ResourceLocation(Constants.MOD_ID, REGISTRY_NAME);
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
    public InteractionResult use(
      final BlockState state,
      final Level worldIn,
      final BlockPos pos,
      final Player player,
      final InteractionHand hand,
      final BlockHitResult ray)
    {
        // If the world is client, open the inventory of the field.
        if (worldIn.isClientSide)
        {
            // Get the entity of the bottom half
            DoubleBlockHalf half = state.getValue(HALF);
            final BlockEntity entity = worldIn.getBlockEntity(half == DoubleBlockHalf.UPPER ? pos.below() : pos);

            if (entity instanceof TileEntityScarecrow scarecrow)
            {
                new WindowField(scarecrow).open();
                return InteractionResult.SUCCESS;
            }
            else
            {
                return InteractionResult.FAIL;
            }
        }

        // This must succeed in Remote to stop more right click interactions like placing blocks
        return InteractionResult.SUCCESS;
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

        if (context.getClickedPos().getY() < 255 && context.getLevel().getBlockState(context.getClickedPos().above()).canBeReplaced(context))
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
            colony.getBuildingManager().addField(FarmField.create(pos));
        }
    }

    @Override
    public void playerWillDestroy(final Level worldIn, @NotNull final BlockPos pos, final BlockState state, @NotNull final Player player)
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
        super.playerWillDestroy(worldIn, pos, state, player);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(HALF, FACING);
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
            final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(worldIn, pos);
            if (colony != null)
            {
                colony.getBuildingManager().removeField(field -> field.getFieldType().equals(FieldRegistries.farmField.get()) && field.getPosition().equals(pos));
            }
        }
    }
}
