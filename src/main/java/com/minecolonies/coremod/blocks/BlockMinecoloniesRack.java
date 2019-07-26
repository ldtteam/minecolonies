package com.minecolonies.coremod.blocks;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.blocks.types.RackType;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.IColonyManager;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import com.minecolonies.coremod.tileentities.TileEntityRack;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.Suppression.DEPRECATION;

/**
 * Block for the shelves of the warehouse.
 */
public class BlockMinecoloniesRack extends AbstractBlockMinecolonies<BlockMinecoloniesRack> implements IBlockMinecoloniesRack<BlockMinecoloniesRack>
{

    /**
     * The hardness this block has.
     */
    private static final float BLOCK_HARDNESS = 10.0F;

    /**
     * This blocks name.
     */
    private static final String BLOCK_NAME = "blockMinecoloniesRack";

    /**
     * The resistance this block has.
     */
    private static final float RESISTANCE = Float.POSITIVE_INFINITY;

    /**
     * How much light goes through the block.
     */
    private static final int LIGHT_OPACITY = 0;

    public BlockMinecoloniesRack()
    {
        super(Material.WOOD);
        initBlock();
    }

    /**
     * initialize the block
     * sets the creative tab, as well as the resistance and the hardness.
     */
    private void initBlock()
    {
        setRegistryName(Constants.MOD_ID.toLowerCase() + ":" + BLOCK_NAME);
        setTranslationKey(String.format("%s.%s", Constants.MOD_ID.toLowerCase(), BLOCK_NAME));
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(VARIANT, RackType.DEFAULT));
        setHardness(BLOCK_HARDNESS);
        setResistance(RESISTANCE);
        setLightOpacity(LIGHT_OPACITY);
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
     * @deprecated but we still need this because there is nothing better.
     */
    @Deprecated
    @Override
    public IBlockState getStateFromMeta(final int meta)
    {
        final EnumFacing enumFacing = EnumFacing.byHorizontalIndex(meta);
        return this.getDefaultState().withProperty(FACING, enumFacing);
    }

    @Override
    public int getMetaFromState(@NotNull final IBlockState state)
    {
        return state.getValue(FACING).getHorizontalIndex();
    }

