package com.minecolonies.blocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.buildings.AbstractBuilding;
import com.minecolonies.creativetab.ModCreativeTabs;
import com.minecolonies.lib.Constants;
import com.minecolonies.tileentities.TileEntityColonyBuilding;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Abstract class for all minecolonies blocks.
 * <p>
 * The method {@link AbstractBlockHut#getName()} is abstract
 * <p>
 * All AbstractBlockHut[something] should extend this class
 */
public abstract class AbstractBlockHut extends Block implements ITileEntityProvider
{

    private static final float HARDNESS   = 10F;
    private static final float RESISTANCE = Float.POSITIVE_INFINITY;
    public static final PropertyDirection FACING = BlockDirectional.FACING;
    //private static final PropertyDirection FACING = PropertyDirection.create("FACING", EnumFacing.Plane.HORIZONTAL);
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

    @Nonnull
    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        //Creates a tile entity for our building
        return new TileEntityColonyBuilding();
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        EnumFacing facing = EnumFacing.getFront(meta);
        if (facing.getAxis() == EnumFacing.Axis.Y)
        {
            facing = EnumFacing.NORTH;
        }
        return this.getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public int getMetaFromState(@Nonnull IBlockState state)
    {
        return state.getValue(FACING).getIndex();
    }


    // =======================================================================
    // ======================= Rendering & IBlockState =======================
    // =======================================================================

    // render as a solid block, we don't want transparency here
    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.SOLID;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        /*
        If the world is client, open the gui of the building
         */
        if (worldIn.isRemote)
        {
            @Nullable final AbstractBuilding.View building = ColonyManager.getBuildingView(pos);

            if (building != null)
            {
                building.openGui();
            }
        }
        return true;
    }

    @Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, @Nullable EntityLivingBase placer)
    {
        @Nonnull final EnumFacing enumFacing = (placer == null) ? EnumFacing.NORTH : EnumFacing.fromAngle(placer.rotationYaw);
        return this.getDefaultState().withProperty(FACING, enumFacing);
    }

    /**
     * Event-Handler for placement of this block.
     * <p>
     * Override for custom logic.
     *
     * @param worldIn the word we are in
     * @param pos     the position where the block was placed
     * @param state   the state the placed block is in
     * @param placer  the player placing the block
     * @param stack   the itemstack from where the block was placed
     * @see Block#onBlockPlacedBy(World, BlockPos, IBlockState, EntityLivingBase, ItemStack)
     */
    @Override
    public void onBlockPlacedBy(@Nonnull World worldIn, @Nonnull BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
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
            @Nonnull final TileEntityColonyBuilding hut = (TileEntityColonyBuilding) tileEntity;
            @Nullable final Colony colony = ColonyManager.getColony(worldIn, hut.getPosition());

            if (colony != null)
            {
                colony.addNewBuilding(hut);
            }
        }
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] {FACING});
    }

    // =======================================================================
    // ===================== END of Rendering & Meta-Data ====================
    // =======================================================================
}
