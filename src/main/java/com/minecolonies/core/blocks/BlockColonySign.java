package com.minecolonies.core.blocks;

import com.minecolonies.api.blocks.interfaces.IMinecoloniesTickableBlock;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.items.component.BuildingId;
import com.minecolonies.api.items.component.ColonyId;
import com.minecolonies.core.tileentities.TileEntityColonySign;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * Creates a colony sign block.
 */
public class BlockColonySign extends HorizontalDirectionalBlock implements IMinecoloniesTickableBlock
{
    public static final MapCodec<BlockColonySign> CODEC = simpleCodec(BlockColonySign::new);

    /**
     * Property if it's a sign of two connected colonies or not.
     */
    public static final BooleanProperty CONNECTED = BooleanProperty.create("connected");

    /**
     * Smaller shape.
     */
    private static final VoxelShape SHAPE = Shapes.box(0.1, 0.1, 0.1, 0.9, 0.9, 0.9);

    public BlockColonySign(final Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(CONNECTED, false));
    }

    @NotNull
    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos, final CollisionContext context)
    {
        return SHAPE;
    }

    @Override
    public void setPlacedBy(@NotNull final Level worldIn, @NotNull final BlockPos pos, final BlockState state, final LivingEntity placer, final ItemStack stack)
    {
        if (worldIn.isClientSide)
        {
            super.setPlacedBy(worldIn, pos, state, placer, stack);
            return;
        }

        final TileEntityColonySign tileEntityColonySign = (TileEntityColonySign) worldIn.getBlockEntity(pos);
        final ColonyId colonyComponent = ColonyId.readFromItemStack(stack);
        if (!colonyComponent.hasColonyId())
        {
            return;
        }
        final BuildingId buildingId = BuildingId.readFromItemStack(stack);

        final int colonyId = colonyComponent.id();
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyId, worldIn.dimension());
        tileEntityColonySign.setColonyAndAnchor(colony, buildingId.hasId() ? buildingId.id() : null);
        super.setPlacedBy(worldIn, pos, state, placer, stack);
    }

    @Override
    public void destroy(final LevelAccessor level, final BlockPos pos, final BlockState state)
    {
        super.destroy(level, pos, state);
    }

    @Override
    public void onRemove(final BlockState currentState, final Level level, final BlockPos pos, final BlockState p_60518_, final boolean p_60519_)
    {
        BlockEntity tileEntity = level.getBlockEntity(pos);
        if (!level.isClientSide && tileEntity instanceof TileEntityColonySign tileEntityColonySign)
        {
            final IColony colony = IColonyManager.getInstance().getColonyByDimension(tileEntityColonySign.getColonyId(), level.dimension());
            if (colony != null)
            {
                colony.getConnectionManager().removeConnectionNode(pos);
            }
        }
        super.onRemove(currentState, level, pos, p_60518_, p_60519_);
    }

    @Override
    public RenderShape getRenderShape(final BlockState p_60550_)
    {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(HorizontalDirectionalBlock.FACING, CONNECTED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos blockPos, final BlockState blockState)
    {
        return new TileEntityColonySign(blockPos, blockState);
    }

    @Override
    @NotNull
    protected MapCodec<? extends HorizontalDirectionalBlock> codec()
    {
        return CODEC;
    }
}
