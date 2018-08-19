package com.minecolonies.coremod.blocks;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.blocks.types.BarrelType;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import com.minecolonies.coremod.tileentities.TileEntityBarrel;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Random;

import static com.minecolonies.api.util.constant.Suppression.DEPRECATION;

public class BlockBarrel extends AbstractBlockMinecoloniesDirectional<BlockBarrel> implements ITileEntityProvider
{

    public static final PropertyEnum<BarrelType> VARIANT        = PropertyEnum.create("variant", BarrelType.class);
    /**
     * The hardness this block has.
     */
    private static final float                      BLOCK_HARDNESS = 5F;
    /**
     * This blocks name.
     */
    private static final String                     BLOCK_NAME     = "barrel_block";
    /**
     * The resistance this block has.
     */
    private static final float                      RESISTANCE     = 1F;

    /**
     * The position it faces.
     */
    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    /**
     * BoundingBox of the block
     * 0.0625 -> factor of the offset in pixels (1/16)
     */
    private final static AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0,0,0,1,1.5,1);

    public BlockBarrel()
    {
        super(Material.WOOD);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(VARIANT, BarrelType.ZERO));
        initBlock();
    }

    @Override
    public void registerItemBlock(final IForgeRegistry<Item> registry)
    {
        registry.register((new ItemColored(this, true)).setRegistryName(this.getRegistryName()));
    }

    /**
     * initialize the block
     */
    @SuppressWarnings(DEPRECATION)
    private void initBlock()
    {
        setRegistryName(BLOCK_NAME);
        setTranslationKey(String.format("%s.%s", Constants.MOD_ID.toLowerCase(), BLOCK_NAME));
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setHardness(BLOCK_HARDNESS);
        setResistance(RESISTANCE);
    }

    /**
     * Used to determine ambient occlusion and culling when rebuilding chunks
     * for render.
     *
     * @return true
     */
    @SuppressWarnings(DEPRECATION)
    @Override
    public boolean isOpaqueCube(final IBlockState state)
    {
        return false;
    }

    @Override
    public void updateTick(final World worldIn, final BlockPos pos, final IBlockState state, final Random rand)
    {
        final TileEntity te = worldIn.getTileEntity(pos);
        if(te!=null)
        {
            if(!worldIn.isRemote)
            {
                ((TileEntityBarrel) te).updateTick(worldIn, pos, state, rand);
            }
            ((TileEntityBarrel) te).updateBlock(worldIn, state);
        }
    }

    @NotNull
    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, FACING, VARIANT);
    }

    @Override
    public TileEntity createTileEntity(@NotNull final World world, @NotNull final IBlockState state)
    {
        return new TileEntityBarrel();
    }


    @Override
    public boolean hasTileEntity(final IBlockState state)
    {
        return true;
    }

    /**
     * Gets called whenever a player interacts with the block
     */
    @Override
    public boolean onBlockActivated(
            final World worldIn,
            final BlockPos pos,
            final IBlockState state,
            final EntityPlayer playerIn,
            final EnumHand hand,
            final EnumFacing facing,
            final float hitX,
            final float hitY,
            final float hitZ)
    {

        final ItemStack itemstack = playerIn.inventory.getCurrentItem();
        final TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEntityBarrel && !worldIn.isRemote)
        {
            ((TileEntityBarrel) te).useBarrel(worldIn, playerIn, itemstack, state, pos);
            ((TileEntityBarrel) te).updateBlock(worldIn, state);
        }

        return true;
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    @Override
    public IBlockState getStateFromMeta(final int meta)
    {
        return this.getDefaultState().withProperty(FACING,
                EnumFacing.byHorizontalIndex(meta));
    }

    /**
     * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
     * returns the metadata of the dropped item based on the old metadata of the block.
     */
    @Override
    public int damageDropped(final IBlockState state)
    {
        return this.getDefaultState().getValue(VARIANT).getMetadata();
    }

    @Override
    protected ItemStack getSilkTouchDrop(@NotNull final IBlockState state)
    {
        return new ItemStack(Item.getItemFromBlock(this), 1, state.getValue(VARIANT).getMetadata());
    }

    @NotNull
    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer()
    {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    @Override
    public int getMetaFromState(final IBlockState state)
    {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @NotNull
    @Override
    public AxisAlignedBB getBoundingBox(final IBlockState state, final IBlockAccess source, final BlockPos pos)
    {
        return BOUNDING_BOX;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(final IBlockState blockState, final IBlockAccess worldIn, final BlockPos pos)
    {
        return BOUNDING_BOX;
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

    @NotNull
    @Override
    public IBlockState getStateForPlacement(
      @NotNull final World world, @NotNull final BlockPos pos, @NotNull final EnumFacing facing, final float hitX, final float hitY,
                                            final float hitZ, final int meta, @NotNull final EntityLivingBase placer, final EnumHand hand)
    {
        return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand).withProperty(FACING, placer.getHorizontalFacing());
    }

    /**
     * @deprecated remove when minecraft invents something better.
     */
    @Deprecated
    @Override
    public IBlockState getActualState(@NotNull final IBlockState state, @NotNull final IBlockAccess worldIn, @NotNull final BlockPos pos)
    {
        final TileEntity entity = worldIn.getTileEntity(pos);

        if (!(entity instanceof TileEntityBarrel))
        {
            return super.getActualState(state, worldIn, pos);
        }

        return changeStateOverFullness((TileEntityBarrel) entity, worldIn, state, pos);
    }


    public static IBlockState changeStateOverFullness(final TileEntityBarrel entity, final IBlockAccess worldIn,
                                                      final IBlockState blockState, final BlockPos pos)
    {

        final TileEntityBarrel te = entity;

        /**
         * 12.8 -> the number of items needed to go up on a state (having 6 filling states)
         * So items/12.8 -> meta of the state we should get
         */
        BarrelType type = BarrelType.byMetadata((int) Math.round(te.getItems()/12.8));

        /**
         * We check if the barrel is marked as empty but it have items inside. If so, means that it
         * does not have all the items needed to go on TWENTY state, but we need to mark it so the player
         * knows it have some items inside
         */

        if(type.equals(BarrelType.ZERO) && te.getItems() > 0)
        {
            type = BarrelType.TWENTY;
        }
        else if (te.getItems() == TileEntityBarrel.MAX_ITEMS)
        {
            type = BarrelType.WORKING;
        }
        if(te.isDone())
        {
            type = BarrelType.DONE;
        }

        return blockState.withProperty(BlockBarrel.VARIANT,
          type).withProperty(BlockBarrel.FACING, blockState.getValue(BlockBarrel.FACING));
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(final World world, final int i)
    {
        return new TileEntityBarrel();
    }

    @Override
    public boolean canPlaceBlockAt(final World worldIn, final BlockPos pos)
    {
        return worldIn.getBlockState(pos.down()).getBlock().getClass() == BlockAir.class
               ||worldIn.getBlockState(pos.down()).getBlock().getClass() == BlockBarrel.class ?false:true;
    }

    @Override
    public void neighborChanged(final IBlockState state, final World worldIn, final BlockPos pos, final Block blockIn, final BlockPos fromPos)
    {
        if(worldIn.isAirBlock(pos.down()) || worldIn.getBlockState(pos.down()).getBlock() == ModBlocks.blockBarrel)
        {
            dropBlockAsItem(worldIn, pos, getDefaultState(), 0);
            worldIn.setBlockToAir(pos);
        }
    }

    @Override
    public boolean isPassable(final IBlockAccess worldIn, final BlockPos pos)
    {
        return false;
    }

    @Override
    public boolean isFullCube(final IBlockState state)
    {
        return false;
    }
}