    /**
     * @deprecated but we still need this because there is nothing better.
     */
    @Override
    public IBlockState getActualState(final IBlockState state, final IBlockAccess worldIn, final BlockPos pos)
    {
        final TileEntity entity = worldIn.getTileEntity(pos);

        if (!(entity instanceof TileEntityRack))
        {
            return super.getActualState(state, worldIn, pos);
        }

        final TileEntityRack rack = (TileEntityRack) entity;
        if (rack.isEmpty() && (rack.getOtherChest() == null || rack.getOtherChest().isEmpty()))
        {
            if (rack.getOtherChest() != null)
            {
                if (rack.isMain())
                {
                     return state.withProperty(IBlockMinecoloniesRack.VARIANT, RackType.DEFAULTDOUBLE).withProperty(FACING, BlockPosUtil.getFacing(rack.getNeighbor(), pos));
                }
                else
                {
                    return state.withProperty(IBlockMinecoloniesRack.VARIANT, RackType.EMPTYAIR);
                }
            }
            else
            {
                return state.withProperty(IBlockMinecoloniesRack.VARIANT, RackType.DEFAULT);
            }
        }
        else
        {
            if (rack.getOtherChest() != null)
            {
                if (rack.isMain())
                {
                    return state.withProperty(IBlockMinecoloniesRack.VARIANT, RackType.FULLDOUBLE)
                             .withProperty(FACING, BlockPosUtil.getFacing(rack.getNeighbor(), pos));
                }
                else
                {
                    return state.withProperty(IBlockMinecoloniesRack.VARIANT, RackType.EMPTYAIR);
                }
            }
            else
            {
                return state.withProperty(IBlockMinecoloniesRack.VARIANT, RackType.FULL);
            }
        }
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

    @Override
    @SuppressWarnings(DEPRECATION)
    public void neighborChanged(final IBlockState state, final World worldIn, final BlockPos pos, final Block blockIn, final BlockPos fromPos)
    {
        if (state.getBlock() instanceof BlockMinecoloniesRack)
        {
            final TileEntity rack = worldIn.getTileEntity(pos);
            for (final EnumFacing offsetFacing : BlockHorizontal.FACING.getAllowedValues())
            {
                final BlockPos neighbor = pos.offset(offsetFacing);
                final Block block = worldIn.getBlockState(neighbor).getBlock();
                if (rack instanceof TileEntityRack && pos.getY() == neighbor.getY() && !pos.equals(neighbor) && !pos.equals(BlockPos.ORIGIN)
                      && (block instanceof BlockMinecoloniesRack || blockIn instanceof BlockMinecoloniesRack))
                {
                    ((TileEntityRack) rack).neighborChanged(neighbor);
                }
            }
        }
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
    }

    @Override
    public void breakBlock(@NotNull final World worldIn, @NotNull final BlockPos pos, @NotNull final IBlockState state)
    {
        final TileEntity tileentity = worldIn.getTileEntity(pos);

        if (tileentity instanceof TileEntityRack)
        {
            final IItemHandler handler = ((TileEntityRack) tileentity).getInventory();
            InventoryUtils.dropItemHandler(handler, worldIn, pos.getX(), pos.getY(), pos.getZ());
        }

        super.breakBlock(worldIn, pos, state);
    }

    @NotNull
    @Override
    @SuppressWarnings(DEPRECATION)
    public BlockFaceShape getBlockFaceShape(final IBlockAccess worldIn, final IBlockState state, final BlockPos pos, final EnumFacing face)
    {
        return BlockFaceShape.CENTER_BIG;
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
                                     final IBlockState state,
                                     final EntityPlayer playerIn,
                                     final EnumHand hand,
                                     final EnumFacing facing,
                                     final float hitX,
                                     final float hitY,
                                     final float hitZ)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(worldIn, pos);
        final TileEntity tileEntity = worldIn.getTileEntity(pos);

        if ((colony == null || colony.getPermissions().hasPermission(playerIn, Action.ACCESS_HUTS))
              && tileEntity instanceof TileEntityRack)
        {
            if (!worldIn.isRemote)
            {
                playerIn.openGui(MineColonies.instance, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
            }
            return true;
        }
        return false;
    }

    @Override
    public void onBlockPlacedBy(
                                 final World worldIn, final BlockPos pos, final IBlockState state, final EntityLivingBase placer, final ItemStack stack)
    {
        IBlockState tempState = state;
        tempState = tempState.withProperty(VARIANT, RackType.byMetadata(stack.getItemDamage()));
        tempState = tempState.withProperty(FACING, placer.getHorizontalFacing().getOpposite());

        worldIn.setBlockState(pos, tempState, 2);
    }

    /**
     * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
     */
    @Override
    public void getSubBlocks(final CreativeTabs itemIn, final NonNullList<ItemStack> items)
    {
        items.add(new ItemStack(this, 1, RackType.DEFAULT.getMetadata()));
    }

    /**
     * Convert the given metadata into a BlockState for this Block.
     *
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */

    @NotNull
    @Override
    @Deprecated
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, FACING, VARIANT);
    }

    @Override
    public boolean hasTileEntity(final IBlockState state)
    {
        return true;
    }

    @Override
    public TileEntity createTileEntity(final World world, final IBlockState state)
    {
        return new TileEntityRack();
    }

    /**
     * This returns a complete list of items dropped from this block.
     *
     * @param world   The current world
     * @param pos     Block position in world
     * @param state   Current state
     * @param fortune Breakers fortune level
     * @return A ArrayList containing all items this block drops
     */
    @Override
    @SuppressWarnings(DEPRECATION)
    public List<ItemStack> getDrops(final IBlockAccess world, final BlockPos pos, final IBlockState state, final int fortune)
    {
        final List<ItemStack> drops = new ArrayList<>();

        drops.add(new ItemStack(this, 1));

        return drops;
    }

    /**
     * Called when a user uses the creative pick block button on this block
     *
     * @param state  the state.
     * @param target the target.
     * @param world  the world.
     * @param pos    the position.
     * @param player the player.
     * @return the block pick result.
     */
    @Override
    public ItemStack getPickBlock(final IBlockState state, final RayTraceResult target, final World world, final BlockPos pos, final EntityPlayer player)
    {
        return new ItemStack(this, 1);
    }
}
