package com.minecolonies.api.blocks;

import com.ldtteam.structurize.blockentities.interfaces.*;
import com.ldtteam.structurize.blocks.interfaces.IAnchorBlock;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.blocks.interfaces.ITickableBlockMinecolonies;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.IAssignsJob;
import com.minecolonies.api.colony.buildings.modules.IBuildingModule;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.entity.ai.citizen.builder.IBuilderUndestroyable;
import com.minecolonies.api.items.ItemBlockHut;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TranslationConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider.*;
import static com.minecolonies.api.util.constant.BuildingConstants.DEACTIVATED;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.colony.IColony.CLOSE_COLONY_CAP;

/**
 * Abstract class for all minecolonies blocks.
 * <p>
 * The method {@link AbstractBlockHut#getName()} is abstract.
 * <p>
 * All AbstractBlockHut[something] should extend this class.
 */
@SuppressWarnings("PMD.ExcessiveImports")
public abstract class AbstractBlockHut<B extends AbstractBlockHut<B>> extends AbstractBlockMinecolonies<B> implements IBuilderUndestroyable,
                                                                                                                        IAnchorBlock,
                                                                                                                        ITickableBlockMinecolonies,
                                                                                                                        INamedBlueprintAnchorBlock,
                                                                                                                        ILeveledBlueprintAnchorBlock,
                                                                                                                        IRequirementsBlueprintAnchorBlock,
                                                                                                                        ISpecialPasteBlueprintAnchorBlock,
                                                                                                                        IInvisibleBlueprintAnchorBlock

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
    public AbstractBlockHut()
    {
        super(Properties.of(Material.WOOD).strength(HARDNESS, RESISTANCE).noOcclusion());
        setRegistryName(getHutName());
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
        this.name = getHutName();
    }

    @Override
    public float getDestroyProgress(final BlockState state, @NotNull final Player player, @NotNull final BlockGetter world, @NotNull final BlockPos pos)
    {
        final IBuilding building = IColonyManager.getInstance().getBuilding(player.level, pos);
        if (building != null && !building.getChildren().isEmpty() && (player.level.getGameTime() - lastBreakTickWarn) < 100)
        {
            lastBreakTickWarn = player.level.getGameTime();
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
    public AbstractBlockHut(final Properties properties)
    {
        super(properties.noOcclusion());
        setRegistryName(getHutName());
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
        final TileEntityColonyBuilding building = (TileEntityColonyBuilding) MinecoloniesTileEntities.BUILDING.create(blockPos, blockState);
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

            final IColonyTagCapability cap = worldIn.getChunkAt(pos).getCapability(CLOSE_COLONY_CAP, null).resolve().orElse(null);
            final BlockEntity entity = worldIn.getBlockEntity(pos);
            if (entity instanceof final TileEntityColonyBuilding te && te.getPositionedTags().containsKey(BlockPos.ZERO) && te.getPositionedTags().get(BlockPos.ZERO).contains(DEACTIVATED))
            {
                if (building == null && cap.getOwningColony() == 0)
                {
                    MessageUtils.format(MISSING_COLONY).sendTo(player);
                    return InteractionResult.FAIL;
                }

                if (building == null && !cap.getAllClaimingBuildings().values().contains(pos))
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
                colony.getProgressManager().progressBuildingPlacement(this);
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
    }

    /**
     * Event-Handler for placement of this block.
     * <p>
     * Override for custom logic.
     *
     * @param worldIn the word we are in.
     * @param pos     the position where the block was placed.
     * @param state   the state the placed block is in.
     * @param placer  the player placing the block.
     * @param stack   the itemstack from where the block was placed.
     * @param mirror  the mirror used.
     * @param style   the style of the building
     */
    public void onBlockPlacedByBuildTool(@NotNull final Level worldIn, @NotNull final BlockPos pos, final BlockState state, final LivingEntity placer, final ItemStack stack, final boolean mirror, final String style)
    {
        final BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        if (tileEntity instanceof AbstractTileEntityColonyBuilding)
        {
            ((AbstractTileEntityColonyBuilding) tileEntity).setMirror(mirror);
            ((AbstractTileEntityColonyBuilding) tileEntity).setStyle(style);
        }

        setPlacedBy(worldIn, pos, state, placer, stack);
    }

    @Override
    public void registerBlockItem(final IForgeRegistry<Item> registry, final net.minecraft.world.item.Item.Properties properties)
    {
        registry.register((new ItemBlockHut(this, properties)).setRegistryName(this.getRegistryName()));
    }

    @Override
    public boolean isVisible(@Nullable final CompoundTag beData)
    {
        final Map<BlockPos, List<String>> data = readTagPosMapFrom(beData.getCompound(TAG_BLUEPRINTDATA));
        return !data.getOrDefault(BlockPos.ZERO, new ArrayList<>()).contains("invisible");
    }

    @Override
    public List<MutableComponent> getRequirements(final ClientLevel level, final BlockPos pos, final LocalPlayer player)
    {
        final List<MutableComponent> requirements = new ArrayList<>();
        final IColonyView colonyView = IColonyManager.getInstance().getClosestColonyView(level, pos);
        if (InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(player.getInventory()), this) == -1)
        {
            requirements.add(new TranslatableComponent("com.minecolonies.coremod.hut.cost", new TranslatableComponent("block." + Constants.MOD_ID + "." + getHutName())).setStyle((Style.EMPTY).withColor(
              ChatFormatting.RED)));
        }
        if (colonyView == null)
        {
            requirements.add(new TranslatableComponent("com.minecolonies.coremod.hut.incolony"));
            return requirements;
        }
        final ResourceLocation effectId = colonyView.getResearchManager().getResearchEffectIdFrom(this);
        if (colonyView.getResearchManager().getResearchEffects().getEffectStrength(effectId) > 0)
        {
            return requirements;
        }
        if (MinecoloniesAPIProxy.getInstance().getGlobalResearchTree().getResearchForEffect(effectId) != null)
        {
            requirements.add(new TranslatableComponent(TranslationConstants.HUT_NEEDS_RESEARCH_TOOLTIP_1, getName()));
            requirements.add(new TranslatableComponent(TranslationConstants.HUT_NEEDS_RESEARCH_TOOLTIP_2, getName()));
        }

        return requirements;
    }

    @Override
    public boolean areRequirementsMet(final ClientLevel level, final BlockPos pos, final LocalPlayer player)
    {
        return this.getRequirements(level, pos, player).isEmpty();
    }

    @Override
    public List<MutableComponent> getDesc()
    {
        final List<MutableComponent> desc = new ArrayList<>();
        desc.add(new TranslatableComponent(getBuildingEntry().getTranslationKey() + ".desc"));
        return desc;
    }

    @Override
    public Component getBlueprintDisplayName()
    {
        return new TranslatableComponent("block." + Constants.MOD_ID + "." + getHutName());
    }

    @Override
    public int getLevel(final CompoundTag beData)
    {
        if (beData == null)
        {
            return 0;
        }

        try
        {
            return Integer.parseInt(beData.getCompound(TAG_BLUEPRINTDATA).getString(TAG_SCHEMATIC_NAME).replaceAll("[^0-9]", ""));
        }
        catch (final NumberFormatException exception)
        {
            Log.getLogger().error("Couldn't get level from hut: " + getHutName() + ". Potential corrubt blockEntity data.");
            return 0;
        }
    }

    @Override
    public void paste(
      final Blueprint blueprint,
      final Level world,
      final Player player,
      final BlockPos buildPos,
      final PlacementSettings placementSettings,
      final boolean complete,
      final BlockState blockState)
    {
       Log.getLogger().warn("MineColonies Pasting Action Ongoing");
    }

    //todo on receiving the first colony view, we set the style in the colony (if finished loading, else delay for later).
    //todo supplycamp/ship just search all styles -> New fully custom UI just for those, no more old UI, no more mixing! (no shared variables!)
    //todo we need a mapping that allows buildings to recover their blueprint paths (now based on StylePack name + subpath + filename)

}
