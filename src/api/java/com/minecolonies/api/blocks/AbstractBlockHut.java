package com.minecolonies.api.blocks;

import com.ldtteam.structurize.blocks.interfaces.IAnchorBlock;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.entity.ai.citizen.builder.IBuilderUndestroyable;
import com.minecolonies.api.items.ItemBlockHut;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.IForgeRegistry;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Abstract class for all minecolonies blocks.
 * <p>
 * The method {@link AbstractBlockHut#getName()} is abstract.
 * <p>
 * All AbstractBlockHut[something] should extend this class.
 */
@SuppressWarnings("PMD.ExcessiveImports")
public abstract class AbstractBlockHut<B extends AbstractBlockHut<B>> extends AbstractBlockMinecolonies<B> implements IBuilderUndestroyable, IAnchorBlock
{
    /**
     * Hardness factor of the pvp mode.
     */
    private static final int HARDNESS_PVP_FACTOR = 4;

    /**
     * The direction the block is facing.
     */
    public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;

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
    private static final VoxelShape SHAPE = VoxelShapes.create(0.1, 0.1, 0.1, 0.9, 0.9, 0.9);

    /**
     * Whether this hut is yet to be researched in the current colony.
     * This is only ever used client side, but adding @OnlyIn(Dist.CLIENT) causes the server to crash, so its not there.
     */
    protected boolean needsResearch = false;

    /**
     * The hut's lower-case building-registry-compatible name.
     */
    private final String name;

    /**
     * Constructor for a hut block.
     * <p>
     * Registers the block, sets the creative tab, as well as the resistance and the hardness.
     */
    public AbstractBlockHut()
    {
        super(Properties.create(Material.WOOD).hardnessAndResistance(HARDNESS, RESISTANCE).notSolid());
        setRegistryName(getName());
        this.setDefaultState(this.getDefaultState().with(FACING, Direction.NORTH));
        this.name = getName();
    }

    @Override
    public float getPlayerRelativeBlockHardness(final BlockState state, @NotNull final PlayerEntity player, @NotNull final IBlockReader world, @NotNull final BlockPos pos)
    {
        return MinecoloniesAPIProxy.getInstance().getConfig().getServer().pvp_mode.get() ? 1/(HARDNESS * HARDNESS_PVP_FACTOR) : 1/HARDNESS;
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
        super(properties.notSolid());
        setRegistryName(getName());
        this.setDefaultState(this.getDefaultState().with(FACING, Direction.NORTH));
        this.name = getName();
    }

    /**
     * Method to return the name of the block.
     *
     * @return Name of the block.
     */
    public abstract String getName();

    @Nullable
    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        final TileEntityColonyBuilding building = (TileEntityColonyBuilding) MinecoloniesTileEntities.BUILDING.create();
        building.registryName = this.getBuildingEntry().getRegistryName();
        return building;
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

    /**
     * Method to get the building registry entry.
     *
     * @return The building entry.
     */
    public abstract BuildingEntry getBuildingEntry();

    @NotNull
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos, final ISelectionContext context)
    {
        return SHAPE;
    }

    @NotNull
    @Override
    public ActionResultType onBlockActivated(
      final BlockState state,
      final World worldIn,
      final BlockPos pos,
      final PlayerEntity player,
      final Hand hand,
      final BlockRayTraceResult ray)
    {
       /*
        If the world is client, open the gui of the building
         */
        if (worldIn.isRemote)
        {
            @Nullable final IBuildingView building = IColonyManager.getInstance().getBuildingView(worldIn.getDimensionKey(), pos);

            if (building == null)
            {
                LanguageHandler.sendPlayerMessage(player, "com.minecolonies.coremod.gui.nobuilding");
                return ActionResultType.FAIL;
            }

            if (building.getColony() == null)
            {
                LanguageHandler.sendPlayerMessage(player, "com.minecolonies.coremod.gui.nocolony");
                return ActionResultType.FAIL;
            }

            if (!building.getColony().getPermissions().hasPermission(player, Action.ACCESS_HUTS))
            {
                LanguageHandler.sendPlayerMessage(player, "com.minecolonies.coremod.permission.no");
                return ActionResultType.FAIL;
            }

            building.openGui(player.isSneaking());
        }
        return ActionResultType.SUCCESS;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        @NotNull final Direction facing = (context.getPlayer() == null) ? Direction.NORTH : Direction.fromAngle(context.getPlayer().rotationYaw);
        return this.getDefaultState().with(FACING, facing);
    }

    @NotNull
    @Override
    public BlockState rotate(final BlockState state, final Rotation rot)
    {
        return state.with(FACING, rot.rotate(state.get(FACING)));
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
     * @see Block#onBlockPlacedBy(World, BlockPos, BlockState, LivingEntity, ItemStack)
     */
    @Override
    public void onBlockPlacedBy(@NotNull final World worldIn, @NotNull final BlockPos pos, final BlockState state, final LivingEntity placer, final ItemStack stack)
    {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

        /*
        Only work on server side
        */
        if (worldIn.isRemote)
        {
            return;
        }

        final TileEntity tileEntity = worldIn.getTileEntity(pos);
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
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
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
     * @see Block#onBlockPlacedBy(World, BlockPos, BlockState, LivingEntity, ItemStack)
     */
    public void onBlockPlacedByBuildTool(
      @NotNull final World worldIn, @NotNull final BlockPos pos,
      final BlockState state, final LivingEntity placer, final ItemStack stack, final boolean mirror, final String style)
    {
        final TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof AbstractTileEntityColonyBuilding)
        {
            ((AbstractTileEntityColonyBuilding) tileEntity).setMirror(mirror);
            ((AbstractTileEntityColonyBuilding) tileEntity).setStyle(style);
        }

        onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }

    /**
     * Checks whether the research for this hut block is already researched in the given colony.
     *
     * @param colony     the colony to check.
     */
    @OnlyIn(Dist.CLIENT)
    public void checkResearch(final IColonyView colony)
    {
        if (colony == null)
        {
            needsResearch = false;
            return;
        }
        final ResourceLocation effectId = colony.getResearchManager().getResearchEffectIdFrom(this);
        if(colony.getResearchManager().getResearchEffects().getEffectStrength(effectId) > 0)
        {
            needsResearch = false;
            return;
        }
        if(MinecoloniesAPIProxy.getInstance().getGlobalResearchTree().hasResearchEffect(effectId))
        {
            needsResearch = true;
            return;
        }
        needsResearch = false;
    }

    @Override
    public void registerBlockItem(final IForgeRegistry<Item> registry, final net.minecraft.item.Item.Properties properties)
    {
        registry.register((new ItemBlockHut(this, properties)).setRegistryName(this.getRegistryName()));
    }

    /**
     * Whether this hut blocks building needs to be researched.
     * 
     * @return true if this building needs to be researched, but isn't yet.
     */
    @OnlyIn(Dist.CLIENT)
    public boolean needsResearch()
    {
        return needsResearch;
    }
}
