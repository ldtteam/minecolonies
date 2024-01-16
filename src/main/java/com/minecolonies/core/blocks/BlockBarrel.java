package com.minecolonies.core.blocks;

import com.minecolonies.api.blocks.AbstractBlockBarrel;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.blocks.types.BarrelType;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.tileentities.TileEntityBarrel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

public class BlockBarrel extends AbstractBlockBarrel<BlockBarrel> implements EntityBlock
{
    /**
     * The hardness this block has.
     */
    private static final float  BLOCK_HARDNESS = 5F;
    /**
     * This blocks name.
     */
    private static final String BLOCK_NAME     = "barrel_block";
    /**
     * The resistance this block has.
     */
    private static final float  RESISTANCE     = 1F;

    public BlockBarrel()
    {
        super(Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(BLOCK_HARDNESS, RESISTANCE));
        this.registerDefaultState(this.defaultBlockState().setValue(AbstractBlockBarrel.FACING, Direction.NORTH).setValue(VARIANT, BarrelType.ZERO));
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return new ResourceLocation(Constants.MOD_ID, BLOCK_NAME);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(AbstractBlockBarrel.FACING, VARIANT);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull final BlockPos blockPos, @NotNull final BlockState blockState)
    {
        return new TileEntityBarrel(blockPos, blockState);
    }

    @NotNull
    @Override
    public InteractionResult use(
      final BlockState state,
      final Level worldIn,
      final BlockPos pos,
      final Player player,
      final InteractionHand hand,
      final BlockHitResult ray)
    {
        final ItemStack itemstack = player.getInventory().getSelected();
        final BlockEntity te = worldIn.getBlockEntity(pos);
        if (te instanceof TileEntityBarrel && !worldIn.isClientSide)
        {
            ((TileEntityBarrel) te).useBarrel(player, itemstack, ray.getDirection());
            ((TileEntityBarrel) te).updateBlock(worldIn);
        }

        return InteractionResult.SUCCESS;
    }

    @NotNull
    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos, final CollisionContext context)
    {
        return Shapes.box(0, 0, 0, 1, 1.5, 1);
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
        return state.setValue(AbstractBlockBarrel.FACING, rot.rotate(state.getValue(AbstractBlockBarrel.FACING)));
    }

    /**
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
    @NotNull
    @Override
    @Deprecated
    public BlockState mirror(@NotNull final BlockState state, final Mirror mirrorIn)
    {
        return state.rotate(mirrorIn.getRotation(state.getValue(AbstractBlockBarrel.FACING)));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        return super.getStateForPlacement(context).setValue(AbstractBlockBarrel.FACING, context.getHorizontalDirection());
    }

    @Override
    public boolean canSurvive(final BlockState state, final LevelReader worldIn, final BlockPos pos)
    {
        return !worldIn.isEmptyBlock(pos.below())
                 && worldIn.getBlockState(pos.below()).getBlock() != ModBlocks.blockBarrel;
    }
}
