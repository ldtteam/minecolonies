package com.minecolonies.coremod.blocks;

import com.ldtteam.structurize.blocks.interfaces.IAnchorBlock;
import com.minecolonies.api.blocks.AbstractBlockMinecoloniesHorizontal;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.workerbuildings.fields.FieldRecord;
import com.minecolonies.api.colony.buildings.workerbuildings.fields.FieldType;
import com.minecolonies.api.colony.buildings.workerbuildings.plantation.PlantationFieldType;
import com.minecolonies.coremod.client.gui.WindowPlantationField;
import com.minecolonies.coremod.colony.buildings.workerbuildings.fields.PlantationField;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.PlantationModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.PlantationModuleRegistry;
import com.minecolonies.coremod.tileentities.TileEntityPlantationField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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

import java.util.List;

/**
 * Block class for the plantation field block.
 */
public class BlockPlantationField extends AbstractBlockMinecoloniesHorizontal<BlockPlantationField> implements IAnchorBlock, EntityBlock
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
     * Default constructor.
     */
    public BlockPlantationField()
    {
        super(Properties.of(Material.WOOD).strength(BLOCK_HARDNESS, RESISTANCE));
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(MIRROR, false));
        setRegistryName(BLOCK_NAME);
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull final BlockPos blockPos, @NotNull final BlockState blockState)
    {
        return new TileEntityPlantationField(blockPos, blockState);
    }

    @NotNull
    @Override
    public BlockState rotate(BlockState state, Rotation rot)
    {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @NotNull
    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn)
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
            if (tileEntity instanceof TileEntityPlantationField plantationField && plantationField.canOpenMenu(player))
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
        // Force the different halves to share the same collision space,
        // the user will think it is one big block
        Direction dir = state.getValue(FACING);
        return Shapes.box(
          0D + (dir.getStepX() > 0 ? 0.5 : 0),
          0D,
          0D + (dir.getStepZ() > 0 ? 0.5 : 0),
          1D - (dir.getStepX() < 0 ? 0.5 : 0),
          0.625D,
          1D - (dir.getStepZ() < 0 ? 0.5 : 0)
        );
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
        if (tileEntity instanceof TileEntityPlantationField plantationField)
        {
            final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(worldIn, pos);
            if (colony != null)
            {
                for (PlantationFieldType plantationFieldType : plantationField.getPlantationFieldTypes())
                {
                    PlantationModule module = PlantationModuleRegistry.getPlantationModule(plantationFieldType);
                    if (module != null)
                    {
                        final List<BlockPos> validWorkingPositions = module.getValidWorkingPositions(worldIn, plantationField.getWorkingPositions(module.getWorkTag()));
                        PlantationField field = new PlantationField(colony, pos, plantationFieldType, module.getItem(), validWorkingPositions);
                        colony.getBuildingManager().addOrUpdateField(field);
                    }
                }
            }
        }
    }

    @Override
    public void playerWillDestroy(final Level worldIn, @NotNull final BlockPos pos, final BlockState state, @NotNull final Player player)
    {
        final BlockEntity blockEntity = worldIn.getBlockEntity(pos);
        if (blockEntity instanceof TileEntityPlantationField plantationField)
        {
            for (PlantationFieldType plantationFieldType : plantationField.getPlantationFieldTypes())
            {
                PlantationModule module = PlantationModuleRegistry.getPlantationModule(plantationFieldType);
                if (module != null)
                {
                    notifyColonyAboutDestruction(worldIn, new FieldRecord(pos, module.getItem()));
                }
            }
        }
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
     * @param matcher the field record matcher.
     */
    private void notifyColonyAboutDestruction(final Level worldIn, final FieldRecord matcher)
    {
        if (!worldIn.isClientSide())
        {
            final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(worldIn, matcher.position());
            if (colony != null)
            {
                colony.getBuildingManager().removeField(FieldType.PLANTATION_FIELDS, matcher);
            }
        }
    }
}


