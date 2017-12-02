package com.minecolonies.coremod.blocks;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import com.minecolonies.coremod.tileentities.TileEntityRack;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Block for the shelves of the warehouse.
 */
public class BlockMinecoloniesRack extends AbstractBlockMinecolonies<BlockMinecoloniesRack>
{
    public static final PropertyEnum<BlockMinecoloniesRack.EnumType> VARIANT
            = PropertyEnum.<BlockMinecoloniesRack.EnumType>create("variant", BlockMinecoloniesRack.EnumType.class);
    public static final int                                          DEFAULT_META = BlockMinecoloniesRack.EnumType.DEFAULT.getMetadata();
    public static final int                                          FULL_META    = EnumType.FULL.getMetadata();

    /**
     * The position it faces.
     */
    public static final PropertyDirection FACING = BlockHorizontal.FACING;

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
        setRegistryName(BLOCK_NAME);
        setUnlocalizedName(String.format("%s.%s", Constants.MOD_ID.toLowerCase(), BLOCK_NAME));
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(VARIANT, EnumType.DEFAULT));
        setHardness(BLOCK_HARDNESS);
        setResistance(RESISTANCE);
        setLightOpacity(LIGHT_OPACITY);
    }

    @Override
    public void getSubBlocks(final Item itemIn, final CreativeTabs tab, final List<ItemStack> list)
    {
        list.add(new ItemStack(this, 1, EnumType.DEFAULT.getMetadata()));
    }

    @Override
    public void onBlockPlacedBy(
            final World worldIn, final BlockPos pos, final IBlockState state, final EntityLivingBase placer, final ItemStack stack)
    {
        IBlockState tempState = state;
        tempState = tempState.withProperty(VARIANT, EnumType.byMetadata(stack.getItemDamage()));
        tempState = tempState.withProperty(FACING, placer.getHorizontalFacing().getOpposite());

        worldIn.setBlockState(pos, tempState, 2);
    }

    /**
     * @deprecated but we still need this because there is nothing better.
     */
    @Deprecated
    @Override
    public IBlockState getStateFromMeta(final int meta)
    {
        final EnumFacing enumFacing = EnumFacing.getHorizontal(meta);
        return this.getDefaultState().withProperty(FACING, enumFacing);
    }

    /**
     * @deprecated but we still need this because there is nothing better.
     */
    @Override
    public IBlockState getActualState(final IBlockState state, final IBlockAccess worldIn, final BlockPos pos)
    {
        final TileEntity entity = worldIn.getTileEntity(pos);

        if(!(entity instanceof TileEntityRack))
        {
            return super.getActualState(state, worldIn, pos);
        }

        final TileEntityRack rack = (TileEntityRack) entity;
        if (rack.isEmpty() && (rack.getOtherChest() == null || rack.getOtherChest().isEmpty()))
        {
            if(rack.getOtherChest() != null)
            {
                if(rack.isMain())
                {
                    return state.withProperty(BlockMinecoloniesRack.VARIANT, BlockMinecoloniesRack.EnumType.DEFAULTDOUBLE)
                            .withProperty(FACING, BlockPosUtil.getFacing(rack.getNeighbor(), pos));
                }
                else
                {
                    return state.withProperty(BlockMinecoloniesRack.VARIANT, EnumType.EMPTYAIR);
                }
            }
            else
            {
                return state.withProperty(BlockMinecoloniesRack.VARIANT, EnumType.DEFAULT);
            }
        }
        else
        {
            if(rack.getOtherChest() != null)
            {
                if(rack.isMain())
                {
                    return state.withProperty(BlockMinecoloniesRack.VARIANT, EnumType.FULLDOUBLE)
                            .withProperty(FACING, BlockPosUtil.getFacing(rack.getNeighbor(), pos));
                }
                else
                {
                    return state.withProperty(BlockMinecoloniesRack.VARIANT, BlockMinecoloniesRack.EnumType.EMPTYAIR);
                }
            }
            else
            {
                return state.withProperty(BlockMinecoloniesRack.VARIANT, EnumType.FULL);
            }
        }
    }

    /**
     * Check if a certain block should be replaced with a rack.
     * @param block the block to check.
     * @return true if so.
     */
    public static boolean shouldBlockBeReplacedWithRack(final Block block)
    {
        return block == Blocks.CHEST || block == ModBlocks.blockRack;
    }

    @Override
    public int getMetaFromState(@NotNull final IBlockState state)
    {
        return state.getValue(FACING).getHorizontalIndex();
    }

    /**
     * On the change of a neighbor block.
     *
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
    @Override
    public void neighborChanged(final IBlockState state, final World worldIn, final BlockPos pos, final Block blockIn)
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
        super.neighborChanged(state, worldIn, pos, blockIn);
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
    public List<ItemStack> getDrops(final IBlockAccess world, final BlockPos pos, final IBlockState state, final int fortune)
    {
        final List<ItemStack> drops = new ArrayList<>();

        drops.add(new ItemStack(this, 1));

        return drops;
    }

    @Override
    public void breakBlock(final World worldIn, final BlockPos pos, final IBlockState state)
    {
        final TileEntity tileentity = worldIn.getTileEntity(pos);

        if (tileentity instanceof TileEntityRack)
        {
            final IItemHandler handler = ((TileEntityRack) tileentity).getInventory();
            InventoryUtils.dropItemHandler(handler, worldIn, pos.getX(), pos.getY(), pos.getZ());
        }

        super.breakBlock(worldIn, pos, state);
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
        if (!worldIn.isRemote)
        {
            final Colony colony = ColonyManager.getColony(worldIn, pos);
            final TileEntity tileEntity = worldIn.getTileEntity(pos);

            if ((colony == null || colony.getPermissions().hasPermission(playerIn, Action.ACCESS_HUTS))
                    && tileEntity instanceof TileEntityRack)
            {
                playerIn.openGui(MineColonies.instance, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
                return true;
            }
        }
        return false;
    }

    /**
     * Called when a user uses the creative pick block button on this block
     * @param state the state.
     * @param target the target.
     * @param world the world.
     * @param pos the position.
     * @param player the player.
     * @return the block pick result.
     */
    @Override
    public ItemStack getPickBlock(final IBlockState state, final RayTraceResult target, final World world, final BlockPos pos, final EntityPlayer player)
    {
        return new ItemStack(this, 1);
    }

    @NotNull
    @Override
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

    public enum EnumType implements IStringSerializable
    {
        DEFAULT(0, "blockrackemptysingle", "emptysingle"),
        FULL(1, "blockrackfullsingle", "fullsingle"),
        DEFAULTDOUBLE(2, "blockrackempty", "empty"),
        FULLDOUBLE(3, "blockrackfull", "full"),
        EMPTYAIR(4, "blockrackair", "dontrender");

        private static final BlockMinecoloniesRack.EnumType[] META_LOOKUP = new BlockMinecoloniesRack.EnumType[values().length];
        private final int    meta;
        private final String name;
        private final String unlocalizedName;

        EnumType(final int meta, final String name, final String unlocalizedName)
        {
            this.meta = meta;
            this.name = name;
            this.unlocalizedName = unlocalizedName;
        }

        public int getMetadata()
        {
            return this.meta;
        }

        @Override
        public String toString()
        {
            return this.name;
        }

        public static BlockMinecoloniesRack.EnumType byMetadata(final int meta)
        {
            int tempMeta = meta;
            if (tempMeta < 0 || tempMeta >= META_LOOKUP.length)
            {
                tempMeta = 0;
            }

            return META_LOOKUP[tempMeta];
        }

        public String getName()
        {
            return this.name;
        }

        public String getUnlocalizedName()
        {
            return this.unlocalizedName;
        }

        static
        {
            for (final BlockMinecoloniesRack.EnumType blockRack : values())
            {
                META_LOOKUP[blockRack.getMetadata()] = blockRack;
            }
        }
    }
}
