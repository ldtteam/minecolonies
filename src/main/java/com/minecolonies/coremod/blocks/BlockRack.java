package com.minecolonies.coremod.blocks;

import com.minecolonies.api.colony.permissions.Action;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
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
public class BlockRack extends Block
{
    public static final PropertyEnum<BlockRack.EnumType> VARIANT      = PropertyEnum.<BlockRack.EnumType>create("variant", BlockRack.EnumType.class);
    public static final int                              DEFAULT_META = BlockRack.EnumType.DEFAULT.getMetadata();
    public static final int                              FULL_META    = EnumType.FULL.getMetadata();

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
    private static final String BLOCK_NAME = "blockRack";

    /**
     * The resistance this block has.
     */
    private static final float RESISTANCE = Float.POSITIVE_INFINITY;

    /**
     * How much light goes through the block.
     */
    private static final int LIGHT_OPACITY = 0;

    public BlockRack()
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
        GameRegistry.register(this);
        GameRegistry.register((new ItemBlock(this)).setRegistryName(this.getRegistryName()));
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
    public IBlockState getStateFromMeta(int meta)
    {
        EnumType type = EnumType.byMetadata(meta);
        EnumFacing facing = EnumFacing.getHorizontal(meta - (type.getMetadata()));

        return this.getDefaultState().withProperty(VARIANT, type).withProperty(FACING, facing);
    }

    @Override
    public void neighborChanged(final IBlockState state, final World worldIn, final BlockPos pos, final Block blockIn)
    {
        if(state.getBlock() instanceof BlockRack)
        {
            final TileEntity rack = worldIn.getTileEntity(pos);
            for(EnumFacing offsetFacing: BlockHorizontal.FACING.getAllowedValues())
            {
                final BlockPos neighbor = pos.offset(offsetFacing);
                final Block block = worldIn.getBlockState(neighbor).getBlock();
                if(rack instanceof TileEntityRack && pos.getY() == neighbor.getY() && !pos.equals(neighbor) && !pos.equals(BlockPos.ORIGIN)
                        && (block instanceof BlockRack || blockIn instanceof BlockRack))
                {
                    ((TileEntityRack) rack).neighborChanged(neighbor);
                }
            }
        }
        super.neighborChanged(state, worldIn, pos, blockIn);
    }

    @Override
    public int getMetaFromState(@NotNull IBlockState state)
    {
        return (state.getValue(VARIANT).getMetadata()) + state.getValue(FACING).getHorizontalIndex();
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
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
    {
        List<ItemStack> drops = new ArrayList<>();

        drops.add(new ItemStack(this, 1));

        return drops;
    }

    @Override
    public void breakBlock(final World worldIn, final BlockPos pos, final IBlockState state)
    {
        TileEntity tileentity = worldIn.getTileEntity(pos);

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
     *
     * @param target The full target the player is looking at
     * @param player @return A ItemStack to add to the player's inventory, empty itemstack if nothing should be added.
     */
    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
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

    public static enum EnumType implements IStringSerializable
    {
        DEFAULT(0, "blockrackemptysingle", "emptysingle"),
        FULL(1, "blockrackfullsingle", "fullsingle"),
        DEFAULTDOUBLE(2, "blockrackempty", "empty"),
        FULLDOUBLE(3, "blockrackfull", "full"),
        EMPTYAIR(4, "blockrackair", "dontrender");

        private static final BlockRack.EnumType[] META_LOOKUP = new BlockRack.EnumType[values().length];
        private final int    meta;
        private final String name;
        private final String unlocalizedName;

        EnumType(int meta, String name, String unlocalizedName)
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

        public static BlockRack.EnumType byMetadata(int meta)
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
            for (BlockRack.EnumType blockRack : values())
            {
                META_LOOKUP[blockRack.getMetadata()] = blockRack;
            }
        }
    }
}
