package com.minecolonies.api.blocks;

import com.ldtteam.structurize.blocks.interfaces.*;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.placement.structure.AbstractStructureHandler;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.blocks.interfaces.IBuildingBrowsableBlock;
import com.minecolonies.api.blocks.interfaces.ITickableBlockMinecolonies;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.entity.ai.workers.util.IBuilderUndestroyable;
import com.minecolonies.api.items.ItemBlockHut;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.minecolonies.core.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TranslationConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE.*;
import static com.minecolonies.api.util.constant.BuildingConstants.DEACTIVATED;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

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
                                                                                                                        IInvisibleBlueprintAnchorBlock,
                                                                                                                        ISpecialCreativeHandlerAnchorBlock,
                                                                                                                        IBuildingBrowsableBlock

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
    public AbstractBlockHut(final Properties properties)
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
    public void onBlockPlacedByBuildTool(
      @NotNull final Level worldIn,
      @NotNull final BlockPos pos,
      final BlockState state,
      final LivingEntity placer,
      final ItemStack stack,
      final boolean mirror,
      final String style,
      final String blueprintPath)
    {
        final BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        if (tileEntity instanceof AbstractTileEntityColonyBuilding)
        {
            ((AbstractTileEntityColonyBuilding) tileEntity).setMirror(mirror);
            ((AbstractTileEntityColonyBuilding) tileEntity).setPackName(style);
            ((AbstractTileEntityColonyBuilding) tileEntity).setBlueprintPath(blueprintPath);
        }

        setPlacedBy(worldIn, pos, state, placer, stack);
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
    public B registerBlock(final Registry<Block> registry)
    {
        Registry.register(registry, getRegistryName(), this);
        return (B) this;
    }

    @Override
    public boolean isVisible(@Nullable final CompoundTag beData)
    {
        final Map<BlockPos, List<String>> data = readTagPosMapFrom(beData.getCompound(TAG_BLUEPRINTDATA));
        return !data.getOrDefault(BlockPos.ZERO, new ArrayList<>()).contains("invisible");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<MutableComponent> getRequirements(final ClientLevel level, final BlockPos pos, final LocalPlayer player)
    {
        final List<MutableComponent> requirements = new ArrayList<>();
        final IColonyView colonyView = IColonyManager.getInstance().getClosestColonyView(level, pos);
        if (colonyView == null)
        {
            requirements.add(Component.translatable("com.minecolonies.coremod.hut.incolony").setStyle((Style.EMPTY).withColor(ChatFormatting.RED)));
            return requirements;
        }

        if (InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(player.getInventory()), this) == -1)
        {
            requirements.add(Component.translatable("com.minecolonies.coremod.hut.cost", Component.translatable("block." + Constants.MOD_ID + "." + getHutName())).setStyle((Style.EMPTY).withColor(ChatFormatting.RED)));
            return requirements;
        }

        final ResourceLocation effectId = colonyView.getResearchManager().getResearchEffectIdFrom(this);
        if (colonyView.getResearchManager().getResearchEffects().getEffectStrength(effectId) > 0)
        {
            return requirements;
        }

        if (MinecoloniesAPIProxy.getInstance().getGlobalResearchTree().getResearchForEffect(effectId) != null)
        {
            requirements.add(Component.translatable(TranslationConstants.HUT_NEEDS_RESEARCH_TOOLTIP_1, getName()));
            requirements.add(Component.translatable(TranslationConstants.HUT_NEEDS_RESEARCH_TOOLTIP_2, getName()));
        }

        return requirements;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean areRequirementsMet(final ClientLevel level, final BlockPos pos, final LocalPlayer player)
    {
        if (player.isCreative())
        {
            return true;
        }
        return this.getRequirements(level, pos, player).isEmpty();
    }

    @Override
    public List<MutableComponent> getDesc()
    {
        final List<MutableComponent> desc = new ArrayList<>();
        desc.add(Component.translatable(getBuildingEntry().getTranslationKey()));
        desc.add(Component.translatable(getBuildingEntry().getTranslationKey() + ".desc"));
        return desc;
    }

    @Override
    public Component getBlueprintDisplayName()
    {
        return Component.translatable(getBuildingEntry().getTranslationKey());
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
    public AbstractStructureHandler getStructureHandler(final Level level, final BlockPos blockPos, final Blueprint blueprint, final PlacementSettings placementSettings, final boolean b)
    {
        return new CreativeBuildingStructureHandler(level, blockPos, blueprint, placementSettings, b);
    }

    @Override
    public boolean setup(
      final ServerPlayer player,
      final Level world,
      final BlockPos pos,
      final Blueprint blueprint,
      final PlacementSettings settings,
      final boolean fancyPlacement,
      final String pack,
      final String path)
    {
        final BlockState anchor = blueprint.getBlockState(blueprint.getPrimaryBlockOffset());
        if (!(anchor.getBlock() instanceof AbstractBlockHut<?>) || (!fancyPlacement && player.isCreative()))
        {
            return true;
        }

        if (!IMinecoloniesAPI.getInstance().getConfig().getServer().blueprintBuildMode.get() && !canPaste(anchor.getBlock(), player, pos))
        {
            return false;
        }
        world.destroyBlock(pos, true);
        world.setBlockAndUpdate(pos, anchor);
        ((AbstractBlockHut<?>) anchor.getBlock()).onBlockPlacedByBuildTool(world,
          pos,
          anchor,
          player,
          null,
          settings.getMirror() != Mirror.NONE,
          pack,
          path);

        if (IMinecoloniesAPI.getInstance().getConfig().getServer().blueprintBuildMode.get())
        {
            return true;
        }

        @Nullable final IBuilding building = IColonyManager.getInstance().getBuilding(world, pos);
        if (building == null)
        {
            if (anchor.getBlock() != ModBlocks.blockHutTownHall)
            {
                SoundUtils.playErrorSound(player, player.blockPosition());
                Log.getLogger().error("BuildTool: building is null!", new Exception());
                return false;
            }
        }
        else
        {
            SoundUtils.playSuccessSound(player, player.blockPosition());
            if (building.getTileEntity() != null)
            {
                final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(world, pos);
                if (colony == null)
                {
                    Log.getLogger().info("No colony for " + player.getName().getString());
                    return false;
                }
                else
                {
                    building.getTileEntity().setColony(colony);
                }
            }

            final String adjusted = path.replace(".blueprint", "");
            final String num = adjusted.substring(path.replace(".blueprint", "").length() - 2, adjusted.length() - 1);

            building.setStructurePack(pack);
            building.setBlueprintPath(path);
            try
            {
                building.setBuildingLevel(Integer.parseInt(num));
            }
            catch (final NumberFormatException ex)
            {
                building.setBuildingLevel(1);
            }

            building.setIsMirrored(settings.mirror != Mirror.NONE);
            building.onUpgradeComplete(building.getBuildingLevel());
        }
        return true;
    }

    /**
     * Check if we got permissions to paste.
     * @param anchor the anchor of the paste.
     * @param player the player pasting it.
     * @param pos the position its pasted at.
     * @return true if fine.
     */
    private boolean canPaste(final Block anchor, final Player player, final BlockPos pos)
    {
        final IColony colony = IColonyManager.getInstance().getIColony(player.level(), pos);

        if (colony == null)
        {
            if(anchor == ModBlocks.blockHutTownHall)
            {
                return true;
            }

            //  Not in a colony
            if (IColonyManager.getInstance().getIColonyByOwner(player.level(), player) == null)
            {
                MessageUtils.format(MESSAGE_WARNING_TOWN_HALL_NOT_PRESENT).sendTo(player);
            }
            else
            {
                MessageUtils.format(MESSAGE_WARNING_TOWN_HALL_TOO_FAR_AWAY).sendTo(player);
            }

            return false;
        }
        else if (!colony.getPermissions().hasPermission(player, Action.PLACE_HUTS))
        {
            //  No permission to place hut in colony
            MessageUtils.format(PERMISSION_OPEN_HUT, colony.getName()).sendTo(player);
            return false;
        }
        else
        {
            return colony.getBuildingManager().canPlaceAt(anchor, pos, player);
        }
    }

    /**
     * Get the blueprint name.
     * @return the name.
     */
    public String getBlueprintName()
    {
        return getBuildingEntry().getRegistryName().getPath();
    }

    @Override
    public void registerBlockItem(final Registry<Item> registry, final Item.Properties properties)
    {
        Registry.register(registry, getRegistryName(), new ItemBlockHut(this, properties));
    }
}
