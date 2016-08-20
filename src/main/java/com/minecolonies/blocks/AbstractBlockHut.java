package com.minecolonies.blocks;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.buildings.AbstractBuilding;
import com.minecolonies.creativetab.ModCreativeTabs;
import com.minecolonies.lib.Constants;
import com.minecolonies.tileentities.TileEntityColonyBuilding;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Abstract class for all minecolonies blocks.
 * The method {@link AbstractBlockHut#getName()} is abstract
 * All AbstractBlockHut[something] should extend this class
 */
public abstract class AbstractBlockHut extends AbstractBlockMineColonies implements ITileEntityProvider
{

    protected int workingRange;

    private static final float HARDNESS   = 10F;
    private static final float RESISTANCE = Float.POSITIVE_INFINITY;

    private static final PropertyDirection FACING = PropertyDirection.create("FACING", EnumFacing.Plane.HORIZONTAL);

    /**
     * Constructor for a block using the minecolonies mod.
     * Registers the block, sets the creative tab, as well as the resistance and the hardness.
     */
    public AbstractBlockHut()
    {
        super(Material.wood);
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
        GameRegistry.registerBlock(this);
    }

    /**
     * Method to return the name of the block.
     *
     * @return Name of the block.
     */
    public abstract String getName();

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        
        /*
        Only work on server side
        */
        if(worldIn.isRemote)
        {
            return;
        }

        final TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(placer instanceof EntityPlayer && tileEntity instanceof TileEntityColonyBuilding)
        {
            final TileEntityColonyBuilding hut = (TileEntityColonyBuilding) tileEntity;
            final Colony colony = ColonyManager.getColony(worldIn, hut.getPosition());

            if(colony != null)
            {
                colony.addNewBuilding(hut);
            }
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        /*
        If the world is client, open the gui of the building
         */
        if(worldIn.isRemote)
        {
            final AbstractBuilding.View building = ColonyManager.getBuildingView(pos);

            if(building != null)
            {
                building.openGui();
            }
        }
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        //Creates a tile entity for our building
        return new TileEntityColonyBuilding();
    }


    // =======================================================================
    // ======================= Rendering & IBlockState =======================
    // =======================================================================

    @Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        final EnumFacing enumFacing = (placer == null) ? EnumFacing.NORTH : EnumFacing.fromAngle(placer.rotationYaw);
        return this.getDefaultState().withProperty(FACING, enumFacing);
    }

    // render as a solid block, we don't want transparency here
    @Override
    @SideOnly(Side.CLIENT)
    public EnumWorldBlockLayer getBlockLayer()
    {
        return EnumWorldBlockLayer.SOLID;
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        EnumFacing facing = EnumFacing.getFront(meta);
        if(facing.getAxis() == EnumFacing.Axis.Y)
        {
            facing = EnumFacing.NORTH;
        }
        return this.getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return ((EnumFacing) state.getValue(FACING)).getIndex();
    }

    @Override
    protected BlockState createBlockState()
    {
        return new BlockState(this, FACING);
    }

    // =======================================================================
    // ===================== END of Rendering & Meta-Data ====================
    // =======================================================================
}
