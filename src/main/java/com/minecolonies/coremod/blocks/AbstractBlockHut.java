package com.minecolonies.coremod.blocks;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.entity.ai.citizen.builder.IBuilderUndestroyable;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.IColonyManager;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import com.minecolonies.coremod.tileentities.ITileEntityColonyBuilding;
import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import com.ldtteam.structurize.blocks.interfaces.IAnchorBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockState;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.LivingEntityBase;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.Suppression.DEPRECATION;

/**
 * Abstract class for all minecolonies blocks.
 * <p>
 * The method {@link AbstractBlockHut#getName()} is abstract.
 * <p>
 * All AbstractBlockHut[something] should extend this class.
 */
public abstract class AbstractBlockHut<B extends AbstractBlockHut<B>> extends AbstractBlockMinecolonies<B> implements IBuilderUndestroyable, IAnchorBlock
{
    /**
     * Hardness factor of the pvp mode.
     */
    private static final int HARDNESS_PVP_FACTOR = 4;

    /**
     * The direction the block is facing.
     */
    public static final  PropertyDirection FACING     = BlockHorizontal.FACING;

    /**
     * The default hardness.
     */
    private static final float             HARDNESS   = 10F;

    /**
     * The default resistance (against explosions).
     */
    private static final float             RESISTANCE = Float.POSITIVE_INFINITY;

    /**
     * Constructor for a block using the minecolonies mod.
     * <p>
     * Registers the block, sets the creative tab, as well as the resistance and
     * the hardness.
     */
    public AbstractBlockHut()
    {
        super(Material.WOOD);
        initBlock();
    }

    /**
     * Initiates the basic block variables.
     */
    private void initBlock()
    {
        setRegistryName(Constants.MOD_ID.toLowerCase() + ":" + getName());
        setTranslationKey(Constants.MOD_ID.toLowerCase() + "." + getName());
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        //Blast resistance for creepers etc. makes them explosion proof
        setResistance(RESISTANCE);
        //Hardness of 10 takes a long time to mine to not loose progress
        setHardness(Configurations.gameplay.pvp_mode ? HARDNESS * HARDNESS_PVP_FACTOR : HARDNESS);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, Direction.NORTH));
    }

    /**
     * Method to return the name of the block.
     *
     * @return Name of the block.
     */
    public abstract String getName();

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

    @NotNull
    @Override
    public TileEntity createNewTileEntity(final World world, final int meta)
    {
        //Creates a tile entity for our building
        return new TileEntityColonyBuilding();
    }

    /**
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
    @SuppressWarnings(DEPRECATION)
    @Override
    @Deprecated
    public boolean isFullBlock(final BlockState state)
    {
        return false;
    }

    /**
     * Convert the given metadata into a BlockState for this Block.
     *
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
    @SuppressWarnings(DEPRECATION)
    @NotNull
    @Override
    @Deprecated
    public BlockState getStateFromMeta(final int meta)
    {
        Direction enumfacing = Direction.byIndex(meta);

        if (enumfacing.getAxis() == Direction.Axis.Y)
        {
            enumfacing = Direction.NORTH;
        }

        return this.getDefaultState().withProperty(FACING, enumfacing);
    }

    // =======================================================================
    // ======================= Rendering & BlockState =======================
    // =======================================================================

    /**
     * Convert the BlockState into the correct metadata value.
     */
    @Override
    public int getMetaFromState(final BlockState state)
    {
        return state.getValue(FACING).getIndex();
    }

    /**
     * Convert the BlockState into the correct metadata value.
     *
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
    @SuppressWarnings(DEPRECATION)
    @NotNull
    @Override
    @Deprecated
    public BlockState withRotation(@NotNull final BlockState state, final Rotation rot)
    {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    /**
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
    @SuppressWarnings(DEPRECATION)
    @NotNull
    @Override
    @Deprecated
    public BlockState withMirror(@NotNull final BlockState state, final Mirror mirrorIn)
    {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }

    /**
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
    @SuppressWarnings(DEPRECATION)
    @Override
    @Deprecated
    public boolean isFullCube(final BlockState state)
    {
        return false;
    }

    /**
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
    @SuppressWarnings(DEPRECATION)
    @Override
    @Deprecated
    public boolean isOpaqueCube(final BlockState state)
    {
        return false;
    }

    @NotNull
    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer()
    {
        return BlockRenderLayer.SOLID;
    }

    @Override
    public boolean onBlockActivated(
                                     final World worldIn,
                                     final BlockPos pos,
                                     final BlockState state,
                                     final PlayerEntity playerIn,
                                     final EnumHand hand,
                                     final Direction facing,
                                     final float hitX,
                                     final float hitY,
                                     final float hitZ)
    {
        /*
        If the world is client, open the gui of the building
         */
        if (worldIn.isRemote)
        {
            @Nullable final IBuildingView building = IColonyManager.getInstance().getBuildingView(worldIn.provider.getDimension(), pos);

            if (building != null
                  && building.getColony() != null
                  && building.getColony().getPermissions().hasPermission(playerIn, Action.ACCESS_HUTS))
            {
                building.openGui(playerIn.isSneaking());
            }
        }
        return true;
    }

    @SuppressWarnings(DEPRECATION)
    @NotNull
    @Override
    public BlockState getStateForPlacement(
                                             final World worldIn,
                                             final BlockPos pos,
                                             final Direction facing,
                                             final float hitX,
                                             final float hitY,
                                             final float hitZ,
                                             final int meta,
                                             final LivingEntityBase placer)
    {
        @NotNull final Direction enumFacing = (placer == null) ? Direction.NORTH : Direction.fromAngle(placer.rotationYaw);
        return this.getDefaultState().withProperty(FACING, enumFacing);
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
     * @see Block#onBlockPlacedBy(World, BlockPos, BlockState,
     * LivingEntityBase, ItemStack)
     */
    @Override
    public void onBlockPlacedBy(@NotNull final World worldIn, @NotNull final BlockPos pos, final BlockState state, final LivingEntityBase placer, final ItemStack stack)
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
        if (placer instanceof PlayerEntity && tileEntity instanceof TileEntityColonyBuilding)
        {
            @NotNull final TileEntityColonyBuilding hut = (TileEntityColonyBuilding) tileEntity;
            @Nullable final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(worldIn, hut.getPosition());

            if (colony != null)
            {
                colony.getBuildingManager().addNewBuilding(hut, worldIn);
                colony.getProgressManager().progressBuildingPlacement(this);
            }
        }
    }

    @NotNull
    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public boolean canRenderInLayer(final BlockState state, final BlockRenderLayer layer)
    {
        if (layer == BlockRenderLayer.CUTOUT || layer == BlockRenderLayer.SOLID)
        {
            return true;
        }
        return super.canRenderInLayer(state, layer);
    }

    @Override
    public boolean doesSideBlockRendering(final BlockState state, final IBlockAccess world, final BlockPos pos, final Direction face)
    {
        return false;
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
     * @see Block#onBlockPlacedBy(World, BlockPos, BlockState,
     * LivingEntityBase, ItemStack)
     */
    public void onBlockPlacedByBuildTool(
                                          @NotNull final World worldIn, @NotNull final BlockPos pos,
                                          final BlockState state, final LivingEntity placer, final ItemStack stack, final boolean mirror, final String style)
    {
        final TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof TileEntityColonyBuilding)
        {
            ((ITileEntityColonyBuilding) tileEntity).setMirror(mirror);
            ((ITileEntityColonyBuilding) tileEntity).setStyle(style);
        }

        onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }

    // =======================================================================
    // ===================== END of Rendering & Meta-Data ====================
    // =======================================================================
}
