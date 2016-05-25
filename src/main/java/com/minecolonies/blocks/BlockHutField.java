package com.minecolonies.blocks;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.creativetab.ModCreativeTabs;
import com.minecolonies.lib.Constants;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockHutField extends Block
{

    private static final float HARDNESS   = 10F;
    private static final float RESISTANCE = 10F;
    private static final PropertyDirection FACING = PropertyDirection.create("FACING", EnumFacing.Plane.HORIZONTAL);

    public BlockHutField()
    {
        super(Material.wood);
        initBlock();
    }

    public String getName() {
        return "blockHutField";
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


    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        /*
        Only work on server side
        */
        if(worldIn.isRemote)
        {
            return;
        }

        if(placer instanceof EntityPlayer)
        {
            Colony colony = ColonyManager.getColony(worldIn, pos);

            if (colony != null)
            {
                colony.addNewField(pos);
            }
        }
    }

    /**
     * Used to determine ambient occlusion and culling when rebuilding chunks for render.
     *
     * @return true
     */
    @Override
    public boolean isOpaqueCube()
    {
        return true;
    }


    // =======================================================================
    // ======================= Rendering & IBlockState =======================
    // =======================================================================
    @Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        EnumFacing enumFacing = (placer == null) ? EnumFacing.NORTH : EnumFacing.fromAngle(placer.rotationYaw);
        return this.getDefaultState().withProperty(FACING, enumFacing);
    }

    // render as a solid block, we don't want transparency here
    @Override
    @SideOnly(Side.CLIENT)
    public EnumWorldBlockLayer getBlockLayer() {
        return EnumWorldBlockLayer.SOLID;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = EnumFacing.getFront(meta);
        if(facing.getAxis() == EnumFacing.Axis.Y)
            facing = EnumFacing.NORTH;
        return this.getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return ((EnumFacing)state.getValue(FACING)).getIndex();
    }

    @Override
    protected BlockState createBlockState() {
        return new BlockState(this, FACING);
    }
    // =======================================================================
    // ===================== END of Rendering & Meta-Data ====================
    // =======================================================================
}
