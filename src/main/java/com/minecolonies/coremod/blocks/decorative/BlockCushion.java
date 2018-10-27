package com.minecolonies.coremod.blocks.decorative;

import java.util.List;
import java.util.Locale;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.blocks.AbstractBlockMinecolonies;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import com.minecolonies.coremod.entity.EntityCushion;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


/**
 * Block that renders as a tiny "cushion"
 * 
 */
public class BlockCushion extends  AbstractBlockMinecolonies<BlockCushion>  {
    /** Name of the block */
    public static final String BLOCK_PREFIX = "blockcushion";

    /**
     * The direction the block is facing.
     */
    public static final  PropertyDirection FACING     = BlockHorizontal.FACING;

    /** Colour property for this block */
    public static final PropertyEnum<EnumDyeColor> COLOR = PropertyEnum.<EnumDyeColor>create("color",
            EnumDyeColor.class);


    public BlockCushion() {
        super(Material.CARPET);
        init(BLOCK_PREFIX);
    }

    public BlockCushion( final String name) {
        super(Material.CARPET);
        init(name);
    }

    private void init(final String name)
    {
        setRegistryName(name);
        setUnlocalizedName(String.format("%s.%s", Constants.MOD_ID.toLowerCase(Locale.US), name));
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setHardness(0.5F);
//        setDefaultState(blockState.getBaseState().withProperty(COLOR, EnumDyeColor.WHITE));
        setSoundType(SoundType.CLOTH);
        setTickRandomly(false);
    }

    
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    public String getName() {
        return BLOCK_PREFIX;
    }

    public boolean isFullCube() {
        return false;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
            EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            // Creates a dummy entity the player can ride in order to show the
            // player as sitting
            if (playerIn.getRidingEntity() == null
                    && worldIn.getBlockState(pos.add(0, 1, 0)).getBlock() == Blocks.AIR) {

                EntityCushion entity = new EntityCushion(worldIn, 0);
                entity.setPosition(pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5);
                worldIn.spawnEntity(entity);
                playerIn.startRiding(entity);
            } else {
                playerIn.startRiding(null);
            }
            return true;
        }
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos,
            EnumFacing side) {
        return side == EnumFacing.UP ? true : super.shouldSideBeRendered(blockState, blockAccess, pos, side);
    }

}