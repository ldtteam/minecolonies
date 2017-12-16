package com.minecolonies.coremod.blocks;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemColored;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class BlockShingle extends AbstractBlockMinecoloniesStairs<BlockShingle>
{
    public static final PropertyEnum<PaperwallType> VARIANT        = PropertyEnum.create("variant", PaperwallType.class);

    /**
     * The hardness this block has.
     */
    private static final    float                       BLOCK_HARDNESS = 0.0F;

    /**
     * This blocks name.
     */
    private static final String BLOCK_NAME = "blockshingle";

    /**
     * The resistance this block has.
     */
    private static final float RESISTANCE = 1F;

    protected BlockShingle(final IBlockState modelState)
    {
        super(modelState);
        init();
    }

    private void init()
    {
        this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, PaperwallType.OAK));
        setRegistryName(BLOCK_NAME);
        setUnlocalizedName(String.format("%s.%s", Constants.MOD_ID.toLowerCase(), BLOCK_NAME));
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setHardness(BLOCK_HARDNESS);
        setResistance(RESISTANCE);
        this.useNeighborBrightness = true;
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

    /**
     * Convert the BlockState into the correct metadata value
     */
    @Override
    public int getMetaFromState(final IBlockState state)
    {
        return state.getValue(VARIANT).getMetadata();
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        final List<IProperty<?>> list = super.createBlockState().getProperties().stream().collect(Collectors.toList());
        final IProperty[] properties = new IProperty[list.size() + 1];
        for(int i = 0; i < properties.length; i++)
        {
            if(i < list.size())
            {
                properties[i] = list.get(i);
            }
            else
            {
                properties[i] = VARIANT;
            }
        }
        return new BlockStateContainer(this, properties);
    }
}
