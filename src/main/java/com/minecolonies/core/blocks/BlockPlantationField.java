package com.minecolonies.core.blocks;

import com.ldtteam.structurize.blocks.interfaces.IAnchorBlock;
import com.minecolonies.api.blocks.AbstractBlockMinecoloniesHorizontal;
import com.minecolonies.api.blocks.interfaces.IBuildingBrowsableBlock;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.fields.registry.FieldRegistries;
import com.minecolonies.api.entity.ai.citizen.builder.IBuilderUndestroyable;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.WindowPlantationField;
import com.minecolonies.core.colony.fields.PlantationField;
import com.minecolonies.core.tileentities.TileEntityPlantationField;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Block class for the plantation field block.
 */
public class BlockPlantationField extends AbstractBlockMinecoloniesHorizontal<BlockPlantationField> implements IBuilderUndestroyable, IAnchorBlock, IBuildingBrowsableBlock, EntityBlock
{
    /**
     * If the block is mirrored.
     */
    public static final BooleanProperty MIRROR = BooleanProperty.create("mirror");

    /**
     * The hardness this block has.
     */
    private static final float BLOCK_HARDNESS = 5F;

    /**
     * This blocks name.
     */
    private static final String BLOCK_NAME = "blockhutplantationfield";

    /**
     * The resistance this block has.
     */
    private static final float RESISTANCE = 1F;

    /**
     * Cached list of shapes
     */
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    /**
     * Default constructor.
     */
    public BlockPlantationField()
    {
        super(Properties.of(Material.WOOD).strength(BLOCK_HARDNESS, RESISTANCE));
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(MIRROR, false));
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return new ResourceLocation(Constants.MOD_ID, BLOCK_NAME);
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull final BlockPos blockPos, @NotNull final BlockState blockState)
    {
        return new TileEntityPlantationField(blockPos, blockState);
    }

    @NotNull
    @Override
    public BlockState rotate(@NotNull BlockState state, Rotation rot)
    {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @NotNull
    @Override
    public BlockState mirror(@NotNull BlockState state, Mirror mirrorIn)
    {
        return state.setValue(MIRROR, mirrorIn != Mirror.NONE);
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
        // If this is the client side, open the plantation field GUI
        if (worldIn.isClientSide)
        {
            if (hand == InteractionHand.OFF_HAND)
            {
                return InteractionResult.FAIL;
            }

            final BlockEntity tileEntity = worldIn.getBlockEntity(pos);
            if (tileEntity instanceof TileEntityPlantationField plantationField)
            {
                new WindowPlantationField(plantationField).open();
                return InteractionResult.SUCCESS;
            }

            return InteractionResult.FAIL;
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos, final CollisionContext context)
    {
        Direction dir = state.getValue(FACING);
        if (SHAPES.containsKey(dir))
        {
            return SHAPES.get(dir);
        }
        VoxelShape shape = Shapes.box(
          0D + (dir.getStepX() > 0 ? 0.5 : 0),
          0D,
          0D + (dir.getStepZ() > 0 ? 0.5 : 0),
          1D - (dir.getStepX() < 0 ? 0.5 : 0),
          0.625D,
          1D - (dir.getStepZ() < 0 ? 0.5 : 0)
        );
        SHAPES.put(dir, shape);
        return shape;
    }

    @Override
    public void wasExploded(final Level worldIn, final BlockPos pos, final Explosion explosionIn)
    {
        notifyColonyAboutDestruction(worldIn, pos);
        super.wasExploded(worldIn, pos, explosionIn);
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        return super.getStateForPlacement(context).setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    public void setPlacedBy(@NotNull final Level worldIn, @NotNull final BlockPos pos, final BlockState state, final LivingEntity placer, final ItemStack stack)
    {
        super.setPlacedBy(worldIn, pos, state, placer, stack);

        if (worldIn.isClientSide)
        {
            return;
        }

        final BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        if (tileEntity instanceof TileEntityPlantationField tileEntityPlantationField)
        {
            final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(worldIn, pos);
            if (colony != null)
            {
                for (FieldRegistries.FieldEntry plantationFieldType : tileEntityPlantationField.getPlantationFieldTypes())
                {
                    final PlantationField plantationField = PlantationField.create(plantationFieldType, pos);

                    final List<BlockPos> workingPositions = tileEntityPlantationField.getWorkingPositions(plantationField.getModule().getWorkTag());
                    final List<BlockPos> validPositions = plantationField.getModule().getValidWorkingPositions(worldIn, workingPositions);
                    if (!validPositions.isEmpty())
                    {
                        plantationField.setWorkingPositions(validPositions);
                        colony.getBuildingManager().addField(plantationField);
                        colony.getBuildingManager().addLeisureSite(pos);
                    }
                }
            }
        }
    }

    @Override
    public void playerWillDestroy(final Level worldIn, @NotNull final BlockPos pos, final BlockState state, @NotNull final Player player)
    {
        notifyColonyAboutDestruction(worldIn, pos);
        super.playerWillDestroy(worldIn, pos, state, player);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, MIRROR);
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
                final BlockEntity blockEntity = worldIn.getBlockEntity(pos);
                if (blockEntity instanceof TileEntityPlantationField plantationField)
                {
                    for (FieldRegistries.FieldEntry plantationFieldType : plantationField.getPlantationFieldTypes())
                    {
                        colony.getBuildingManager().removeField(field -> field.getFieldType().equals(plantationFieldType) && field.getPosition().equals(pos));
                        colony.getBuildingManager().removeLeisureSite(pos);
                    }
                }
            }
        }
    }
}