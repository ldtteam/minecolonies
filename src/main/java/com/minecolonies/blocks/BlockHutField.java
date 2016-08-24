package com.minecolonies.blocks;

import com.minecolonies.MineColonies;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.creativetab.ModCreativeTabs;
import com.minecolonies.inventory.InventoryField;
import com.minecolonies.lib.Constants;
import com.minecolonies.tileentities.ScarecrowTileEntity;
import com.minecolonies.util.LanguageHandler;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The class handling the fieldBlocks, placement and activation.
 */
public class BlockHutField extends BlockContainer implements ITileEntityProvider
{

    /**
     * Hardness of the block.
     */
    private static final float HARDNESS   = 10F;

    /**
     * Resistance of the block.
     */
    private static final float RESISTANCE = 10F;

    /**
     * The position it faces.
     */
    private static final PropertyDirection FACING = PropertyDirection.create("FACING", EnumFacing.Plane.HORIZONTAL);

    /**
     * Start of the collision box at y.
     */
    private static final double BOTTOM_COLLISION = 0.0;

    /**
     * Start of the collision box at x and z.
     */
    private static final double START_COLLISION = 0.2;

    /**
     * End of the collision box.
     */
    private static final double END_COLLISION = 0.8;

    /**
     * Height of the collision box.
     */
    private static final double HEIGHT_COLLISION = 2.5;

    /**
     * Constructor called on block placement.
     */
    BlockHutField()
    {
        super(Material.wood);
        initBlock();
    }

    /**
     * Getter of the description of the blockHut.
     * @return the name as a String.
     */
    public String getName()
    {
        return "blockHutField";
    }

    /**
     * Method called by constructor.
     * Sets basic details of the block.
     */
    private void initBlock()
    {
        setRegistryName(getName());
        setUnlocalizedName(Constants.MOD_ID.toLowerCase() + "." + getName());
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        //Blast resistance for creepers etc. makes them explosion proof.
        setResistance(RESISTANCE);
        //Hardness of 10 takes a long time to mine to not loose progress.
        setHardness(HARDNESS);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        GameRegistry.registerBlock(this);
        setBlockBounds((float)START_COLLISION, (float)BOTTOM_COLLISION, (float)START_COLLISION, (float)END_COLLISION, (float)HEIGHT_COLLISION, (float)END_COLLISION);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        //Only work on server side.
        if(worldIn.isRemote)
        {
            return;
        }

        if(placer instanceof EntityPlayer)
        {
            Colony colony = ColonyManager.getColony(worldIn, pos);

            if (colony != null)
            {
                InventoryField inventoryField = new InventoryField(LanguageHandler.getString("com.minecolonies.gui.inventory.scarecrow"), true);

                ((ScarecrowTileEntity)worldIn.getTileEntity(pos)).setInventoryField(inventoryField);
                colony.addNewField(inventoryField, ((EntityPlayer) placer).inventory, pos, worldIn);
            }
        }
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public int getRenderType()
    {
        return -1;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        //If the world is server, open the inventory of the field.
        if(!worldIn.isRemote)
        {
            Colony colony = ColonyManager.getColony(worldIn, pos);
            if(colony != null)
            {
                playerIn.openGui(MineColonies.instance, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBlockDestroyedByPlayer(final World worldIn, final BlockPos pos, final IBlockState state)
    {
        super.onBlockDestroyedByPlayer(worldIn, pos, state);
        //Only work on server side.
        if(worldIn.isRemote)
        {
            return;
        }


        Colony colony = ColonyManager.getColony(worldIn, pos);

        if (colony != null)
        {
            colony.removeField(pos);
        }
    }


    @Override
    public void onBlockHarvested(final World worldIn, final BlockPos pos, final IBlockState state, final EntityPlayer player)
    {
        super.onBlockHarvested(worldIn, pos, state, player);
    }


    // =======================================================================
    // ======================= Rendering & IBlockState =======================
    // =======================================================================
    @Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        EnumFacing enumFacing = (placer == null) ? EnumFacing.NORTH : EnumFacing.fromAngle(placer.rotationYaw);
        return this.getDefaultState().withProperty(FACING, enumFacing);
    }

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
        return state.getValue(FACING).getIndex();
    }

    @Override
    protected BlockState createBlockState()
    {
        return new BlockState(this, FACING);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new ScarecrowTileEntity();
    }
    // =======================================================================
    // ===================== END of Rendering & Meta-Data ====================
    // =======================================================================
}
