package com.minecolonies.api.blocks;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.blocks.interfaces.ITickableBlockMinecolonies;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.entity.ai.workers.util.IBuilderUndestroyable;
import com.minecolonies.api.items.ItemBlockHut;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.minecolonies.core.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static com.minecolonies.api.util.constant.BuildingConstants.DEACTIVATED;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Base class for all blocks that have a functionality within a colony. This applies to both buildings as well as functional blocks like postbox/stash.
 */
@SuppressWarnings("PMD.ExcessiveImports")
public abstract class AbstractColonyBlock<B extends AbstractColonyBlock<B>> extends AbstractBlockMinecolonies<B> implements IBuilderUndestroyable, ITickableBlockMinecolonies

{
    /**
     * Hardness factor of the pvp mode.
     */
    private static final int HARDNESS_PVP_FACTOR = 4;

    /**
     * The direction the block is facing.
     */
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    /**
     * The default hardness.
     */
    public static final float HARDNESS = 10F;

    /**
     * The default resistance (against explosions).
     */
    public static final float RESISTANCE = Float.POSITIVE_INFINITY;

    /**
     * Smaller shape.
     */
    private static final VoxelShape SHAPE = Shapes.box(0.1, 0.1, 0.1, 0.9, 0.9, 0.9);

    /**
     * The hut's lower-case building-registry-compatible name.
     */
    private final String name;

    /**
     * The timepoint of the last chat warning message
     */
    private long lastBreakTickWarn = 0;

    /**
     * Constructor for a hut block.
     * <p>
     * Registers the block, sets the creative tab, as well as the resistance and the hardness.
     */
    public AbstractColonyBlock()
    {
        super(Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(HARDNESS, RESISTANCE).noOcclusion());
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
        this.name = getHutName();
    }

    @Override
    public float getDestroyProgress(final BlockState state, @NotNull final Player player, @NotNull final BlockGetter world, @NotNull final BlockPos pos)
    {
        final IBuilding building = IColonyManager.getInstance().getBuilding(player.level(), pos);
        if (building != null && !building.getChildren().isEmpty() && (player.level().getGameTime() - lastBreakTickWarn) < 100)
        {
            lastBreakTickWarn = player.level().getGameTime();
            MessageUtils.format(HUT_BREAK_WARNING_CHILD_BUILDINGS).sendTo(player);
        }

        return (MinecoloniesAPIProxy.getInstance().getConfig().getServer().pvp_mode.get() ? 1 / (HARDNESS * HARDNESS_PVP_FACTOR) : 1 / HARDNESS) / 30;
    }

    /**
     * Constructor for a hut block.
     * <p>
     * Registers the block, sets the creative tab, as well as the resistance and the hardness.
     *
     * @param properties custom properties.
     */
    public AbstractColonyBlock(final Properties properties)
    {
        super(properties.noOcclusion());
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
        this.name = getHutName();
    }

    /**
     * Method to return the name of the block.
     *
     * @return Name of the block.
     */
    public abstract String getHutName();

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull final BlockPos blockPos, @NotNull final BlockState blockState)
    {
        final TileEntityColonyBuilding building = (TileEntityColonyBuilding) MinecoloniesTileEntities.BUILDING.get().create(blockPos, blockState);
        building.registryName = this.getBuildingEntry().getRegistryName();
        return building;
    }

    /**
     * Method to get the building registry entry.
     *
     * @return The building entry.
     */
    public abstract BuildingEntry getBuildingEntry();

    @NotNull
    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos, final CollisionContext context)
    {
        return SHAPE;
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
       /*
        If the world is client, open the gui of the building
         */
        if (worldIn.isClientSide)
        {
            if (hand == InteractionHand.OFF_HAND)
            {
                return InteractionResult.FAIL;
            }

            @Nullable final IBuildingView building = IColonyManager.getInstance().getBuildingView(worldIn.dimension(), pos);
            final LevelChunk chunk = worldIn.getChunkAt(pos);
            final BlockEntity entity = worldIn.getBlockEntity(pos);
            if (entity instanceof final TileEntityColonyBuilding te && te.getPositionedTags().containsKey(BlockPos.ZERO) && te.getPositionedTags().get(BlockPos.ZERO).contains(DEACTIVATED))
            {
                if (building == null && ColonyUtils.getOwningColony(chunk) == 0)
                {
                    MessageUtils.format(MISSING_COLONY).sendTo(player);
                    return InteractionResult.FAIL;
                }

                if (building == null && ColonyUtils.getAllClaimingBuildings(chunk).values().stream().flatMap(Collection::stream).noneMatch(p -> p.equals(pos)))
                {
                    IColonyManager.getInstance().openReactivationWindow(pos);
                    return InteractionResult.SUCCESS;
                }
            }

            if (building == null)
            {
                MessageUtils.format(HUT_BLOCK_MISSING_BUILDING).sendTo(player);
                return InteractionResult.FAIL;
            }

            if (building.getColony() == null)
            {
                MessageUtils.format(HUT_BLOCK_MISSING_COLONY).sendTo(player);
                return InteractionResult.FAIL;
            }

            if (!building.getColony().getPermissions().hasPermission(player, Action.ACCESS_HUTS))
            {
                MessageUtils.format(PERMISSION_DENIED).sendTo(player);
                return InteractionResult.FAIL;
            }

            building.openGui(player.isShiftKeyDown());
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        @NotNull final Direction facing = (context.getPlayer() == null) ? Direction.NORTH : Direction.fromYRot(context.getPlayer().getYRot());
        return this.defaultBlockState().setValue(FACING, facing);
    }

    @NotNull
    @Override
    public BlockState rotate(final BlockState state, final Rotation rot)
    {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public void setPlacedBy(@NotNull final Level worldIn, @NotNull final BlockPos pos, final BlockState state, final LivingEntity placer, final ItemStack stack)
    {
        super.setPlacedBy(worldIn, pos, state, placer, stack);

        /*
        Only work on server side
        */
        if (worldIn.isClientSide)
        {
            return;
        }

        final BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        if (tileEntity instanceof TileEntityColonyBuilding)
        {
            @NotNull final TileEntityColonyBuilding hut = (TileEntityColonyBuilding) tileEntity;
            if (hut.getBuildingName() != getBuildingEntry().getRegistryName())
            {
                hut.registryName = getBuildingEntry().getRegistryName();
            }
            @Nullable final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(worldIn, hut.getPosition());

            if (colony != null)
            {
                colony.getBuildingManager().addNewBuilding(hut, worldIn);
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
    }

    /**
     * Get the registry name frm the blck hut.
     * @return the key.
     */
    public ResourceLocation getRegistryName()
    {
        return new ResourceLocation(Constants.MOD_ID, getHutName());
    }

    @Override
    public B registerBlock(final IForgeRegistry<Block> registry)
    {
        registry.register(getRegistryName(), this);
        return (B) this;
    }

    @Override
    public void registerBlockItem(final IForgeRegistry<Item> registry, final Item.Properties properties)
    {
        registry.register(getRegistryName(), new ItemBlockHut(this, properties));
    }
}
