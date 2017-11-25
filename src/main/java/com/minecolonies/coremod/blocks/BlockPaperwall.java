package com.minecolonies.coremod.blocks;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
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
import net.minecraftforge.fml.common.registry.IForgeRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BlockPaperwall extends AbstractBlockMinecoloniesPane<BlockPaperwall>
{
    public static final PropertyEnum<BlockPaperwall.EnumType> VARIANT = PropertyEnum.create("variant", EnumType.class);

    /**
     * The hardness this block has.
     */
    private static final float BLOCK_HARDNESS = 3F;

    /**
     * This blocks name.
     */
    public static final String BLOCK_NAME = "blockPaperwall";

    /**
     * The resistance this block has.
     */
    private static final float RESISTANCE = 1F;

    public BlockPaperwall()
    {
        super(Material.GLASS, true);
        this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, BlockPaperwall.EnumType.JUNGLE));
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
     * @param registry the registry to use.
     */
    @Override
    public void registerItemBlock(final IForgeRegistry<Item> registry)
    {
        registry.register((new ItemColored(this, true)).setRegistryName(this.getRegistryName()));
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
    public void getSubBlocks(final Item itemIn, final CreativeTabs tab, final NonNullList<ItemStack> list)
    {
        for(final BlockPaperwall.EnumType type: BlockPaperwall.EnumType.values())
        {
            list.add(new ItemStack(itemIn, 1, type.getMetadata()));
        }
    }

    /**
     * Get the MapColor for this Block and the given BlockState
     */
    @Override
    public MapColor getMapColor(final IBlockState state)
    {
        return state.getValue(VARIANT).getMapColor();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.TRANSLUCENT;
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    @Override
    public IBlockState getStateFromMeta(final int meta)
    {
        return this.getDefaultState().withProperty(VARIANT, EnumType.byMetadata(meta));
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    @Override
    public int getMetaFromState(final IBlockState state)
    {
        return state.getValue(VARIANT).getMetadata();
    }

    @Override
    public boolean canPaneConnectTo(final IBlockAccess world, final BlockPos pos, final EnumFacing dir)
    {
        final BlockPos off = pos.offset(dir);
        final IBlockState state = world.getBlockState(off);
        return canPaneConnectToBlock(state.getBlock())
                || state.isSideSolid(world, off, dir.getOpposite()) || state.getBlock() instanceof BlockPaperwall;
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
    protected ItemStack getSilkTouchDrop(final IBlockState state)
    {
        return new ItemStack(Item.getItemFromBlock(this), 1, state.getValue(VARIANT).getMetadata());
    }

    public enum EnumType implements IStringSerializable
    {
        OAK(0, "oak", MapColor.WOOD),
        SPRUCE(1, "spruce", MapColor.OBSIDIAN),
        BIRCH(2, "birch", MapColor.SAND),
        JUNGLE(3, "jungle", MapColor.DIRT);

        private static final BlockPaperwall.EnumType[] META_LOOKUP = new BlockPaperwall.EnumType[values().length];
        private final int meta;
        private final String name;
        private final String unlocalizedName;
        /** The color that represents this entry on a map. */
        private final MapColor mapColor;

        EnumType(final int metaIn, final String nameIn, final MapColor mapColorIn)
        {
            this(metaIn, nameIn, nameIn, mapColorIn);
        }

        EnumType(final int metaIn, final String nameIn, final String unlocalizedNameIn, final MapColor mapColorIn)
        {
            this.meta = metaIn;
            this.name = nameIn;
            this.unlocalizedName = unlocalizedNameIn;
            this.mapColor = mapColorIn;
        }

        public int getMetadata()
        {
            return this.meta;
        }

        /**
         * The color which represents this entry on a map.
         */
        public MapColor getMapColor()
        {
            return this.mapColor;
        }

        @Override
        public String toString()
        {
            return this.name;
        }

        public static BlockPaperwall.EnumType byMetadata(int meta)
        {
            int tempMeta = meta;
            if (tempMeta < 0 || tempMeta >= META_LOOKUP.length)
            {
                tempMeta = 0;
            }

            return META_LOOKUP[tempMeta];
        }

        @NotNull
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
            for (BlockPaperwall.EnumType enumtype : values())
            {
                META_LOOKUP[enumtype.getMetadata()] = enumtype;
            }
        }
    }
}
