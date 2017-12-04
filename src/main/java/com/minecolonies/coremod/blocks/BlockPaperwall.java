package com.minecolonies.coremod.blocks;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemColored;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;


public class BlockPaperwall extends AbstractBlockMinecoloniesPane<BlockPaperwall>
{
    public static final PropertyEnum<PaperwallType> VARIANT        = PropertyEnum.create("variant", PaperwallType.class);
    /**
     * This blocks name.
     */
    public static final String                      BLOCK_NAME     = "blockPaperwall";
    /**
     * The hardness this block has.
     */
    private static final float                      BLOCK_HARDNESS = 3F;
    /**
     * The resistance this block has.
     */
    private static final float                      RESISTANCE     = 1F;

    public BlockPaperwall()
    {
        super(Material.GLASS, true);
        this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, PaperwallType.JUNGLE));
        initBlock();
    }

    private void initBlock()
    {
        setRegistryName(BLOCK_NAME);
        setUnlocalizedName(String.format("%s.%s", Constants.MOD_ID.toLowerCase(), BLOCK_NAME));
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setHardness(BLOCK_HARDNESS);
        setResistance(RESISTANCE);
    }

    /**
     * Registery block at gameregistry.
     *
     * @param registry the registry to use.
     */
    @Override
    public void registerItemBlock(final IForgeRegistry<Item> registry)
    {
        registry.register((new ItemColored(this, true)).setRegistryName(this.getRegistryName()));
    }

    /**
     * Get the MapColor for this Block and the given BlockState
     */
    @Override
    public MapColor getMapColor(final IBlockState state, final IBlockAccess worldIn, final BlockPos pos)
    {
        return  state.getValue(VARIANT).getMapColor();
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    @Override
    public IBlockState getStateFromMeta(final int meta)
    {
        return this.getDefaultState().withProperty(VARIANT, PaperwallType.byMetadata(meta));
    }

    /**
     * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
     * returns the metadata of the dropped item based on the old metadata of the block.
     */
    @Override
    public int damageDropped(final IBlockState state)
    {
        return state.getValue(VARIANT).getMetadata();
    }

    @Override
    protected ItemStack getSilkTouchDrop(final IBlockState state)
    {
        return new ItemStack(Item.getItemFromBlock(this), 1, state.getValue(VARIANT).getMetadata());
    }

    /**
     * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
     */
    @Override
    public void getSubBlocks(final CreativeTabs itemIn, final NonNullList<ItemStack> items)
    {
        for (final PaperwallType type : PaperwallType.values())
        {
            items.add(new ItemStack(this, 1, type.getMetadata()));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.TRANSLUCENT;
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    @Override
    public int getMetaFromState(final IBlockState state)
    {
        return state.getValue(VARIANT).getMetadata();
    }

    /**
     * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     */
    @Override
    public IBlockState withRotation(final IBlockState state, final Rotation rot)
    {
        switch (rot)
        {
            case CLOCKWISE_180:
                return state.withProperty(NORTH, state.getValue(SOUTH))
                         .withProperty(EAST, state.getValue(WEST)).withProperty(SOUTH, state.getValue(NORTH))
                         .withProperty(WEST, state.getValue(EAST));
            case COUNTERCLOCKWISE_90:
                return state.withProperty(NORTH, state.getValue(EAST))
                         .withProperty(EAST, state.getValue(SOUTH)).withProperty(SOUTH, state.getValue(WEST))
                         .withProperty(WEST, state.getValue(NORTH));
            case CLOCKWISE_90:
                return state.withProperty(NORTH, state.getValue(WEST))
                         .withProperty(EAST, state.getValue(NORTH)).withProperty(SOUTH, state.getValue(EAST))
                         .withProperty(WEST, state.getValue(SOUTH));
            default:
                return state;
        }
    }

    /**
     * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     */
    @Override
    public IBlockState withMirror(final IBlockState state, final Mirror mirrorIn)
    {
        switch (mirrorIn)
        {
            case LEFT_RIGHT:
                return state.withProperty(NORTH, state.getValue(SOUTH)).withProperty(SOUTH, state.getValue(NORTH));
            case FRONT_BACK:
                return state.withProperty(EAST, state.getValue(WEST)).withProperty(WEST, state.getValue(EAST));
            default:
                return super.withMirror(state, mirrorIn);
        }
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] {NORTH, EAST, WEST, SOUTH, VARIANT});
    }

    @Override
    public boolean canPaneConnectTo(final IBlockAccess world, final BlockPos pos, final EnumFacing dir)
    {
        final BlockPos off = pos.offset(dir);
        final IBlockState state = world.getBlockState(off);
        return super.canPaneConnectTo(world, pos, dir)
                 || state.isSideSolid(world, off, dir.getOpposite()) || state.getBlock() instanceof BlockPaperwall;
    }
}
