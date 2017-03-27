package com.minecolonies.coremod.blocks;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.permissions.Permissions;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import com.minecolonies.coremod.lib.Constants;
import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Abstract class for all minecolonies blocks.
 * <p>
 * The method {@link AbstractBlockHut#getName()} is abstract.
 * <p>
 * All AbstractBlockHut[something] should extend this class.
 */
public abstract class AbstractBlockHut extends Block implements ITileEntityProvider
{
    public static final  PropertyDirection FACING     = BlockHorizontal.FACING;
    private static final float             HARDNESS   = 10F;
    private static final float             RESISTANCE = Float.POSITIVE_INFINITY;
    protected int workingRange;

    /**
     * Constructor for a block using the minecolonies mod.
     * <p>
     * Registers the block, sets the creative tab, as well as the resistance and the hardness.
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
        setRegistryName(getName());
        setUnlocalizedName(Constants.MOD_ID.toLowerCase() + "." + getName());
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        //Blast resistance for creepers etc. makes them explosion proof
        setResistance(RESISTANCE);
        //Hardness of 10 takes a long time to mine to not loose progress
        setHardness(HARDNESS);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        GameRegistry.register(this);
        GameRegistry.register((new ItemBlock(this)).setRegistryName(this.getRegistryName()));
    }

    /**
     * Method to return the name of the block.
     *
     * @return Name of the block.
     */
    public abstract String getName();
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
    @Override
    @Deprecated
    public boolean isFullBlock(final IBlockState state)
    {
        return false;
    }

    /**
     * Convert the given metadata into a BlockState for this Block.
     *
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
    @NotNull
    @Override
    @Deprecated
    public IBlockState getStateFromMeta(final int meta)
    {
        EnumFacing enumfacing = EnumFacing.getFront(meta);

        if (enumfacing.getAxis() == EnumFacing.Axis.Y)
        {
            enumfacing = EnumFacing.NORTH;
        }

        return this.getDefaultState().withProperty(FACING, enumfacing);
    }

    // =======================================================================
    // ======================= Rendering & IBlockState =======================
    // =======================================================================

    /**
     * Convert the BlockState into the correct metadata value.
     */
    @Override
    public int getMetaFromState(final IBlockState state)
    {
        return state.getValue(FACING).getIndex();
    }

    /**
     * Convert the BlockState into the correct metadata value.
     *
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
    @NotNull
    @Override
    @Deprecated
    public IBlockState withRotation(@NotNull final IBlockState state, final Rotation rot)
    {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    /**
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
    @NotNull
    @Override
    @Deprecated
    public IBlockState withMirror(@NotNull final IBlockState state, final Mirror mirrorIn)
    {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }

    /**
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
    @Override
    @Deprecated
    public boolean isFullCube(final IBlockState state)
    {
        return false;
    }

    /**
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
    @Override
    @Deprecated
    public boolean isOpaqueCube(final IBlockState state)
    {
        return false;
    }

    @NotNull
    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.SOLID;
    }

    @Override
    public boolean onBlockActivated(
                                     final World worldIn,
                                     final BlockPos pos,
                                     final IBlockState state,
                                     final EntityPlayer playerIn,
                                     final EnumHand hand,
                                     @Nullable final ItemStack heldItem,
                                     final EnumFacing side,
                                     final float hitX,
                                     final float hitY,
                                     final float hitZ)
    {
        /*
        If the world is client, open the gui of the building
         */
        if (worldIn.isRemote)
        {
            @Nullable final AbstractBuilding.View building = ColonyManager.getBuildingView(pos);

            if (building != null
                    && building.getColony() != null
                    && building.getColony().getPermissions() != null
                    && building.getColony().getPermissions().hasPermission(playerIn, Permissions.Action.ACCESS_HUTS))
            {
                building.openGui();
            }
        }
        return true;
    }

    @Override
    public IBlockState onBlockPlaced(
                                      final World worldIn,
                                      final BlockPos pos,
                                      final EnumFacing facing,
                                      final float hitX,
                                      final float hitY,
                                      final float hitZ,
                                      final int meta,
                                      @Nullable final EntityLivingBase placer)
    {
        @NotNull final EnumFacing enumFacing = (placer == null) ? EnumFacing.NORTH : EnumFacing.fromAngle(placer.rotationYaw);
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
     * @see Block#onBlockPlacedBy(World, BlockPos, IBlockState, EntityLivingBase, ItemStack)
     */
    @Override
    public void onBlockPlacedBy(@NotNull final World worldIn, @NotNull final BlockPos pos, final IBlockState state, final EntityLivingBase placer, final ItemStack stack)
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
        if (placer instanceof EntityPlayer && tileEntity instanceof TileEntityColonyBuilding)
        {
            @NotNull final TileEntityColonyBuilding hut = (TileEntityColonyBuilding) tileEntity;
            @Nullable final Colony colony = ColonyManager.getColony(worldIn, hut.getPosition());

            if (colony != null)
            {
                colony.addNewBuilding(hut);
            }
        }
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
     * @see Block#onBlockPlacedBy(World, BlockPos, IBlockState, EntityLivingBase, ItemStack)
     */
    public void onBlockPlacedByBuildTool(
                                          @NotNull final World worldIn, @NotNull final BlockPos pos,
                                          final IBlockState state, final EntityLivingBase placer, final ItemStack stack, final boolean mirror)
    {
        final TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof TileEntityColonyBuilding)
        {
            ((TileEntityColonyBuilding) tileEntity).setMirror(mirror);
        }

        onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }

    @NotNull
    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public boolean doesSideBlockRendering(final IBlockState state, final IBlockAccess world, final BlockPos pos, final EnumFacing face)
    {
        return false;
    }

    // =======================================================================
    // ===================== END of Rendering & Meta-Data ====================
    // =======================================================================
}
