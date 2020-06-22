package com.minecolonies.coremod.blocks;

import com.minecolonies.api.blocks.huts.AbstractBlockMinecoloniesDefault;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.*;
import net.minecraftforge.fml.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.util.Direction.NORTH;
import static net.minecraft.util.Direction.fromAngle;

/**
 * The class handling the fieldBlocks, placement and activation.
 */
@SuppressWarnings("PMD.ExcessiveImports")
public class BlockScarecrow extends AbstractBlockMinecoloniesDefault<BlockScarecrow>
{
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    /**
     * Constructor called on block placement.
     */
    public BlockScarecrow()
    {
        super(Properties.create(Material.WOOD).hardnessAndResistance(HARDNESS, RESISTANCE));
        setRegistryName(REGISTRY_NAME);
        this.setDefaultState(this.getDefaultState().with(FACING, NORTH).with(HALF, DoubleBlockHalf.LOWER));
    }

    @NotNull
    @Override
    public BlockRenderType getRenderType(final BlockState state)
    {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(
      final BlockState state, final IBlockReader worldIn, final BlockPos pos, final ISelectionContext context)
    {
        // Force the different halves to share the same collision space;
        // the user will think it is one big block
        return VoxelShapes.create(
          (float) START_COLLISION,
          (float)(BOTTOM_COLLISION - (state.get(HALF) == DoubleBlockHalf.UPPER ? 1 : 0)),
          (float) START_COLLISION,
          (float) END_COLLISION,
          (float)(HEIGHT_COLLISION - (state.get(HALF) == DoubleBlockHalf.UPPER ? 1 : 0)),
          (float) END_COLLISION
        );
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new ScarecrowTileEntity();
    }

    @Override
    public ActionResultType onBlockActivated(
      final BlockState state,
      final World worldIn,
      final BlockPos pos,
      final PlayerEntity player,
      final Hand hand,
      final BlockRayTraceResult ray)
    {
        //If the world is server, open the inventory of the field.
        if (!worldIn.isRemote)
        {
            // Get the entity of the bottom half
            DoubleBlockHalf half = state.get(HALF);
            final TileEntity entity = worldIn.getTileEntity(
                half == DoubleBlockHalf.UPPER ? pos.down() : pos
            );

            if (entity instanceof ScarecrowTileEntity)
            {
                NetworkHooks.openGui((ServerPlayerEntity) player, (ScarecrowTileEntity) entity, pos);
            }
            else
            {
                return ActionResultType.FAIL;
            }
        }

        // This must succeed in Remote to stop more right click interactions like placing blocks
        return ActionResultType.SUCCESS;
    }

    @javax.annotation.Nullable
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        @NotNull final Direction Direction = (context.getPlayer() == null) ? NORTH : fromAngle(context.getPlayer().rotationYaw + 180);

        if (context.getPos().getY() < 255 && context.getWorld().getBlockState(context.getPos().up()).isReplaceable(context))
        {
            return this.getDefaultState().with(FACING, Direction).with(HALF, DoubleBlockHalf.LOWER);
        }
        else
        {
            return null;
        }
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos)
    {
        BlockPos blockpos = pos.down();
        BlockState blockstate = worldIn.getBlockState(blockpos);
        if (state.get(HALF) == DoubleBlockHalf.LOWER)
        {
            return blockstate.isSolidSide(worldIn, blockpos, Direction.UP);
        }
        else
        {
            return blockstate.getBlock() == this;
        }
    }

    @Override
    public void onBlockAdded(final BlockState state, final World worldIn, final BlockPos pos, final BlockState oldState, final boolean isMoving)
    {
        super.onBlockAdded(state, worldIn, pos, oldState, isMoving);
        if (worldIn.isRemote)
        {
            return;
        }

        @Nullable final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(worldIn, pos);
        if (colony != null)
        {
            final ScarecrowTileEntity scareCrow = (ScarecrowTileEntity) worldIn.getTileEntity(pos);
            if (scareCrow != null)
            {
                colony.getBuildingManager().addNewField(scareCrow, pos, worldIn);
            }
        }
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
    {
        worldIn.setBlockState(pos.up(), state.with(HALF, DoubleBlockHalf.UPPER), 3);
    }

    @Override
    public void onExplosionDestroy(final World worldIn, final BlockPos pos, final Explosion explosionIn)
    {
        notifyColonyAboutDestruction(worldIn, pos);
        super.onExplosionDestroy(worldIn, pos, explosionIn);
    }

    @Override
    public void onBlockHarvested(final World worldIn, @NotNull final BlockPos pos, final BlockState state, @NotNull final PlayerEntity player)
    {
        DoubleBlockHalf half = state.get(HALF);
        BlockPos otherpos = half == DoubleBlockHalf.LOWER ? pos.up() : pos.down();
        BlockState otherstate = worldIn.getBlockState(otherpos);

        // just double check the other block is also the scarecrow and not the same half,
        // then destroy it (make it air)
        if (otherstate.getBlock() == this && otherstate.get(HALF) != half)
        {
            worldIn.setBlockState(otherpos, Blocks.AIR.getDefaultState(), 35);
        }

        notifyColonyAboutDestruction(worldIn, pos);
        super.onBlockHarvested(worldIn, pos, state, player);
    }

    /**
     * Notify the colony about the destruction of the field.
     * @param worldIn the world.
     * @param pos the position.
     */
    private static void notifyColonyAboutDestruction(final IWorld worldIn, final BlockPos pos)
    {
        if (!worldIn.isRemote())
        {
            @Nullable final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld((World) worldIn, pos);
            if (colony != null)
            {
                colony.getBuildingManager().removeField(pos);
            }
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(HALF, FACING);
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return state.get(HALF) == DoubleBlockHalf.LOWER;
    }
}
